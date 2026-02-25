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
class ErpFinanceApiTest {

    @Autowired private MockMvc mockMvc;
    private static final String T = "1", U = "100";

    @Test @Order(1)
    void getReceivables_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/customers/1/erp-receivables")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalReceivable").isNumber())
                .andExpect(jsonPath("$.data.agingAnalysis").isArray());
    }

    @Test @Order(2)
    void getTransactions_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/customers/1/erp-transactions")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.records").isArray());
    }
}
