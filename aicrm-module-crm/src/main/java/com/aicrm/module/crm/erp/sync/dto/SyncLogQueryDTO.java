package com.aicrm.module.crm.erp.sync.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP同步日志查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ERP同步日志查询条件")
public class SyncLogQueryDTO extends PageQuery {

    @Schema(description = "同步类型 1:ERP→CRM 2:CRM→ERP")
    private Integer syncType;
    @Schema(description = "状态 1进行中/2成功/3部分失败/4失败")
    private Integer status;
    @Schema(description = "业务类型 customer/product/order")
    private String bizType;
}
