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

    // Generate token, evict cache, and broadcast WebSocket message
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token generateToken(Long doctorId, String patientName) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        int nextTokenNum = doctor.getLastTokenIssued() + 1;
        doctor.setLastTokenIssued(nextTokenNum);
        doctorRepository.save(doctor);

        Token token = new Token();
        token.setDoctor(doctor);
        token.setTokenNumber(nextTokenNum);
        token.setPatientName(patientName);
        token.setStatus("WAITING");

        Token savedToken = tokenRepository.save(token);
        notifyQueueUpdate(doctorId);
        return savedToken;
    }

    // Calculate estimated wait time based on doctor's average consultation minutes
    public int calculateWaitTime(Long doctorId, int patientTokenNumber) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        int current = doctor.getCurrentTokenServing();
        if (patientTokenNumber <= current) {
            return 0;
        }

        int peopleAhead = patientTokenNumber - current;
        return peopleAhead * doctor.getAvgConsultMinutes();
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

    // Complete token consultation
    @Transactional
    @CacheEvict(value = "doctorQueue", key = "#doctorId")
    public Token completeToken(Long doctorId, int tokenNumber) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        Token token = tokenRepository.findByDoctorAndTokenNumber(doctor, tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token #" + tokenNumber + " not found for this doctor"));

        token.setStatus("DONE");
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

    // Helper method to push live updates over WebSocket STOMP topic
    private void notifyQueueUpdate(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor != null) {
            List<Token> queue = tokenRepository.findByDoctor(doctor);
            messagingTemplate.convertAndSend("/topic/queue/" + doctorId, queue);
        }
    }
}