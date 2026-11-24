package com.example.service;

import com.example.domain.PaymentMethod;
import com.example.model.PaymentOrder;
import com.example.payload.response.PaymentLinkResponse;
import com.example.payload.response.dto.BookingDTO;
import com.example.payload.response.dto.UserDTO;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;

public interface PaymentService {
    PaymentLinkResponse createOrder(UserDTO user,
                                    BookingDTO booking,
                                    PaymentMethod payementMethod) throws RazorpayException, StripeException;


    PaymentOrder getPaymentOrderById(Long id) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentId);

    PaymentLink createRazorpayPaymentLink(UserDTO user,
                                          Long amount,
                                          Long orderId) throws RazorpayException;
    String createStriperPaymentLink(UserDTO user,
                                    Long amount,
                                    Long orderId) throws StripeException;


    Boolean proceedPayment(PaymentOrder paymentOrder,String paymentId,String paymentLinkId) throws RazorpayException;
}
