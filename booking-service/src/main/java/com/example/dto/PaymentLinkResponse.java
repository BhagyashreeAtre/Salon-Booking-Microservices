
package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentLinkResponse {

    @JsonProperty("payment_link_url")
    private String paymentLinkUrl;

    @JsonProperty("payment_link_id")
    private String paymentLinkId;
}
