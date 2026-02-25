package com.aicrm.module.social.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.activity.entity.Activity;
import com.aicrm.module.activity.mapper.ActivityMapper;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.aicrm.module.social.dto.*;
import com.aicrm.module.social.entity.Friend;
import com.aicrm.module.social.mapper.FriendMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 好友服务实现
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private static final int STATUS_NORMAL = 1;
    private static final int FRIEND_TYPE_PLATFORM = 1;
    private static final int FRIEND_TYPE_WECHAT = 2;
    private static final int FRIEND_TYPE_DUAL_CHANNEL = 3;

    private static final String[] FRIEND_TYPE_LABELS = {"", "平台", "企微", "双渠道"};
    private static final String[] SOURCE_LABELS = {"", "活动扫码", "企微同步", "手动添加", "线索导入", "名片扫描"};

    private final FriendMapper friendMapper;
    private final CustomerMapper customerMapper;
    private final ActivityMapper activityMapper;

    @Override
    public PageResult<FriendVO> page(FriendQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getTenantId, tenantId)
                .eq(query.getUserId() != null, Friend::getUserId, query.getUserId())
                .eq(query.getFriendType() != null, Friend::getFriendType, query.getFriendType())
                .eq(query.getSource() != null, Friend::getSource, query.getSource())
                .eq(query.getSourceActivityId() != null, Friend::getSourceActivityId, query.getSourceActivityId())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Friend::getFriendName, query.getKeyword())
                        .or().like(Friend::getFriendPhone, query.getKeyword())
                        .or().like(Friend::getFriendCompany, query.getKeyword()))
                .orderByDesc(Friend::getAddTime);

        if (query.getHasCustomer() != null) {
            if (query.getHasCustomer()) {
                wrapper.isNotNull(Friend::getCustomerId);
            } else {
                wrapper.isNull(Friend::getCustomerId);
            }
        }

        Page<Friend> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Friend> result = friendMapper.selectPage(page, wrapper);

        List<FriendVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public FriendVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getId, id)
                        .eq(Friend::getTenantId, tenantId)
        );
        if (friend == null) {
            throw BizException.notFound("好友");
        }
        return convertToVO(friend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(FriendCreateDTO dto) {
        Friend friend = new Friend();
        BeanUtil.copyProperties(dto, friend);
        friend.setUserId(TenantContext.getUserId());
        friend.setAddTime(LocalDateTime.now());
        friend.setStatus(STATUS_NORMAL);

        friendMapper.insert(friend);
        return friend.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FriendUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getId, dto.getId())
                        .eq(Friend::getTenantId, tenantId)
        );
        if (friend == null) {
            throw BizException.notFound("好友");
        }

        if (StringUtils.hasText(dto.getFriendName())) {
            friend.setFriendName(dto.getFriendName());
        }
        if (dto.getFriendCompany() != null) {
            friend.setFriendCompany(dto.getFriendCompany());
        }
        if (dto.getFriendPosition() != null) {
            friend.setFriendPosition(dto.getFriendPosition());
        }
        if (dto.getTags() != null) {
            friend.setTags(dto.getTags());
        }
        if (dto.getRemark() != null) {
            friend.setRemark(dto.getRemark());
        }

        friendMapper.updateById(friend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getId, id)
                        .eq(Friend::getTenantId, tenantId)
        );
        if (friend == null) {
            throw BizException.notFound("好友");
        }

        friendMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void linkCustomer(Long friendId, Long customerId) {
        if (customerId == null) {
            throw new BizException("客户ID不能为空");
        }
        Long tenantId = TenantContext.getTenantId();
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getId, friendId)
                        .eq(Friend::getTenantId, tenantId)
        );
        if (friend == null) {
            throw BizException.notFound("好友");
        }

        friend.setCustomerId(customerId);
        friendMapper.updateById(friend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void convertToLead(Long friendId) {
        Long tenantId = TenantContext.getTenantId();
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getId, friendId)
                        .eq(Friend::getTenantId, tenantId)
        );
        if (friend == null) {
            throw BizException.notFound("好友");
        }

        friend.setLeadId(1L);
        friendMapper.updateById(friend);
    }

    @Override
    public FriendStatisticsVO statistics() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            FriendStatisticsVO vo = new FriendStatisticsVO();
            vo.setTotalFriends(0);
            vo.setPlatformFriends(0);
            vo.setWechatFriends(0);
            vo.setDualChannelFriends(0);
            vo.setConvertedToCustomer(0);
            vo.setNewThisMonth(0);
            return vo;
        }

        LambdaQueryWrapper<Friend> baseWrapper = new LambdaQueryWrapper<Friend>()
                .eq(Friend::getTenantId, tenantId);

        long totalFriends = friendMapper.selectCount(baseWrapper);
        long platformFriends = friendMapper.selectCount(new LambdaQueryWrapper<Friend>().eq(Friend::getTenantId, tenantId).eq(Friend::getFriendType, FRIEND_TYPE_PLATFORM));
        long wechatFriends = friendMapper.selectCount(new LambdaQueryWrapper<Friend>().eq(Friend::getTenantId, tenantId).eq(Friend::getFriendType, FRIEND_TYPE_WECHAT));
        long dualChannelFriends = friendMapper.selectCount(new LambdaQueryWrapper<Friend>().eq(Friend::getTenantId, tenantId).eq(Friend::getFriendType, FRIEND_TYPE_DUAL_CHANNEL));
        long convertedToCustomer = friendMapper.selectCount(new LambdaQueryWrapper<Friend>().eq(Friend::getTenantId, tenantId).isNotNull(Friend::getCustomerId));

        YearMonth thisMonth = YearMonth.now();
        LocalDateTime startOfMonth = thisMonth.atDay(1).atStartOfDay();
        long newThisMonth = friendMapper.selectCount(new LambdaQueryWrapper<Friend>().eq(Friend::getTenantId, tenantId).ge(Friend::getAddTime, startOfMonth));

        FriendStatisticsVO vo = new FriendStatisticsVO();
        vo.setTotalFriends((int) totalFriends);
        vo.setPlatformFriends((int) platformFriends);
        vo.setWechatFriends((int) wechatFriends);
        vo.setDualChannelFriends((int) dualChannelFriends);
        vo.setConvertedToCustomer((int) convertedToCustomer);
        vo.setNewThisMonth((int) newThisMonth);
        return vo;
    }

    private FriendVO convertToVO(Friend friend) {
        FriendVO vo = new FriendVO();
        BeanUtil.copyProperties(friend, vo);
        vo.setUserName(resolveUserName(friend.getUserId()));
        vo.setFriendTypeLabel(getFriendTypeLabel(friend.getFriendType()));
        vo.setSourceLabel(getSourceLabel(friend.getSource()));
        vo.setSourceActivityName(resolveActivityName(friend.getSourceActivityId()));
        vo.setIsCustomer(friend.getCustomerId() != null);
        vo.setCustomerName(resolveCustomerName(friend.getCustomerId()));
        return vo;
    }

    private String getFriendTypeLabel(Integer friendType) {
        if (friendType == null || friendType < 1 || friendType >= FRIEND_TYPE_LABELS.length) {
            return null;
        }
        return FRIEND_TYPE_LABELS[friendType];
    }

    private String getSourceLabel(Integer source) {
        if (source == null || source < 1 || source >= SOURCE_LABELS.length) {
            return null;
        }
        return SOURCE_LABELS[source];
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名
        return null;
    }

    private String resolveCustomerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = customerMapper.selectById(customerId);
        return customer != null ? customer.getCustomerName() : null;
    }

    private String resolveActivityName(Long activityId) {
        if (activityId == null) {
            return null;
        }
        Activity activity = activityMapper.selectById(activityId);
        return activity != null ? activity.getActivityName() : null;
    }
}
