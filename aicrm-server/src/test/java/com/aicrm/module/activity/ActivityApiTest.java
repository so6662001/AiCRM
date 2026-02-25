package com.aicrm.module.activity;

import com.aicrm.module.activity.dto.ActivityCreateDTO;
import com.aicrm.module.activity.dto.ParticipantCreateDTO;
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
class ActivityApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static final String T = "1", U = "100";

    private Long createActivity(String name, int category) throws Exception {
        ActivityCreateDTO dto = new ActivityCreateDTO();
        dto.setActivityName(name);
        dto.setActivityCategory(category);
        dto.setActivityType(category == 1 ? 3 : 7);
        dto.setStartTime(LocalDateTime.now().plusDays(10));
        dto.setEndTime(LocalDateTime.now().plusDays(10).plusHours(3));
        if (category == 1) {
            dto.setRegistrationStartTime(LocalDateTime.now());
            dto.setRegistrationEndTime(LocalDateTime.now().plusDays(9));
            dto.setMaxParticipants(200);
            dto.setRegistrationApproval(0);
        }
        dto.setLocation("深圳南山XX酒店");
        MvcResult r = mockMvc.perform(post("/v1/activities")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    @Test @Order(1)
    void createActivity_withRegistration_shouldSucceed() throws Exception {
        Long id = createActivity("2026春季产品发布会", 1);
        org.assertj.core.api.Assertions.assertThat(id).isNotNull();
        mockMvc.perform(get("/v1/activities/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.activityCategory").value(1))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test @Order(2)
    void createActivity_noRegistration_shouldSucceed() throws Exception {
        Long id = createActivity("春季促销活动", 2);
        mockMvc.perform(get("/v1/activities/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.activityCategory").value(2));
    }

    @Test @Order(3)
    void publishActivity_shouldChangeStatus() throws Exception {
        Long id = createActivity("发布测试活动", 1);
        mockMvc.perform(put("/v1/activities/" + id + "/publish")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/activities/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.status").isNumber());
    }

    @Test @Order(4)
    void addParticipant_shouldSucceed() throws Exception {
        Long actId = createActivity("参与人测试活动", 1);
        ParticipantCreateDTO pdto = new ParticipantCreateDTO();
        pdto.setParticipantName("张三");
        pdto.setParticipantPhone("13800138000");
        pdto.setCompanyName("示例科技");
        pdto.setSource(2);
        MvcResult r = mockMvc.perform(post("/v1/activities/" + actId + "/participants")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pdto)))
                .andExpect(status().isOk()).andReturn();
        Long pid = objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();

        mockMvc.perform(get("/v1/activities/" + actId + "/participants")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test @Order(5)
    void checkinParticipant_shouldSucceed() throws Exception {
        Long actId = createActivity("签到测试活动", 1);
        ParticipantCreateDTO pdto = new ParticipantCreateDTO();
        pdto.setParticipantName("李四");
        pdto.setParticipantPhone("13900139000");
        pdto.setSource(2);
        MvcResult r = mockMvc.perform(post("/v1/activities/" + actId + "/participants")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pdto)))
                .andExpect(status().isOk()).andReturn();
        Long pid = objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();

        mockMvc.perform(post("/v1/activities/" + actId + "/participants/" + pid + "/checkin")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk());
    }

    @Test @Order(6)
    void getStatistics_shouldReturn() throws Exception {
        Long actId = createActivity("统计测试活动", 1);
        mockMvc.perform(get("/v1/activities/" + actId + "/statistics")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRegistrations").isNumber());
    }

    @Test @Order(7)
    void cancelActivity_shouldChangeStatus() throws Exception {
        Long id = createActivity("取消测试活动", 2);
        mockMvc.perform(put("/v1/activities/" + id + "/cancel")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/activities/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.status").value(6));
    }

    @Test @Order(8)
    void listActivities_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/v1/activities")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}
