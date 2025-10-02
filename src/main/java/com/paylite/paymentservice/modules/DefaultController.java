package com.paylite.paymentservice.modules;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "Home", description = "Paylite Payment Service Landing")
public class DefaultController {

    @GetMapping
    public Map<String, Object> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Welcome to Paylite Payment Service!");
        response.put("links", Map.of(
                "swagger_docs", "http://localhost:8080/swagger-ui/index.html",
                "openapi_specs", "http://localhost:8080/v3/api-docs",
                "health_check", "http://localhost:8080/actuator/health"
        ));
        return response;
    }

}