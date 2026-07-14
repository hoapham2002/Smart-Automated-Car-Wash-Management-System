package com.autowash.autowash_pro.service;

import com.autowash.autowash_pro.entity.Customer;
import com.autowash.autowash_pro.entity.WashHistory;
import com.autowash.autowash_pro.enums.ServiceType;
import com.autowash.autowash_pro.enums.Tier;
import com.autowash.autowash_pro.repository.CustomerRepository;
import com.autowash.autowash_pro.repository.WashHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final WashHistoryRepository washHistoryRepository;
    private final CustomerRepository customerRepository;

    public Map<String, Object> getRevenueReport(String granularity, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<WashHistory> washes = washHistoryRepository.findByWashedAtBetween(startDateTime, endDateTime);

        BigDecimal totalRevenue = washes.stream()
                .map(WashHistory::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) days = 1;

        BigDecimal avgRevenuePerDay = totalRevenue.divide(BigDecimal.valueOf(days), 0, RoundingMode.HALF_UP);
        int totalWashes = washes.size();
        BigDecimal avgRevenuePerWash = totalWashes > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalWashes), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Wash breakdown: motorbike vs car
        long motorbikeCount = washes.stream()
                .filter(w -> w.getVehicle() != null && 
                        (w.getVehicle().getVehicleType().equalsIgnoreCase("MOTORBIKE") || 
                         w.getVehicle().getVehicleType().toLowerCase().contains("xe máy") ||
                         w.getVehicle().getVehicleType().toLowerCase().contains("xe may")))
                .count();
        long carCount = totalWashes - motorbikeCount;

        Map<String, Long> washBreakdown = new HashMap<>();
        washBreakdown.put("motorbike", motorbikeCount);
        washBreakdown.put("car", carCount);

        // Service breakdown: BASIC, PREMIUM, FULL_DETAIL
        long basicCount = washes.stream().filter(w -> w.getServiceType() == ServiceType.BASIC).count();
        long premiumCount = washes.stream().filter(w -> w.getServiceType() == ServiceType.PREMIUM).count();
        long fullDetailCount = washes.stream().filter(w -> w.getServiceType() == ServiceType.FULL_DETAIL).count();
        long sumServiceCount = basicCount + premiumCount + fullDetailCount;

        Map<String, Double> serviceRevenueBreakdown = new HashMap<>();
        if (sumServiceCount > 0) {
            serviceRevenueBreakdown.put("basicWashPercent", (double) Math.round((double) basicCount / sumServiceCount * 100));
            serviceRevenueBreakdown.put("premiumWashPercent", (double) Math.round((double) premiumCount / sumServiceCount * 100));
            serviceRevenueBreakdown.put("fullDetailPercent", (double) Math.round((double) fullDetailCount / sumServiceCount * 100));
        } else {
            serviceRevenueBreakdown.put("basicWashPercent", 0.0);
            serviceRevenueBreakdown.put("premiumWashPercent", 0.0);
            serviceRevenueBreakdown.put("fullDetailPercent", 0.0);
        }

        // Group chartData
        List<Map<String, Object>> chartData = new ArrayList<>();
        if ("month".equalsIgnoreCase(granularity)) {
            Map<String, BigDecimal> grouped = washes.stream()
                    .collect(Collectors.groupingBy(
                            w -> w.getWashedAt().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                            TreeMap::new,
                            Collectors.mapping(WashHistory::getAmountPaid, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    ));
            grouped.forEach((key, val) -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("date", key);
                map.put("revenue", val);
                chartData.add(map);
            });
        } else if ("week".equalsIgnoreCase(granularity)) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            Map<String, BigDecimal> grouped = washes.stream()
                    .collect(Collectors.groupingBy(
                            w -> "Tuần " + w.getWashedAt().get(weekFields.weekOfWeekBasedYear()) + "/" + w.getWashedAt().getYear(),
                            TreeMap::new,
                            Collectors.mapping(WashHistory::getAmountPaid, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    ));
            grouped.forEach((key, val) -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("date", key);
                map.put("revenue", val);
                chartData.add(map);
            });
        } else { // default: day
            Map<String, BigDecimal> grouped = washes.stream()
                    .collect(Collectors.groupingBy(
                            w -> w.getWashedAt().format(DateTimeFormatter.ofPattern("dd/MM")),
                            TreeMap::new,
                            Collectors.mapping(WashHistory::getAmountPaid, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    ));
            grouped.forEach((key, val) -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("date", key);
                map.put("revenue", val);
                chartData.add(map);
            });
        }

        // Top Days (grouped by day, sorted descending)
        Map<String, List<WashHistory>> dayGrouped = washes.stream()
                .collect(Collectors.groupingBy(w -> w.getWashedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        List<Map<String, Object>> topDays = dayGrouped.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<WashHistory> dayWashes = entry.getValue();
                    BigDecimal dayRev = dayWashes.stream().map(WashHistory::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
                    int count = dayWashes.size();
                    BigDecimal avg = count > 0 ? dayRev.divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                    
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", date);
                    map.put("revenue", dayRev);
                    map.put("washes", count);
                    map.put("avg", String.format("%,.0fđ", avg.doubleValue()));
                    map.put("growth", "+5.0%"); // Placeholder growth
                    return map;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")))
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("avgRevenuePerDay", avgRevenuePerDay);
        report.put("totalWashes", totalWashes);
        report.put("avgRevenuePerWash", avgRevenuePerWash);
        report.put("washBreakdown", washBreakdown);
        report.put("serviceRevenueBreakdown", serviceRevenueBreakdown);
        report.put("chartData", chartData);
        report.put("topDays", topDays);

        return report;
    }

    public Map<String, Object> getCustomerReport(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Customer> allCustomers = customerRepository.findAll();
        List<Customer> newCustomers = customerRepository.findByRegisteredAtBetween(startDateTime, endDateTime);

        long totalCustomers = allCustomers.size();
        long newCustomersThisMonth = newCustomers.size();
        
        // Active customers: visited/washed in date range
        List<WashHistory> washesInRange = washHistoryRepository.findByWashedAtBetween(startDateTime, endDateTime);
        long activeCustomers = washesInRange.stream()
                .map(w -> w.getCustomer().getCustomerId())
                .distinct()
                .count();

        // Issued points in date range
        int issuedPoints = washesInRange.stream()
                .mapToInt(WashHistory::getPointsEarned)
                .sum();

        // Tier distribution
        long memberCount = allCustomers.stream().filter(c -> c.getTier() == Tier.MEMBER).count();
        long silverCount = allCustomers.stream().filter(c -> c.getTier() == Tier.SILVER).count();
        long goldCount = allCustomers.stream().filter(c -> c.getTier() == Tier.GOLD).count();
        long platinumCount = allCustomers.stream().filter(c -> c.getTier() == Tier.PLATINUM).count();
        long totalWithTier = totalCustomers > 0 ? totalCustomers : 1;

        Map<String, Object> tierDistribution = new LinkedHashMap<>();
        tierDistribution.put("memberPercent", Math.round((double) memberCount / totalWithTier * 100));
        tierDistribution.put("silverPercent", Math.round((double) silverCount / totalWithTier * 100));
        tierDistribution.put("goldPercent", Math.round((double) goldCount / totalWithTier * 100));
        tierDistribution.put("platinumPercent", Math.round((double) platinumCount / totalWithTier * 100));

        // Growth chartData (grouped by day)
        Map<String, Long> growthGrouped = newCustomers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getRegisteredAt().format(DateTimeFormatter.ofPattern("dd/MM")),
                        TreeMap::new,
                        Collectors.counting()
                ));

        List<Map<String, Object>> growthChartData = new ArrayList<>();
        growthGrouped.forEach((key, val) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", key);
            map.put("newCustomers", val);
            growthChartData.add(map);
        });

        // Fallback to ensure there is chart data even if no new customers
        if (growthChartData.isEmpty()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", startDate.format(DateTimeFormatter.ofPattern("dd/MM")));
            map.put("newCustomers", 0);
            growthChartData.add(map);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalCustomers", totalCustomers);
        report.put("newCustomersThisMonth", newCustomersThisMonth);
        report.put("activeCustomers", activeCustomers);
        report.put("issuedPoints", issuedPoints);
        report.put("tierDistribution", tierDistribution);
        report.put("growthChartData", growthChartData);

        return report;
    }
}
