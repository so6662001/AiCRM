package com.aicrm.module.crm.erp.sync.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.erp.sync.dto.SyncDetailVO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogDetailVO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogQueryDTO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogVO;
import com.aicrm.module.crm.erp.sync.entity.ErpSyncDetail;
import com.aicrm.module.crm.erp.sync.entity.ErpSyncLog;
import com.aicrm.module.crm.erp.sync.mapper.ErpSyncDetailMapper;
import com.aicrm.module.crm.erp.sync.mapper.ErpSyncLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ERP同步日志服务实现
 */
@Service
@RequiredArgsConstructor
public class ErpSyncLogServiceImpl implements ErpSyncLogService {

    private static final Map<Integer, String> SYNC_TYPE_LABELS = Map.of(
            1, "ERP→CRM",
            2, "CRM→ERP"
    );
    private static final Map<Integer, String> SYNC_MODE_LABELS = Map.of(
            1, "全量",
            2, "增量",
            3, "单条"
    );
    private static final Map<Integer, String> STATUS_LABELS = Map.of(
            1, "进行中",
            2, "成功",
            3, "部分失败",
            4, "失败"
    );

    private final ErpSyncLogMapper erpSyncLogMapper;
    private final ErpSyncDetailMapper erpSyncDetailMapper;

    @Override
    public PageResult<SyncLogVO> page(SyncLogQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<ErpSyncLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErpSyncLog::getTenantId, tenantId)
                .eq(query.getSyncType() != null, ErpSyncLog::getSyncType, query.getSyncType())
                .eq(query.getStatus() != null, ErpSyncLog::getStatus, query.getStatus())
                .eq(query.getBizType() != null, ErpSyncLog::getBizType, query.getBizType())
                .orderByDesc(ErpSyncLog::getCreatedTime);

        Page<ErpSyncLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ErpSyncLog> result = erpSyncLogMapper.selectPage(page, wrapper);

        List<SyncLogVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public SyncLogDetailVO getDetail(Long logId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        ErpSyncLog log = erpSyncLogMapper.selectOne(
                new LambdaQueryWrapper<ErpSyncLog>()
                        .eq(ErpSyncLog::getId, logId)
                        .eq(ErpSyncLog::getTenantId, tenantId)
        );
        if (log == null) {
            throw BizException.notFound("同步日志");
        }

        SyncLogDetailVO vo = new SyncLogDetailVO();
        BeanUtils.copyProperties(log, vo);
        vo.setSyncTypeLabel(SYNC_TYPE_LABELS.getOrDefault(log.getSyncType(), "未知"));
        vo.setSyncModeLabel(SYNC_MODE_LABELS.getOrDefault(log.getSyncMode(), "未知"));
        vo.setStatusLabel(STATUS_LABELS.getOrDefault(log.getStatus(), "未知"));

        var details = erpSyncDetailMapper.selectList(
                new LambdaQueryWrapper<ErpSyncDetail>()
                        .eq(ErpSyncDetail::getSyncLogId, logId)
                        .eq(ErpSyncDetail::getTenantId, tenantId)
                        .orderByAsc(ErpSyncDetail::getCreatedTime)
        );
        vo.setDetails(details.stream()
                .map(d -> {
                    SyncDetailVO dvo = new SyncDetailVO();
                    BeanUtils.copyProperties(d, dvo);
                    return dvo;
                })
                .toList());

        return vo;
    }

    @Override
    public Long createMockSync(Integer syncType, String bizType) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 1L; // DEMO fallback
        }

        LocalDateTime now = LocalDateTime.now();
        ErpSyncLog log = new ErpSyncLog();
        log.setTenantId(tenantId);
        log.setErpConfigId(null);
        log.setSyncType(syncType);
        log.setSyncMode(1);
        log.setBizType(bizType);
        log.setTotalCount(10);
        log.setSuccessCount(9);
        log.setFailCount(1);
        log.setSkipCount(0);
        log.setStatus(2); // 成功
        log.setErrorMessage(null);
        log.setStartTime(now.minusMinutes(5));
        log.setEndTime(now);
        log.setOperatedBy(TenantContext.getUserId());
        log.setCreatedTime(now);

        erpSyncLogMapper.insert(log);
        return log.getId();
    }

    private SyncLogVO convertToVO(ErpSyncLog entity) {
        SyncLogVO vo = new SyncLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setSyncTypeLabel(SYNC_TYPE_LABELS.getOrDefault(entity.getSyncType(), "未知"));
        vo.setSyncModeLabel(SYNC_MODE_LABELS.getOrDefault(entity.getSyncMode(), "未知"));
        vo.setStatusLabel(STATUS_LABELS.getOrDefault(entity.getStatus(), "未知"));
        return vo;
    }
}
