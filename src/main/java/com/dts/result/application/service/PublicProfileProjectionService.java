package com.dts.result.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PublicProfileProjectionService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public PublicProfileProjectionService(JdbcTemplate jdbc,ObjectMapper mapper) {this.jdbc=jdbc;this.mapper=mapper;}
    @Transactional
    public void record(String json,int partition,long offset) {
        JsonNode node;
        try {node=mapper.readTree(json==null?"null":json);}
        catch(JsonProcessingException ex) {reject(partition,offset);return;}
        if(node==null || !node.isObject()) {reject(partition,offset);return;}
        String type=node.path("eventType").asText("");
        int order=switch(type) {case "USER_CREATED"->1;case "USER_UPDATED"->2;case "USER_DELETED"->3;default->0;};
        if(order==0) return;
        UUID user;
        try {user=UUID.fromString(node.path("userId").asText(""));}
        catch(IllegalArgumentException ex) {reject(partition,offset);return;}
        JsonNode stamp=node.get("timestamp");
        if(stamp==null || !stamp.isIntegralNumber() || !stamp.canConvertToLong() || stamp.longValue()<1) {reject(partition,offset);return;}
        String name=node.path("fullName").isTextual()?node.path("fullName").textValue().strip():null;
        boolean deleted=order==3;
        if(deleted || name!=null && name.isBlank()) name=null;
        if(name!=null && name.length()>255) {reject(partition,offset);return;}
        jdbc.update("""
            INSERT INTO leaderboard_public_profiles(user_id,display_name,deleted,source_timestamp,
              source_event_order,source_partition,source_offset) VALUES(?,?,?,?,?,?,?)
            ON CONFLICT(user_id) DO UPDATE SET display_name=EXCLUDED.display_name,deleted=EXCLUDED.deleted,
              source_timestamp=EXCLUDED.source_timestamp,source_event_order=EXCLUDED.source_event_order,
              source_partition=EXCLUDED.source_partition,source_offset=EXCLUDED.source_offset,updated_at=NOW()
            WHERE (EXCLUDED.source_timestamp,EXCLUDED.source_event_order,EXCLUDED.source_partition,EXCLUDED.source_offset)
              >(leaderboard_public_profiles.source_timestamp,leaderboard_public_profiles.source_event_order,
                leaderboard_public_profiles.source_partition,leaderboard_public_profiles.source_offset)
            """,user,name,deleted,stamp.longValue(),order,partition,offset);
    }
    private void reject(int partition,long offset) {log.warn("Public profile evidence rejected: partition={}, offset={}",partition,offset);}
}
