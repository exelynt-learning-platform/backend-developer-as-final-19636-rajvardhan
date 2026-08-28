package com.example.booking.service;

import com.example.booking.dto.resource.*;
import com.example.booking.entity.Resource;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository repository;
    private final ReservationRepository reservationRepository;

    public ResourceService(ResourceRepository repository, ReservationRepository reservationRepository) {
        this.repository = repository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        Resource resource = new Resource(
                request.name(),
                request.description(),
                request.price(),
                request.active());

        return toResponse(repository.save(resource));
    }

    @Transactional
    public ResourceResponse update(Long id, ResourceRequest request) {
        Resource resource = find(id);

        resource.update(
                request.name(),
                request.description(),
                request.price(),
                request.active());

        return toResponse(repository.save(resource));
    }

    @Transactional
    public ResourceResponse delete(Long id) {
        Resource resource = find(id);
        ResourceResponse response = toResponse(resource);
        
        reservationRepository.deleteByResourceId(id);
        
        repository.delete(resource);
        return response;
    }

    private Resource find(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found: " + id));
    }

    private ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getPrice(),
                resource.isActive());
    }
}
