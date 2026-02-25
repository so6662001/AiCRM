package com.aicrm.module.crm.blacklist.mapper;

import com.aicrm.module.crm.blacklist.entity.CustomerBlacklist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户黑名单Mapper
 */
@Mapper
public interface CustomerBlacklistMapper extends BaseMapper<CustomerBlacklist> {
}
