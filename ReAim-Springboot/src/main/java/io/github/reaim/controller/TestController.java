package io.github.reaim.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    
    @GetMapping("/")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "ReAim Backend API is running!");
        response.put("status", "OK");
        response.put("database", "MongoDB Atlas Connected");
        return response;
    }
}
