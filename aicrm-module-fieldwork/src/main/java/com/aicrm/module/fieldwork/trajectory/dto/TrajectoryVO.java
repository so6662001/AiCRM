package com.aicrm.module.fieldwork.trajectory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 轨迹VO
 */
@Data
@Schema(description = "日轨迹")
public class TrajectoryVO {

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名称")
    private String userName;
    @Schema(description = "日期")
    private String date;
    @Schema(description = "总距离(公里)")
    private Double totalDistance;
    @Schema(description = "总停留点数")
    private Integer totalStops;
    @Schema(description = "轨迹点列表")
    private List<TrajectoryPointVO> points;
}
