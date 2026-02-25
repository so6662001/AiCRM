package com.aicrm.module.crm.lead.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 线索实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lead")
public class Lead extends BaseEntity {

    /**
     * 线索编号
     */
    private String leadNo;
    /**
     * 联系人姓名
     */
    private String contactName;
    /**
     * 联系人电话
     */
    private String contactPhone;
    /**
     * 联系人邮箱
     */
    private String contactEmail;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 职位
     */
    private String position;
    /**
     * 来源
     */
    private String source;
    /**
     * 来源明细
     */
    private String sourceDetail;
    /**
     * 意向等级 A/B/C/D
     */
    private String intentionLevel;
    /**
     * 线索评分
     */
    private Integer leadScore;
    /**
     * 状态 0待分配/1已分配/2跟进中/3已转化/4已退回/5无效
     */
    private Integer status;
    /**
     * 负责人用户ID
     */
    private Long ownerUserId;
    /**
     * 负责人组织ID
     */
    private Long ownerOrgId;
    /**
     * 分配人用户ID
     */
    private Long assignUserId;
    /**
     * 分配时间
     */
    private LocalDateTime assignTime;
    /**
     * 首次跟进时间
     */
    private LocalDateTime firstFollowTime;
    /**
     * 最后跟进时间
     */
    private LocalDateTime lastFollowTime;
    /**
     * 跟进次数
     */
    private Integer followCount;
    /**
     * 转化时间
     */
    private LocalDateTime convertTime;
    /**
     * 转化客户ID
     */
    private Long convertCustomerId;
    /**
     * 退回原因
     */
    private String returnReason;
    /**
     * 是否在公海池 0否/1是
     */
    private Integer inPool;
    /**
     * 进入公海池时间
     */
    private LocalDateTime poolEnterTime;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 行业
     */
    private String industry;
    /**
     * 备注
     */
    private String remark;
}
