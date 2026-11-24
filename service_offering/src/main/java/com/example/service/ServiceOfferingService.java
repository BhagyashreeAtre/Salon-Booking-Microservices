package com.example.service;

import com.example.model.ServiceOffering;
import com.example.payload.dto.CategoryDTO;
import com.example.payload.dto.SalonDTO;
import com.example.payload.dto.ServiceDTO;

import java.util.List;
import java.util.Set;

public interface ServiceOfferingService {
    ServiceOffering createService(SalonDTO salonDTO,
                                  ServiceDTO serviceDTO,
                                  CategoryDTO categoryDTO);
    ServiceOffering updateService(Long serviceId,ServiceOffering service) throws Exception;

    Set<ServiceOffering> getAllServiceBySalonId(Long salonId, Long categoryId);

    Set<ServiceOffering> getServicesByIds(Set<Long> ids);

    ServiceOffering getServiceById(Long id) throws Exception;

}
