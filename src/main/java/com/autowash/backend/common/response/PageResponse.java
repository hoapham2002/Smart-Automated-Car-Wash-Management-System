package com.autowash.backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Standard pagination envelope, nested inside ApiResponse.data for every
 * paginated endpoint (bookings, loyalty transactions, payment history,
 * admin bookings/promotions, etc.):
 * { "success": true, "data": { "items": [...], "page": 1, "limit": 20, "total": 35 } }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int limit;
    private long total;
}
