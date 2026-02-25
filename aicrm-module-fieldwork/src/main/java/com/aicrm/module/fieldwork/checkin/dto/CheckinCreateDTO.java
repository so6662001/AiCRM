package com.aicrm.module.fieldwork.checkin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建签到DTO
 */
@Data
@Schema(description = "创建签到请求")
public class CheckinCreateDTO {

    @NotNull(message = "签到类型不能为空")
    @Schema(description = "签到类型 1外出/2拜访签到/3日常", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer checkinType;

    @NotNull(message = "地址不能为空")
    @Schema(description = "地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "照片URL")
    private String photoUrl;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "关联拜访ID")
    private Long relatedVisitId;

    @Schema(description = "备注")
    private String remark;
}
