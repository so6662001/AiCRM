package com.aicrm.module.system.role.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {

    /** 角色编码 */
    private String roleCode;
    /** 角色名称 */
    private String roleName;
    /** 角色类型 1系统内置 2自定义 */
    private Integer roleType;
    /** 数据范围 1全部 2本部门及以下 3本部门 4仅本人 */
    private Integer dataScope;
    /** 备注 */
    private String remark;
    /** 状态 */
    private Integer status;
}
