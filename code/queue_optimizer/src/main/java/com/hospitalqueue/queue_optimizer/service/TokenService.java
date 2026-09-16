package com.hospitalqueue.queue_optimizer.service;

import com.hospitalqueue.queue_optimizer.entity.Doctor;
import com.hospitalqueue.queue_optimizer.entity.Token;
import com.hospitalqueue.queue_optimizer.repository.DoctorRepository;
import com.hospitalqueue.queue_optimizer.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public Token generateToken(Long doctorId, String patientName) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        int nextTokenNumber = doctor.getLastTokenIssued() + 1;
        doctor.setLastTokenIssued(nextTokenNumber);
        doctorRepository.save(doctor);

        Token token = new Token();
        token.setTokenNumber(nextTokenNumber);
        token.setPatientName(patientName);
        token.setDoctor(doctor);

        return tokenRepository.save(token);
    }

    public List<Token> getQueueForDoctor(Long doctorId) {
        return tokenRepository.findByDoctorIdOrderByTokenNumber(doctorId);
    }

    public int getEstimatedWaitMinutes(Long doctorId, int tokenNumber) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        int patientsAhead = Math.max(tokenNumber - doctor.getCurrentTokenServing(), 0);
        return patientsAhead * doctor.getAvgConsultMinutes();
    }

    public Token callNext(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        int nextToServe = doctor.getCurrentTokenServing() + 1;

        Token token = tokenRepository.findByDoctorIdAndTokenNumber(doctorId, nextToServe)
                .orElseThrow(() -> new RuntimeException("No waiting token found at number " + nextToServe));

        token.setStatus("IN_PROGRESS");
        doctor.setCurrentTokenServing(nextToServe);
        doctorRepository.save(doctor);

        return tokenRepository.save(token);
    }

    public Token completeToken(Long doctorId, int tokenNumber) {
        Token token = tokenRepository.findByDoctorIdAndTokenNumber(doctorId, tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        token.setStatus("DONE");
        return tokenRepository.save(token);
    }

    public Token skipToken(Long doctorId, int tokenNumber) {
        Token token = tokenRepository.findByDoctorIdAndTokenNumber(doctorId, tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        token.setStatus("SKIPPED");

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        if (doctor.getCurrentTokenServing() < tokenNumber) {
            doctor.setCurrentTokenServing(tokenNumber);
            doctorRepository.save(doctor);
        }

        return tokenRepository.save(token);
    }
}