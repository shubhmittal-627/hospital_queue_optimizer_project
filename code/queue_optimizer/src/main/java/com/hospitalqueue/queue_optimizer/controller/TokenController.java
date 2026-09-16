package com.hospitalqueue.queue_optimizer.controller;

import com.hospitalqueue.queue_optimizer.entity.Token;
import com.hospitalqueue.queue_optimizer.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/{doctorId}")
    public Token generateToken(@PathVariable Long doctorId, @RequestParam String patientName) {
        return tokenService.generateToken(doctorId, patientName);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Token> getQueue(@PathVariable Long doctorId) {
        return tokenService.getQueueForDoctor(doctorId);
    }

    @GetMapping("/wait/{doctorId}/{tokenNumber}")
    public int getWait(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return tokenService.getEstimatedWaitMinutes(doctorId, tokenNumber);
    }

    @PutMapping("/next/{doctorId}")
    public Token callNext(@PathVariable Long doctorId) {
        return tokenService.callNext(doctorId);
    }

    @PutMapping("/complete/{doctorId}/{tokenNumber}")
    public Token completeToken(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return tokenService.completeToken(doctorId, tokenNumber);
    }

    @PutMapping("/skip/{doctorId}/{tokenNumber}")
    public Token skipToken(@PathVariable Long doctorId, @PathVariable int tokenNumber) {
        return tokenService.skipToken(doctorId, tokenNumber);
    }
}