package com.aicrm.module.fieldwork.checkin.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.fieldwork.checkin.dto.CheckinCreateDTO;
import com.aicrm.module.fieldwork.checkin.dto.CheckinQueryDTO;
import com.aicrm.module.fieldwork.checkin.dto.CheckinVO;
import com.aicrm.module.fieldwork.checkin.entity.CheckinRecord;
import com.aicrm.module.fieldwork.checkin.mapper.CheckinRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 签到服务实现
 */
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinRecordMapper checkinRecordMapper;

    @Override
    public PageResult<CheckinVO> page(CheckinQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTenantId, tenantId)
                .eq(query.getUserId() != null, CheckinRecord::getUserId, query.getUserId())
                .eq(query.getCheckinType() != null, CheckinRecord::getCheckinType, query.getCheckinType())
                .ge(query.getCheckinTimeStart() != null, CheckinRecord::getCheckinTime, query.getCheckinTimeStart())
                .le(query.getCheckinTimeEnd() != null, CheckinRecord::getCheckinTime, query.getCheckinTimeEnd())
                .orderByDesc(CheckinRecord::getCheckinTime);

        Page<CheckinRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CheckinRecord> result = checkinRecordMapper.selectPage(page, wrapper);

        List<CheckinVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public CheckinVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        CheckinRecord record = checkinRecordMapper.selectOne(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getId, id)
                        .eq(CheckinRecord::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("签到记录");
        }
        return convertToVO(record);
    }

    @Override
    public List<CheckinVO> getToday(Long userId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        Long effectiveUserId = userId != null ? userId : TenantContext.getUserId();
        if (effectiveUserId == null) {
            return List.of();
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTenantId, tenantId)
                .eq(CheckinRecord::getUserId, effectiveUserId)
                .ge(CheckinRecord::getCheckinTime, startOfDay)
                .le(CheckinRecord::getCheckinTime, endOfDay)
                .orderByAsc(CheckinRecord::getCheckinTime);

        List<CheckinRecord> records = checkinRecordMapper.selectList(wrapper);
        return records.stream().map(this::convertToVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CheckinCreateDTO dto) {
        CheckinRecord record = new CheckinRecord();
        BeanUtil.copyProperties(dto, record);
        record.setUserId(TenantContext.getUserId());
        record.setCheckinTime(LocalDateTime.now());

        checkinRecordMapper.insert(record);
        return record.getId();
    }

    private CheckinVO convertToVO(CheckinRecord record) {
        CheckinVO vo = new CheckinVO();
        BeanUtil.copyProperties(record, vo);
        vo.setUserName(resolveUserName(record.getUserId()));
        return vo;
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名
        return null;
    }
}
