package com.aicrm.module.crm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 采购倒计时VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "采购倒计时")
public class PurchaseCountdownVO {

    @Schema(description = "状态: upcoming/overdue/none")
    private String status;
    @Schema(description = "剩余天数")
    private Integer daysRemaining;
    @Schema(description = "逾期天数")
    private Integer daysOverdue;
    @Schema(description = "展示文本")
    private String displayText;
}
