package com.aicrm.module.system.notification.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息通知查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知查询条件")
public class NotificationQueryDTO extends PageQuery {

    @Schema(description = "消息类型 1任务提醒/2线索分配/3审核通知/4违规警报/5系统通知")
    private Integer msgType;
    @Schema(description = "是否已读 0未读/1已读")
    private Integer isRead;
}
