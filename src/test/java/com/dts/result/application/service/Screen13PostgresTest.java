package com.dts.result.application.service;

import com.dts.result.application.event.LearningResultEvent;
import com.dts.result.application.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties={"spring.jpa.hibernate.ddl-auto=none","spring.flyway.enabled=false"})
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import({LearningResultService.class,LearningResultMapper.class,ResultWriteLock.class,ExamHistoryProjection.class,
    LearningSummaryService.class,LearningSummaryMapper.class,HistoryService.class})
@Transactional(propagation=Propagation.NOT_SUPPORTED)
@Testcontainers(disabledWithoutDocker=true)
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named="DTS_SCREEN13_POSTGRES_TEST",matches="true")
class Screen13PostgresTest {
    @Container static final GenericContainer<?> postgres=new GenericContainer<>("postgres:16-alpine")
        .withEnv("POSTGRES_DB","dts_result_db").withEnv("POSTGRES_USER","test").withEnv("POSTGRES_PASSWORD","test")
        .withExposedPorts(5432).withCopyFileToContainer(MountableFile.forHostPath(Path.of("docs/db").toAbsolutePath()),"/tmp/schema")
        .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*database system is ready to accept connections.*\n",2));
    @DynamicPropertySource static void database(DynamicPropertyRegistry p){
        p.add("spring.datasource.url",()->"jdbc:postgresql://"+postgres.getHost()+":"+postgres.getMappedPort(5432)+"/dts_result_db");
        p.add("spring.datasource.username",()->"test");p.add("spring.datasource.password",()->"test");
    }
    @BeforeAll static void migrate() throws Exception {
        for(int i=0;i<2;i++){
            var result=postgres.execInContainer("psql","-X","-U","test","-d","dts_result_db","-v","ON_ERROR_STOP=1","-f","/tmp/schema/apply-schema.psql");
            assertEquals(0,result.getExitCode(),result.getStderr());
        }
    }
    @Autowired LearningResultService consumer;
    @Autowired HistoryService history;
    @Autowired JdbcTemplate jdbc;
    UUID user,target;
    @BeforeEach void clean(){jdbc.update("DELETE FROM learning_summaries");jdbc.update("DELETE FROM learning_results");user=UUID.randomUUID();target=UUID.randomUUID();}
    LearningResultEvent event(int attempt,int score,String activity,Instant when){
        return LearningResultEvent.builder().eventId(UUID.randomUUID()).eventType("LEARNING_RESULT_CREATED").eventVersion(2)
            .sourceType("PRACTICE_SESSION").sourceId(UUID.randomUUID()).userId(user).targetType("EXAM").targetId(target)
            .attemptNo(attempt).result("PASSED").score(BigDecimal.valueOf(score)).maxScore(BigDecimal.valueOf(100))
            .progress(100.0).activityType(activity).displayTitle("Source title").licenseClass("A1")
            .correctCount(23).questionCount(25).resultAvailable(true).reviewAvailable(true).durationSeconds(10)
            .startedAt(when.minusSeconds(10)).completedAt(when).build();
    }
    @Test void tenConcurrentCopiesCommitOneResultAndOneProjectionIncrement() throws Exception {
        var e=event(7,92,"RANDOM_EXAM",Instant.parse("2026-10-01T00:00:00Z"));
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(10);
        try{
            var futures=new ArrayList<Future<?>>();
            for(int i=0;i<10;i++)futures.add(pool.submit(()->{start.await();consumer.processLearningResult(e);return null;}));
            start.countDown();for(var f:futures)f.get(30,TimeUnit.SECONDS);
        }finally{pool.shutdownNow();}
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM learning_results",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT attempt_count FROM learning_summaries",Integer.class));
        assertEquals(7,jdbc.queryForObject("SELECT attempt_no FROM learning_results",Integer.class));
    }
    @Test void outOfOrderEventsKeepSourceNumbersAndLatestScoreAndPermitAnotherVersionAttemptOne(){
        var later=event(1,96,"PRESET_EXAM",Instant.parse("2026-10-02T00:00:00Z"));
        var earlier=event(1,88,"PRESET_EXAM",Instant.parse("2026-10-01T00:00:00Z"));
        consumer.processLearningResult(later);consumer.processLearningResult(earlier);
        assertEquals(2,jdbc.queryForObject("SELECT attempt_count FROM learning_summaries",Integer.class));
        assertEquals(0,new BigDecimal("96").compareTo(jdbc.queryForObject("SELECT latest_score FROM learning_summaries",BigDecimal.class)));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM learning_results WHERE attempt_no=1",Integer.class));
    }
    @Test void filtersApplyBeforePaginationAndTimestampTiesDoNotRepeatRows(){
        var when=Instant.parse("2026-10-01T00:00:00Z");
        for(int i=1;i<=23;i++)consumer.processLearningResult(event(i,92,i<=20?"RANDOM_EXAM":"PRESET_EXAM",when));
        var other=event(1,92,"RANDOM_EXAM",when);other.setUserId(UUID.randomUUID());consumer.processLearningResult(other);
        var ids=new HashSet<UUID>();
        for(int page=0;page<3;page++){
            var result=history.getHistoryList(user,"EXAM",null,"PASSED","RANDOM_EXAM","A1",when,when,PageRequest.of(page,7));
            assertEquals(20,result.getTotalElements());for(var row:result)assertTrue(ids.add(row.getId()));
        }
        assertEquals(20,ids.size());
    }
}
