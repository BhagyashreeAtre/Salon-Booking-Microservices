package com.example.controller;

import com.example.model.ServiceOffering;
import com.example.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/service_offering")
@RequiredArgsConstructor
public class ServiceOfferingController {
    private final ServiceOfferingService serviceOfferingService;

    @GetMapping("/salon/{salonId}")
    public ResponseEntity<Set<ServiceOffering>> getServicesBySalonId(@PathVariable Long salonId,
                                                                     @RequestParam(required = false) Long categoryId){
        Set<ServiceOffering> serviceOfferings=serviceOfferingService.getAllServiceBySalonId(salonId,categoryId);
        return ResponseEntity.ok(serviceOfferings);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOffering> getServiceById(@PathVariable Long id) throws Exception {
        ServiceOffering serviceOfferings=serviceOfferingService.getServiceById(id);
        return ResponseEntity.ok(serviceOfferings);

    }

    @GetMapping("/list/{ids}")
    public ResponseEntity<Set<ServiceOffering>> getServicesIds(@PathVariable Set<Long> ids){
        Set<ServiceOffering> serviceOfferings=serviceOfferingService.getServicesByIds(ids);
        return ResponseEntity.ok(serviceOfferings);

    }
}
