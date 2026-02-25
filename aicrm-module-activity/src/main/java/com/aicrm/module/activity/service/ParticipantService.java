package com.aicrm.module.activity.service;

import com.aicrm.common.page.PageQuery;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.activity.dto.ParticipantCreateDTO;
import com.aicrm.module.activity.dto.ParticipantVO;

/**
 * 参与人服务接口
 */
public interface ParticipantService {

    PageResult<ParticipantVO> page(Long activityId, PageQuery query);

    Long add(Long activityId, ParticipantCreateDTO dto);

    void approve(Long activityId, Long participantId);

    void reject(Long activityId, Long participantId, String reason);

    void checkin(Long activityId, Long participantId);
}
