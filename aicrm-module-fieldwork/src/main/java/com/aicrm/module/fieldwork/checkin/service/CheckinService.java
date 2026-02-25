package com.aicrm.module.fieldwork.checkin.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.checkin.dto.CheckinCreateDTO;
import com.aicrm.module.fieldwork.checkin.dto.CheckinQueryDTO;
import com.aicrm.module.fieldwork.checkin.dto.CheckinVO;

import java.util.List;

/**
 * 签到服务
 */
public interface CheckinService {

    PageResult<CheckinVO> page(CheckinQueryDTO query);

    CheckinVO getById(Long id);

    List<CheckinVO> getToday(Long userId);

    Long create(CheckinCreateDTO dto);
}
