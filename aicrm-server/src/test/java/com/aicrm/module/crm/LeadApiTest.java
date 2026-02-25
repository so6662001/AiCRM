package com.aicrm.module.crm;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.lead.dto.LeadCreateDTO;
import com.aicrm.module.crm.lead.dto.LeadVO;
import com.aicrm.module.crm.lead.service.LeadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LeadApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeadService leadService;

    private static final String TENANT_ID = "1";
    private static final String USER_ID = "100";

    @Test
    @Order(1)
    void createLead_shouldReturn200() throws Exception {
        LeadCreateDTO dto = new LeadCreateDTO();
        dto.setContactName("张三");
        dto.setContactPhone("13800138000");
        dto.setCompanyName("测试科技有限公司");
        dto.setSource("manual");
        dto.setIntentionLevel("A");
        dto.setProvince("广东");
        dto.setCity("深圳");
        dto.setIndustry("互联网");

        mockMvc.perform(post("/v1/leads")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @Order(2)
    void listLeads_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/v1/leads")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @Order(3)
    void createAndGetLead_shouldReturnCorrectData() throws Exception {
        LeadCreateDTO dto = new LeadCreateDTO();
        dto.setContactName("李四");
        dto.setContactPhone("13900139000");
        dto.setCompanyName("示例公司");
        dto.setSource("website");
        dto.setIntentionLevel("B");

        MvcResult createResult = mockMvc.perform(post("/v1/leads")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String body = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("data").asLong();

        mockMvc.perform(get("/v1/leads/" + id)
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactName").value("李四"))
                .andExpect(jsonPath("$.data.companyName").value("示例公司"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @Order(4)
    void assignLead_shouldUpdateStatus() throws Exception {
        LeadCreateDTO dto = new LeadCreateDTO();
        dto.setContactName("王五");
        dto.setContactPhone("13700137000");
        dto.setSource("manual");

        MvcResult createResult = mockMvc.perform(post("/v1/leads")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").asLong();

        String assignBody = "{\"leadIds\":[" + id + "],\"targetUserId\":200}";
        mockMvc.perform(post("/v1/leads/assign")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/v1/leads/" + id)
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.ownerUserId").value(200));
    }

    @Test
    @Order(5)
    void returnToPool_shouldUpdateStatus() throws Exception {
        LeadCreateDTO dto = new LeadCreateDTO();
        dto.setContactName("赵六");
        dto.setContactPhone("13600136000");
        dto.setSource("manual");

        MvcResult createResult = mockMvc.perform(post("/v1/leads")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").asLong();

        mockMvc.perform(post("/v1/leads/" + id + "/return")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"returnReason\":\"联系方式无效\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/leads/" + id)
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(jsonPath("$.data.status").value(4))
                .andExpect(jsonPath("$.data.inPool").value(1));
    }

    @Test
    @Order(6)
    void deleteLead_shouldLogicalDelete() throws Exception {
        LeadCreateDTO dto = new LeadCreateDTO();
        dto.setContactName("删除测试");
        dto.setSource("manual");

        MvcResult createResult = mockMvc.perform(post("/v1/leads")
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").asLong();

        mockMvc.perform(delete("/v1/leads/" + id)
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/leads/" + id)
                        .header("X-Tenant-Id", TENANT_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(jsonPath("$.code").value(404));
    }
}
