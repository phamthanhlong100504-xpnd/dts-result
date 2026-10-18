package com.dts.result.application.service;

import com.dts.result.api.exception.*;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class Screen13HistoryTest {
    LearningResultRepository repository;
    HistoryService service;
    UUID user=UUID.randomUUID();
    @BeforeEach void setup(){repository=mock(LearningResultRepository.class);service=new HistoryService(repository);}
    LearningResultEntity exam(){return LearningResultEntity.builder().id(UUID.randomUUID()).userId(user)
        .sourceId(UUID.randomUUID()).sourceType("PRACTICE_SESSION").targetId(UUID.randomUUID()).targetType("EXAM")
        .result("PASSED").resultAvailable(true).activityType("PRESET_EXAM").displayTitle("Bộ đề nguồn")
        .licenseClass("B").questionCount(30).correctCount(28).score(new java.math.BigDecimal("93"))
        .maxScore(new java.math.BigDecimal("100")).startedAt(Instant.parse("2026-10-01T00:00:00Z"))
        .completedAt(Instant.parse("2026-10-01T00:10:00Z")).build();}
    @Test void scoreAndQuestionCountsRemainIndependentAndRouteUsesSourceId(){
        var entity=exam();when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        var detail=service.getHistoryDetail(user,entity.getId());
        assertEquals(28,detail.getCorrectCount());assertEquals(new java.math.BigDecimal("93"),detail.getScore());
        assertEquals("/practice/result/"+entity.getSourceId(),detail.getSourceResult().resultPath());
    }
    @Test void unpublishedResultAndAnotherUsersResultBothReturnNotFound(){
        var entity=exam();when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        assertThrows(ResourceNotFoundException.class,()->service.getHistoryDetail(UUID.randomUUID(),entity.getId()));
        entity.setResultAvailable(false);
        assertThrows(ResourceNotFoundException.class,()->service.getHistoryDetail(user,entity.getId()));
    }
    @Test void privateAnswerKeysAndNestedPayloadsNeverAppearInExamDetail(){
        var entity=exam();entity.setResultSnapshot(Map.of("correctAnswer","A","totalQuestions",30,"examVersionId",Map.of("answers","secret")));
        entity.setMetadata(Map.of("officialPaper",Map.of("key","secret"),"submissionReason","USER_SUBMIT"));
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        var detail=service.getHistoryDetail(user,entity.getId());
        assertEquals(Map.of("totalQuestions",30),detail.getResultSnapshot());
        assertEquals(Map.of("submissionReason","USER_SUBMIT"),detail.getMetadata());
    }
    @Test void invalidRangeFilterSortAndPageSizeAreRejectedBeforeQuery(){
        var now=Instant.now();var page=PageRequest.of(0,7);
        assertThrows(ValidationException.class,()->service.getHistoryList(user,"EXAM",null,null,null,null,now,now.minusSeconds(1),page));
        assertThrows(ValidationException.class,()->service.getHistoryList(user,"EXAM",null,null,"WRONG",null,null,null,page));
        assertThrows(ValidationException.class,()->service.getHistoryList(user,"EXAM",null,null,null,"INVALID",null,null,page));
        assertThrows(ValidationException.class,()->service.getHistoryList(user,"EXAM",null,"SUBMITTED",null,null,null,null,page));
        assertThrows(ValidationException.class,()->service.getHistoryList(user,"EXAM",null,null,null,null,null,null,PageRequest.of(0,101)));
        assertThrows(ValidationException.class,()->service.getHistoryList(user,"EXAM",null,null,null,null,null,null,PageRequest.of(0,7,Sort.by("metadata"))));
        verifyNoInteractions(repository);
    }
    @Test void defaultOrderIncludesUniqueTieBreaker(){
        when(repository.findAll(any(Specification.class),any(Pageable.class))).thenReturn(Page.empty());
        service.getHistoryList(user,"EXAM",null,null,"RANDOM_EXAM","A1",null,null,PageRequest.of(0,7));
        var paging=ArgumentCaptor.forClass(Pageable.class);verify(repository).findAll(any(Specification.class),paging.capture());
        assertEquals(Sort.by(Sort.Order.desc("completedAt"),Sort.Order.desc("id")),paging.getValue().getSort());
    }
}
