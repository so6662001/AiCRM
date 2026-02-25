package com.aicrm.module.activity.scanlog.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动扫码日志查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "活动扫码日志查询条件")
public class ScanLogQueryDTO extends PageQuery {

    @Schema(description = "活动ID")
    private Long activityId;
}
