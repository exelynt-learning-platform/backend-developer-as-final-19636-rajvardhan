package com.example.booking.controller;

import com.example.booking.dto.resource.*;
import com.example.booking.service.ResourceService;
import com.example.booking.utility.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    ResourceService resourceService;

    @GetMapping
    public List<ResourceResponse> getAll() {
        return resourceService.getAll();
    }

    @GetMapping("/{id}")
    public ResourceResponse getById(@PathVariable Long id) {
        return resourceService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> create(
            @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.create(request));
    }

    @PutMapping("/{id}")
    public ResourceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<ResourceResponse>> delete(@PathVariable Long id) {
        ResourceResponse deleted = resourceService.delete(id);
        return ResponseEntity.ok(BaseResponse.success("Resource deleted successfully", deleted));
    }
}
