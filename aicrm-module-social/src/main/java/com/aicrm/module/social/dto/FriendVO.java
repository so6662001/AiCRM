package com.aicrm.module.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友视图VO
 */
@Data
@Schema(description = "好友视图")
public class FriendVO {

    private Long id;
    private Long tenantId;
    private Long userId;
    private String userName;
    private String friendName;
    private String friendPhone;
    private String friendAvatarUrl;
    private String friendCompany;
    private String friendPosition;
    private Integer friendType;
    private String friendTypeLabel;
    private Integer source;
    private String sourceLabel;
    private Long sourceActivityId;
    private String sourceActivityName;
    private String wechatExternalUserid;
    private String wechatUnionid;
    private String wechatNickname;
    private Long customerId;
    private String customerName;
    private Boolean isCustomer;
    private Long contactId;
    private Long leadId;
    private Integer status;
    private String tags;
    private String remark;
    private LocalDateTime addTime;
    private LocalDateTime lastChatTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
