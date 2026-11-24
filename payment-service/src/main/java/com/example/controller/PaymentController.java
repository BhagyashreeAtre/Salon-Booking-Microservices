package com.example.controller;

import com.example.domain.PaymentMethod;
import com.example.model.PaymentOrder;
import com.example.payload.response.PaymentLinkResponse;
import com.example.payload.response.dto.BookingDTO;
import com.example.payload.response.dto.UserDTO;
import com.example.service.PaymentService;
import com.example.service.client.UserFeignClient;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserFeignClient userFeignClient;
    @PostMapping("/create")
    public ResponseEntity<PaymentLinkResponse> createPaymentLink(@RequestBody BookingDTO booking,
                                                                 @RequestParam PaymentMethod paymentMethod,
                                                                 @RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user=userFeignClient.getUserProfile(jwt).getBody();

        PaymentLinkResponse res=paymentService.createOrder(user,booking,paymentMethod);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/{paymentOrderId}")
    public ResponseEntity<PaymentOrder> getPaymentOrderById(@PathVariable Long paymentOrderId) throws Exception {


        PaymentOrder res=paymentService.getPaymentOrderById(paymentOrderId);

        return ResponseEntity.ok(res);
    }
    @PatchMapping("/proceed")
    public ResponseEntity<Boolean> proceedPayment(@RequestParam String paymentId,
                                                       @RequestParam String paymentLinkId) throws RazorpayException {
        PaymentOrder paymentOrder=paymentService.getPaymentOrderByPaymentId(paymentLinkId);

        Boolean res=paymentService.proceedPayment(paymentOrder,paymentId,paymentLinkId);

        return ResponseEntity.ok(res);
    }

}
