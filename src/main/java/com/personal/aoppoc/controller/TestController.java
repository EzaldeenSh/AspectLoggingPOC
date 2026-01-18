package com.personal.aoppoc.controller;


import com.personal.aoppoc.annotation.LogEndpoint;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TestController {

    @PostMapping("/v1/log-test/{id}")
    @LogEndpoint(excludeHeaders = {"x-client-id", "accept", "host"}, excludePathVariables = {"id"})
    public ResponseEntity<?> testLog(@RequestBody Object request, @RequestHeader HttpHeaders headers, @RequestParam String param1, @PathVariable String id) {

            return ResponseEntity.ok(HttpStatus.NOT_IMPLEMENTED);
    }

}
