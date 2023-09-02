package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.dto.FileDto;
import com.sakura.meetu.entity.File;
import com.sakura.meetu.utils.Result;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-09-02
 */
public interface IFileService extends IService<File> {

    Result beforeDownLoadFile(String fileName);

    Result removeFileAndMq(FileDto fileDto);

    Result removeFileBatch(FileDto fileDto);
}
