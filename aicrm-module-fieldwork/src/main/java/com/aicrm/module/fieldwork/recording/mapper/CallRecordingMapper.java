package com.aicrm.module.fieldwork.recording.mapper;

import com.aicrm.module.fieldwork.recording.entity.CallRecording;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电话录音Mapper
 */
@Mapper
public interface CallRecordingMapper extends BaseMapper<CallRecording> {
}
