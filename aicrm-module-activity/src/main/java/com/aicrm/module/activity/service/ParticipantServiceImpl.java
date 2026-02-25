package com.aicrm.module.activity.service;

import cn.hutool.core.util.StrUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageQuery;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.activity.dto.ParticipantCreateDTO;
import com.aicrm.module.activity.dto.ParticipantVO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参与人服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper participantMapper;

    @Override
    public PageResult<ParticipantVO> page(Long activityId, PageQuery query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        verifyActivityExists(activityId, tenantId);

        LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getTenantId, tenantId)
                .orderByDesc(ActivityParticipant::getRegistrationTime);

        if (StrUtil.isNotBlank(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "participantName" -> wrapper.orderBy(true, isAsc, ActivityParticipant::getParticipantName);
                case "registrationTime" -> wrapper.orderBy(true, isAsc, ActivityParticipant::getRegistrationTime);
                case "checkinTime" -> wrapper.orderBy(true, isAsc, ActivityParticipant::getCheckinTime);
                default -> wrapper.orderByDesc(ActivityParticipant::getRegistrationTime);
            }
        }

        Page<ActivityParticipant> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ActivityParticipant> result = participantMapper.selectPage(page, wrapper);

        List<ParticipantVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Long activityId, ParticipantCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Activity activity = verifyActivityExists(activityId, tenantId);

        ActivityParticipant participant = new ActivityParticipant();
        BeanUtils.copyProperties(dto, participant);
        participant.setActivityId(activityId);
        participant.setRegistrationTime(LocalDateTime.now());
        participant.setCheckinStatus(0);

        if (activity.getRegistrationApproval() != null && activity.getRegistrationApproval() == 1) {
            participant.setRegistrationStatus(0); // 待审核
        } else {
            participant.setRegistrationStatus(1); // 已通过
        }

        participantMapper.insert(participant);

        activity.setCurrentParticipants((activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0) + 1);
        activityMapper.updateById(activity);

        return participant.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long activityId, Long participantId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        verifyActivityExists(activityId, tenantId);

        ActivityParticipant participant = participantMapper.selectOne(new LambdaQueryWrapper<ActivityParticipant>()
                .eq(ActivityParticipant::getId, participantId)
                .eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getTenantId, tenantId));

        if (participant == null) {
            throw BizException.notFound("参与人");
        }

        participant.setRegistrationStatus(1);
        participantMapper.updateById(participant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long activityId, Long participantId, String reason) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        verifyActivityExists(activityId, tenantId);

        ActivityParticipant participant = participantMapper.selectOne(new LambdaQueryWrapper<ActivityParticipant>()
                .eq(ActivityParticipant::getId, participantId)
                .eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getTenantId, tenantId));

        if (participant == null) {
            throw BizException.notFound("参与人");
        }

        participant.setRegistrationStatus(2);
        participant.setRejectReason(reason);
        participantMapper.updateById(participant);

        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, activityId)
                .eq(Activity::getTenantId, tenantId));
        if (activity != null && activity.getCurrentParticipants() != null && activity.getCurrentParticipants() > 0) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            activityMapper.updateById(activity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long activityId, Long participantId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        verifyActivityExists(activityId, tenantId);

        ActivityParticipant participant = participantMapper.selectOne(new LambdaQueryWrapper<ActivityParticipant>()
                .eq(ActivityParticipant::getId, participantId)
                .eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getTenantId, tenantId));

        if (participant == null) {
            throw BizException.notFound("参与人");
        }

        participant.setCheckinStatus(1);
        participant.setCheckinTime(LocalDateTime.now());
        participantMapper.updateById(participant);
    }

    private Activity verifyActivityExists(Long activityId, Long tenantId) {
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, activityId)
                .eq(Activity::getTenantId, tenantId));

        if (activity == null) {
            throw BizException.notFound("活动");
        }
        return activity;
    }

    private ParticipantVO convertToVO(ActivityParticipant participant) {
        ParticipantVO vo = new ParticipantVO();
        BeanUtils.copyProperties(participant, vo);
        return vo;
    }
}
