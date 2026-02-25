package com.aicrm.module.crm.violation.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 违规词库实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violation_word")
public class ViolationWord extends BaseEntity {

    /** 违规词 */
    private String word;
    /** 分类 虚假承诺/过度宣传/恶意攻击/其他 */
    private String category;
    /** 等级 1低/2中/3高 */
    private Integer level;
    /** 状态 默认1 */
    private Integer status;
}
