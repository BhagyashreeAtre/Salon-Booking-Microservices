package com.example.service.client;

import com.example.dto.ServiceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient("SERVICE-OFFERING")  //eureka name
public interface ServiceOfferingFeignClient {
    @GetMapping("/api/service-offering/list/{ids}")
    public ResponseEntity<Set<ServiceDTO>> getServicesById(@PathVariable Set<Long> ids);
}
