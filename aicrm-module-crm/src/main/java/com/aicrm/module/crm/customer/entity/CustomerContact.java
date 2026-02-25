package com.aicrm.module.crm.customer.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户联系人实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_contact")
public class CustomerContact extends BaseEntity {

    /** 客户ID */
    private Long customerId;
    /** 联系人姓名 */
    private String contactName;
    /** 性别 0未知/1男/2女 */
    private Integer gender;
    /** 职位 */
    private String position;
    /** 部门 */
    private String department;
    /** 手机 */
    private String phone;
    /** 座机 */
    private String telephone;
    /** 邮箱 */
    private String email;
    /** 微信 */
    private String wechat;
    /** 是否主联系人 0否/1是 */
    private Integer isPrimary;
    /** 是否决策人 0否/1是 */
    private Integer isDecisionMaker;
    /** 备注 */
    private String remark;
}
