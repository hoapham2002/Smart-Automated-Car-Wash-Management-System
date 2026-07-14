package com.autowash.autowash_pro.dto.response.booking;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilitySlotResponse {

    private LocalDateTime startsAt;
    private int bookedCount;
    private int remainingCapacity;
    private boolean available;
}
