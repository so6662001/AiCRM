package com.aicrm.module.social;

import com.aicrm.module.social.dto.FriendCreateDTO;
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
class FriendApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static final String T = "1", U = "100";

    private Long createFriend(String name) throws Exception {
        FriendCreateDTO dto = new FriendCreateDTO();
        dto.setFriendName(name);
        dto.setFriendPhone("138" + System.nanoTime() % 100000000);
        dto.setFriendCompany("测试公司");
        dto.setFriendPosition("经理");
        dto.setFriendType(1);
        dto.setSource(3);
        MvcResult r = mockMvc.perform(post("/v1/friends")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    @Test @Order(1)
    void createFriend_shouldSucceed() throws Exception {
        Long id = createFriend("张三");
        org.assertj.core.api.Assertions.assertThat(id).isNotNull();
    }

    @Test @Order(2)
    void getFriendById_shouldReturnData() throws Exception {
        Long id = createFriend("李四");
        mockMvc.perform(get("/v1/friends/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.friendName").value("李四"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test @Order(3)
    void linkCustomer_shouldUpdate() throws Exception {
        Long id = createFriend("王五");
        mockMvc.perform(post("/v1/friends/" + id + "/link-customer")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":5001}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/friends/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.customerId").value(5001));
    }

    @Test @Order(4)
    void convertToLead_shouldMark() throws Exception {
        Long id = createFriend("赵六");
        mockMvc.perform(post("/v1/friends/" + id + "/convert-to-lead")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/friends/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.leadId").isNumber());
    }

    @Test @Order(5)
    void listFriends_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/v1/friends")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test @Order(6)
    void statistics_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/friends/statistics")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalFriends").isNumber());
    }

    @Test @Order(7)
    void deleteFriend_shouldLogicalDelete() throws Exception {
        Long id = createFriend("删除测试");
        mockMvc.perform(delete("/v1/friends/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/friends/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.code").value(404));
    }
}
