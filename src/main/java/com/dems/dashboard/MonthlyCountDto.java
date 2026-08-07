package com.dems.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing monthly count trend aggregates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyCountDto {

    private int year;
    private int month;
    private String monthName;
    private long count;
}
