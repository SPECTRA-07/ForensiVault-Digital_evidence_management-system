package com.dems.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing top active user activity counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopActiveUserDto {

    private String username;
    private long activityCount;
}
