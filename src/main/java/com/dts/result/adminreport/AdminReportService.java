package com.dts.result.adminreport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Read-only aggregate projections. Never recalculates PASSED/FAILED or EXP eligibility. */
@Service
@Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
public class AdminReportService {
    private final JdbcTemplate jdbc;
    public AdminReportService(JdbcTemplate jdbc){this.jdbc=jdbc;}
    private static Timestamp ts(Instant t){return Timestamp.from(t);}
    private static String modeTheory(String value){return switch(value){
        case "RANDOM"->"RANDOM_EXAM";case "PRESET"->"PRESET_EXAM";case "OFFICIAL"->"OFFICIAL_EXAM";default->null;};}
    private static String modeSimulation(String value){return switch(value){
        case "RANDOM"->"SIM_RANDOM_EXAM";case "PRESET"->"SIM_PRESET_EXAM";default->null;};}
    private boolean simulationComplete(){
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT backfill_complete FROM simulation_performance_state WHERE singleton=TRUE",Boolean.class));
    }
    private long simulationWatermark(){return jdbc.queryForObject("SELECT last_sequence FROM simulation_performance_state WHERE singleton=TRUE",Long.class);}
    private List<Map<String,Object>> factRows(AdminReportScope scope){
        var all=new ArrayList<Map<String,Object>>();
        if(!scope.module().equals("SIMULATION")){
            all.addAll(jdbc.queryForList("""
                SELECT source_id::text AS source_id,user_id::text AS user_id,
                  activity_type,completed_at::text AS completed_at,result,
                  ROUND(correct_count::numeric*100/question_count,5) AS normalized_score,
                  'THEORY'::text AS module FROM screen14_exam_facts
                WHERE completed_at>=? AND completed_at<? AND (?::text IS NULL OR license_class=?)
                  AND (?::text IS NULL OR activity_type=?)
                """,ts(scope.from()),ts(scope.to()),scope.licenseClass(),scope.licenseClass(),modeTheory(scope.examMode()),modeTheory(scope.examMode())));
        }
        // Incomplete simulation projection cannot be represented as zero activity.
        if(!scope.module().equals("THEORY") && !scope.examMode().equals("OFFICIAL") && simulationComplete()){
            all.addAll(jdbc.queryForList("""
                SELECT source_id::text AS source_id,user_id::text AS user_id,
                  activity_type,completed_at::text AS completed_at,result,
                  ROUND(source_score::numeric*100/source_max_score,5) AS normalized_score,
                  'SIMULATION'::text AS module FROM simulation_performance_exam_facts
                WHERE completed_at>=? AND completed_at<? AND (?::text IS NULL OR license_class=?)
                  AND (?::text IS NULL OR activity_type=?)
                """,ts(scope.from()),ts(scope.to()),scope.licenseClass(),scope.licenseClass(),modeSimulation(scope.examMode()),modeSimulation(scope.examMode())));
        }
        // Preserve event-source identity, never count repeated delivery twice.
        var unique=new LinkedHashMap<String,Map<String,Object>>();
        for(var row:all){String key=row.get("module")+":"+row.get("source_id");
            Map<String,Object> old=unique.putIfAbsent(key,row);
            if(old!=null && !old.equals(row))throw new IllegalStateException("Conflicting exam projection source");
        }
        return List.copyOf(unique.values());
    }
    private String status(AdminReportScope scope){
        if(scope.module().equals("THEORY") || scope.examMode().equals("OFFICIAL"))return "READY";
        return simulationComplete()?"READY":"PARTIAL";
    }
    private Map<String,Object> meta(AdminReportScope s,String state,Instant now){
        var out=new LinkedHashMap<String,Object>();out.put("from",s.from());out.put("to",s.to());
        out.put("timezone",AdminReportScope.REPORT_ZONE.toString());out.put("licenseClass",s.licenseClass());
        out.put("module",s.module());out.put("examMode",s.examMode());out.put("asOf",now);
        out.put("sourceService","dts-result");out.put("dataStatus",state);
        if(!"READY".equals(state))out.put("missingSources",List.of("simulation_performance_backfill"));
        return out;
    }
    private static Map<String,Object> summary(List<Map<String,Object>> rows){
        long pass=0,fail=0;BigDecimal sum=BigDecimal.ZERO;Set<Object> users=new HashSet<>();
        for(var r:rows){if("PASSED".equals(r.get("result")))pass++;else if("FAILED".equals(r.get("result")))fail++;
            sum=sum.add((BigDecimal)r.get("normalized_score"));users.add(r.get("user_id"));}
        long count=pass+fail;var summary=new LinkedHashMap<String,Object>();
        summary.put("completedAttempts",count);summary.put("passedAttempts",pass);summary.put("failedAttempts",fail);
        summary.put("activeLearners",users.size());
        summary.put("passRate",count==0?null:BigDecimal.valueOf(pass*100).divide(BigDecimal.valueOf(count),2,RoundingMode.HALF_UP));
        summary.put("averageNormalizedScore",count==0?null:sum.divide(BigDecimal.valueOf(count),2,RoundingMode.HALF_UP));
        return summary;
    }
    public Map<String,Object> overview(AdminReportScope s,Instant now){
        String status=status(s);var out=meta(s,status,now);
        out.put("summary","READY".equals(status)?summary(factRows(s)):null);
        out.put("warnings","READY".equals(status)?List.of():List.of("Simulation projection is not fully synchronized; totals are withheld"));
        return out;
    }
    public Map<String,Object> trends(AdminReportScope s,Instant now){
        String status=status(s);var out=meta(s,status,now);
        if(!"READY".equals(status)){out.put("dailyTrend",null);return out;}
        var byDay=new TreeMap<LocalDate,List<Map<String,Object>>>();
        for(var row:factRows(s)){
            // SQL timestamptz textual representation is recognized as PostgreSQL local text.
            // Explicit date is extracted at the DB boundary, not from a JVM timezone.
            String source=String.valueOf(row.get("completed_at"));
            Instant instant;
            try{instant=Timestamp.valueOf(source.replace('T',' ').substring(0,19)).toInstant();}
            catch(RuntimeException ex){throw new IllegalStateException("Cannot parse source timestamp",ex);}
            LocalDate day=instant.atZone(AdminReportScope.REPORT_ZONE).toLocalDate();
            byDay.computeIfAbsent(day,k->new ArrayList<>()).add(row);
        }
        var days=new ArrayList<Map<String,Object>>();
        for(var entry:byDay.entrySet()){
            var row=new LinkedHashMap<String,Object>();row.put("date",entry.getKey());row.putAll(summary(entry.getValue()));days.add(row);
        }
        out.put("dailyTrend",days);return out;
    }
    public Map<String,Object> breakdown(AdminReportScope s,Instant now){
        String status=status(s);var out=meta(s,status,now);
        if(!"READY".equals(status)){out.put("items",null);return out;}
        var buckets=new TreeMap<String,List<Map<String,Object>>>();
        for(var row:factRows(s)){
            String key=row.get("module")+"/"+row.get("activity_type");
            buckets.computeIfAbsent(key,k->new ArrayList<>()).add(row);
        }
        var items=new ArrayList<Map<String,Object>>();
        for(var entry:buckets.entrySet()){
            var item=new LinkedHashMap<String,Object>();item.put("sourceGroup",entry.getKey());item.putAll(summary(entry.getValue()));items.add(item);
        }
        out.put("items",items);return out;
    }
    public Map<String,Object> exp(AdminReportScope s,Instant now){
        if(s.licenseClass()==null)throw new IllegalArgumentException("licenseClass required for EXP policy scope");
        String state=status(s);var out=meta(s,state,now);
        if(!"READY".equals(state)){out.put("totalCreditedExp",null);out.put("creditedActivities",null);return out;}
        var items=new ArrayList<Map<String,Object>>();
        if(!s.module().equals("SIMULATION")){
            items.addAll(jdbc.queryForList("""
                SELECT 'THEORY'::text module,activity_type,COUNT(*)::bigint credited_activities,
                   COALESCE(SUM(credited_exp),0)::bigint credited_exp
                FROM dts_exp_credits(?::text,?,?) GROUP BY activity_type
                """,s.licenseClass(),ts(s.from()),ts(s.to())));
        }
        if(!s.module().equals("THEORY")){
            items.addAll(jdbc.queryForList("""
                SELECT 'SIMULATION'::text module,activity_type,COUNT(*)::bigint credited_activities,
                   COALESCE(SUM(credited_exp),0)::bigint credited_exp
                FROM dts_simulation_exp_credits(?::text,?,?,?) GROUP BY activity_type
                """,s.licenseClass(),ts(s.from()),ts(s.to()),simulationWatermark()));
        }
        long total=0,activities=0;
        for(var r:items){total+=((Number)r.get("credited_exp")).longValue();activities+=((Number)r.get("credited_activities")).longValue();}
        out.put("totalCreditedExp",total);out.put("creditedActivities",activities);
        out.put("bySource",items);out.put("ruleBasis","SOURCE_AUTHORIZED_CREDIT_FUNCTIONS");return out;
    }
    public Map<String,Object> leaderboard(AdminReportScope s,Instant now,int limit){
        if(s.licenseClass()==null)throw new IllegalArgumentException("licenseClass required for ranking scope");
        if(limit<1||limit>100)throw new IllegalArgumentException("limit must be between 1 and 100");
        String state=status(s);var out=meta(s,state,now);
        if(!state.equals("READY")){out.put("items",null);out.put("totalParticipants",null);return out;}
        String credits=s.module().equals("THEORY")?"dts_exp_credits(?::text,?,?)":
                s.module().equals("SIMULATION")?"dts_simulation_exp_credits(?::text,?,?,?)":"dts_combined_exp_credits(?::text,?,?,?)";
        String sql="WITH credits AS (SELECT * FROM "+credits+"), totals AS ("+
             "SELECT user_id,SUM(credited_exp)::bigint ranking_score,MAX(credited_at) last_credited_at FROM credits GROUP BY user_id),"+
             "ranked AS (SELECT t.user_id,t.ranking_score,t.last_credited_at,"+
             "ROW_NUMBER() OVER(ORDER BY t.ranking_score DESC,t.last_credited_at ASC,t.user_id ASC) rank"+
             " FROM totals t LEFT JOIN leaderboard_public_profiles p ON p.user_id=t.user_id"+
             " WHERE t.ranking_score>0 AND COALESCE(p.deleted,FALSE)=FALSE)"+
             " SELECT rank,ranking_score,last_credited_at,LEFT(MD5(user_id::text),12) learner_reference"+
             " FROM ranked ORDER BY rank LIMIT ?";
        var args=new ArrayList<Object>();args.add(s.licenseClass());args.add(ts(s.from()));args.add(ts(s.to()));
        if(!s.module().equals("THEORY"))args.add(simulationWatermark());args.add(limit);
        out.put("items",jdbc.queryForList(sql,args.toArray()));out.put("leaderIdentifiers","PSEUDONYMIZED");
        out.put("ruleBasis","SAME_SOURCE_CREDIT_FUNCTIONS_AS_LEARNER_LEADERBOARD");return out;
    }
    public Map<String,Object> event(UUID eventId){
        var rows=jdbc.queryForList("""
            SELECT event_id,event_sequence,event_type,occurred_at,received_at,applied
            FROM simulation_performance_inbox WHERE event_id=?
            """,eventId);
        if(rows.isEmpty())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Event not found");
        var out=new LinkedHashMap<String,Object>();out.put("sourceService","dts-result");out.put("asOf",Instant.now());
        out.put("dataStatus","READY");out.put("event",rows.getFirst());
        out.put("sourceDelivered",true);out.put("projectionApplied",Boolean.TRUE.equals(rows.getFirst().get("applied")));
        out.put("brokerAcknowledged",null);return out;
    }
    public Map<String,Object> integrationStatus(){
        var rows=jdbc.queryForMap("""
            SELECT (SELECT COUNT(*) FROM simulation_performance_inbox WHERE NOT applied) AS pending,
              (SELECT MIN(received_at) FROM simulation_performance_inbox WHERE NOT applied) AS oldest_pending,
              (SELECT MAX(received_at) FROM simulation_performance_inbox WHERE applied) AS last_applied,
              (SELECT last_sequence FROM simulation_performance_state WHERE singleton) AS watermark,
              (SELECT backfill_complete FROM simulation_performance_state WHERE singleton) AS backfill_complete
            """);
        var out=new LinkedHashMap<String,Object>();out.put("sourceService","dts-result");out.put("asOf",Instant.now());
        out.put("dataStatus",Boolean.TRUE.equals(rows.get("backfill_complete"))?"READY":"PARTIAL");out.put("simulationStream",rows);
        out.put("brokerAcknowledgedMeansProjectionApplied",false);return out;
    }
}
