package com.aicrm.module.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnterpriseApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static final String T = "1", U = "100";

    @Test @Order(1)
    void searchEnterprise_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/v1/enterprise/search")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .param("keyword", "示例科技"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(2)
    void queryContacts_shouldReturnContacts() throws Exception {
        mockMvc.perform(get("/v1/enterprise/search")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .param("keyword", "测试公司"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/enterprise/DEMO_CREDIT_001/contacts")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(3)
    void getContactQuota_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/enterprise/contact-quota")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
