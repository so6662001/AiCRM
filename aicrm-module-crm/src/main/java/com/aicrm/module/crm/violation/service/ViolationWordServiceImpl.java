package com.aicrm.module.crm.violation.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.violation.dto.ViolationWordDTO;
import com.aicrm.module.crm.violation.dto.ViolationWordVO;
import com.aicrm.module.crm.violation.entity.ViolationWord;
import com.aicrm.module.crm.violation.mapper.ViolationWordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 违规词库服务实现
 */
@Service
@RequiredArgsConstructor
public class ViolationWordServiceImpl implements ViolationWordService {

    private static final int STATUS_ACTIVE = 1;

    private static final Map<Integer, String> LEVEL_LABELS = Map.of(
            1, "低",
            2, "中",
            3, "高"
    );

    private final ViolationWordMapper violationWordMapper;

    @Override
    public List<ViolationWordVO> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<ViolationWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViolationWord::getTenantId, tenantId)
                .orderByDesc(ViolationWord::getCreatedTime);

        List<ViolationWord> records = violationWordMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ViolationWordDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        ViolationWord entity = new ViolationWord();
        BeanUtil.copyProperties(dto, entity);
        entity.setStatus(STATUS_ACTIVE);

        violationWordMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ViolationWordDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        ViolationWord exist = violationWordMapper.selectOne(
                new LambdaQueryWrapper<ViolationWord>()
                        .eq(ViolationWord::getId, id)
                        .eq(ViolationWord::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("违规词");
        }

        ViolationWord entity = new ViolationWord();
        BeanUtil.copyProperties(dto, entity);
        entity.setId(id);
        violationWordMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        ViolationWord exist = violationWordMapper.selectOne(
                new LambdaQueryWrapper<ViolationWord>()
                        .eq(ViolationWord::getId, id)
                        .eq(ViolationWord::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("违规词");
        }

        violationWordMapper.deleteById(id);
    }

    private ViolationWordVO convertToVO(ViolationWord entity) {
        ViolationWordVO vo = new ViolationWordVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setLevelLabel(LEVEL_LABELS.getOrDefault(entity.getLevel(), "未知"));
        return vo;
    }
}
