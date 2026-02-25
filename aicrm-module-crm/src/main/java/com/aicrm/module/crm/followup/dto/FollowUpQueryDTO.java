package com.aicrm.module.crm.followup.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 跟进记录查询参数DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "跟进记录查询参数")
public class FollowUpQueryDTO extends PageQuery {

    @Schema(description = "业务类型 1线索/2客户/3商机")
    private Integer bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "跟进方式 1现场拜访/2电话/3微信/4邮件/5企业微信/6其他")
    private Integer followType;

    @Schema(description = "跟进人用户ID")
    private Long followUserId;

    @Schema(description = "是否违规")
    private Boolean hasViolation;

    @Schema(description = "创建时间开始")
    private LocalDateTime createdTimeStart;

    @Schema(description = "创建时间结束")
    private LocalDateTime createdTimeEnd;
}
