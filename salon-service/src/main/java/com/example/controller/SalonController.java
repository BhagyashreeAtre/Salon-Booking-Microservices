package com.example.controller;

import com.example.mapper.SalonMapper;
import com.example.model.Salon;
import com.example.dto.SalonDTO;
import com.example.dto.UserDTO;
import com.example.service.SalonService;
import com.example.client.UserFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@RestController
@RequestMapping("/api/salons")
@RequiredArgsConstructor
public class SalonController {

    private final SalonService salonService;
    private final UserFeignClient userFeignClient;

    @PostMapping
    public ResponseEntity<SalonDTO> createSalon(
            @RequestBody SalonDTO salonDTO,@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO userDTO=userFeignClient.getUserProfile(jwt).getBody();


        Salon salon=salonService.createSalon(salonDTO,userDTO);
        SalonDTO salonDTO1= SalonMapper.mapToDTO(salon);
        return ResponseEntity.ok(salonDTO1);
    }

    @PatchMapping("/{salonId}")
    public ResponseEntity<SalonDTO> updateSalon(@PathVariable Long salonId,
                                                @RequestBody SalonDTO salonDTO,@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO userDTO=userFeignClient.getUserProfile(jwt).getBody();

        Salon salon=salonService.updateSalon(salonDTO,userDTO,salonId);
        SalonDTO salonDTO1= SalonMapper.mapToDTO(salon);
        return ResponseEntity.ok(salonDTO1);
    }

    @GetMapping()
    public ResponseEntity<List<SalonDTO>> getSalons() throws Exception {
        List<Salon> salons=salonService.getAllSalons();
        List<SalonDTO> salonDTOS=salons.stream().map(
                (salon)->{
                    SalonDTO salonDTO=SalonMapper.mapToDTO(salon);
                    return salonDTO;
                }
        ).toList();
        return ResponseEntity.ok(salonDTOS);

    }

    @GetMapping("/{salonId}")
    public ResponseEntity<SalonDTO> getSalonById(@PathVariable Long salonId) throws Exception {
        Salon salon=salonService.getSalonById(salonId);
        SalonDTO salonDTO=SalonMapper.mapToDTO(salon);

        return ResponseEntity.ok(salonDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SalonDTO>> searchSalons(@RequestParam("city") String city ) throws Exception {

        List<Salon> salons=salonService.searchSalonByCity(city);
        List<SalonDTO> salonDTOS=salons.stream().map(
                (salon)->{
                    SalonDTO salonDTO=SalonMapper.mapToDTO(salon);
                    return salonDTO;
                }
        ).toList();
        return ResponseEntity.ok(salonDTOS);
    }
    @GetMapping("/owner")
    public ResponseEntity<SalonDTO> getSalonByOwnerId(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO userDTO=userFeignClient.getUserProfile(jwt).getBody();

        if(userDTO==null){
            throw new Exception("User not found from jwt.");
        }
        Salon salon=salonService.getSalonByOwnerId(userDTO.getId());
        SalonDTO salonDTO=SalonMapper.mapToDTO(salon);

        return ResponseEntity.ok(salonDTO);
    }
}
