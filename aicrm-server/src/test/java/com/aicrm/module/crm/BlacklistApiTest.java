package com.aicrm.module.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BlacklistApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static final String T = "1", U = "100";

    @Test @Order(1)
    void addToBlacklist_shouldSucceed() throws Exception {
        String body = """
            {"customerId":null,"companyName":"骗子公司","blacklistType":1,"reason":"多次诈骗行为"}
            """;
        mockMvc.perform(post("/v1/customers/blacklist")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test @Order(2)
    void listBlacklist_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/customers/blacklist")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test @Order(3)
    void checkBlacklist_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/customers/blacklist/check")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .param("companyName", "骗子公司"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isBoolean());
    }

    @Test @Order(4)
    void releaseBlacklist_shouldSucceed() throws Exception {
        String createBody = """
            {"companyName":"临时拉黑公司","blacklistType":5,"reason":"测试用"}
            """;
        MvcResult r = mockMvc.perform(post("/v1/customers/blacklist")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isOk()).andReturn();
        Long id = objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();

        mockMvc.perform(post("/v1/customers/blacklist/" + id + "/release")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"releaseReason\":\"误操作解除\"}"))
                .andExpect(status().isOk());
    }
}
