package com.aicrm.module.crm;

import com.aicrm.module.crm.customer.dto.CustomerCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT = "1";
    private static final String USER = "100";

    private Long createCustomer(String name) throws Exception {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setCustomerName(name);
        dto.setCustomerType(1);
        dto.setIndustry("互联网");
        dto.setProvince("广东");
        dto.setCity("深圳");
        dto.setSource("manual");
        dto.setOwnerUserId(Long.parseLong(USER));
        dto.setExpectedPurchaseDate(LocalDate.now().plusDays(7));

        MvcResult result = mockMvc.perform(post("/v1/customers")
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
    void createCustomer_shouldSucceed() throws Exception {
        Long id = createCustomer("示例科技有限公司");
        org.assertj.core.api.Assertions.assertThat(id).isNotNull();

        mockMvc.perform(get("/v1/customers/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.customerName").value("示例科技有限公司"))
                .andExpect(jsonPath("$.data.lifecycleStage").value(1));
    }

    @Test
    @Order(2)
    void listCustomers_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/v1/customers")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @Order(3)
    void purchaseCountdown_shouldCalculateCorrectly() throws Exception {
        Long id = createCustomer("采购倒计时测试公司");

        mockMvc.perform(get("/v1/customers/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.purchaseCountdown.status").value("upcoming"))
                .andExpect(jsonPath("$.data.purchaseCountdown.daysRemaining").isNumber());
    }

    @Test
    @Order(4)
    void transferCustomer_shouldUpdateOwner() throws Exception {
        Long id = createCustomer("转移测试公司");

        mockMvc.perform(post("/v1/customers/transfer")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerIds\":[" + id + "],\"targetUserId\":200}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/customers/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.ownerUserId").value(200));
    }

    @Test
    @Order(5)
    void markInvalid_shouldChangeStage() throws Exception {
        Long id = createCustomer("无效测试公司");

        mockMvc.perform(post("/v1/customers/" + id + "/mark-invalid")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"空壳公司\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/customers/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.lifecycleStage").value(7));
    }

    @Test
    @Order(6)
    void reactivate_shouldRestoreStage() throws Exception {
        Long id = createCustomer("重新激活测试");

        mockMvc.perform(post("/v1/customers/" + id + "/mark-invalid")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"误操作\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/customers/" + id + "/reactivate")
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/customers/" + id)
                        .header("X-Tenant-Id", TENANT)
                        .header("X-User-Id", USER))
                .andExpect(jsonPath("$.data.lifecycleStage").value(1));
    }
}
