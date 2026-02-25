package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拒绝原因DTO
 */
@Data
@Schema(description = "拒绝原因请求")
public class RejectReasonDTO {

    @Schema(description = "拒绝原因")
    private String reason;
}
