package com.autowash.autowash_pro.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueDataDTO {
    private String time;
    private String licensePlate;
    private String serviceType; 
    private String status;
}
