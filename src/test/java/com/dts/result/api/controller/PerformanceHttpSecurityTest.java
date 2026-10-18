package com.dts.result.api.controller;

import com.dts.result.api.response.LeaderboardResponse;
import com.dts.result.application.service.LeaderboardService;
import com.dts.result.application.service.ResultAnalyticsService;
import com.dts.result.config.ResultBusinessClockConfig;
import com.dts.result.infrastructure.repository.PerformanceProjectionRepository;
import com.dts.result.infrastructure.security.JwtAuthenticationFilter;
import com.dts.result.infrastructure.security.SecurityConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers={LeaderboardController.class,ResultAnalyticsController.class},properties={
    "jwt.secret=c2NyZWVuMTQtMTYtaHR0cC10ZXN0LWtleS1vbmx5LTMya2V5cy0wMDA=",
    "jwt.issuer=dts-identity-service"})
@Import({SecurityConfig.class,JwtAuthenticationFilter.class,LeaderboardService.class,
    ResultAnalyticsService.class,ResultBusinessClockConfig.class})
class PerformanceHttpSecurityTest {
    @Autowired MockMvc http;
    @MockBean PerformanceProjectionRepository repository;
    final UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
    String token(String issuer) {
        return Jwts.builder().subject(owner.toString()).issuer(issuer).claim("userId",other.toString())
            .expiration(Date.from(Instant.now().plusSeconds(600)))
            .signWith(Keys.hmacShaKeyFor("screen14-16-http-test-key-only-32keys-000".getBytes(StandardCharsets.UTF_8))).compact();
    }
    @Test void anonymousAndInvalidIssuerCannotReadEitherEndpoint() throws Exception {
        for(String path:List.of("/v1/leaderboards","/v1/results/me/analytics")) {
            http.perform(get(path).param("licenseClass","A1")).andExpect(status().isUnauthorized());
            http.perform(get(path).param("licenseClass","A1").header("Authorization","Bearer "+token("wrong-issuer"))).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(repository);
    }
    @Test void currentUserComesFromSignedSubjectDespiteOtherUserHeaderClaimOrQuery() throws Exception {
        when(repository.leaderboard(eq(owner),eq("A1"),any(),eq(0),eq(10))).thenReturn(
            new LeaderboardResponse("A1","THIS_MONTH","Asia/Ho_Chi_Minh",Instant.now(),null,null,
                new LeaderboardResponse.Entry(owner,null,null,"PENDING",null,0,null),List.of(),0,0,10,0,"EXP_2026_01",null));
        http.perform(get("/v1/leaderboards").param("licenseClass","A1").param("userId",other.toString())
            .header("X-User-ID",other.toString()).header("Authorization","Bearer "+token("dts-identity-service")))
            .andExpect(status().isOk()).andExpect(jsonPath("currentUser.userId").value(owner.toString()));
        verify(repository).leaderboard(eq(owner),eq("A1"),any(),eq(0),eq(10));
        http.perform(get("/v1/results/me/analytics").param("licenseClass","A1").param("userId",other.toString())
            .header("Authorization","Bearer "+token("dts-identity-service"))).andExpect(status().isOk());
        verify(repository).analytics(eq(owner),eq("A1"),any());
    }
    @Test void unsupportedFiltersAndOversizedPagesReturn400BeforeDatabaseQuery() throws Exception {
        for(var parameters:List.of(Map.of("licenseClass","INVALID"),Map.of("licenseClass","A1","period","7D"),
                Map.of("licenseClass","A1","size","101"),Map.of("licenseClass","A1","page","-1"),
                Map.of("licenseClass","A1","page","x"),Map.of("licenseClass","A1","size","0"))) {
            var request=get("/v1/leaderboards").header("Authorization","Bearer "+token("dts-identity-service"));
            parameters.forEach(request::param);http.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("code").value("RES-400-001"));
        }
        http.perform(get("/v1/results/me/analytics").param("licenseClass","A1").param("period","THIS_MONTH")
            .header("Authorization","Bearer "+token("dts-identity-service"))).andExpect(status().isBadRequest());
        http.perform(get("/v1/leaderboards").header("Authorization","Bearer "+token("dts-identity-service"))).andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
