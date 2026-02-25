package com.aicrm.module.social.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 好友实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friend")
public class Friend extends BaseEntity {

    /** 销售员用户ID */
    private Long userId;
    /** 好友姓名 */
    private String friendName;
    /** 好友手机号 */
    private String friendPhone;
    /** 好友头像URL */
    private String friendAvatarUrl;
    /** 好友公司 */
    private String friendCompany;
    /** 好友职位 */
    private String friendPosition;
    /** 好友类型 1平台/2企微/3双渠道 */
    private Integer friendType;
    /** 来源 1活动扫码/2企微同步/3手动添加/4线索导入/5名片扫描 */
    private Integer source;
    /** 来源活动ID */
    private Long sourceActivityId;
    /** 企微外部联系人ID */
    private String wechatExternalUserid;
    /** 企微UnionID */
    private String wechatUnionid;
    /** 企微昵称 */
    private String wechatNickname;
    /** 关联客户ID */
    private Long customerId;
    /** 关联联系人ID */
    private Long contactId;
    /** 关联线索ID */
    private Long leadId;
    /** 状态 1正常/2已删除/3已被删/4已拉黑 */
    private Integer status;
    /** 标签 */
    private String tags;
    /** 备注 */
    private String remark;
    /** 添加时间 */
    private LocalDateTime addTime;
    /** 最后聊天时间 */
    private LocalDateTime lastChatTime;
}
