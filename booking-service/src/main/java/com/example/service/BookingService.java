package com.example.service;

import com.example.domain.BookingStatus;
import com.example.dto.BookingRequest;
import com.example.dto.SalonDTO;
import com.example.dto.ServiceDTO;
import com.example.dto.UserDTO;
import com.example.model.Booking;
import com.example.model.PaymentOrder;
import com.example.model.SalonReport;

import java.awt.print.Book;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface BookingService {
    Booking createBooking(BookingRequest booking,
                          UserDTO user,
                          SalonDTO salon,
                          Set<ServiceDTO> serviceDTOSet) throws Exception;

    List<Booking> getBookingsByCustomer(Long customerId);

    List<Booking> getBookingsBySalon(Long salonId);

    Booking getBookingById(Long id) throws Exception;

    Booking updateBooking(Long bookingId, BookingStatus status) throws Exception;

    List<Booking> getBookingByDate(LocalDate date,Long salonId);

    SalonReport getSalonReport(Long salonId);

    Booking bookingSucess(PaymentOrder order) throws Exception;
}
