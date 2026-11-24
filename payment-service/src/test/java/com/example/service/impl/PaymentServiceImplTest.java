
package com.example.service.impl;

import com.example.domain.PaymentMethod;
import com.example.domain.PaymentOrderStatus;
import com.example.messaging.BookingEventProducer;
import com.example.messaging.NotificationEventProducer;
import com.example.model.PaymentOrder;
import com.example.payload.response.PaymentLinkResponse;
import com.example.payload.response.dto.BookingDTO;
import com.example.payload.response.dto.UserDTO;
import com.example.repository.PaymentOrderRepository;
import com.razorpay.PaymentLink;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private BookingEventProducer bookingEventProducer;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @Spy
    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UserDTO user;
    private BookingDTO booking;

    @BeforeEach
    void setUp() {
        user = new UserDTO();
        user.setId(10L);
        user.setFullName("Test Customer");
        user.setEmail("customer@test.com");

        booking = new BookingDTO();
        booking.setId(100L);
        booking.setSalonId(20L);
        booking.setCustomerId(10L);
        booking.setTotalPrice(1500);
    }

    private void mockOrderSave() {
        when(paymentOrderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(invocation -> {
                    PaymentOrder order = invocation.getArgument(0);
                    if (order.getId() == null) {
                        order.setId(501L);
                    }
                    return order;
                });
    }

    // 1. Razorpay order creation

    @Test
    void createOrder_shouldCreateRazorpayPaymentLink() throws Exception {

        PaymentLink paymentLink = mock(PaymentLink.class);

        when(paymentLink.get("short_url"))
                .thenReturn("https://razorpay.test/payment/123");

        when(paymentLink.get("id"))
                .thenReturn("razorpay-link-123");

        doReturn(paymentLink)
                .when(paymentService)
                .createRazorpayPaymentLink(user, 1500L, 501L);

        mockOrderSave();

        PaymentLinkResponse response = paymentService.createOrder(
                user, booking, PaymentMethod.RAZORPAY);

        assertNotNull(response);
        assertEquals(
                "https://razorpay.test/payment/123",
                response.getPayment_link_url());

        verify(paymentOrderRepository, times(2))
                .save(any(PaymentOrder.class));

        verify(paymentService)
                .createRazorpayPaymentLink(user, 1500L, 501L);
    }

    // 2. Stripe order creation

    @Test
    void createOrder_shouldCreateStripePaymentLink() throws Exception {

        doReturn("https://stripe.test/payment/123")
                .when(paymentService)
                .createStriperPaymentLink(user, 1500L, 501L);

        mockOrderSave();

        PaymentLinkResponse response = paymentService.createOrder(
                user, booking, PaymentMethod.STRIPE);

        assertNotNull(response);
        assertEquals(
                "https://stripe.test/payment/123",
                response.getPayment_link_url());

        verify(paymentOrderRepository, times(1))
                .save(any(PaymentOrder.class));

        verify(paymentService)
                .createStriperPaymentLink(user, 1500L, 501L);
    }

    // 3. Retrieve payment order by ID

    @Test
    void getPaymentOrderById_shouldReturnOrderWhenFound()
            throws Exception {

        PaymentOrder order = new PaymentOrder();
        order.setId(501L);
        order.setAmount(1500L);

        when(paymentOrderRepository.findById(501L))
                .thenReturn(Optional.of(order));

        PaymentOrder result =
                paymentService.getPaymentOrderById(501L);

        assertNotNull(result);
        assertEquals(501L, result.getId());
        assertEquals(1500L, result.getAmount());
    }

    @Test
    void getPaymentOrderById_shouldThrowWhenOrderNotFound() {

        when(paymentOrderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> paymentService.getPaymentOrderById(999L));
    }

    // 4. Retrieve payment order by payment link ID

    @Test
    void getPaymentOrderByPaymentId_shouldReturnMatchingOrder() {

        PaymentOrder order = new PaymentOrder();
        order.setId(501L);
        order.setPaymentLinkId("razorpay-link-123");

        when(paymentOrderRepository.findByPaymentLinkId(
                "razorpay-link-123"))
                .thenReturn(order);

        PaymentOrder result =
                paymentService.getPaymentOrderByPaymentId(
                        "razorpay-link-123");

        assertNotNull(result);
        assertEquals("razorpay-link-123", result.getPaymentLinkId());
    }

    // 5. Payment status handling

    @Test
    void proceedPayment_shouldRejectAlreadyProcessedOrder()
            throws Exception {

        PaymentOrder order = new PaymentOrder();
        order.setId(501L);
        order.setStatus(PaymentOrderStatus.SUCCESS);
        order.setPaymentMethod(PaymentMethod.RAZORPAY);

        Boolean result = paymentService.proceedPayment(
                order, "payment-123", "razorpay-link-123");

        assertFalse(result);

        verify(paymentOrderRepository, never())
                .save(any(PaymentOrder.class));

        verifyNoInteractions(
                bookingEventProducer,
                notificationEventProducer);
    }

}
