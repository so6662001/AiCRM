package com.aicrm.module.system.notification.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.system.notification.dto.NotificationQueryDTO;
import com.aicrm.module.system.notification.dto.NotificationVO;
import com.aicrm.module.system.notification.entity.Notification;
import com.aicrm.module.system.notification.mapper.NotificationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Map<Integer, String> MSG_TYPE_LABELS = Map.of(
            1, "任务提醒",
            2, "线索分配",
            3, "审核通知",
            4, "违规警报",
            5, "系统通知"
    );

    private final NotificationMapper notificationMapper;

    @Override
    public PageResult<NotificationVO> page(NotificationQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getTenantId, tenantId)
                .eq(Notification::getUserId, userId);

        if (query.getMsgType() != null) {
            wrapper.eq(Notification::getMsgType, query.getMsgType());
        }
        if (query.getIsRead() != null) {
            wrapper.eq(Notification::getIsRead, query.getIsRead());
        }

        wrapper.orderByDesc(Notification::getCreatedTime);

        Page<Notification> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);

        List<NotificationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            throw new BizException("租户或用户ID不能为空");
        }

        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
                .eq(Notification::getTenantId, tenantId)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now());

        notificationMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            return;
        }

        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getTenantId, tenantId)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now());

        notificationMapper.update(null, wrapper);
    }

    @Override
    public long unreadCount() {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            return 0;
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getTenantId, tenantId)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        return notificationMapper.selectCount(wrapper);
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtil.copyProperties(notification, vo);
        vo.setMsgTypeLabel(MSG_TYPE_LABELS.getOrDefault(notification.getMsgType(), "未知"));
        return vo;
    }
}
