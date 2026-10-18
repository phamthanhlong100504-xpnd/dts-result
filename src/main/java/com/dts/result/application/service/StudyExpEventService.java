package com.dts.result.application.service;

import com.dts.result.api.exception.ValidationException;
import com.dts.result.application.event.LearningResultEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Separate EXP evidence: these messages never create History rows or Progress events. */
@Service
public class StudyExpEventService {
    public static final String QUESTION="QUESTION_CORRECT_RECORDED";
    public static final String READY="PRACTICE_EXP_SOURCE_READY";
    private static final Set<String> CONTEXTS=Set.of("CHAPTER_LEARNING","CHAPTER_REVIEW",
            "CRITICAL_PRACTICE","WRONG_REVIEW","PRACTICE_MODE");
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public StudyExpEventService(JdbcTemplate jdbc,ObjectMapper mapper) {this.jdbc=jdbc;this.mapper=mapper;}
    public boolean handles(LearningResultEvent e) {return e!=null && (QUESTION.equals(e.getEventType()) || READY.equals(e.getEventType()));}
    @Transactional
    public void record(LearningResultEvent e) {
        if(!handles(e) || !Integer.valueOf(1).equals(e.getEventVersion()) || e.getEventId()==null
                || !e.getEventId().equals(e.getSourceId()) || e.getOccurredAt()==null || e.getMetadata()==null)
            throw invalid("Invalid EXP evidence header");
        JsonNode m=mapper.valueToTree(e.getMetadata());
        String bank=text(m,"bankVersion",64);
        Instant captured=instant(m,"captureStartedAt");
        if(e.getOccurredAt().isBefore(captured)) throw invalid("EXP evidence predates capture");
        if(QUESTION.equals(e.getEventType())) recordQuestion(e,m,bank,captured);
        else recordPolicy(e,m,bank,captured);
    }
    private void recordQuestion(LearningResultEvent e,JsonNode m,String bank,Instant captured) {
        if(e.getUserId()==null || !"PRACTICE_QUESTION".equals(e.getSourceType()) || !"QUESTION".equals(e.getTargetType()))
            throw invalid("Invalid correct-question source");
        ResultBusinessScope.license(e.getLicenseClass());
        int question=positiveInt(m,"questionId",Integer.MAX_VALUE);
        String context=text(m,"context",40);
        if(!CONTEXTS.contains(context)) throw invalid("Invalid question context");
        jdbc.update("""
            INSERT INTO study_question_facts(event_id,user_id,license_class,bank_version,
              question_id,context,occurred_at,capture_started_at) VALUES(?,?,?,?,?,?,?,?)
            ON CONFLICT(event_id) DO NOTHING
            """,e.getEventId(),e.getUserId(),e.getLicenseClass(),bank,question,context,
                Timestamp.from(e.getOccurredAt()),Timestamp.from(captured));
        Boolean same=jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM study_question_facts WHERE event_id=? AND user_id=?
              AND license_class=? AND bank_version=? AND question_id=? AND context=?
              AND occurred_at=? AND capture_started_at=?)
            """,Boolean.class,e.getEventId(),e.getUserId(),e.getLicenseClass(),bank,question,context,
                Timestamp.from(e.getOccurredAt()),Timestamp.from(captured));
        if(!Boolean.TRUE.equals(same)) throw invalid("An EXP event ID cannot replace different evidence");
    }
    private void recordPolicy(LearningResultEvent e,JsonNode m,String bank,Instant captured) {
        if(e.getUserId()!=null || !"PRACTICE_CONFIG".equals(e.getSourceType()) || !"CONFIG".equals(e.getTargetType()))
            throw invalid("Invalid Practice policy source");
        JsonNode revision=m.get("policyRevision");
        if(revision==null || !revision.isIntegralNumber() || !revision.canConvertToLong() || revision.longValue()<1)
            throw invalid("Invalid policy revision");
        JsonNode counts=m.get("licenseQuestionCounts");
        if(counts==null || !counts.isObject() || counts.isEmpty()) throw invalid("Source question-count policy is required");
        counts.fieldNames().forEachRemaining(license -> {
            ResultBusinessScope.license(license);
            positiveInt(counts,license,1000);
        });
        String json=counts.toString();
        jdbc.update("""
            INSERT INTO practice_exp_policy_events(event_id,capture_started_at,policy_revision,
              policy_observed_at,bank_version,question_counts) VALUES(?,?,?,?,?,?::jsonb)
            ON CONFLICT(event_id) DO NOTHING
            """,e.getEventId(),Timestamp.from(captured),revision.longValue(),Timestamp.from(e.getOccurredAt()),bank,json);
        Boolean same=jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM practice_exp_policy_events WHERE event_id=? AND capture_started_at=?
              AND policy_revision=? AND policy_observed_at=? AND bank_version=? AND question_counts=?::jsonb)
            """,Boolean.class,e.getEventId(),Timestamp.from(captured),revision.longValue(),Timestamp.from(e.getOccurredAt()),bank,json);
        if(!Boolean.TRUE.equals(same)) throw invalid("A source policy ID cannot replace different evidence");
    }
    private static String text(JsonNode node,String key,int max) {
        JsonNode value=node.get(key);
        if(value==null || !value.isTextual() || value.textValue().isBlank() || value.textValue().length()>max)
            throw invalid("Invalid EXP field: "+key);
        return value.textValue();
    }
    private static int positiveInt(JsonNode node,String key,int max) {
        JsonNode value=node.get(key);
        if(value==null || !value.isIntegralNumber() || !value.canConvertToInt() || value.intValue()<1 || value.intValue()>max)
            throw invalid("Invalid EXP integer: "+key);
        return value.intValue();
    }
    private static Instant instant(JsonNode node,String key) {
        try {return java.time.OffsetDateTime.parse(text(node,key,64)).toInstant();}
        catch(java.time.DateTimeException ex) {throw invalid("Invalid EXP timestamp: "+key);}
    }
    private static ValidationException invalid(String text) {return new ValidationException(text);}
}
