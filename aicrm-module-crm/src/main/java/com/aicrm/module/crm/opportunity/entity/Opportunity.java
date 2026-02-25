package com.aicrm.module.crm.opportunity.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商机实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("opportunity")
public class Opportunity extends BaseEntity {

    /** 商机编号 */
    private String opportunityNo;
    /** 商机名称 */
    private String opportunityName;
    /** 关联客户ID */
    private Long customerId;
    /** 关联联系人ID */
    private Long contactId;
    /** 当前阶段ID */
    private Long stageId;
    /** 预计金额 */
    private BigDecimal expectedAmount;
    /** 实际成交金额 */
    private BigDecimal actualAmount;
    /** 赢率（%） */
    private Integer winRate;
    /** 预计成交日期 */
    private LocalDate expectedCloseDate;
    /** 实际成交日期 */
    private LocalDate actualCloseDate;
    /** 状态 1进行中/2赢单/3输单/4无效 */
    private Integer status;
    /** 输单原因 */
    private String lossReason;
    /** 竞争对手 */
    private String competitor;
    /** 负责人用户ID */
    private Long ownerUserId;
    /** 负责人组织ID */
    private Long ownerOrgId;
    /** 最后跟进时间 */
    private LocalDateTime lastFollowTime;
    /** 跟进次数 */
    private Integer followCount;
    /** 备注 */
    private String remark;
}
