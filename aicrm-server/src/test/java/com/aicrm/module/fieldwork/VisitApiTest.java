package com.aicrm.module.fieldwork;

import com.aicrm.module.fieldwork.visit.dto.VisitCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VisitApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static final String T = "1", U = "100";

    private Long createVisit(int type, String purpose) throws Exception {
        VisitCreateDTO dto = new VisitCreateDTO();
        dto.setCustomerId(1L);
        dto.setVisitType(type);
        dto.setVisitPurpose(purpose);
        dto.setVisitTime(LocalDateTime.now().plusHours(2));
        MvcResult r = mockMvc.perform(post("/v1/visits")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    @Test @Order(1)
    void createVisit_shouldSucceed() throws Exception {
        Long id = createVisit(1, "产品演示");
        org.assertj.core.api.Assertions.assertThat(id).isNotNull();
    }

    @Test @Order(2)
    void getVisitById_shouldReturnData() throws Exception {
        Long id = createVisit(2, "电话沟通");
        mockMvc.perform(get("/v1/visits/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.visitPurpose").value("电话沟通"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test @Order(3)
    void completeVisit_shouldUpdateStatus() throws Exception {
        Long id = createVisit(1, "需求调研");
        mockMvc.perform(put("/v1/visits/" + id + "/complete")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"visitResult\":\"客户对产品很满意\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/visits/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.status").value(3))
                .andExpect(jsonPath("$.data.visitResult").value("客户对产品很满意"));
    }

    @Test @Order(4)
    void cancelVisit_shouldUpdateStatus() throws Exception {
        Long id = createVisit(3, "微信沟通");
        mockMvc.perform(delete("/v1/visits/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk());
    }

    @Test @Order(5)
    void listVisits_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/v1/visits")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}
