package com.dts.result.simulationperformance;
import com.dts.result.infrastructure.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(controllers=SimulationPerformanceController.class,properties={"jwt.secret=c2NyZWVuMTQtMTYtaHR0cC10ZXN0LWtleS1vbmx5LTMya2V5cy0wMDA=","jwt.issuer=dts-identity-service"})
@Import({SecurityConfig.class,JwtAuthenticationFilter.class})
class SimulationHttpSecurityTest {
    @Autowired MockMvc http;@MockBean SimulationPerformanceService service;
    final UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
    String token(String issuer){return Jwts.builder().subject(owner.toString()).issuer(issuer).claim("userId",other.toString()).expiration(Date.from(Instant.now().plusSeconds(600))).signWith(Keys.hmacShaKeyFor("screen14-16-http-test-key-only-32keys-000".getBytes(StandardCharsets.UTF_8))).compact();}
    @Test void anonymousAndInvalidIssuerCannotReadNewRoutes()throws Exception{
        for(String path:List.of("/v1/results/me/history/simulations","/v1/results/me/analytics/combined","/v1/leaderboards/combined")){
            http.perform(get(path).param("licenseClass","B")).andExpect(status().isUnauthorized());
            http.perform(get(path).param("licenseClass","B").header("Authorization","Bearer "+token("wrong"))).andExpect(status().isUnauthorized());
        }verifyNoInteractions(service);
    }
    @Test void signedSubjectControlsOwnerDespiteForeignClaimHeaderAndQuery()throws Exception{
        when(service.analytics(eq(owner),eq("B"),eq("7D"),isNull(),isNull())).thenReturn(new ObjectMapper().createObjectNode().put("licenseClass","B"));
        http.perform(get("/v1/results/me/analytics/combined").param("licenseClass","B").param("userId",other.toString()).header("X-User-ID",other.toString()).header("Authorization","Bearer "+token("dts-identity-service")))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
        verify(service).analytics(owner,"B","7D",null,null);
    }
    @Test void invalidWatermarkOrMissingLicenseFailsBeforeService()throws Exception{
        for(var request:List.of(get("/v1/results/me/analytics/combined"),get("/v1/leaderboards/combined").param("licenseClass","B").param("watermark","bad")))
            http.perform(request.header("Authorization","Bearer "+token("dts-identity-service"))).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
