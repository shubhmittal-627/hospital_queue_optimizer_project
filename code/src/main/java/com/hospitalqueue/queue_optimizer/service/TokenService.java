package com.hospitalqueue.queue_optimizer.service;

import com.hospitalqueue.queue_optimizer.entity.Doctor;
import com.hospitalqueue.queue_optimizer.entity.Token;
import com.hospitalqueue.queue_optimizer.repository.DoctorRepository;
import com.hospitalqueue.queue_optimizer.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Fetch queue for a doctor with Redis Caching
    @Cacheable(value = "doctorQueue", key = "#doctorId")
    public List<Token> getDoctorQueue(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
        return tokenRepository.findByDoctor(doctor);
    }

    // Controller compatibility alias method
    public List<Token> getQueueForDoctor(Long doctorId) {
        return getDoctorQueue(doctorId);
    }

    // Overloaded method to maintain backward compatibility with existing code
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token generateToken(Long doctorId, String patientName) {
        return generateToken(doctorId, patientName, "", "", "NORMAL");
    }

    // Overloaded method to maintain 4-parameter backward compatibility
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token generateToken(Long doctorId, String patientName, String visitDate, String timeSlot) {
        return generateToken(doctorId, patientName, visitDate, timeSlot, "NORMAL");
    }

    // Generate token with priority triage: Normal is auto-approved; Urgent/Emergency require Admin Approval
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token generateToken(Long doctorId, String patientName, String visitDate, String timeSlot, String priorityLevel) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        String normalizedPriority = (priorityLevel != null && !priorityLevel.trim().isEmpty()) ? priorityLevel.toUpperCase() : "NORMAL";
        boolean requiresApproval = "URGENT".equals(normalizedPriority) || "EMERGENCY".equals(normalizedPriority);

        int nextTokenNum = doctor.getLastTokenIssued() + 1;
        if (!requiresApproval) {
            doctor.setLastTokenIssued(nextTokenNum);
            doctorRepository.save(doctor);
        }

        Token token = new Token();
        token.setDoctor(doctor);
        token.setTokenNumber(requiresApproval ? 0 : nextTokenNum);
        token.setPatientName(patientName);
        token.setStatus(requiresApproval ? "PENDING" : "WAITING");
        token.setVisitDate(visitDate);
        token.setTimeSlot(timeSlot);
        token.setPriorityLevel(normalizedPriority);
        token.setApprovalStatus(requiresApproval ? "PENDING_APPROVAL" : "APPROVED");

        Token savedToken = tokenRepository.save(token);

        if (!requiresApproval) {
            notifyQueueUpdate(doctorId);
        }
        return savedToken;
    }

    // Admin Approval Method for Urgent/Emergency requests
    @Transactional
    public Token approvePriorityToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token request not found with ID: " + tokenId));

        if (!"PENDING_APPROVAL".equals(token.getApprovalStatus())) {
            throw new RuntimeException("This token request is already processed or approved.");
        }

        Doctor doctor = token.getDoctor();
        int nextTokenNum = doctor.getLastTokenIssued() + 1;
        doctor.setLastTokenIssued(nextTokenNum);
        doctorRepository.save(doctor);

        token.setTokenNumber(nextTokenNum);
        token.setStatus("WAITING");
        token.setApprovalStatus("APPROVED");

        Token updated = tokenRepository.save(token);
        notifyQueueUpdate(doctor.getId());
        return updated;
    }

    // Calculate estimated wait time based on doctor's average consultation minutes + 5 min buffer
    public int calculateWaitTime(Long doctorId, int patientTokenNumber) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        int current = doctor.getCurrentTokenServing();
        if (patientTokenNumber <= current || patientTokenNumber == 0) {
            return 0;
        }

        int peopleAhead = patientTokenNumber - current;
        int avgConsultMinutes = (doctor.getAvgConsultMinutes() > 0) ? doctor.getAvgConsultMinutes() : 10;

        // 5-minute buffer per person ahead for sanitization/setup
        int bufferMinutesPerPatient = 5;

        return peopleAhead * (avgConsultMinutes + bufferMinutesPerPatient);
    }

    // Controller compatibility alias method
    public int getEstimatedWaitMinutes(Long doctorId, int patientTokenNumber) {
        return calculateWaitTime(doctorId, patientTokenNumber);
    }

    // Call next patient in queue, update state, evict cache, and broadcast update
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token callNext(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        int nextServing = doctor.getCurrentTokenServing() + 1;
        Token token = tokenRepository.findByDoctorAndTokenNumber(doctor, nextServing)
                .orElseThrow(() -> new RuntimeException("No waiting patient found for token #" + nextServing));

        doctor.setCurrentTokenServing(nextServing);
        doctorRepository.save(doctor);

        token.setStatus("IN_PROGRESS");
        Token updatedToken = tokenRepository.save(token);

        notifyQueueUpdate(doctorId);
        return updatedToken;
    }

    // Complete token consultation without notes (backward compatible)
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token completeToken(Long doctorId, int tokenNumber) {
        return completeToken(doctorId, tokenNumber, "");
    }

    // Complete token consultation with custom notes/prescription remarks
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token completeToken(Long doctorId, int tokenNumber, String notes) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        Token token = tokenRepository.findByDoctorAndTokenNumber(doctor, tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token #" + tokenNumber + " not found for this doctor"));

        token.setStatus("DONE");
        token.setConsultationNotes(notes);
        Token updatedToken = tokenRepository.save(token);

        notifyQueueUpdate(doctorId);
        return updatedToken;
    }

    // Skip absent patient
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token skipToken(Long doctorId, int tokenNumber) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        Token token = tokenRepository.findByDoctorAndTokenNumber(doctor, tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token #" + tokenNumber + " not found for this doctor"));

        token.setStatus("SKIPPED");
        Token updatedToken = tokenRepository.save(token);

        notifyQueueUpdate(doctorId);
        return updatedToken;
    }

    // Submit Rating and Feedback for a completed token
    @Transactional
    public Token submitRating(Long tokenId, int rating, String feedback) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found with ID: " + tokenId));

        if (!"DONE".equals(token.getStatus())) {
            throw new RuntimeException("Ratings can only be submitted for completed consultations.");
        }

        token.setRating(rating);
        token.setFeedback(feedback);
        return tokenRepository.save(token);
    }

    // Fetch patient appointment history
    public List<Token> getPatientHistory(String patientName) {
        return tokenRepository.findByPatientNameOrderByCreatedAtDesc(patientName);
    }

    // Helper method to push live updates over WebSocket STOMP topic
    private void notifyQueueUpdate(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor != null) {
            List<Token> queue = tokenRepository.findByDoctor(doctor);
            messagingTemplate.convertAndSend("/topic/queue/" + doctorId, queue);
        }
    }
}