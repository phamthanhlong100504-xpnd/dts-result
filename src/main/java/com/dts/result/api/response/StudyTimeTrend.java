package com.dts.result.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyTimeTrend {
    private String date; // Format YYYY-MM-DD
    private long durationSeconds;
}
