package com.aicrm.module.fieldwork.task.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.task.dto.TaskCreateDTO;
import com.aicrm.module.fieldwork.task.dto.TaskQueryDTO;
import com.aicrm.module.fieldwork.task.dto.TaskUpdateDTO;
import com.aicrm.module.fieldwork.task.dto.TaskVO;

import java.util.Map;

/**
 * 任务服务
 */
public interface TaskService {

    PageResult<TaskVO> page(TaskQueryDTO query);

    TaskVO getById(Long id);

    Long create(TaskCreateDTO dto);

    void update(TaskUpdateDTO dto);

    void delete(Long id);

    void complete(Long id, String completionNote);

    Map<String, Object> todaySummary();
}
