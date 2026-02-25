package com.aicrm.module.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 好友统计VO
 */
@Data
@Schema(description = "好友统计数据")
public class FriendStatisticsVO {

    @Schema(description = "好友总数")
    private Integer totalFriends;

    @Schema(description = "平台好友数")
    private Integer platformFriends;

    @Schema(description = "企微好友数")
    private Integer wechatFriends;

    @Schema(description = "双渠道好友数")
    private Integer dualChannelFriends;

    @Schema(description = "已转化客户数")
    private Integer convertedToCustomer;

    @Schema(description = "本月新增好友数")
    private Integer newThisMonth;
}
