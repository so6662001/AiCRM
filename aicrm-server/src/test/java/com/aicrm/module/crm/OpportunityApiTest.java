package com.aicrm.module.crm;

import com.aicrm.module.crm.opportunity.dto.OpportunityCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpportunityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT = "1";
    private static final String USER = "100";

    private Long createOpportunity(String name) throws Exception {
        OpportunityCreateDTO dto = new OpportunityCreateDTO();
        dto.setOpportunityName(name);
        dto.setCustomerId(1L);
        dto.setStageId(1L);
        dto.setExpectedAmount(new BigDecimal("500000"));
        dto.setExpectedCloseDate(LocalDate.now().plusMonths(3));

        MvcResult result = mockMvc.perform(post("/v1/opportunities")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").asLong();
    }

    @Test
    @Order(1)
    void createOpportunity_shouldSucceed() throws Exception {
        Long id = createOpportunity("ERP采购项目");
        org.assertj.core.api.Assertions.assertThat(id).isNotNull();
    }

    @Test
    @Order(2)
    void changeStage_shouldUpdateStage() throws Exception {
        Long id = createOpportunity("阶段变更测试");

        mockMvc.perform(put("/v1/opportunities/" + id + "/stage")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stageId\":2,\"remark\":\"需求已确认\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/opportunities/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.stageId").value(2));
    }

    @Test
    @Order(3)
    void winOpportunity_shouldUpdateStatus() throws Exception {
        Long id = createOpportunity("赢单测试");

        mockMvc.perform(post("/v1/opportunities/" + id + "/win")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actualAmount\":480000}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/opportunities/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.actualAmount").value(480000));
    }

    @Test
    @Order(4)
    void loseOpportunity_shouldUpdateStatus() throws Exception {
        Long id = createOpportunity("输单测试");

        mockMvc.perform(post("/v1/opportunities/" + id + "/lose")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lossReason\":\"价格不合适\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/opportunities/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.status").value(3))
                .andExpect(jsonPath("$.data.lossReason").value("价格不合适"));
    }

    @Test
    @Order(5)
    void listOpportunities_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/v1/opportunities")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}
