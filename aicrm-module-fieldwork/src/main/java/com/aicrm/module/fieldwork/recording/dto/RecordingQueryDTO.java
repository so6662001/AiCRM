package com.aicrm.module.fieldwork.recording.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 录音查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "录音查询条件")
public class RecordingQueryDTO extends PageQuery {

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "是否违规")
    private Boolean hasViolation;
    @Schema(description = "通话开始时间-起始")
    private LocalDateTime callStartTimeStart;
    @Schema(description = "通话开始时间-结束")
    private LocalDateTime callStartTimeEnd;
}
