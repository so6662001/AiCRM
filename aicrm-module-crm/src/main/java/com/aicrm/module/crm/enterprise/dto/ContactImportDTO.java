package com.aicrm.module.crm.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 联系人导入DTO
 */
@Data
public class ContactImportDTO {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    /** 是否主联系人，默认false */
    private Boolean isPrimary = false;
}
