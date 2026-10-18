package com.hospitalqueue.queue_optimizer.controller;

import com.hospitalqueue.queue_optimizer.entity.Token;
import com.hospitalqueue.queue_optimizer.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Token>> getDoctorQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.getDoctorQueue(doctorId));
    }

    @PostMapping("/{doctorId}")
    public ResponseEntity<Token> generateToken(@PathVariable Long doctorId, @RequestParam String patientName) {
        return ResponseEntity.ok(tokenService.generateToken(doctorId, patientName));
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
    public ResponseEntity<Token> completeToken(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return ResponseEntity.ok(tokenService.completeToken(doctorId, tokenNumber));
    }

    @PutMapping("/skip/{doctorId}/{tokenNumber}")
    public ResponseEntity<Token> skipToken(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return ResponseEntity.ok(tokenService.skipToken(doctorId, tokenNumber));
    }
}