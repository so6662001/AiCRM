package com.aicrm.module.crm.violation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违规词视图VO
 */
@Data
@Schema(description = "违规词视图")
public class ViolationWordVO {

    private Long id;
    private Long tenantId;
    private String word;
    private String category;
    private Integer level;
    private String levelLabel;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
