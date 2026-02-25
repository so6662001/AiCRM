package com.aicrm.module.fieldwork.checkin.mapper;

import com.aicrm.module.fieldwork.checkin.entity.CheckinRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到记录Mapper
 */
@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {
}
