package com.aicrm.module.fieldwork.recording.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.recording.dto.RecordingCreateDTO;
import com.aicrm.module.fieldwork.recording.dto.RecordingQueryDTO;
import com.aicrm.module.fieldwork.recording.dto.RecordingVO;

/**
 * 电话录音服务
 */
public interface RecordingService {

    PageResult<RecordingVO> page(RecordingQueryDTO query);

    RecordingVO getById(Long id);

    Long create(RecordingCreateDTO dto);

    void triggerTranscribe(Long id);

    void triggerAnalyze(Long id);
}
