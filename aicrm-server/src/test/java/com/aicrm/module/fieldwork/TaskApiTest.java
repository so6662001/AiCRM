package com.aicrm.module.fieldwork;

import com.aicrm.module.fieldwork.task.dto.TaskCreateDTO;
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
class TaskApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static final String T = "1", U = "100";

    private Long createTask(String title) throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTaskTitle(title);
        dto.setTaskType(1);
        dto.setPriority(3);
        dto.setAssignType(1);
        dto.setAssigneeUserId(100L);
        dto.setPlanStartTime(LocalDateTime.now());
        dto.setPlanEndTime(LocalDateTime.now().plusHours(4));
        MvcResult r = mockMvc.perform(post("/v1/tasks")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    @Test @Order(1)
    void createTask_shouldSucceed() throws Exception {
        Long id = createTask("拜访示例公司");
        org.assertj.core.api.Assertions.assertThat(id).isNotNull();
    }

    @Test @Order(2)
    void getTaskById_shouldReturnData() throws Exception {
        Long id = createTask("电话跟进客户");
        mockMvc.perform(get("/v1/tasks/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.taskTitle").value("电话跟进客户"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test @Order(3)
    void completeTask_shouldUpdateStatus() throws Exception {
        Long id = createTask("准备报价方案");
        mockMvc.perform(put("/v1/tasks/" + id + "/complete")
                .header("X-Tenant-Id", T).header("X-User-Id", U)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"completionNote\":\"已完成报价文档\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/tasks/" + id)
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.completionRate").value(100));
    }

    @Test @Order(4)
    void todaySummary_shouldReturn() throws Exception {
        mockMvc.perform(get("/v1/tasks/today/summary")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test @Order(5)
    void listTasks_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/v1/tasks")
                .header("X-Tenant-Id", T).header("X-User-Id", U))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}
