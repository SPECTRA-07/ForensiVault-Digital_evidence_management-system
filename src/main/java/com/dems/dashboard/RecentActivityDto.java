package com.dems.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Unified cross-module recent activity feed item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {

    private String activityType;
    private String reference;
    private String description;
    private String performedBy;
    private OffsetDateTime timestamp;
}
