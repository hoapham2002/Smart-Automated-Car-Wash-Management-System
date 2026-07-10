package com.autowash.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * A08 - one bookable slot within a day's list (see SlotController, which
 * groups these by date to match the API doc's { "2024-07-10": [...] } shape).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {
    private UUID id;
    private LocalTime slotStart;
    private LocalTime slotEnd;
    private int remaining;
}
