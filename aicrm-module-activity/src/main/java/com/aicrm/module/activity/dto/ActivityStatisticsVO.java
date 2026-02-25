package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动统计视图对象
 */
@Data
@Schema(description = "活动统计")
public class ActivityStatisticsVO {

    @Schema(description = "总扫码次数")
    private Integer totalScans;
    @Schema(description = "独立扫码人数")
    private Integer uniqueScans;
    @Schema(description = "总报名数")
    private Integer totalRegistrations;
    @Schema(description = "已通过数")
    private Integer approved;
    @Schema(description = "已拒绝数")
    private Integer rejected;
    @Schema(description = "待审核数")
    private Integer pending;
    @Schema(description = "已签到数")
    private Integer checkedIn;
    @Schema(description = "签到率")
    private Double checkinRate;
    @Schema(description = "加好友数")
    private Integer friendAdded;
    @Schema(description = "转化为线索数")
    private Integer convertedToLead;
}
