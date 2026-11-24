
package com.example.model;

import com.example.domain.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long salonId;

    @Column(nullable = false)
    private Long customerId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @ElementCollection
    @CollectionTable(
            name = "booking_service_ids",
            joinColumns = @JoinColumn(name = "booking_id")
    )
    @Column(name = "service_id")
    private Set<Long> serviceIds;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private int totalPrice;
}
