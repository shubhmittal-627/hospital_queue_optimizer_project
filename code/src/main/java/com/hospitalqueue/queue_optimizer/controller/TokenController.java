package com.hospitalqueue.queue_optimizer.controller;

import com.hospitalqueue.queue_optimizer.entity.Token;
import com.hospitalqueue.queue_optimizer.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Token>> getDoctorQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.getDoctorQueue(doctorId));
    }

    @PostMapping("/{doctorId}")
    public ResponseEntity<Token> generateToken(
            @PathVariable Long doctorId,
            @RequestParam String patientName,
            @RequestParam(required = false, defaultValue = "") String visitDate,
            @RequestParam(required = false, defaultValue = "") String timeSlot,
            @RequestParam(required = false, defaultValue = "NORMAL") String priorityLevel) {

        return ResponseEntity.ok(tokenService.generateToken(doctorId, patientName, visitDate, timeSlot, priorityLevel));
    }

    // Admin endpoint to approve pending Urgent/Emergency triage requests
    @PutMapping("/admin/approve/{tokenId}")
    public ResponseEntity<Token> approvePriorityToken(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.approvePriorityToken(tokenId));
    }

    @GetMapping("/wait/{doctorId}/{tokenNumber}")
    public ResponseEntity<Integer> getWaitTime(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return ResponseEntity.ok(tokenService.calculateWaitTime(doctorId, tokenNumber));
    }

    @PutMapping("/next/{doctorId}")
    public ResponseEntity<Token> callNext(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.callNext(doctorId));
    }

    @PutMapping("/complete/{doctorId}/{tokenNumber}")
    public ResponseEntity<Token> completeToken(
            @PathVariable Long doctorId,
            @PathVariable int tokenNumber,
            @RequestParam(required = false, defaultValue = "") String notes) {
        return ResponseEntity.ok(tokenService.completeToken(doctorId, tokenNumber, notes));
    }

    @PutMapping("/skip/{doctorId}/{tokenNumber}")
    public ResponseEntity<Token> skipToken(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return ResponseEntity.ok(tokenService.skipToken(doctorId, tokenNumber));
    }

    // Task 3: Submit rating and feedback endpoint for completed consultations
    @PostMapping("/{tokenId}/rate")
    public ResponseEntity<Token> submitRating(
            @PathVariable Long tokenId,
            @RequestParam int rating,
            @RequestParam(required = false, defaultValue = "") String feedback) {
        return ResponseEntity.ok(tokenService.submitRating(tokenId, rating, feedback));
    }

    @GetMapping("/patient/{patientName}")
    public ResponseEntity<List<Token>> getPatientHistory(@PathVariable String patientName) {
        return ResponseEntity.ok(tokenService.getPatientHistory(patientName));
    }
}