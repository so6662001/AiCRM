package com.aicrm.module.fieldwork.trajectory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 位置上报DTO
 */
@Data
@Schema(description = "位置上报请求")
public class LocationReportDTO {

    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "精度(米)")
    private Float accuracy;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "上报类型 1定时/2打卡/3拜访")
    private Integer reportType;
    @Schema(description = "关联业务类型")
    private String relatedBizType;
    @Schema(description = "关联业务ID")
    private Long relatedBizId;
    @Schema(description = "电量百分比")
    private Integer batteryLevel;
    @Schema(description = "网络类型")
    private String networkType;
    @Schema(description = "设备信息")
    private String deviceInfo;
}
