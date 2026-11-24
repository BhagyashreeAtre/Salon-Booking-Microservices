
package com.example.service.client;

import com.example.payload.dto.BookingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("BOOKING-SERVICE")
public interface BookingFeignClient {

    @GetMapping("/api/bookings/{bookingId}")
    ResponseEntity<BookingDTO> getBookingById(
            @PathVariable("bookingId") Long bookingId
    );
}
