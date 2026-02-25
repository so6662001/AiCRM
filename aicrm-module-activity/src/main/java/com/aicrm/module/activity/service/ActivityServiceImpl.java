package com.aicrm.module.activity.service;

import cn.hutool.core.util.StrUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.activity.dto.*;
import com.aicrm.module.activity.entity.Activity;
import com.aicrm.module.activity.entity.ActivityParticipant;
import com.aicrm.module.activity.mapper.ActivityMapper;
import com.aicrm.module.activity.mapper.ActivityParticipantMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private static final String ACTIVITY_NO_PREFIX = "ACT";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String QR_CODE_BASE_URL = "https://crm.example.com/a/";

    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper participantMapper;

    @Override
    public PageResult<ActivityVO> page(ActivityQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getTenantId, tenantId)
                .eq(query.getActivityCategory() != null, Activity::getActivityCategory, query.getActivityCategory())
                .eq(query.getActivityType() != null, Activity::getActivityType, query.getActivityType())
                .eq(query.getStatus() != null, Activity::getStatus, query.getStatus())
                .eq(query.getOwnerUserId() != null, Activity::getOwnerUserId, query.getOwnerUserId());

        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(Activity::getActivityName, query.getKeyword());
        }

        if (StrUtil.isNotBlank(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "activityNo" -> wrapper.orderBy(true, isAsc, Activity::getActivityNo);
                case "activityName" -> wrapper.orderBy(true, isAsc, Activity::getActivityName);
                case "startTime" -> wrapper.orderBy(true, isAsc, Activity::getStartTime);
                case "createdTime" -> wrapper.orderBy(true, isAsc, Activity::getCreatedTime);
                default -> wrapper.orderByDesc(Activity::getCreatedTime);
            }
        } else {
            wrapper.orderByDesc(Activity::getCreatedTime);
        }

        Page<Activity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Activity> result = activityMapper.selectPage(page, wrapper);

        List<ActivityVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public ActivityVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        return convertToVO(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ActivityCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setActivityNo(generateActivityNo(tenantId));
        activity.setQrCodeContent(QR_CODE_BASE_URL + activity.getActivityNo());
        activity.setQrCodeUrl(null);
        activity.setStatus(0);
        activity.setCurrentParticipants(0);
        activity.setTotalScans(0);
        activity.setUniqueScans(0);
        activity.setWaitlistEnabled(activity.getWaitlistEnabled() != null ? activity.getWaitlistEnabled() : 0);
        activity.setWechatWorkBind(activity.getWechatWorkBind() != null ? activity.getWechatWorkBind() : 0);

        activityMapper.insert(activity);
        return activity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActivityUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity existActivity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, dto.getId())
                .eq(Activity::getTenantId, tenantId));

        if (existActivity == null) {
            throw BizException.notFound("活动");
        }

        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setId(dto.getId());
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        activityMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        if (activity.getStatus() != 0) {
            throw new BizException("只有草稿状态的活动才能发布");
        }

        LocalDateTime now = LocalDateTime.now();
        int newStatus;

        if (activity.getActivityCategory() == 1) {
            // 需要报名
            if (activity.getRegistrationStartTime() != null && !now.isBefore(activity.getRegistrationStartTime())) {
                if (activity.getRegistrationEndTime() != null && now.isAfter(activity.getRegistrationEndTime())) {
                    newStatus = 3; // 报名截止
                } else {
                    newStatus = 2; // 报名中
                }
            } else if (now.isBefore(activity.getStartTime())) {
                newStatus = 1; // 未开始
            } else if (now.isAfter(activity.getEndTime())) {
                newStatus = 5; // 已结束
            } else {
                newStatus = 4; // 进行中
            }
        } else {
            // 不需要报名
            if (now.isBefore(activity.getStartTime())) {
                newStatus = 1; // 未开始
            } else if (now.isAfter(activity.getEndTime())) {
                newStatus = 5; // 已结束
            } else {
                newStatus = 4; // 进行中
            }
        }

        activity.setStatus(newStatus);
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        activity.setStatus(6);
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void end(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        activity.setStatus(5);
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeRegistration(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        activity.setStatus(3);
        activityMapper.updateById(activity);
    }

    @Override
    public ActivityStatisticsVO getStatistics(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, id)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }

        ActivityStatisticsVO vo = new ActivityStatisticsVO();
        vo.setTotalScans(activity.getTotalScans() != null ? activity.getTotalScans() : 0);
        vo.setUniqueScans(activity.getUniqueScans() != null ? activity.getUniqueScans() : 0);

        List<ActivityParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<ActivityParticipant>()
                        .eq(ActivityParticipant::getActivityId, id)
                        .eq(ActivityParticipant::getTenantId, tenantId));

        int totalRegistrations = participants.size();
        int approved = (int) participants.stream().filter(p -> p.getRegistrationStatus() != null && p.getRegistrationStatus() == 1).count();
        int rejected = (int) participants.stream().filter(p -> p.getRegistrationStatus() != null && p.getRegistrationStatus() == 2).count();
        int pending = (int) participants.stream().filter(p -> p.getRegistrationStatus() != null && p.getRegistrationStatus() == 0).count();
        int checkedIn = (int) participants.stream().filter(p -> p.getCheckinStatus() != null && p.getCheckinStatus() == 1).count();
        int friendAdded = (int) participants.stream().filter(p -> p.getFriendAdded() != null && p.getFriendAdded() == 1).count();
        int convertedToLead = (int) participants.stream().filter(p -> p.getIsLead() != null && p.getIsLead() == 1).count();

        vo.setTotalRegistrations(totalRegistrations);
        vo.setApproved(approved);
        vo.setRejected(rejected);
        vo.setPending(pending);
        vo.setCheckedIn(checkedIn);
        vo.setFriendAdded(friendAdded);
        vo.setConvertedToLead(convertedToLead);

        double checkinRate = approved > 0 ? (double) checkedIn / approved : 0.0;
        vo.setCheckinRate(Math.round(checkinRate * 100.0) / 100.0);

        return vo;
    }

    private ActivityVO convertToVO(Activity activity) {
        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setOwnerUserName(null); // TODO: 从用户服务获取负责人姓名
        vo.setCategoryLabel(activity.getActivityCategory() != null && activity.getActivityCategory() == 1 ? "需要报名" : "不需要报名");
        vo.setStatusLabel(getStatusLabel(activity.getStatus()));
        return vo;
    }

    private String getStatusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "未开始";
            case 2 -> "报名中";
            case 3 -> "报名截止";
            case 4 -> "进行中";
            case 5 -> "已结束";
            case 6 -> "已取消";
            default -> "";
        };
    }

    private String generateActivityNo(Long tenantId) {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = ACTIVITY_NO_PREFIX + dateStr;

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getTenantId, tenantId)
                .likeRight(Activity::getActivityNo, prefix)
                .orderByDesc(Activity::getActivityNo);

        Page<Activity> p = activityMapper.selectPage(new Page<>(1, 1), wrapper);
        Activity lastActivity = p.getRecords().isEmpty() ? null : p.getRecords().get(0);
        int seq = 1;
        if (lastActivity != null && lastActivity.getActivityNo() != null && lastActivity.getActivityNo().length() >= prefix.length() + 4) {
            try {
                String seqStr = lastActivity.getActivityNo().substring(prefix.length());
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException ignored) {
                // use default seq
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }
}
