package com.aicrm.module.fieldwork.recording.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.fieldwork.recording.dto.RecordingCreateDTO;
import com.aicrm.module.fieldwork.recording.dto.RecordingQueryDTO;
import com.aicrm.module.fieldwork.recording.dto.RecordingVO;
import com.aicrm.module.fieldwork.recording.entity.CallRecording;
import com.aicrm.module.fieldwork.recording.mapper.CallRecordingMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 电话录音服务实现
 */
@Service
@RequiredArgsConstructor
public class RecordingServiceImpl implements RecordingService {

    private static final int TRANSCRIPTION_STATUS_PENDING = 0;
    private static final int TRANSCRIPTION_STATUS_DONE = 2;
    private static final int VIOLATION_CHECK_STATUS_DONE = 2;

    private final CallRecordingMapper callRecordingMapper;

    @Override
    public PageResult<RecordingVO> page(RecordingQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<CallRecording> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CallRecording::getTenantId, tenantId)
                .eq(query.getUserId() != null, CallRecording::getUserId, query.getUserId())
                .eq(query.getCustomerId() != null, CallRecording::getCustomerId, query.getCustomerId())
                .eq(query.getHasViolation() != null, CallRecording::getHasViolation, Boolean.TRUE.equals(query.getHasViolation()) ? 1 : 0)
                .ge(query.getCallStartTimeStart() != null, CallRecording::getCallStartTime, query.getCallStartTimeStart())
                .le(query.getCallStartTimeEnd() != null, CallRecording::getCallStartTime, query.getCallStartTimeEnd())
                .orderByDesc(CallRecording::getCallStartTime);

        Page<CallRecording> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CallRecording> result = callRecordingMapper.selectPage(page, wrapper);

        List<RecordingVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public RecordingVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        CallRecording record = callRecordingMapper.selectOne(
                new LambdaQueryWrapper<CallRecording>()
                        .eq(CallRecording::getId, id)
                        .eq(CallRecording::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("录音");
        }
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RecordingCreateDTO dto) {
        CallRecording record = new CallRecording();
        BeanUtil.copyProperties(dto, record);
        record.setTranscriptionStatus(TRANSCRIPTION_STATUS_PENDING);
        record.setViolationCheckStatus(0);
        record.setHasViolation(0);

        callRecordingMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerTranscribe(Long id) {
        Long tenantId = TenantContext.getTenantId();
        CallRecording record = callRecordingMapper.selectOne(
                new LambdaQueryWrapper<CallRecording>()
                        .eq(CallRecording::getId, id)
                        .eq(CallRecording::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("录音");
        }

        // DEMO版：直接设 transcriptionStatus=2 + 模拟 transcriptionText
        record.setTranscriptionStatus(TRANSCRIPTION_STATUS_DONE);
        record.setTranscriptionText("[DEMO] 模拟转写文本 - 通话时间: " + formatDateTime(record.getCallStartTime()));
        callRecordingMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerAnalyze(Long id) {
        Long tenantId = TenantContext.getTenantId();
        CallRecording record = callRecordingMapper.selectOne(
                new LambdaQueryWrapper<CallRecording>()
                        .eq(CallRecording::getId, id)
                        .eq(CallRecording::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("录音");
        }

        // DEMO版：设 aiSummary + aiKeywords 等模拟数据
        record.setAiSummary("[DEMO] 模拟AI摘要：客户咨询了产品相关信息，表达了初步意向。");
        record.setAiKeywords("[DEMO] 产品咨询,意向,报价");
        record.setAiSentiment("[DEMO] 中性偏积极");
        record.setViolationCheckStatus(VIOLATION_CHECK_STATUS_DONE);
        record.setHasViolation(0);
        record.setViolationDetail(null);
        record.setViolationLevel(null);
        callRecordingMapper.updateById(record);
    }

    private RecordingVO convertToVO(CallRecording record) {
        RecordingVO vo = new RecordingVO();
        BeanUtil.copyProperties(record, vo);
        return vo;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }
}
