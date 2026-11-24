
package com.example.service.impl;

import com.example.domain.BookingStatus;
import com.example.dto.BookingRequest;
import com.example.dto.SalonDTO;
import com.example.dto.ServiceDTO;
import com.example.dto.UserDTO;
import com.example.model.Booking;
import com.example.model.PaymentOrder;
import com.example.model.SalonReport;
import com.example.repository.BookingRepository;
import com.example.service.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    // 1. CREATE BOOKING

    @Override
    @Transactional
    public Booking createBooking(
            BookingRequest booking,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> serviceDTOSet) throws Exception {

        if (booking == null || booking.getStartTime() == null) {
            throw new Exception("Booking start time is required.");
        }

        if (user == null || user.getId() == null) {
            throw new Exception("Invalid customer.");
        }

        if (salon == null || salon.getId() == null) {
            throw new Exception("Invalid salon.");
        }

        if (serviceDTOSet == null || serviceDTOSet.isEmpty()) {
            throw new Exception("Please select at least one service.");
        }

        // Validate that all requested services were found.
        Set<Long> requestedServiceIds = booking.getServiceIds();

        if (requestedServiceIds == null ||
                requestedServiceIds.isEmpty()) {
            throw new Exception("Service IDs are required.");
        }

        Set<Long> availableServiceIds = serviceDTOSet.stream()
                .map(ServiceDTO::getId)
                .collect(Collectors.toSet());

        if (!availableServiceIds.containsAll(requestedServiceIds) ||
                !requestedServiceIds.containsAll(availableServiceIds)) {
            throw new Exception("Invalid or missing services.");
        }

        // Calculate total duration and price.
        int totalDuration = serviceDTOSet.stream()
                .mapToInt(ServiceDTO::getDuration)
                .sum();

        int totalPrice = serviceDTOSet.stream()
                .mapToInt(ServiceDTO::getPrice)
                .sum();

        if (totalDuration <= 0 || totalPrice < 0) {
            throw new Exception("Invalid service duration or price.");
        }

        LocalDateTime bookingStartTime = booking.getStartTime();

        if (bookingStartTime.isBefore(LocalDateTime.now())) {
            throw new Exception("Booking time cannot be in the past.");
        }

        LocalDateTime bookingEndTime =
                bookingStartTime.plusMinutes(totalDuration);

        // Check salon working hours and existing bookings.
        boolean slotAvailable = isTimeSlotAvailable(
                salon,
                bookingStartTime,
                bookingEndTime
        );

        if (!slotAvailable) {
            throw new Exception("Selected time slot is unavailable.");
        }

        Set<Long> serviceIds = serviceDTOSet.stream()
                .map(ServiceDTO::getId)
                .collect(Collectors.toSet());

        Booking newBooking = new Booking();

        newBooking.setCustomerId(user.getId());
        newBooking.setSalonId(salon.getId());
        newBooking.setServiceIds(serviceIds);
        newBooking.setStatus(BookingStatus.PENDING);
        newBooking.setStartTime(bookingStartTime);
        newBooking.setEndTime(bookingEndTime);
        newBooking.setTotalPrice(totalPrice);

        return bookingRepository.save(newBooking);
    }

    // 2. CHECK SLOT AVAILABILITY

    public Boolean isTimeSlotAvailable(
            SalonDTO salonDTO,
            LocalDateTime bookingStartTime,
            LocalDateTime bookingEndTime) throws Exception {

        if (salonDTO == null ||
                salonDTO.getOpenTime() == null ||
                salonDTO.getCloseTime() == null) {
            throw new Exception("Salon working hours are not configured.");
        }

        if (bookingStartTime == null ||
                bookingEndTime == null ||
                !bookingEndTime.isAfter(bookingStartTime)) {
            throw new Exception("Invalid booking time.");
        }

        LocalDate bookingDate = bookingStartTime.toLocalDate();

        LocalDateTime salonOpenTime =
                salonDTO.getOpenTime().atDate(bookingDate);

        LocalDateTime salonCloseTime =
                salonDTO.getCloseTime().atDate(bookingDate);

        // Support salons that operate past midnight.
        if (!salonCloseTime.isAfter(salonOpenTime)) {
            salonCloseTime = salonCloseTime.plusDays(1);
        }

        if (bookingStartTime.isBefore(salonOpenTime) ||
                bookingEndTime.isAfter(salonCloseTime)) {
            throw new Exception(
                    "Booking must be within salon working hours."
            );
        }

        // Cancelled bookings should not block a slot.
        List<Booking> existingBookings =
                getBookingsBySalon(salonDTO.getId())
                        .stream()
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .toList();

        for (Booking existingBooking : existingBookings) {

            LocalDateTime existingStart =
                    existingBooking.getStartTime();

            LocalDateTime existingEnd =
                    existingBooking.getEndTime();

            // Two time intervals overlap when:
            // newStart < existingEnd AND newEnd > existingStart

            boolean overlaps =
                    bookingStartTime.isBefore(existingEnd) &&
                            bookingEndTime.isAfter(existingStart);

            if (overlaps) {
                throw new Exception(
                        "Slot not available. Please choose another time."
                );
            }
        }

        return true;
    }

    // 3. GET BOOKINGS BY CUSTOMER

    @Override
    public List<Booking> getBookingsByCustomer(Long customerId) {

        return bookingRepository.findByCustomerId(customerId);
    }

    // 4. GET BOOKINGS BY SALON

    @Override
    public List<Booking> getBookingsBySalon(Long salonId) {

        return bookingRepository.findBySalonId(salonId);
    }

    // 5. GET BOOKING BY ID

    @Override
    public Booking getBookingById(Long id) throws Exception {

        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new Exception("Booking not found.")
                );
    }

    // 6. UPDATE BOOKING STATUS

    @Override
    @Transactional
    public Booking updateBooking(
            Long bookingId,
            BookingStatus status) throws Exception {

        if (status == null) {
            throw new Exception("Booking status is required.");
        }

        Booking booking = getBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new Exception("Cancelled booking cannot be modified.");
        }

        booking.setStatus(status);

        return bookingRepository.save(booking);
    }

    // 7. GET BOOKINGS BY DATE

    @Override
    public List<Booking> getBookingByDate(
            LocalDate date,
            Long salonId) {

        List<Booking> allBookings = getBookingsBySalon(salonId);

        if (date == null) {
            return allBookings;
        }

        return allBookings.stream()
                .filter(booking ->
                        !booking.getStartTime().toLocalDate().isAfter(date)
                                &&
                                !booking.getEndTime().toLocalDate().isBefore(date)
                )
                .collect(Collectors.toList());
    }

    // 8. GENERATE SALON REPORT

    @Override
    public SalonReport getSalonReport(Long salonId) {

        List<Booking> bookings = getBookingsBySalon(salonId);

        // Only confirmed bookings count as earnings.
        Double totalEarnings = bookings.stream()
                .filter(booking ->
                        booking.getStatus() == BookingStatus.CONFIRMED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        Integer totalBookings = bookings.size();

        List<Booking> cancelledBookings = bookings.stream()
                .filter(booking ->
                        booking.getStatus() == BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        // Assumes all cancelled bookings were paid and refunded.
        Double totalRefund = cancelledBookings.stream()
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        SalonReport report = new SalonReport();

        report.setSalonId(salonId);
        report.setCancelledBookings(cancelledBookings);
        report.setTotalBookings(totalBookings);
        report.setTotalEarnings(totalEarnings);
        report.setTotalRefund(totalRefund);

        return report;
    }

    // 9. CONFIRM BOOKING AFTER SUCCESSFUL PAYMENT

    @Override
    @Transactional
    public Booking bookingSucess(PaymentOrder order) throws Exception {

        if (order == null || order.getBookingId() == null) {
            throw new Exception("Invalid payment order.");
        }

        Booking existingBooking =
                getBookingById(order.getBookingId());

        if (existingBooking.getStatus() == BookingStatus.CANCELLED) {
            throw new Exception(
                    "Cancelled booking cannot be confirmed."
            );
        }

        existingBooking.setStatus(BookingStatus.CONFIRMED);

        return bookingRepository.save(existingBooking);
    }
}
