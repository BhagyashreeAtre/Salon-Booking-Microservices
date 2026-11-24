package com.example.messaging;

import com.example.payload.response.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@RequiredArgsConstructor
public class NotificationEventProducer {
    private final RabbitTemplate rabbitTemplate;
    public void sendNotification(Long bookingId,Long userId,Long salonId){
        NotificationDTO notificationDTO=new NotificationDTO();
        notificationDTO.setBookingId(bookingId);
        notificationDTO.setUserId(userId);
        notificationDTO.setSalongId(salonId);
        notificationDTO.setDescription("New booking confirmed");
        notificationDTO.setType("BOOKING");

        rabbitTemplate.convertAndSend("notification-queue",notificationDTO);
    }
}
