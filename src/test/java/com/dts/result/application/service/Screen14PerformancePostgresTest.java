package com.dts.result.application.service;

import com.dts.result.api.exception.ValidationException;
import com.dts.result.application.event.LearningResultEvent;
import com.dts.result.infrastructure.repository.PerformanceProjectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import static org.junit.jupiter.api.Assertions.*;

/** Real canonical SQL and query/credit services, in a test-owned PostgreSQL only. */
@Testcontainers
@EnabledIfEnvironmentVariable(named="DTS_SCREEN14_16_POSTGRES_TEST",matches="true")
class Screen14PerformancePostgresTest {
    @Container static final GenericContainer<?> postgres=new GenericContainer<>("postgres:16-alpine")
        .withEnv("POSTGRES_DB","dts_result_db").withEnv("POSTGRES_USER","test").withEnv("POSTGRES_PASSWORD","test")
        .withExposedPorts(5432).withCopyFileToContainer(MountableFile.forHostPath(Path.of("docs/db").toAbsolutePath()),"/tmp/schema")
        .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n",2));
    static JdbcTemplate jdbc;
    static final Instant NOW=Instant.parse("2026-10-16T07:00:00Z");
    static final Instant CAPTURE=Instant.parse("2026-09-01T00:00:00Z");
    static final Instant DAY=Instant.parse("2026-10-10T04:00:00Z");
    ObjectMapper mapper;
    StudyExpEventService events;
    PublicProfileProjectionService profiles;
    LeaderboardService leaderboard;
    ResultAnalyticsService analytics;
    UUID user,target;

    @BeforeAll static void schema() throws Exception {
        for(int run=0;run<2;run++) {
            var result=postgres.execInContainer("psql","-X","-U","test","-d","dts_result_db","-v","ON_ERROR_STOP=1","-f","/tmp/schema/apply-schema.psql");
            assertEquals(0,result.getExitCode(),result.getStderr());
        }
        jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:postgresql://"+postgres.getHost()+":"+
            postgres.getMappedPort(5432)+"/dts_result_db","test","test"));
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM dts_result_schema_history",Integer.class));
    }
    @BeforeEach void setup() {
        jdbc.execute("TRUNCATE learning_summaries,learning_results,study_question_facts,practice_exp_policy_events,result_projection_adjustments,leaderboard_public_profiles RESTART IDENTITY CASCADE");
        mapper=new ObjectMapper().findAndRegisterModules();
        events=new StudyExpEventService(jdbc,mapper);profiles=new PublicProfileProjectionService(jdbc,mapper);
        var repository=new PerformanceProjectionRepository(jdbc,mapper);
        var clock=Clock.fixed(NOW,ZoneOffset.UTC);
        leaderboard=new LeaderboardService(repository,clock);analytics=new ResultAnalyticsService(repository,clock);
        user=UUID.randomUUID();target=UUID.randomUUID();
        events.record(policy(1,CAPTURE,Map.of("A1",25,"B",30)));
    }
    @Test void canonicalFreshLegacyUpgradeRepeatAndFailureRollbackWorkInAnOwnedDatabase() throws Exception {
        String db="dts12_schema_test_screen14_owned";
        assertEquals(0,postgres.execInContainer("createdb","-U","test",db).getExitCode());
        for(String file:List.of("apply-schema.psql","apply-schema.psql","tests/legacy-fixture.sql",
                "apply-schema.psql","tests/verify-preserved.sql","tests/schema-smoke.sql","tests/invalid-legacy-fixture.sql")) {
            var run=postgres.execInContainer("psql","-X","-U","test","-d",db,"-v","ON_ERROR_STOP=1",
                "-v","expected_database="+db,"-f","/tmp/schema/"+file);
            assertEquals(0,run.getExitCode(),file+": "+run.getStderr());
        }
        var refused=postgres.execInContainer("psql","-X","-U","test","-d",db,"-v","ON_ERROR_STOP=1",
            "-v","expected_database="+db,"-f","/tmp/schema/apply-schema.psql");
        assertNotEquals(0,refused.getExitCode());
        var preserved=postgres.execInContainer("psql","-X","-U","test","-d",db,"-v","ON_ERROR_STOP=1","-f","/tmp/schema/tests/verify-rollback.sql");
        assertEquals(0,preserved.getExitCode(),preserved.getStderr());
        assertEquals(0,postgres.execInContainer("dropdb","-U","test",db).getExitCode());
    }
    LearningResultEvent policy(long revision,Instant when,Map<String,Integer> counts) {
        UUID id=UUID.randomUUID();
        return LearningResultEvent.builder().eventId(id).sourceId(id).eventType(StudyExpEventService.READY).eventVersion(1)
            .sourceType("PRACTICE_CONFIG").targetType("CONFIG").occurredAt(when)
            .metadata(Map.of("captureStartedAt",CAPTURE.toString(),"bankVersion","screen14-test",
                "policyRevision",revision,"licenseQuestionCounts",counts)).build();
    }
    LearningResultEvent question(UUID who,String license,String bank,int id,String context,Instant when) {
        UUID event=UUID.randomUUID();
        return LearningResultEvent.builder().eventId(event).sourceId(event).eventType(StudyExpEventService.QUESTION).eventVersion(1)
            .sourceType("PRACTICE_QUESTION").targetType("QUESTION").userId(who).licenseClass(license).occurredAt(when)
            .metadata(Map.of("captureStartedAt",CAPTURE.toString(),"bankVersion",bank,"questionId",id,"context",context)).build();
    }
    UUID exam(UUID who,UUID family,String activity,int correct,int total,boolean passed,Instant when) {
        UUID source=UUID.randomUUID();boolean official=activity.equals("OFFICIAL_EXAM");
        BigDecimal max=BigDecimal.valueOf(official?total:100);
        BigDecimal score=official?BigDecimal.valueOf(correct):BigDecimal.valueOf(100.0*correct/total);
        jdbc.update("""
            INSERT INTO learning_results(user_id,source_id,source_type,target_id,target_type,attempt_no,
              activity_type,display_title,license_class,correct_count,question_count,result_available,
              review_available,event_version,result,score,max_score,progress,duration_seconds,started_at,completed_at)
            VALUES(?,?,?,?,'EXAM',1,?,'Source title','A1',?,?,TRUE,TRUE,2,?,?,?,100,10,?,?)
            """,who,source,official?"EXAM_SESSION":"PRACTICE_SESSION",family,activity,correct,total,
                passed?"PASSED":"FAILED",score,max,Timestamp.from(when.minusSeconds(10)),Timestamp.from(when));
        return source;
    }
    long exp(UUID who,String period) {return leaderboard.read(who,"A1",period,"0","10").currentUser().rankingScore();}
    void adjustment(UUID source,boolean excluded) {
        String type=jdbc.queryForObject("SELECT source_type FROM learning_results WHERE source_id=?",String.class,source);
        jdbc.update("INSERT INTO result_projection_adjustments(event_id,source_type,source_id,excluded,reason_code,evidence_ref) VALUES(?,?,?,?,?,?)",
            UUID.randomUUID(),type,source,excluded,excluded?"VERIFIED_TEST_DATA":"SOURCE_RESTORED","owned integration fixture");
    }
    static void decimal(String expected,BigDecimal actual) {assertNotNull(actual);assertEquals(0,new BigDecimal(expected).compareTo(actual));}

    @Test void officialUsesCorrectCountsAndAuthoritativePassBonus() {
        exam(user,UUID.randomUUID(),"OFFICIAL_EXAM",10,25,false,DAY);
        exam(user,UUID.randomUUID(),"OFFICIAL_EXAM",10,25,true,DAY.plusSeconds(1));
        assertEquals(400,exp(user,"THIS_MONTH"));
    }
    @Test void officialCreditsAtMostTwoActualAttemptsPerParentWithoutRemovingAnalytics() {
        for(int i=0;i<3;i++) exam(user,target,"OFFICIAL_EXAM",10,25,true,DAY.plusSeconds(i));
        assertEquals(600,exp(user,"ALL_TIME"));assertEquals(3,analytics.read(user,"A1","ALL").summary().attempts());
    }
    @Test void randomUsesTwoPerCorrectAndThirtyForPass() {
        exam(user,target,"RANDOM_EXAM",10,25,false,DAY);
        exam(user,target,"RANDOM_EXAM",10,25,true,DAY.plusSeconds(1));
        assertEquals(70,exp(user,"THIS_MONTH"));
    }
    @Test void firstFiveRandomsAreChosenBySourceTimeRegardlessOfArrivalOrder() {
        for(int i=6;i>=1;i--) exam(user,target,"RANDOM_EXAM",i,25,false,DAY.plusSeconds(i));
        assertEquals(30,exp(user,"THIS_MONTH"));assertEquals(6,analytics.read(user,"A1","ALL").summary().attempts());
    }
    @Test void randomQuotaResetsAtVietnamMidnight() {
        Instant before=Instant.parse("2026-10-10T16:59:00Z");
        for(int i=0;i<5;i++) exam(user,target,"RANDOM_EXAM",1,25,false,before.plusSeconds(i));
        exam(user,target,"RANDOM_EXAM",2,25,false,Instant.parse("2026-10-10T17:00:00Z"));
        assertEquals(14,exp(user,"THIS_MONTH"));
    }
    @Test void presetCreditsOnlyItsBestExpAndNeverAddsInferiorAttempts() {
        exam(user,target,"PRESET_EXAM",10,25,false,DAY);assertEquals(20,exp(user,"THIS_MONTH"));
        exam(user,target,"PRESET_EXAM",8,25,false,DAY.plusSeconds(1));assertEquals(20,exp(user,"THIS_MONTH"));
        exam(user,target,"PRESET_EXAM",15,25,false,DAY.plusSeconds(2));assertEquals(30,exp(user,"THIS_MONTH"));
        assertEquals(3,analytics.read(user,"A1","ALL").summary().attempts());
    }
    @Test void higherRawScoreWithCriticalFailureCannotReducePreviouslyEarnedPresetExp() {
        exam(user,target,"PRESET_EXAM",21,25,true,DAY);
        exam(user,target,"PRESET_EXAM",22,25,false,DAY.plusSeconds(1));
        assertEquals(72,exp(user,"THIS_MONTH"));
    }
    @Test void presetBestIsIndependentForWeekMonthAndAllTime() {
        exam(user,target,"PRESET_EXAM",24,25,true,Instant.parse("2026-09-10T04:00:00Z"));
        exam(user,target,"PRESET_EXAM",10,25,false,Instant.parse("2026-10-14T04:00:00Z"));
        assertEquals(20,exp(user,"THIS_WEEK"));assertEquals(20,exp(user,"THIS_MONTH"));assertEquals(78,exp(user,"ALL_TIME"));
    }
    @Test void questionReplayAndDifferentContextsShareOneQuestionCreditPerPeriod() {
        var first=question(user,"A1","bank-one",1,"CHAPTER_LEARNING",DAY);
        events.record(first);events.record(first);
        events.record(question(user,"A1","bank-one",1,"CRITICAL_PRACTICE",DAY.plusSeconds(1)));
        assertEquals(1,exp(user,"THIS_MONTH"));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM study_question_facts",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM learning_results",Integer.class));
    }
    @Test void questionCreditsResetByPeriodButAllTimeDoesNotSumMonthlyDuplicates() {
        events.record(question(user,"A1","bank-one",1,"CHAPTER_LEARNING",Instant.parse("2026-09-10T00:00:00Z")));
        events.record(question(user,"A1","bank-one",1,"CHAPTER_REVIEW",Instant.parse("2026-10-14T00:00:00Z")));
        assertEquals(1,exp(user,"THIS_WEEK"));assertEquals(1,exp(user,"THIS_MONTH"));assertEquals(1,exp(user,"ALL_TIME"));
    }
    @Test void bankAndLicenseAreSeparateQuestionNamespaces() {
        events.record(question(user,"A1","bank-one",1,"CHAPTER_LEARNING",DAY));
        events.record(question(user,"A1","bank-two",1,"CHAPTER_LEARNING",DAY));
        events.record(question(user,"B","bank-one",1,"CHAPTER_LEARNING",DAY));
        assertEquals(2,exp(user,"THIS_MONTH"));
        assertEquals(1,leaderboard.read(user,"B","THIS_MONTH","0","10").currentUser().rankingScore());
    }
    @Test void concurrentDuplicateQuestionMessagesCommitOneLogicalFact() throws Exception {
        var event=question(user,"A1","bank-one",1,"CHAPTER_LEARNING",DAY);
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(8);
        try {
            var futures=new ArrayList<Future<?>>();
            for(int i=0;i<8;i++) futures.add(pool.submit(()->{start.await();events.record(event);return null;}));
            start.countDown();for(var f:futures)f.get(30,TimeUnit.SECONDS);
        } finally {pool.shutdownNow();}
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM study_question_facts",Integer.class));assertEquals(1,exp(user,"THIS_MONTH"));
    }
    @Test void anEventIdCannotReplaceAnotherUsersQuestionFact() {
        var event=question(user,"A1","bank-one",1,"CHAPTER_LEARNING",DAY);events.record(event);
        event.setUserId(UUID.randomUUID());assertThrows(ValidationException.class,()->events.record(event));
        assertEquals(1,exp(user,"THIS_MONTH"));
    }
    @Test void malformedQuestionProofAndPredatingCaptureAreRejected() {
        var event=question(user,"A1","bank-one",1,"CHAPTER_LEARNING",CAPTURE.minusSeconds(1));
        assertThrows(ValidationException.class,()->events.record(event));
        var wrong=question(user,"A1","bank-one",1,"UNKNOWN",DAY);
        assertThrows(ValidationException.class,()->events.record(wrong));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM study_question_facts",Integer.class));
    }
    @Test void oldOrDuplicatePolicyCannotOverwriteANewerSourceRevision() {
        var newer=policy(3,DAY,Map.of("A1",30));events.record(newer);events.record(newer);
        events.record(policy(2,DAY.plusSeconds(10),Map.of("A1",35)));
        assertEquals(30,analytics.read(user,"A1","ALL").summary().maxScore());
        newer.setMetadata(Map.of("captureStartedAt",CAPTURE.toString(),"bankVersion","screen14-test",
            "policyRevision",3,"licenseQuestionCounts",Map.of("A1",99)));
        assertThrows(ValidationException.class,()->events.record(newer));
    }
    @Test void rankAndPageAreGlobalAndCurrentUserWorksOutsideTopTen() {
        UUID current=null;
        for(int i=1;i<=24;i++) {UUID learner=UUID.randomUUID();exam(learner,UUID.randomUUID(),"RANDOM_EXAM",i,25,false,DAY);if(i==7)current=learner;}
        var page=leaderboard.read(current,"A1","THIS_MONTH","1","10");
        assertEquals(24,page.totalParticipants());assertEquals(3,page.totalPages());assertEquals(18,page.currentUser().rank());
        assertEquals(11,page.items().get(0).rank());assertEquals(20,page.items().get(9).rank());
        var empty=leaderboard.read(user,"A1","THIS_MONTH","9","10");
        assertTrue(empty.items().isEmpty());assertNull(empty.currentUser().rank());assertEquals(0,empty.currentUser().rankingScore());
    }
    @Test void equalExpUsesSourceAchievementTimeThenStableUserId() {
        UUID a=UUID.fromString("14000000-0000-0000-0000-000000000001");
        UUID b=UUID.fromString("14000000-0000-0000-0000-000000000002");
        UUID c=UUID.fromString("14000000-0000-0000-0000-000000000003");
        exam(c,target,"RANDOM_EXAM",5,25,false,DAY.plusSeconds(1));exam(b,target,"RANDOM_EXAM",5,25,false,DAY.plusSeconds(1));
        exam(a,target,"RANDOM_EXAM",5,25,false,DAY);
        assertEquals(List.of(a,b,c),leaderboard.read(user,"A1","THIS_MONTH","0","10").items().stream().map(x->x.userId()).toList());
    }
    @Test void analyticsNormalizesCountsInsteadOfMixingRawHundredAndTwentyFiveScales() {
        exam(user,target,"RANDOM_EXAM",20,25,true,DAY);exam(user,UUID.randomUUID(),"OFFICIAL_EXAM",15,25,false,DAY.plusSeconds(1));
        var result=analytics.read(user,"A1","ALL");
        decimal("17.5",result.summary().averageScore());assertEquals(25,result.summary().maxScore());
        decimal("50",result.summary().passRate());decimal("70",result.summary().accuracyRate());
        assertEquals(1,result.scoreTrend().size());assertEquals(2,result.scoreTrend().get(0).attempts());decimal("17.5",result.scoreTrend().get(0).score());
        assertEquals("DAILY_AVERAGE",result.trendMode());decimal("15",result.recentResults().get(0).score());
    }
    @Test void accuracyIsWeightedByQuestionCountAndNotAnAverageOfPercentages() {
        exam(user,target,"RANDOM_EXAM",10,10,false,DAY);exam(user,UUID.randomUUID(),"OFFICIAL_EXAM",0,30,false,DAY);
        var s=analytics.read(user,"A1","ALL").summary();decimal("25",s.accuracyRate());decimal("12.5",s.averageScore());
    }
    @Test void passRateHonorsSourceCriticalFailureEvenAtHighAccuracy() {
        exam(user,target,"RANDOM_EXAM",24,25,false,DAY);
        var s=analytics.read(user,"A1","ALL").summary();decimal("0",s.passRate());decimal("96",s.accuracyRate());
    }
    @Test void analyticsOwnLicenseAndPeriodScopeApplyToAllThreeSections() {
        exam(user,target,"RANDOM_EXAM",10,25,false,NOW.minusSeconds(20));
        exam(user,target,"RANDOM_EXAM",20,25,true,NOW.minus(Duration.ofDays(20)));
        exam(UUID.randomUUID(),target,"RANDOM_EXAM",25,25,true,DAY);
        UUID b=exam(user,target,"RANDOM_EXAM",25,25,true,DAY);jdbc.update("UPDATE learning_results SET license_class='B' WHERE source_id=?",b);
        assertEquals(1,analytics.read(user,"A1","7D").summary().attempts());
        assertEquals(2,analytics.read(user,"A1","30D").summary().attempts());
        assertEquals(1,analytics.read(user,"B","ALL").summary().attempts());
    }
    @Test void emptyAnalyticsHasZeroKpisAndNoFabricatedChartPoints() {
        var r=analytics.read(user,"A1","30D");assertEquals(0,r.summary().attempts());
        decimal("0",r.summary().averageScore());decimal("0",r.summary().passRate());decimal("0",r.summary().accuracyRate());
        assertEquals(25,r.summary().maxScore());assertTrue(r.scoreTrend().isEmpty());assertTrue(r.recentResults().isEmpty());
    }
    @Test void absentSourcePolicyIsExplicitRatherThanHardcodingALicenseScale() {
        jdbc.execute("TRUNCATE practice_exp_policy_events");
        assertNull(analytics.read(user,"A1","ALL").summary().averageScore());
        exam(user,target,"RANDOM_EXAM",10,25,false,DAY);
        var r=analytics.read(user,"A1","ALL");assertEquals("SOURCE_POLICY_PENDING",r.dataStatus());
        assertNull(r.summary().maxScore());assertNull(r.summary().averageScore());assertEquals(1,r.summary().attempts());
    }
    @Test void unreleasedLegacyAndNonExamFactsDoNotLeakIntoEitherNewScreen() {
        UUID unreleased=exam(user,target,"OFFICIAL_EXAM",25,25,true,DAY);
        jdbc.update("UPDATE learning_results SET result_available=FALSE WHERE source_id=?",unreleased);
        UUID legacy=exam(user,target,"RANDOM_EXAM",25,25,true,DAY);
        jdbc.update("UPDATE learning_results SET event_version=1,activity_type=NULL WHERE source_id=?",legacy);
        UUID other=exam(user,target,"RANDOM_EXAM",25,25,true,DAY);
        jdbc.update("UPDATE learning_results SET target_type='LESSON' WHERE source_id=?",other);
        assertEquals(0,exp(user,"THIS_MONTH"));assertEquals(0,analytics.read(user,"A1","ALL").summary().attempts());
        jdbc.update("UPDATE learning_results SET result_available=TRUE WHERE source_id=?",unreleased);
        assertEquals(450,exp(user,"THIS_MONTH"));assertEquals(1,analytics.read(user,"A1","ALL").summary().attempts());
    }
    @Test void lateReleasedOfficialBelongsToItsCompletedPeriodAndIsNotCreditedTwice() {
        exam(user,target,"OFFICIAL_EXAM",10,25,false,Instant.parse("2026-10-11T16:59:59Z"));
        assertEquals(0,exp(user,"THIS_WEEK"));assertEquals(100,exp(user,"THIS_MONTH"));
    }
    @Test void verifiedExclusionAndRestorationRecomputeExpAndAnalyticsWithoutChangingHistory() {
        UUID source=exam(user,target,"RANDOM_EXAM",10,25,true,DAY);
        String before=jdbc.queryForObject("SELECT to_jsonb(r)::text FROM learning_results r WHERE source_id=?",String.class,source);
        adjustment(source,true);assertEquals(0,exp(user,"THIS_MONTH"));assertEquals(0,analytics.read(user,"A1","ALL").summary().attempts());
        adjustment(source,false);assertEquals(50,exp(user,"THIS_MONTH"));
        assertEquals(before,jdbc.queryForObject("SELECT to_jsonb(r)::text FROM learning_results r WHERE source_id=?",String.class,source));
    }
    @Test void sourceEvidenceAndAdjustmentJournalRejectUpdates() {
        var event=question(user,"A1","bank-one",1,"CHAPTER_LEARNING",DAY);events.record(event);
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE study_question_facts SET question_id=2 WHERE event_id=?",event.getEventId()));
        UUID source=exam(user,target,"RANDOM_EXAM",10,25,true,DAY);adjustment(source,true);
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE result_projection_adjustments SET excluded=FALSE"));
        assertEquals(1,exp(user,"THIS_MONTH"));
    }
    @Test void profilesUseOnlyIdentityNamesIgnoreStaleEventsAndRemoveDeletedPublicEntries() {
        exam(user,target,"RANDOM_EXAM",10,25,true,DAY);
        profiles.record("{\"eventType\":\"USER_UPDATED\",\"userId\":\""+user+"\",\"timestamp\":200,\"fullName\":\"Tên đúng\",\"email\":\"private@example.invalid\"}",0,2);
        profiles.record("{\"eventType\":\"USER_CREATED\",\"userId\":\""+user+"\",\"timestamp\":100,\"fullName\":\"Tên cũ\"}",1,9);
        var r=leaderboard.read(user,"A1","THIS_MONTH","0","10");assertEquals("Tên đúng",r.currentUser().displayName());
        assertFalse(mapper.valueToTree(r).toString().contains("private@example.invalid"));assertNull(r.currentUser().avatarUrl());
        profiles.record("{\"eventType\":\"USER_DELETED\",\"userId\":\""+user+"\",\"timestamp\":300}",2,3);
        assertEquals(0,leaderboard.read(user,"A1","THIS_MONTH","0","10").totalParticipants());
    }
    @Test void recentResultsKeepFiveStableSourceRoutesAndConsistentNormalizedUnits() {
        for(int i=0;i<7;i++) exam(user,UUID.randomUUID(),i%2==0?"OFFICIAL_EXAM":"RANDOM_EXAM",i+1,25,false,DAY.plusSeconds(i));
        var recent=analytics.read(user,"A1","ALL").recentResults();assertEquals(5,recent.size());decimal("7",recent.get(0).score());
        for(var r:recent) {
            String source=r.sourceResult().sourceType();
            assertEquals((source.equals("EXAM_SESSION")?"/examination/result/":"/practice/result/")+r.sourceResult().sourceId(),r.sourceResult().resultPath());
            assertEquals(25,r.maxScore());assertNotNull(r.sourceMaxScore());
        }
    }
}
