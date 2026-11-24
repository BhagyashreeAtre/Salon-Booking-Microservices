package com.example.service.impl;

import com.example.mapper.NotificationMapper;
import com.example.model.Notification;
import com.example.payload.dto.BookingDTO;
import com.example.payload.dto.NotificationDTO;
import com.example.repository.NotificationRepository;
import com.example.service.NotificationService;
import com.example.service.client.BookingFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookingFeignClient bookingFeignClient;
    @Override
    public NotificationDTO createNotification(Notification notification) throws Exception {
        Notification savedNotification=notificationRepository.save(notification);
        BookingDTO bookingDTO=bookingFeignClient.getBookingById(savedNotification.getBookingId()).getBody();

        NotificationDTO notificationDTO= NotificationMapper.toDTO(savedNotification,bookingDTO);
        return notificationDTO;
    }

    @Override
    public List<Notification> getAllNotificationByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getAllNotificationBySalonId(Long salonId) {
        return notificationRepository.findBySalonId(salonId);
    }

    @Override
    public Notification markNotificationAsRead(Long notificationId) throws Exception {
        return notificationRepository.findById(notificationId).map(
                notification -> {
                    notification.setIsRead(true);
                    return notificationRepository.save(notification);
                }
        ).orElseThrow(()-> new Exception("Notification not found"));
    }
}
