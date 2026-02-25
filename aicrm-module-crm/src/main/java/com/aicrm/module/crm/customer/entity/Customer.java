package com.aicrm.module.crm.customer.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {

    /** 客户编号 */
    private String customerNo;
    /** 客户名称 */
    private String customerName;
    /** 简称 */
    private String shortName;
    /** 客户类型 1企业/2个人 */
    private Integer customerType;
    /** 行业 */
    private String industry;
    /** 规模 */
    private String scale;
    /** 官网 */
    private String website;
    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区县 */
    private String district;
    /** 详细地址 */
    private String address;
    /** 经度 */
    private BigDecimal longitude;
    /** 纬度 */
    private BigDecimal latitude;
    /** 生命周期阶段 1潜在/2意向/3成交/4活跃/5VIP/6流失/7无效/8黑名单 */
    private Integer lifecycleStage;
    /** 客户等级 A/B/C/D */
    private String level;
    /** 负责人用户ID */
    private Long ownerUserId;
    /** 负责人组织ID */
    private Long ownerOrgId;
    /** 来源线索ID */
    private Long sourceLeadId;
    /** 来源 */
    private String source;
    /** 最后跟进时间 */
    private LocalDateTime lastFollowTime;
    /** 跟进次数 */
    private Integer followCount;
    /** 下次跟进时间 */
    private LocalDateTime nextFollowTime;
    /** 预计采购日期 */
    private LocalDate expectedPurchaseDate;
    /** 成交金额 */
    private BigDecimal dealAmount;
    /** 成交次数 */
    private Integer dealCount;
    /** ERP客户ID */
    private String erpCustomerId;
    /** ERP同步状态 0未同步/1已同步/2同步中/3同步失败 */
    private Integer erpSyncStatus;
    /** ERP最后同步时间 */
    private LocalDateTime erpLastSyncTime;
    /** 审批状态 */
    private Integer approvalStatus;
    /** 备注 */
    private String remark;
    /** 标签 */
    private String tags;
}
