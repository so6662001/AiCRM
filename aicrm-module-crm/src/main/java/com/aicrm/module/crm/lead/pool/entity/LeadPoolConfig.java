package com.aicrm.module.crm.lead.pool.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公海池配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lead_pool_config")
public class LeadPoolConfig extends BaseEntity {

    /** 公海池名称 */
    private String poolName;
    /** 回收天数 默认7 */
    private Integer recycleDays;
    /** 最大持有数量 默认50 */
    private Integer maxHoldCount;
    /** 每日领取上限 默认5 */
    private Integer dailyPickLimit;
    /** 可见组织ID列表，逗号分隔 */
    private String visibleOrgIds;
    /** 状态 默认1 */
    private Integer status;
}
