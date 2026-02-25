package com.aicrm.module.system.notification.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    /** 接收用户ID */
    private Long userId;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 消息类型 1任务提醒/2线索分配/3审核通知/4违规警报/5系统通知 */
    private Integer msgType;
    /** 业务类型 */
    private String bizType;
    /** 业务ID */
    private Long bizId;
    /** 是否已读 0未读/1已读 默认0 */
    private Integer isRead;
    /** 阅读时间 */
    private LocalDateTime readTime;
    /** 推送渠道 */
    private String pushChannels;
    /** 推送状态 默认1 */
    private Integer pushStatus;
}
