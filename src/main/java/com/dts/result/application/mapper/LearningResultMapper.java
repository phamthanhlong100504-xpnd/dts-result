package com.dts.result.application.mapper;

import com.dts.result.application.event.LearningResultEvent;
import com.dts.result.domain.entity.LearningResultEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class LearningResultMapper {

    public LearningResultEntity toEntity(LearningResultEvent event, int attemptNo) {
        return LearningResultEntity.builder()
                .userId(event.getUserId())
                .sourceId(event.getSourceId())
                .targetId(event.getTargetId())
                .sourceType(event.getSourceType())
                .targetType(event.getTargetType())
                .attemptNo(attemptNo)
                .activityType(event.getActivityType()).displayTitle(event.getDisplayTitle())
                .licenseClass(event.getLicenseClass()).correctCount(event.getCorrectCount() != null ? event.getCorrectCount() : count(event,"correctCount"))
                .questionCount(event.getQuestionCount() != null ? event.getQuestionCount() : count(event,"totalQuestions")).resultAvailable(event.getResultAvailable())
                .reviewAvailable(event.getReviewAvailable()).eventVersion(event.getEventVersion())
                .score(event.getScore())
                .maxScore(event.getMaxScore())
                .progress(BigDecimal.valueOf(event.getProgress() != null ? event.getProgress() : 0.0))
                .durationSeconds(event.getDurationSeconds() != null ? event.getDurationSeconds() : 0)
                .result(event.getResult())
                .startedAt(event.getStartedAt() != null ? event.getStartedAt() : Instant.now())
                .completedAt(event.getCompletedAt())
                .resultSnapshot(event.getResultSnapshot())
                .metadata(event.getMetadata())
                .build();
    }
    private Integer count(LearningResultEvent event,String name) {
        Object value=event.getResultSnapshot()==null ? null : event.getResultSnapshot().get(name);
        if (!(value instanceof Number number)) return null;
        try {
            int result=new BigDecimal(number.toString()).intValueExact();
            return result>=0 ? result : null;
        } catch(ArithmeticException failure) {return null;}
    }
}
