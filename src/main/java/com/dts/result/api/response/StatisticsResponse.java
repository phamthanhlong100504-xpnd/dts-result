package com.dts.result.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private List<ScoreTrend> scoreTrend;
    private List<StudyTimeTrend> studyTimeTrend;
    private List<AttemptTrend> attemptTrend;
    private List<CompletionTrend> completionTrend;
}
