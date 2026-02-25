package com.aicrm.module.system.notification.mapper;

import com.aicrm.module.system.notification.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
