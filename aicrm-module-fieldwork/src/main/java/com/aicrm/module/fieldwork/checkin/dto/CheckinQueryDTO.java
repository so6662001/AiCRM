package com.aicrm.module.fieldwork.checkin.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 签到查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "签到查询条件")
public class CheckinQueryDTO extends PageQuery {

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "签到类型 1外出/2拜访签到/3日常")
    private Integer checkinType;
    @Schema(description = "签到时间开始")
    private LocalDateTime checkinTimeStart;
    @Schema(description = "签到时间结束")
    private LocalDateTime checkinTimeEnd;
}
