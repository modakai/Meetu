package com.sakura.meetu.service.impl;

import com.sakura.meetu.mapper.DataAnalysisMapper;
import com.sakura.meetu.service.IDataAnalysisService;
import com.sakura.meetu.utils.Result;
import org.springframework.stereotype.Service;

/**
 * 数据分析业务
 *
 * @author sakura
 * @date 2023/9/15 20:20:39 周五
 */
@Service
public class DataAnalysisServiceImpl implements IDataAnalysisService {

    private final DataAnalysisMapper dataAnalysisMapper;

    public DataAnalysisServiceImpl(DataAnalysisMapper dataAnalysisMapper) {
        this.dataAnalysisMapper = dataAnalysisMapper;
    }

    @Override
    public Result listSysAnalysis() {
        return Result.success(dataAnalysisMapper.selectSystemData());
    }
}
