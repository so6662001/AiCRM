package com.aicrm.module.system.notification.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.system.notification.dto.NotificationQueryDTO;
import com.aicrm.module.system.notification.dto.NotificationVO;

/**
 * 消息通知服务接口
 */
public interface NotificationService {

    /**
     * 分页查询通知
     */
    PageResult<NotificationVO> page(NotificationQueryDTO query);

    /**
     * 标记单条已读
     */
    void markRead(Long id);

    /**
     * 标记全部已读
     */
    void markAllRead();

    /**
     * 未读数量
     */
    long unreadCount();
}
