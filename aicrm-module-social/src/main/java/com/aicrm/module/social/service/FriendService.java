package com.aicrm.module.social.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.social.dto.*;

/**
 * 好友服务接口
 */
public interface FriendService {

    PageResult<FriendVO> page(FriendQueryDTO query);

    FriendVO getById(Long id);

    Long create(FriendCreateDTO dto);

    void update(FriendUpdateDTO dto);

    void delete(Long id);

    void linkCustomer(Long friendId, Long customerId);

    void convertToLead(Long friendId);

    FriendStatisticsVO statistics();
}
