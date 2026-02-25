package com.aicrm.module.system.wechat.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.system.wechat.dto.WechatWorkConfigDTO;
import com.aicrm.module.system.wechat.dto.WechatWorkConfigVO;
import com.aicrm.module.system.wechat.entity.TenantWechatWorkConfig;
import com.aicrm.module.system.wechat.mapper.TenantWechatWorkConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 企业微信配置服务实现 - testConnection DEMO版直接返回true
 */
@Service
@RequiredArgsConstructor
public class WechatWorkConfigServiceImpl implements WechatWorkConfigService {

    private static final int STATUS_ACTIVE = 1;

    private final TenantWechatWorkConfigMapper configMapper;

    @Override
    public WechatWorkConfigVO getConfig() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return null;
        }

        TenantWechatWorkConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<TenantWechatWorkConfig>()
                        .eq(TenantWechatWorkConfig::getTenantId, tenantId)
                        .last("LIMIT 1"));
        return config != null ? toVO(config) : null;
    }

    @Override
    public Long saveConfig(WechatWorkConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null) {
            throw new BizException("租户上下文缺失");
        }

        TenantWechatWorkConfig entity = new TenantWechatWorkConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus(STATUS_ACTIVE);
        configMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateConfig(Long id, WechatWorkConfigDTO dto) {
        TenantWechatWorkConfig entity = configMapper.selectById(id);
        if (entity == null) {
            throw new BizException("配置不存在");
        }
        BeanUtils.copyProperties(dto, entity, "id", "tenantId", "createdBy", "createdTime");
        configMapper.updateById(entity);
    }

    @Override
    public boolean testConnection(Long id) {
        return true;
    }

    private WechatWorkConfigVO toVO(TenantWechatWorkConfig entity) {
        WechatWorkConfigVO vo = new WechatWorkConfigVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getSecret() != null && entity.getSecret().length() > 4) {
            vo.setSecret(maskSecret(entity.getSecret()));
        }
        if (entity.getContactSecret() != null && entity.getContactSecret().length() > 4) {
            vo.setContactSecret(maskSecret(entity.getContactSecret()));
        }
        if (entity.getCustomerSecret() != null && entity.getCustomerSecret().length() > 4) {
            vo.setCustomerSecret(maskSecret(entity.getCustomerSecret()));
        }
        if (entity.getCallbackAesKey() != null && entity.getCallbackAesKey().length() > 4) {
            vo.setCallbackAesKey(maskSecret(entity.getCallbackAesKey()));
        }
        return vo;
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 4) {
            return "****";
        }
        return secret.substring(0, 2) + "****" + secret.substring(secret.length() - 2);
    }
}
