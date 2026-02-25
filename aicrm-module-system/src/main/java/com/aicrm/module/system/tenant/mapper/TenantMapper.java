package com.aicrm.module.system.tenant.mapper;

import com.aicrm.module.system.tenant.entity.Tenant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户 Mapper
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
