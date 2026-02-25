package com.aicrm.module.activity.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.activity.dto.*;

/**
 * 活动服务接口
 */
public interface ActivityService {

    PageResult<ActivityVO> page(ActivityQueryDTO query);

    ActivityVO getById(Long id);

    Long create(ActivityCreateDTO dto);

    void update(ActivityUpdateDTO dto);

    void delete(Long id);

    void publish(Long id);

    void cancel(Long id);

    void end(Long id);

    void closeRegistration(Long id);

    ActivityStatisticsVO getStatistics(Long id);
}
