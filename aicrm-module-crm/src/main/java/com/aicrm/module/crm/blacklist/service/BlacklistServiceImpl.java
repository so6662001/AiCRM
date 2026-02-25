package com.aicrm.module.crm.blacklist.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.blacklist.dto.BlacklistCreateDTO;
import com.aicrm.module.crm.blacklist.dto.BlacklistQueryDTO;
import com.aicrm.module.crm.blacklist.dto.BlacklistVO;
import com.aicrm.module.crm.blacklist.entity.CustomerBlacklist;
import com.aicrm.module.crm.blacklist.mapper.CustomerBlacklistMapper;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 黑名单服务实现
 */
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_RELEASED = 2;
    private static final int LIFECYCLE_BLACKLIST = 8;

    private static final Map<Integer, String> BLACKLIST_TYPE_LABELS = Map.of(
            1, "欺诈",
            2, "恶意投诉",
            3, "空壳",
            4, "竞对",
            5, "其他"
    );

    private final CustomerBlacklistMapper blacklistMapper;
    private final CustomerMapper customerMapper;

    @Override
    public PageResult<BlacklistVO> page(BlacklistQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<CustomerBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerBlacklist::getTenantId, tenantId);
        if (query.getStatus() != null) {
            wrapper.eq(CustomerBlacklist::getStatus, query.getStatus());
        }
        if (query.getBlacklistType() != null) {
            wrapper.eq(CustomerBlacklist::getBlacklistType, query.getBlacklistType());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(CustomerBlacklist::getCompanyName, query.getKeyword())
                    .or()
                    .like(CustomerBlacklist::getContactPhone, query.getKeyword()));
        }
        wrapper.orderByDesc(CustomerBlacklist::getCreatedTime);

        Page<CustomerBlacklist> page = blacklistMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<BlacklistVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addToBlacklist(BlacklistCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null) {
            throw new BizException("租户上下文缺失");
        }

        CustomerBlacklist entity = new CustomerBlacklist();
        entity.setCustomerId(dto.getCustomerId());
        entity.setCompanyName(dto.getCompanyName());
        entity.setCreditCode(dto.getCreditCode());
        entity.setContactPhone(dto.getContactPhone());
        entity.setBlacklistType(dto.getBlacklistType());
        entity.setReason(dto.getReason());
        entity.setStatus(STATUS_ACTIVE);
        entity.setOperatedBy(userId);
        if (dto.getEvidenceUrls() != null && !dto.getEvidenceUrls().isEmpty()) {
            entity.setEvidenceUrls(JSONUtil.toJsonStr(dto.getEvidenceUrls()));
        }

        if (dto.getCustomerId() != null) {
            Customer customer = customerMapper.selectById(dto.getCustomerId());
            if (customer != null) {
                if (entity.getCompanyName() == null) {
                    entity.setCompanyName(customer.getCustomerName());
                }
                customerMapper.update(null, new LambdaUpdateWrapper<Customer>()
                        .eq(Customer::getId, dto.getCustomerId())
                        .set(Customer::getLifecycleStage, LIFECYCLE_BLACKLIST));
            }
        }

        blacklistMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(Long id, String releaseReason) {
        Long userId = TenantContext.getUserId();
        CustomerBlacklist entity = blacklistMapper.selectById(id);
        if (entity == null) {
            throw new BizException("黑名单记录不存在");
        }
        if (!Objects.equals(STATUS_ACTIVE, entity.getStatus())) {
            throw new BizException("该记录已解除");
        }

        blacklistMapper.update(null, new LambdaUpdateWrapper<CustomerBlacklist>()
                .eq(CustomerBlacklist::getId, id)
                .set(CustomerBlacklist::getStatus, STATUS_RELEASED)
                .set(CustomerBlacklist::getReleaseBy, userId)
                .set(CustomerBlacklist::getReleaseTime, LocalDateTime.now())
                .set(CustomerBlacklist::getReleaseReason, releaseReason));
    }

    @Override
    public boolean isBlacklisted(String companyName, String phone) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return false;
        }

        LambdaQueryWrapper<CustomerBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerBlacklist::getTenantId, tenantId)
                .eq(CustomerBlacklist::getStatus, STATUS_ACTIVE);
        if (StrUtil.isNotBlank(companyName) || StrUtil.isNotBlank(phone)) {
            wrapper.and(w -> {
                if (StrUtil.isNotBlank(companyName)) {
                    w.eq(CustomerBlacklist::getCompanyName, companyName);
                }
                if (StrUtil.isNotBlank(phone)) {
                    if (StrUtil.isNotBlank(companyName)) {
                        w.or();
                    }
                    w.eq(CustomerBlacklist::getContactPhone, phone);
                }
            });
        } else {
            return false;
        }
        return blacklistMapper.selectCount(wrapper) > 0;
    }

    private BlacklistVO toVO(CustomerBlacklist entity) {
        BlacklistVO vo = new BlacklistVO();
        vo.setId(entity.getId());
        vo.setCustomerId(entity.getCustomerId());
        vo.setCompanyName(entity.getCompanyName());
        vo.setCreditCode(entity.getCreditCode());
        vo.setContactPhone(entity.getContactPhone());
        vo.setBlacklistType(entity.getBlacklistType());
        vo.setBlacklistTypeLabel(BLACKLIST_TYPE_LABELS.getOrDefault(entity.getBlacklistType(), "未知"));
        vo.setReason(entity.getReason());
        vo.setEvidenceUrls(entity.getEvidenceUrls());
        vo.setStatus(entity.getStatus());
        vo.setOperatedBy(entity.getOperatedBy());
        vo.setReleaseBy(entity.getReleaseBy());
        vo.setReleaseTime(entity.getReleaseTime());
        vo.setReleaseReason(entity.getReleaseReason());
        vo.setCreatedTime(entity.getCreatedTime());
        if (entity.getCustomerId() != null) {
            Customer customer = customerMapper.selectById(entity.getCustomerId());
            vo.setCustomerName(customer != null ? customer.getCustomerName() : null);
        }
        return vo;
    }
}
