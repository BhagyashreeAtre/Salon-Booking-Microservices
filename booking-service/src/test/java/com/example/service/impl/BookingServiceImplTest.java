
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UserDTO user;
    private SalonDTO salon;
    private ServiceDTO service;
    private BookingRequest request;

    @BeforeEach
    void setUp() {
        user = new UserDTO();
        user.setId(10L);

        salon = new SalonDTO();
        salon.setId(20L);
        salon.setOpenTime(LocalTime.of(9, 0));
        salon.setCloseTime(LocalTime.of(18, 0));

        service = new ServiceDTO();
        service.setId(100L);
        service.setName("Haircut");
        service.setDuration(60);
        service.setPrice(500);

        request = new BookingRequest();
        request.setStartTime(
                LocalDate.now().plusDays(2).atTime(10, 0));
        request.setServiceIds(Set.of(100L));
    }

    private Booking existingBooking(
            LocalDateTime start,
            LocalDateTime end,
            BookingStatus status) {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setSalonId(20L);
        booking.setCustomerId(10L);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus(status);
        booking.setTotalPrice(500);

        return booking;
    }

    // 1. Successful booking creation

    @Test
    void createBooking_shouldSaveBookingSuccessfully() throws Exception {

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(List.of());

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(
                request, user, salon, Set.of(service));

        assertNotNull(result);
        assertEquals(10L, result.getCustomerId());
        assertEquals(20L, result.getSalonId());
        assertEquals(Set.of(100L), result.getServiceIds());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(500, result.getTotalPrice());
        assertEquals(
                request.getStartTime().plusMinutes(60),
                result.getEndTime());

        verify(bookingRepository).save(any(Booking.class));
    }

    // 2. Invalid booking request

    @Test
    void createBooking_shouldRejectNullRequest() {

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        null, user, salon, Set.of(service)));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldRejectMissingStartTime() {

        request.setStartTime(null);

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    @Test
    void createBooking_shouldRejectInvalidCustomer() {

        user.setId(null);

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    @Test
    void createBooking_shouldRejectInvalidSalon() {

        salon.setId(null);

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    @Test
    void createBooking_shouldRejectEmptyServices() {

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of()));
    }

    @Test
    void createBooking_shouldRejectMissingServiceIds() {

        request.setServiceIds(null);

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    @Test
    void createBooking_shouldRejectMismatchedServiceIds() {

        request.setServiceIds(Set.of(999L));

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    @Test
    void createBooking_shouldRejectInvalidServiceDuration() {

        service.setDuration(0);

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    @Test
    void createBooking_shouldRejectPastBookingTime() {

        request.setStartTime(LocalDateTime.now().minusMinutes(10));

        assertThrows(Exception.class,
                () -> bookingService.createBooking(
                        request, user, salon, Set.of(service)));
    }

    // 3. Time-slot availability

    @Test
    void isTimeSlotAvailable_shouldAllowAvailableSlot() throws Exception {

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(List.of());

        LocalDateTime start =
                LocalDate.now().plusDays(2).atTime(10, 0);

        boolean available = bookingService.isTimeSlotAvailable(
                salon, start, start.plusMinutes(60));

        assertTrue(available);
    }

    @Test
    void isTimeSlotAvailable_shouldRejectMissingWorkingHours() {

        salon.setOpenTime(null);

        LocalDateTime start =
                LocalDate.now().plusDays(2).atTime(10, 0);

        assertThrows(Exception.class,
                () -> bookingService.isTimeSlotAvailable(
                        salon, start, start.plusMinutes(60)));
    }

    @Test
    void isTimeSlotAvailable_shouldRejectBookingOutsideWorkingHours() {

        LocalDateTime start =
                LocalDate.now().plusDays(2).atTime(8, 0);

        assertThrows(Exception.class,
                () -> bookingService.isTimeSlotAvailable(
                        salon, start, start.plusMinutes(60)));
    }

    @Test
    void isTimeSlotAvailable_shouldRejectOverlappingBooking() {

        LocalDateTime start =
                LocalDate.now().plusDays(2).atTime(10, 0);

        Booking existing = existingBooking(
                start.plusMinutes(30),
                start.plusMinutes(90),
                BookingStatus.CONFIRMED);

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(List.of(existing));

        assertThrows(Exception.class,
                () -> bookingService.isTimeSlotAvailable(
                        salon, start, start.plusMinutes(60)));
    }

    @Test
    void isTimeSlotAvailable_shouldAllowAdjacentBooking() throws Exception {

        LocalDateTime start =
                LocalDate.now().plusDays(2).atTime(10, 0);

        Booking existing = existingBooking(
                start.minusHours(1),
                start,
                BookingStatus.CONFIRMED);

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(List.of(existing));

        assertTrue(bookingService.isTimeSlotAvailable(
                salon, start, start.plusMinutes(60)));
    }

    @Test
    void isTimeSlotAvailable_shouldIgnoreCancelledBookings()
            throws Exception {

        LocalDateTime start =
                LocalDate.now().plusDays(2).atTime(10, 0);

        Booking existing = existingBooking(
                start,
                start.plusMinutes(60),
                BookingStatus.CANCELLED);

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(List.of(existing));

        assertTrue(bookingService.isTimeSlotAvailable(
                salon, start, start.plusMinutes(60)));
    }

    // 4. Fetch bookings

    @Test
    void getBookingsByCustomer_shouldReturnCustomerBookings() {

        List<Booking> bookings = List.of(new Booking());

        when(bookingRepository.findByCustomerId(10L))
                .thenReturn(bookings);

        assertEquals(bookings,
                bookingService.getBookingsByCustomer(10L));
    }

    @Test
    void getBookingsBySalon_shouldReturnSalonBookings() {

        List<Booking> bookings = List.of(new Booking());

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(bookings);

        assertEquals(bookings,
                bookingService.getBookingsBySalon(20L));
    }

    @Test
    void getBookingById_shouldReturnBookingWhenFound() throws Exception {

        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertEquals(booking, bookingService.getBookingById(1L));
    }

    @Test
    void getBookingById_shouldThrowWhenNotFound() {

        when(bookingRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> bookingService.getBookingById(99L));
    }

    // 5. Update booking

    @Test
    void updateBooking_shouldUpdateStatus() throws Exception {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.updateBooking(
                1L, BookingStatus.CONFIRMED);

        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBooking_shouldRejectNullStatus() {

        assertThrows(Exception.class,
                () -> bookingService.updateBooking(1L, null));
    }

    @Test
    void updateBooking_shouldRejectCancelledBooking() {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertThrows(Exception.class,
                () -> bookingService.updateBooking(
                        1L, BookingStatus.CONFIRMED));

        verify(bookingRepository, never()).save(any());
    }

    // 6. Salon report

    @Test
    void getSalonReport_shouldCalculateEarningsAndRefunds() {

        Booking confirmed = new Booking();
        confirmed.setStatus(BookingStatus.CONFIRMED);
        confirmed.setTotalPrice(1000);

        Booking cancelled = new Booking();
        cancelled.setStatus(BookingStatus.CANCELLED);
        cancelled.setTotalPrice(500);

        Booking pending = new Booking();
        pending.setStatus(BookingStatus.PENDING);
        pending.setTotalPrice(300);

        when(bookingRepository.findBySalonId(20L))
                .thenReturn(List.of(confirmed, cancelled, pending));

        SalonReport report = bookingService.getSalonReport(20L);

        assertEquals(20L, report.getSalonId());
        assertEquals(3, report.getTotalBookings());
        assertEquals(1000.0, report.getTotalEarnings());
        assertEquals(500.0, report.getTotalRefund());
        assertEquals(1, report.getCancelledBookings().size());
    }

    // 7. Payment success

    @Test
    void bookingSuccess_shouldConfirmBooking() throws Exception {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);

        PaymentOrder order = new PaymentOrder();
        order.setBookingId(1L);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.bookingSucess(order);

        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void bookingSuccess_shouldRejectNullPaymentOrder() {

        assertThrows(Exception.class,
                () -> bookingService.bookingSucess(null));
    }

    @Test
    void bookingSuccess_shouldRejectPaymentWithoutBookingId() {

        PaymentOrder order = new PaymentOrder();

        assertThrows(Exception.class,
                () -> bookingService.bookingSucess(order));
    }

    @Test
    void bookingSuccess_shouldNotConfirmCancelledBooking() {

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CANCELLED);

        PaymentOrder order = new PaymentOrder();
        order.setBookingId(1L);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertThrows(Exception.class,
                () -> bookingService.bookingSucess(order));

        verify(bookingRepository, never()).save(any());
    }
}
