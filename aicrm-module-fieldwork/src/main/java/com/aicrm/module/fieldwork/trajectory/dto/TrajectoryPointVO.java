package com.aicrm.module.fieldwork.trajectory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 轨迹点VO
 */
@Data
@Schema(description = "轨迹点")
public class TrajectoryPointVO {

    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "时间")
    private LocalDateTime time;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "类型 checkin/visit_checkin/location")
    private String type;
    @Schema(description = "标签")
    private String label;
    @Schema(description = "停留时长(分钟)")
    private Integer stayDuration;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "拜访ID")
    private Long visitId;
}
