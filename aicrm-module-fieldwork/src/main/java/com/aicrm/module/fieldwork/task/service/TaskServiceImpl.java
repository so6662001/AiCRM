package com.aicrm.module.fieldwork.task.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.fieldwork.task.dto.TaskCreateDTO;
import com.aicrm.module.fieldwork.task.dto.TaskQueryDTO;
import com.aicrm.module.fieldwork.task.dto.TaskUpdateDTO;
import com.aicrm.module.fieldwork.task.dto.TaskVO;
import com.aicrm.module.fieldwork.task.entity.WorkTask;
import com.aicrm.module.fieldwork.task.mapper.WorkTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_COMPLETED = 2;

    private final WorkTaskMapper workTaskMapper;

    @Override
    public PageResult<TaskVO> page(TaskQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<WorkTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkTask::getTenantId, tenantId)
                .eq(query.getStatus() != null, WorkTask::getStatus, query.getStatus())
                .eq(query.getAssigneeUserId() != null, WorkTask::getAssigneeUserId, query.getAssigneeUserId())
                .eq(query.getTaskType() != null, WorkTask::getTaskType, query.getTaskType())
                .eq(query.getPriority() != null, WorkTask::getPriority, query.getPriority())
                .le(query.getPlanEndTimeBefore() != null, WorkTask::getPlanEndTime, query.getPlanEndTimeBefore())
                .orderByDesc(WorkTask::getCreatedTime);

        Page<WorkTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<WorkTask> result = workTaskMapper.selectPage(page, wrapper);

        List<TaskVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public TaskVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        WorkTask task = workTaskMapper.selectOne(
                new LambdaQueryWrapper<WorkTask>()
                        .eq(WorkTask::getId, id)
                        .eq(WorkTask::getTenantId, tenantId)
        );
        if (task == null) {
            throw BizException.notFound("任务");
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskCreateDTO dto) {
        WorkTask task = new WorkTask();
        BeanUtil.copyProperties(dto, task);
        task.setTaskNo(generateTaskNo());
        task.setStatus(STATUS_PENDING);

        workTaskMapper.insert(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TaskUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        WorkTask existing = workTaskMapper.selectOne(
                new LambdaQueryWrapper<WorkTask>()
                        .eq(WorkTask::getId, dto.getId())
                        .eq(WorkTask::getTenantId, tenantId)
        );
        if (existing == null) {
            throw BizException.notFound("任务");
        }

        if (dto.getTaskTitle() != null) {
            existing.setTaskTitle(dto.getTaskTitle());
        }
        if (dto.getTaskContent() != null) {
            existing.setTaskContent(dto.getTaskContent());
        }
        if (dto.getTaskType() != null) {
            existing.setTaskType(dto.getTaskType());
        }
        if (dto.getPriority() != null) {
            existing.setPriority(dto.getPriority());
        }
        if (dto.getPlanStartTime() != null) {
            existing.setPlanStartTime(dto.getPlanStartTime());
        }
        if (dto.getPlanEndTime() != null) {
            existing.setPlanEndTime(dto.getPlanEndTime());
        }
        if (dto.getAssignType() != null) {
            existing.setAssignType(dto.getAssignType());
        }
        if (dto.getAssigneeUserId() != null) {
            existing.setAssigneeUserId(dto.getAssigneeUserId());
        }
        if (dto.getAssignerUserId() != null) {
            existing.setAssignerUserId(dto.getAssignerUserId());
        }
        if (dto.getRelatedBizType() != null) {
            existing.setRelatedBizType(dto.getRelatedBizType());
        }
        if (dto.getRelatedBizId() != null) {
            existing.setRelatedBizId(dto.getRelatedBizId());
        }
        if (dto.getRemindTime() != null) {
            existing.setRemindTime(dto.getRemindTime());
        }

        workTaskMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        WorkTask task = workTaskMapper.selectOne(
                new LambdaQueryWrapper<WorkTask>()
                        .eq(WorkTask::getId, id)
                        .eq(WorkTask::getTenantId, tenantId)
        );
        if (task == null) {
            throw BizException.notFound("任务");
        }
        workTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, String completionNote) {
        Long tenantId = TenantContext.getTenantId();
        WorkTask task = workTaskMapper.selectOne(
                new LambdaQueryWrapper<WorkTask>()
                        .eq(WorkTask::getId, id)
                        .eq(WorkTask::getTenantId, tenantId)
        );
        if (task == null) {
            throw BizException.notFound("任务");
        }

        task.setStatus(STATUS_COMPLETED);
        task.setActualEndTime(LocalDateTime.now());
        task.setCompletionNote(completionNote);
        task.setCompletionRate(100);
        workTaskMapper.updateById(task);
    }

    @Override
    public Map<String, Object> todaySummary() {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            return Map.of(
                    "total", 0,
                    "completed", 0,
                    "overdue", 0
            );
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        LambdaQueryWrapper<WorkTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkTask::getTenantId, tenantId)
                .eq(WorkTask::getAssigneeUserId, userId)
                .ge(WorkTask::getPlanEndTime, startOfDay)
                .le(WorkTask::getPlanEndTime, endOfDay)
                .in(WorkTask::getStatus, 0, 1, 2, 4); // 待开始/进行中/已完成/已逾期

        List<WorkTask> todayTasks = workTaskMapper.selectList(wrapper);

        int total = todayTasks.size();
        long completed = todayTasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 2).count();
        long overdue = todayTasks.stream()
                .filter(t -> t.getStatus() != null && (t.getStatus() == 0 || t.getStatus() == 1))
                .filter(t -> t.getPlanEndTime() != null && t.getPlanEndTime().isBefore(LocalDateTime.now()))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("completed", (int) completed);
        summary.put("overdue", (int) overdue);
        return summary;
    }

    private TaskVO convertToVO(WorkTask task) {
        TaskVO vo = new TaskVO();
        BeanUtil.copyProperties(task, vo);
        vo.setAssigneeUserName(resolveUserName(task.getAssigneeUserId()));
        vo.setAssignerUserName(resolveUserName(task.getAssignerUserId()));
        return vo;
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名
        return null;
    }

    private String generateTaskNo() {
        String datePrefix = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TK" + datePrefix;

        LambdaQueryWrapper<WorkTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkTask::getTenantId, TenantContext.getTenantId())
                .likeRight(WorkTask::getTaskNo, prefix)
                .orderByDesc(WorkTask::getTaskNo);

        Page<WorkTask> p = workTaskMapper.selectPage(new Page<>(1, 1), wrapper);
        WorkTask last = p.getRecords().isEmpty() ? null : p.getRecords().get(0);
        int seq = 1;
        if (last != null && last.getTaskNo() != null && last.getTaskNo().length() >= prefix.length() + 4) {
            try {
                seq = Integer.parseInt(last.getTaskNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }
}
