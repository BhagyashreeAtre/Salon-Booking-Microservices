package com.example.model;

import lombok.Data;

import java.util.List;

@Data
public class SalonReport {
    private Long salonId;
    private String salonName;
    private Double totalEarnings;
    private Integer totalBookings;
    private List<Booking> cancelledBookings;
    private Double totalRefund;

}
