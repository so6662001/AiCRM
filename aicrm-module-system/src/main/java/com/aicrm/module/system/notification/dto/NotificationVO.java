package com.aicrm.module.system.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知视图对象
 */
@Data
@Schema(description = "消息通知详情")
public class NotificationVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "接收用户ID")
    private Long userId;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "消息类型 1任务提醒/2线索分配/3审核通知/4违规警报/5系统通知")
    private Integer msgType;
    @Schema(description = "消息类型标签")
    private String msgTypeLabel;
    @Schema(description = "业务类型")
    private String bizType;
    @Schema(description = "业务ID")
    private Long bizId;
    @Schema(description = "是否已读 0未读/1已读")
    private Integer isRead;
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
    @Schema(description = "推送渠道")
    private String pushChannels;
    @Schema(description = "推送状态")
    private Integer pushStatus;
    @Schema(description = "创建人")
    private Long createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @Schema(description = "更新人")
    private Long updatedBy;
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
