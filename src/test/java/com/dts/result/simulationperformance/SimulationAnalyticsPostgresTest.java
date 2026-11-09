package com.dts.result.simulationperformance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.dts.result.simulationperformance.SimulationFixtures.*;

@EnabledIfEnvironmentVariable(named="DTS_SIMULATION_INTEGRATION_POSTGRES_TEST",matches="true")
class SimulationAnalyticsPostgresTest {
    static SimulationTestDatabase db;static JdbcTemplate jdbc;SimulationEventStore store;SimulationPerformanceService service;long seq;
    @BeforeAll static void schema()throws Exception{db=new SimulationTestDatabase();jdbc=new JdbcTemplate(db.source);}
    @AfterAll static void done(){if(db!=null)db.close();}
    @BeforeEach void setup(){jdbc.execute("TRUNCATE simulation_performance_inbox,learning_results,leaderboard_public_profiles CASCADE");jdbc.update("UPDATE simulation_performance_state SET last_sequence=0,backfill_complete=FALSE,capture_started_at=NULL");store=new SimulationEventStore(jdbc,JSON);service=new SimulationPerformanceService(jdbc,JSON,store,Clock.fixed(NOW,ZoneOffset.UTC));seq=1;accept(begin());SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(USER,null,List.of()));}
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    void accept(ObjectNode event){db.transaction.executeWithoutResult(s->store.accept(event.toString()));}
    void complete(){accept(end(++seq));accept(marker(++seq,false));}
    JsonNode analytics(){return service.analytics(USER,"B","ALL",null,null);}
    void legacy(int correct,boolean passed){jdbc.update("""
        INSERT INTO learning_results(user_id,source_id,source_type,target_id,target_type,attempt_no,activity_type,display_title,license_class,correct_count,question_count,result_available,review_available,event_version,result,score,max_score,progress,duration_seconds,started_at,completed_at)
        VALUES(?,?,'PRACTICE_SESSION',?,'EXAM',1,'RANDOM_EXAM','Theory source','B',?,30,TRUE,TRUE,2,?,?,100,100,10,?,?)
        """,USER,UUID.randomUUID(),UUID.randomUUID(),correct,passed?"PASSED":"FAILED",new java.math.BigDecimal(correct).multiply(java.math.BigDecimal.valueOf(100)).divide(java.math.BigDecimal.valueOf(30),6,java.math.RoundingMode.HALF_UP),Timestamp.from(NOW.minusSeconds(120)),Timestamp.from(NOW.minusSeconds(110)));}
    void exam(int score,String result){accept(SimulationFixtures.exam(++seq,UUID.randomUUID(),score,result,"B",true));}
    long exp(){return jdbc.queryForObject("SELECT COALESCE(SUM(credited_exp),0)FROM dts_simulation_exp_credits('B',NULL,?,?)",Long.class,Timestamp.from(NOW),seq);}
    @Test void equalAttemptWeightsNormalizeTheoryAndVideoWithoutMixingRawScales(){legacy(27,true);exam(40,"PASSED");complete();var data=analytics();assertEquals(85.0,data.path("summary").path("averageScore").doubleValue());assertEquals(162,data.path("summary").path("totalExp").longValue());assertEquals(2,data.path("summary").path("attempts").intValue());assertEquals(40,data.path("recentResults").get(0).path("sourceScore").intValue());assertFalse(data.path("recentResults").get(0).has("correctCount"));}
    @Test void aCriticalTheoryFailureRemainsFailedDespiteHighNormalizedScore(){legacy(27,false);exam(40,"PASSED");complete();assertEquals(50,analytics().path("summary").path("passRate").intValue());assertEquals(85,analytics().path("summary").path("averageScore").intValue());}
    @Test void noSimulationActivityDoesNotBecomeAFakeZeroInAverage(){legacy(27,true);complete();var summary=analytics().path("summary");assertEquals(90,summary.path("averageScore").intValue());assertEquals(1,summary.path("attempts").intValue());assertEquals(0,summary.path("simulationAttempts").intValue());assertEquals(84,summary.path("totalExp").intValue());}
    @Test void genuineEmptyDataHasNullableAverageAndPassRate(){complete();var summary=analytics().path("summary");assertTrue(summary.path("averageScore").isNull());assertTrue(summary.path("passRate").isNull());assertEquals(0,summary.path("attempts").intValue());}
    @Test void incompleteBackfillDoesNotPretendCountsAndExpAreZero(){legacy(27,true);var response=analytics();assertTrue(response.path("summary").path("totalExp").isNull());assertTrue(response.path("summary").path("attempts").isNull());assertEquals("SYNCING",response.path("dataStatus").asText());}
    @Test void firstFiveRandomsAreNotTheFiveBestAndZeroUsesASlot(){for(int s:new int[]{0,25,34,35,40,50})exam(s,s>=35?"PASSED":"FAILED");complete();assertEquals(221,exp());assertEquals(6,analytics().path("summary").path("attempts").intValue());}
    @Test void historicalBackfillAppearsInHistoryButCannotReceiveExp(){accept(SimulationFixtures.exam(++seq,UUID.randomUUID(),50,"PASSED","B",false));complete();assertEquals(0,exp());assertEquals(1,analytics().path("summary").path("attempts").intValue());}
    @Test void presetHistoryKeepsLatestLowerScoreWhileExpUsesBestPerSet(){UUID preset=UUID.randomUUID();for(int s:new int[]{50,45,30}){var e=SimulationFixtures.exam(++seq,UUID.randomUUID(),s,s>=35?"PASSED":"FAILED","B",true);var d=(ObjectNode)e.get("data");d.put("mode","PRESET");d.put("activityType","SIM_PRESET_EXAM");d.put("presetSetId",preset.toString());d.put("presetVersionId",UUID.randomUUID().toString());accept(e);}complete();assertEquals(90,exp());var history=service.history(USER,"B","SIM_PRESET_EXAM",null,null,null,"0","1","completedAt,desc",null,null);assertEquals(3,history.path("totalElements").intValue());assertEquals(30,history.path("content").get(0).path("score").intValue());assertTrue(history.path("content").get(0).path("durationSeconds").isNull());}
    @Test void historyFilterOccursBeforePaginationAndKeepsOwnerIsolation(){exam(50,"PASSED");exam(20,"FAILED");exam(40,"PASSED");complete();var page=service.history(USER,"B",null,"FAILED",null,null,"0","1","completedAt,desc",null,null);assertEquals(1,page.path("totalElements").intValue());assertEquals(20,page.path("content").get(0).path("score").intValue());var foreign=service.history(UUID.randomUUID(),"B",null,null,null,null,"0","20","completedAt,desc",null,null);assertEquals(0,foreign.path("totalElements").intValue());}
    @Test void normalizedPassRateDoesNotRegradeSourceOutcome(){var failed=SimulationFixtures.exam(++seq,UUID.randomUUID(),40,"FAILED","B",true);((ObjectNode)failed.get("data")).put("passingScore",45);accept(failed);var passed=SimulationFixtures.exam(++seq,UUID.randomUUID(),34,"PASSED","B",true);((ObjectNode)passed.get("data")).put("passingScore",30);accept(passed);complete();assertEquals(50,analytics().path("summary").path("passRate").intValue());}
    @Test void sameScopeAnalyticsAndRankingUseExactlyTheSameCredits(){legacy(27,true);exam(40,"PASSED");complete();var a=analytics();var b=service.leaderboard(USER,"B","ALL_TIME","0","10",null,NOW);assertEquals(a.path("summary").path("totalExp"),b.path("currentUser").path("rankingScore"));assertEquals(a.path("expScope"),b.path("expScope"));}
    @Test void readEndpointSerializesRealDatabaseRowsAndDoesNotPublishEvents()throws Exception{exam(40,"PASSED");complete();long before=jdbc.queryForObject("SELECT COUNT(*) FROM simulation_performance_inbox",Long.class);var mvc=MockMvcBuilders.standaloneSetup(new SimulationPerformanceController(service)).setControllerAdvice(new SimulationPerformanceAdvice()).build();var response=mvc.perform(get("/v1/results/me/analytics/combined").param("licenseClass","B").param("period","ALL")).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(jsonPath("$.summary.averageScore").value(80)).andReturn().getResponse().getContentAsString();Path out=Path.of("target/simulation-performance-examples");Files.createDirectories(out);Files.writeString(out.resolve("combined-analytics.actual.json"),response);var historyBody=mvc.perform(get("/v1/results/me/history/simulations").param("licenseClass","B")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();Files.writeString(out.resolve("simulation-history.actual.json"),historyBody);var rankingBody=mvc.perform(get("/v1/leaderboards/combined").param("licenseClass","B").param("period","ALL")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();Files.writeString(out.resolve("combined-ranking.actual.json"),rankingBody);var errorBody=mvc.perform(get("/v1/results/me/history/simulations").param("licenseClass","A1")).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();Files.writeString(out.resolve("invalid-request.actual.json"),errorBody);assertEquals(before,jdbc.queryForObject("SELECT COUNT(*)FROM simulation_performance_inbox",Long.class));}
    @Test void changingLicenseAndReadFiltersDoesNotEmitLearningEvents(){exam(40,"PASSED");complete();long before=jdbc.queryForObject("SELECT COUNT(*)FROM simulation_performance_inbox",Long.class);var other=service.analytics(USER,"C1","ALL",null,null);assertEquals(0,other.path("summary").path("simulationAttempts").intValue());assertEquals(before,jdbc.queryForObject("SELECT COUNT(*)FROM simulation_performance_inbox",Long.class));}
    @Test void randomDailyQuotaIsAppliedBeforeTheRequestedFromBoundary(){
        for(int score:new int[]{0,25,34,35,40,50})exam(score,score>=35?"PASSED":"FAILED");complete();
        var boundary=Timestamp.from(NOW.minusSeconds(20).plusMillis(5));
        assertEquals(150,jdbc.queryForObject("SELECT COALESCE(SUM(credited_exp),0) FROM dts_simulation_exp_credits('B',?,?,?)",Long.class,boundary,Timestamp.from(NOW),seq));
    }
    @Test void sourceNSnapshotIsNotReplacedByCurrentPolicyOrAnotherLicense(){
        var first=SimulationFixtures.exam(++seq,UUID.randomUUID(),40,"PASSED","C1",true);((ObjectNode)first.get("data")).put("nSnapshot",35);accept(first);
        var second=SimulationFixtures.exam(++seq,UUID.randomUUID(),40,"PASSED","C1",true);((ObjectNode)second.get("data")).put("nSnapshot",40).put("expPolicyRevision",2);accept(second);complete();
        assertEquals(180,service.analytics(USER,"C1","ALL",null,null).path("summary").path("totalExp").longValue());
        assertEquals(0,exp());
    }
    @Test void calendarScopesCanBeComparedWithoutChangingLegacyPeriodContracts(){
        exam(40,"PASSED");complete();var a=service.analytics(USER,"B","THIS_MONTH",null,null);var b=service.leaderboard(USER,"B","THIS_MONTH","0","10",null,null);
        assertEquals(a.path("expScope"),b.path("expScope"));assertEquals(a.path("summary").path("totalExp"),b.path("currentUser").path("rankingScore"));
    }
}
