package com.autowash.autowash_pro.dto.response.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import com.autowash.autowash_pro.enums.BookingStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingStatusEvent {

    private UUID bookingId;
    private BookingStatus status;
    private String message;
    private LocalDateTime updatedAt;
}
