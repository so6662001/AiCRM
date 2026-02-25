package com.aicrm.module.fieldwork.visit.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.fieldwork.visit.dto.VisitCreateDTO;
import com.aicrm.module.fieldwork.visit.dto.VisitQueryDTO;
import com.aicrm.module.fieldwork.visit.dto.VisitVO;
import com.aicrm.module.fieldwork.visit.entity.VisitRecord;
import com.aicrm.module.fieldwork.visit.mapper.VisitRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 拜访服务实现
 */
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private static final int STATUS_PLANNED = 1;
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_CANCELLED = 4;

    private final VisitRecordMapper visitRecordMapper;
    private final CustomerMapper customerMapper;

    @Override
    public PageResult<VisitVO> page(VisitQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<VisitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitRecord::getTenantId, tenantId)
                .eq(query.getCustomerId() != null, VisitRecord::getCustomerId, query.getCustomerId())
                .eq(query.getVisitType() != null, VisitRecord::getVisitType, query.getVisitType())
                .eq(query.getStatus() != null, VisitRecord::getStatus, query.getStatus())
                .eq(query.getVisitorUserId() != null, VisitRecord::getVisitorUserId, query.getVisitorUserId())
                .ge(query.getVisitTimeStart() != null, VisitRecord::getVisitTime, query.getVisitTimeStart())
                .le(query.getVisitTimeEnd() != null, VisitRecord::getVisitTime, query.getVisitTimeEnd())
                .orderByDesc(VisitRecord::getVisitTime);

        Page<VisitRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<VisitRecord> result = visitRecordMapper.selectPage(page, wrapper);

        List<VisitVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public VisitVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        VisitRecord record = visitRecordMapper.selectOne(
                new LambdaQueryWrapper<VisitRecord>()
                        .eq(VisitRecord::getId, id)
                        .eq(VisitRecord::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("拜访记录");
        }
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(VisitCreateDTO dto) {
        VisitRecord record = new VisitRecord();
        BeanUtil.copyProperties(dto, record);
        record.setVisitNo(generateVisitNo());
        record.setStatus(STATUS_PLANNED);
        record.setVisitorUserId(TenantContext.getUserId());

        visitRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, String visitResult) {
        Long tenantId = TenantContext.getTenantId();
        VisitRecord record = visitRecordMapper.selectOne(
                new LambdaQueryWrapper<VisitRecord>()
                        .eq(VisitRecord::getId, id)
                        .eq(VisitRecord::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("拜访记录");
        }

        record.setStatus(STATUS_COMPLETED);
        record.setVisitResult(visitResult);
        record.setVisitEndTime(LocalDateTime.now());
        visitRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Long tenantId = TenantContext.getTenantId();
        VisitRecord record = visitRecordMapper.selectOne(
                new LambdaQueryWrapper<VisitRecord>()
                        .eq(VisitRecord::getId, id)
                        .eq(VisitRecord::getTenantId, tenantId)
        );
        if (record == null) {
            throw BizException.notFound("拜访记录");
        }

        record.setStatus(STATUS_CANCELLED);
        visitRecordMapper.updateById(record);
    }

    private VisitVO convertToVO(VisitRecord record) {
        VisitVO vo = new VisitVO();
        BeanUtil.copyProperties(record, vo);
        vo.setCustomerName(resolveCustomerName(record.getCustomerId()));
        vo.setVisitorUserName(resolveUserName(record.getVisitorUserId()));
        return vo;
    }

    private String resolveCustomerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = customerMapper.selectById(customerId);
        return customer != null ? customer.getCustomerName() : null;
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名
        return null;
    }

    private String generateVisitNo() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "VT" + datePrefix;

        LambdaQueryWrapper<VisitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitRecord::getTenantId, TenantContext.getTenantId())
                .likeRight(VisitRecord::getVisitNo, prefix)
                .orderByDesc(VisitRecord::getVisitNo);

        Page<VisitRecord> p = visitRecordMapper.selectPage(new Page<>(1, 1), wrapper);
        VisitRecord last = p.getRecords().isEmpty() ? null : p.getRecords().get(0);
        int seq = 1;
        if (last != null && last.getVisitNo() != null && last.getVisitNo().length() >= prefix.length() + 4) {
            try {
                seq = Integer.parseInt(last.getVisitNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }
}
