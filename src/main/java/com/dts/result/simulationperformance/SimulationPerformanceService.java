package com.dts.result.simulationperformance;

import com.dts.result.application.service.ResultBusinessScope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
public class SimulationPerformanceService {
    private final JdbcTemplate jdbc;private final ObjectMapper mapper;private final SimulationEventStore events;private final Clock clock;
    static final String ANALYTICS=resource("analytics.sql"),LEADERBOARD=resource("leaderboard.sql");
    public SimulationPerformanceService(JdbcTemplate jdbc,ObjectMapper mapper,SimulationEventStore events,@Qualifier("resultBusinessClock") Clock clock) {
        this.jdbc=jdbc;this.mapper=mapper;this.events=events;this.clock=clock;
    }
    private static String resource(String name) {try(var stream=new ClassPathResource("simulation-performance/"+name).getInputStream()){return new String(stream.readAllBytes(),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException("Simulation query resource missing",e);}}
    private ObjectNode query(String sql,Object...args) {try{return(ObjectNode)mapper.readTree(jdbc.queryForObject(sql,String.class,args));}catch(java.io.IOException e){throw new IllegalStateException("Cannot map simulation projection");}}
    private record Context(String license,SimulationEventStore.Marker marker,Instant asOf){}
    private Context context(String license,Long watermark,Instant requested) {
        SimulationEventStore.requireLicense(license);if(watermark!=null && watermark<1)throw new IllegalArgumentException("Invalid watermark");
        var marker=events.marker(watermark);Instant asOf=requested!=null?requested:marker.asOf()!=null?marker.asOf():clock.instant();
        if(asOf.isAfter(clock.instant().plusSeconds(5)) || marker.asOf()!=null && asOf.isAfter(marker.asOf()))throw new IllegalArgumentException("asOf exceeds the confirmed source marker");
        return new Context(license,marker,asOf);
    }
    private static ResultBusinessScope.Window combinedWindow(String period,Instant now) {
        if(period!=null && Set.of("THIS_WEEK","THIS_MONTH","ALL_TIME").contains(period))return ResultBusinessScope.leaderboard(period,now);
        return ResultBusinessScope.analytics(period,now);
    }
    private static Timestamp ts(Instant value){return value==null?null:Timestamp.from(value);}
    private void meta(ObjectNode response,Context ctx) {
        response.put("dataStatus",ctx.marker().status());response.put("simulationReady",ctx.marker().bankReady());
        response.set("syncStatus",mapper.valueToTree(ctx.marker()));response.put("sourceWatermark",ctx.marker().sequence());
    }
    public JsonNode analytics(UUID user,String license,String period,Long watermark,Instant asOf) {
        var ctx=context(license,watermark,asOf);var window=combinedWindow(period,ctx.asOf());
        var result=query(ANALYTICS,user,license,ts(window.from()),ts(window.effectiveEnd()),ts(ctx.asOf()),ctx.marker().sequence(),period);
        meta(result,ctx);
        if(!ctx.marker().complete()) {
            ObjectNode summary=(ObjectNode)result.get("summary");
            for(String field:List.of("averageScore","passRate","totalExp","attempts","simulationAttempts","simulationExp"))summary.putNull(field);
            result.put("partial",true);
        }else result.put("partial",false);
        return result;
    }
    public JsonNode leaderboard(UUID user,String license,String period,String pageValue,String sizeValue,Long watermark,Instant asOf) {
        var ctx=context(license,watermark,asOf);var window=combinedWindow(period,ctx.asOf());
        int page=ResultBusinessScope.integer(pageValue,0,Integer.MAX_VALUE,"page"),size=ResultBusinessScope.integer(sizeValue,1,100,"size");
        var result=query(LEADERBOARD,user,license,ts(window.from()),ts(window.effectiveEnd()),ts(ctx.asOf()),ctx.marker().sequence(),period,page,size);meta(result,ctx);
        if(!ctx.marker().complete()) {
            result.putNull("totalParticipants");result.putNull("totalPages");result.set("items",mapper.createArrayNode());
            ObjectNode current=(ObjectNode)result.get("currentUser");current.putNull("rank");current.putNull("rankingScore");current.putNull("simulationExp");
        }
        return result;
    }
    public JsonNode history(UUID user,String license,String activity,String outcome,Instant from,Instant to,String pageValue,String sizeValue,String sort,Long watermark,Instant asOf) {
        var ctx=context(license,watermark,asOf);int page=ResultBusinessScope.integer(pageValue,0,Integer.MAX_VALUE,"page"),size=ResultBusinessScope.integer(sizeValue,1,100,"size");
        if(activity!=null && !Set.of("SIM_RANDOM_EXAM","SIM_PRESET_EXAM").contains(activity))throw new IllegalArgumentException("Invalid simulation activityType");
        if(outcome!=null && !Set.of("PASSED","FAILED").contains(outcome))throw new IllegalArgumentException("Invalid result");
        if(!Set.of("completedAt,desc","completedAt,asc").contains(sort))throw new IllegalArgumentException("Use completedAt,desc or completedAt,asc");
        Instant end=to==null || to.isAfter(ctx.asOf())?ctx.asOf():to;
        if(from!=null && !from.isBefore(end))throw new IllegalArgumentException("from must be before to");
        var args=new ArrayList<Object>();args.add(user);args.add(license);args.add(ctx.marker().sequence());
        String where=" WHERE user_id=? AND license_class=? AND event_sequence<=?";
        if(activity!=null){where+=" AND activity_type=?";args.add(activity);}if(outcome!=null){where+=" AND result=?";args.add(outcome);}
        if(from!=null){where+=" AND completed_at>=?";args.add(ts(from));}where+=" AND completed_at<?";args.add(ts(end));
        long total=jdbc.queryForObject("SELECT COUNT(*) FROM simulation_performance_exam_facts"+where,Long.class,args.toArray());
        args.add(size);args.add((long)page*size);String direction=sort.endsWith("asc")?"ASC":"DESC";
        String sql="SELECT jsonb_build_object('resultId',source_id,'sessionId',source_id,'sourceType','SIMULATION_SESSION','sourceId',source_id,'module','SIMULATION',"+
          "'activityType',activity_type,'displayTitle',display_title,'licenseClass',license_class,'score',source_score,'maxScore',source_max_score,'sourceScore',source_score,'sourceMaxScore',source_max_score,"+
          "'result',result,'startedAt',started_at,'completedAt',completed_at,'durationSeconds',duration_seconds,'bankVersion',bank_version,'scoringVersion',scoring_version,"+
          "'presetSetId',preset_set_id,'presetVersionId',preset_version_id,'resultAvailable',TRUE,'reviewAvailable',TRUE,"+
          "'sourceResult',jsonb_build_object('sourceType','SIMULATION_SESSION','sourceId',source_id,'licenseClass',license_class,'resultPath','/practice/simulations/exams/'||source_id||'/result',"+
          "'reviewPath','/practice/simulations/exams/'||source_id||'/review','resultApiPath','/api/practice/v1/simulation-exams/sessions/'||source_id||'/result?licenseClass='||license_class,"+
          "'reviewApiPath','/api/practice/v1/simulation-exams/sessions/'||source_id||'/review?licenseClass='||license_class))::text FROM simulation_performance_exam_facts"+where+
          " ORDER BY completed_at "+direction+",source_id "+direction+" LIMIT ? OFFSET ?";
        var content=mapper.createArrayNode();jdbc.query(sql,r->{try{content.add(mapper.readTree(r.getString(1)));}catch(Exception e){throw new IllegalStateException("Invalid history projection");}},args.toArray());
        var result=mapper.createObjectNode();result.put("licenseClass",license);result.put("module","SIMULATION");result.put("performanceMode","COMBINED");
        result.put("asOf",ctx.asOf().toString());result.put("page",page);result.put("size",size);result.put("sort",sort);result.set("content",content);
        if(ctx.marker().complete()){result.put("totalElements",total);result.put("totalPages",(total+size-1)/size);}else{result.putNull("totalElements");result.putNull("totalPages");}
        meta(result,ctx);return result;
    }
}
