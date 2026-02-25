package com.aicrm.module.crm;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportApiTest {

    @Autowired private MockMvc mockMvc;
    private static final String T = "1", U = "100";

    @Test @Order(1)
    void personalReport_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/reports/sales/personal")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .param("period", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dealAmount").isNumber())
                .andExpect(jsonPath("$.data.completionRate").isNumber());
    }

    @Test @Order(2)
    void teamReport_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/reports/sales/team")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .param("period", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
