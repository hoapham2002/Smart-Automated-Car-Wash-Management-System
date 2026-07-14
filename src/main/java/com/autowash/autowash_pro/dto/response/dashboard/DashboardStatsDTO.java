package com.autowash.autowash_pro.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private BigDecimal todayRevenue;
    private long totalWashCount;
    private long motorbikeCount;
    private long carCount;
    private long newCustomerCount;
    private long issuedPoints;

    private List<RevenueChartData> revenue7Days;
    private List<QueueDataDTO> todayQueue;

    private long memberCount;
    private long silverCount;
    private long goldCount;
    private long platinumCount;
}
