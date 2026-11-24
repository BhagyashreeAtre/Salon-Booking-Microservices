package com.example.mapper;

import com.example.model.Salon;
import com.example.dto.SalonDTO;

public class SalonMapper {
    public static SalonDTO mapToDTO(Salon salon){
        SalonDTO salonDTO=new SalonDTO();

        salonDTO.setId(salon.getId());
        salonDTO.setName(salon.getName());
        salonDTO.setAddress(salon.getAddress());
        salonDTO.setCity(salon.getCity());
        salonDTO.setImages(salon.getImages());
        salonDTO.setEmail(salon.getEmail());
        salonDTO.setOpenTime(salon.getOpenTime());
        salonDTO.setCloseTime(salon.getCloseTime());
        salonDTO.setOwnerId(salon.getOwnerId());
        salonDTO.setPhoneNumber(salon.getPhoneNumber());

        return salonDTO;
    }
}
