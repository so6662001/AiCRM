package com.aicrm.module.system.org.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织架构实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("organization")
public class Organization extends BaseEntity {

    /** 父级ID */
    private Long parentId;
    /** 组织名称 */
    private String orgName;
    /** 组织编码 */
    private String orgCode;
    /** 组织类型 1公司/2部门/3团队 */
    private Integer orgType;
    /** 组织路径 */
    private String orgPath;
    /** 负责人用户ID */
    private Long leaderUserId;
    /** 排序 */
    private Integer sortOrder;
    /** 状态 */
    private Integer status;
}
