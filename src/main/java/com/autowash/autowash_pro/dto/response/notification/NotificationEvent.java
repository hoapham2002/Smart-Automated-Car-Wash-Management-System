package com.autowash.autowash_pro.dto.response.notification;

import java.time.LocalDateTime;

import com.autowash.autowash_pro.enums.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationEvent {

    private NotificationType type;
    private String title;
    private String message;
    private LocalDateTime timestamp;
}
