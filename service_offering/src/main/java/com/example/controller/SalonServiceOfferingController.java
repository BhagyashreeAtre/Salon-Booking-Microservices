package com.example.controller;

import com.example.model.ServiceOffering;
import com.example.payload.dto.CategoryDTO;
import com.example.payload.dto.SalonDTO;
import com.example.payload.dto.ServiceDTO;
import com.example.service.ServiceOfferingService;
import com.example.service.client.CategoryFeignClient;
import com.example.service.client.SalonFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service_offering/salon_owner")
public class SalonServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;
    private final SalonFeignClient salonFeignClient;
    private final CategoryFeignClient categoryFeignClient;

    @PostMapping
    public ResponseEntity<ServiceOffering> createService(
            @RequestBody ServiceDTO serviceDTO,
            @RequestHeader("Authorization") String jwt
          ) throws Exception {
        SalonDTO salonDTO=salonFeignClient.getSalonByOwnerId(jwt).getBody();

        CategoryDTO categoryDTO= categoryFeignClient.getCategoriesByIdAndSalon(serviceDTO.getCategory(),salonDTO.getId()).getBody();

        ServiceOffering serviceOffering=serviceOfferingService.createService(salonDTO,serviceDTO,categoryDTO);

        return ResponseEntity.ok(serviceOffering);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOffering> updateService(
            @PathVariable Long id,
            @RequestBody ServiceOffering serviceOffering
    ) throws Exception {

        ServiceOffering serviceOfferings=serviceOfferingService.updateService(id,serviceOffering);

        return ResponseEntity.ok(serviceOfferings);
    }

}
