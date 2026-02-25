package com.aicrm.module.crm;

import com.aicrm.module.crm.followup.dto.FollowUpCreateDTO;
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
class FollowUpApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT = "1";
    private static final String USER = "100";

    @Test
    @Order(1)
    void createFollowUp_shouldSucceed() throws Exception {
        FollowUpCreateDTO dto = new FollowUpCreateDTO();
        dto.setBizType(2);
        dto.setBizId(1L);
        dto.setCustomerId(1L);
        dto.setFollowType(2);
        dto.setContent("电话沟通了ERP需求，客户对库存管理模块感兴趣");
        dto.setNextFollowTime(LocalDateTime.now().plusDays(3));
        dto.setNextFollowNote("安排产品演示");

        mockMvc.perform(post("/v1/follow-ups")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @Order(2)
    void listFollowUps_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/v1/follow-ups")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .param("bizType", "2")
                        .param("bizId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @Order(3)
    void createAndGet_shouldReturnCorrectContent() throws Exception {
        FollowUpCreateDTO dto = new FollowUpCreateDTO();
        dto.setBizType(1);
        dto.setBizId(99L);
        dto.setFollowType(1);
        dto.setContent("现场拜访，演示了产品功能");

        MvcResult result = mockMvc.perform(post("/v1/follow-ups")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").asLong();

        mockMvc.perform(get("/v1/follow-ups/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.content").value("现场拜访，演示了产品功能"))
                .andExpect(jsonPath("$.data.followType").value(1))
                .andExpect(jsonPath("$.data.followUserId").value(100));
    }

    @Test
    @Order(4)
    void deleteFollowUp_shouldLogicalDelete() throws Exception {
        FollowUpCreateDTO dto = new FollowUpCreateDTO();
        dto.setBizType(2);
        dto.setBizId(1L);
        dto.setFollowType(3);
        dto.setContent("微信沟通报价");

        MvcResult result = mockMvc.perform(post("/v1/follow-ups")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").asLong();

        mockMvc.perform(delete("/v1/follow-ups/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/follow-ups/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.code").value(404));
    }
}
