package com.sakura.meetu.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.service.OssService;
import com.sakura.meetu.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author sakura
 * @date 2023/7/15 20:20:19 周六
 */
@Service
public class OssServiceImpl implements OssService {
    private static final Logger log = LoggerFactory.getLogger(OssService.class);

    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;//地域节点

    @Value("${aliyun.oss.file.keyid}")
    private String keyId;//id

    @Value("${aliyun.oss.file.keysecret}")
    private String keySecret;//秘钥

    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;//项目名称


    @Override
    public Result uploadImg(MultipartFile file) {
        // 限制上传类型
        //获取文件名称
        String fileName = file.getOriginalFilename();
        String imgType = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (!isValidImageFormat(imgType)) {
            return Result.error(Result.CODE_SYS_ERROR, "文件类型有误; 只允许上传 jpg, jpeg, png, bmp, tif, tiff 类型");
        }

        // 创建OSS实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, keyId, keySecret);
        try (
                //获取上传文件输入流
                InputStream inputStream = file.getInputStream()
        ) {


            //1、在文件名称里面添加随机唯一值（因为如果上传文件名称相同的话，后面的问价会将前面的文件给覆盖了）
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");//因为生成后的值有横岗，我们就把它去除，不替换也可以，也没有错
            fileName = uuid + fileName;

            // 文件上传的路径
            fileName = "upload/images/" + fileName;
            log.info("文件上传路径: {}", fileName);

            //调用oss方法实现上传
            //参数一：Bucket名称  参数二：上传到oss文件路径和文件名称  比如 /aa/bb/1.jpg 或者直接使用文件名称  参数三：上传文件的流
            ossClient.putObject(bucketName, fileName, inputStream);

            //把上传之后的文件路径返回
            //需要把上传到阿里云路径返回    https://edu-guli-eric.oss-cn-beijing.aliyuncs.com/1.jpg
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            log.info("文件访问路径: {}", url);
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceException(Result.CODE_SYS_ERROR, "上传失败");
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 判断图片类型
     *
     * @param imgType 图片类型
     * @return 如果图片类型满足 系统限制的 返回 true 不满足则返回 false
     */
    private boolean isValidImageFormat(String imgType) {
        // 常见的图片格式后缀
        ArrayList<String> imgTypeList = CollUtil.newArrayList("jpg", "jpeg", "png", "bmp", "tif", "tiff");

        return imgTypeList.contains(imgType);
    }
}
