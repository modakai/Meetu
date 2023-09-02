package com.sakura.meetu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.OssConstants;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.dto.FileDto;
import com.sakura.meetu.entity.File;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.FileMapper;
import com.sakura.meetu.service.IFileService;
import com.sakura.meetu.service.MqMessageService;
import com.sakura.meetu.service.OssService;
import com.sakura.meetu.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-02
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(OssService.class);

    private final MqMessageService mqMessageService;
    private final OssService ossService;

    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;//地域节点
    @Value("${aliyun.oss.file.keyid}")
    private String keyId;//id
    @Value("${aliyun.oss.file.keysecret}")
    private String keySecret;//秘钥
    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;//项目名称
    @Value("${aliyun.oss.file.upload-file-path}")
    private String uploadFilePath;

    public FileServiceImpl(MqMessageService mqMessageService, OssService ossService) {
        this.mqMessageService = mqMessageService;
        this.ossService = ossService;
    }

    /**
     * 注意区别 数据库 数据与 os 一致
     *
     * @param fileName
     * @return
     */
    @Override
    public Result beforeDownLoadFile(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return Result.error(Result.CODE_ERROR_400, "请选择对应的数据");
        }

        // 1 拼接完整的文件名 去掉 bucket 的名称
        String downFileName = fileName;
        if (!fileName.startsWith(uploadFilePath)) {
            downFileName += uploadFilePath;
        }
        OSS ossClient = new OSSClientBuilder().build(endpoint, keyId, keySecret);
        boolean found = ossClient.doesObjectExist(bucketName, downFileName);
        if (!found) {
            return Result.error(Result.CODE_ERROR_404, "抱歉该文件不存在！");
        }

        return Result.success(downFileName);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result removeFileAndMq(FileDto fileDto) {
        Integer fileId = fileDto.getSimpleFileId();
        String url = fileDto.getSimpleFileUrl();
        if (fileId == null || StrUtil.isBlank(url)) {
            return Result.error(Result.CODE_ERROR_400, "请传递正确的参数");
        }

        removeById(fileId);

        // 删除阿里云上的文件 可以发给 消息队列去干
        mqMessageService.sendFileToMq(RabbitMqConstants.OSS_FILE_ROUTER_KEY, url);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result removeFileBatch(FileDto fileDto) {
        List<Integer> fileIds = fileDto.getFileIds();
        List<String> fileUrls = fileDto.getFileUrls();
        if (fileIds == null || fileUrls == null) {
            return Result.error(Result.CODE_ERROR_400, "请选择数据");
        }

        if (fileIds.size() > OssConstants.OSS_DELETE_BATCH_MAX_SIZE) {
            return Result.error(Result.CODE_ERROR_400, "暂时只支持一次性删除一千条以内的数据！当前以及超过一千条");
        }

        removeBatchByIds(fileIds);

        // 一次只能删除一千条
        try {
            ossService.removeFileBatch(fileUrls);
        } catch (OSSException oe) {
            log.error("Error Message: {}", oe.getErrorMessage());
            log.error("Error Code: {}", oe.getErrorCode());
            throw new ServiceException(Result.CODE_SYS_ERROR, "删除失败！系统出现异常，请联系管理员");
        }

        return Result.success();
    }
}
