package com.aicrm.module.crm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.io.Serializable;

/**
 * 商机阶段配置实体（用于阶段名称查询）
 */
@Data
@TableName("opportunity_stage_config")
public class OpportunityStageConfig implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String stageName;
    private String stageCode;
    private Integer winRate;
    private Integer sortOrder;
    private Integer isWon;
    private Integer isLost;
    private Integer status;
    @TableLogic
    private Integer deleted;
}
