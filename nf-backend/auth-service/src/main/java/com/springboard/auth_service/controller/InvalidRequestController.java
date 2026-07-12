package com.springboard.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invalid")
public class InvalidRequestController {
    @GetMapping
    public String invalid(){
        return "Something went wrong";
    }
}
