package com.sakura.meetu.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.sakura.meetu.entity.File;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.service.OssService;
import com.sakura.meetu.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author sakura
 * @date 2023/7/15 20:20:19 周六
 */
@Service
public class OssServiceImpl implements OssService {
    private static final Logger log = LoggerFactory.getLogger(OssService.class);
    private static final int BUFF_CACHE_SIZE = 1024;
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

    /**
     * 删除单个文件
     *
     * @param fileUrl 文件路径
     */
    @Override
    public void removeFile(String fileUrl) {
        OSS ossClient = getOss();
        try {
            ossClient.deleteObject(bucketName, fileUrl);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void removeFileBatch(List<String> fileUrls) throws OSSException {
        OSS ossClient = getOss();
        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(fileUrls)
                    .withEncodingType("url");
            deleteObjectsRequest.setQuiet(true);
            ossClient.deleteObjects(deleteObjectsRequest);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public File uploadImg(MultipartFile file) {
        final File resultFile = new File();
        // 限制上传类型
        //获取文件名称
        String fileName = file.getOriginalFilename();
        String imgType = fileName.substring(fileName.lastIndexOf(".") + 1);

        // 封装 文件信息
        resultFile.setName(fileName);
        resultFile.setType(imgType);
        resultFile.setSize(file.getSize());

        if (!isValidImageFormat(imgType)) {
            log.info("用户传递错误的文件类型：{}", imgType);
            throw new ServiceException(Result.CODE_ERROR_400, "文件类型有误; 只允许上传 jpg, jpeg, png, bmp, tif, tiff 类型");
        }

        // 创建OSS实例。
        OSS ossClient = getOss();
        try (
                //获取上传文件输入流
                InputStream inputStream = file.getInputStream()
        ) {


            //1、在文件名称里面添加随机唯一值（因为如果上传文件名称相同的话，后面的问价会将前面的文件给覆盖了）
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");//因为生成后的值有横岗，我们就把它去除，不替换也可以，也没有错
            resultFile.setMd5(uuid);
            fileName = uuid + fileName;
            // 文件上传的路径
            fileName = uploadFilePath + fileName;
            log.info("文件上传路径: {}", fileName);
            resultFile.setLocation(fileName);

            //调用oss方法实现上传
            //参数一：Bucket名称  参数二：上传到oss文件路径和文件名称  比如 /aa/bb/1.jpg 或者直接使用文件名称  参数三：上传文件的流
            ossClient.putObject(bucketName, fileName, inputStream);

            //把上传之后的文件路径返回
            //需要把上传到阿里云路径返回    https://edu-guli-eric.oss-cn-beijing.aliyuncs.com/1.jpg
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            log.info("文件访问路径: {}", url);
            resultFile.setUrl(url);
            return resultFile;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceException(Result.CODE_SYS_ERROR, "上传失败");
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void downloadFile(String fileName, HttpServletResponse response) {
        // 创建OSSClient实例。
        OSS ossClient = getOss();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (
                OutputStream os = response.getOutputStream();
                OSSObject ossObject = ossClient.getObject(bucketName, fileName);
                InputStream fileIs = ossObject.getObjectContent()
        ) {
            log.info("用户下载的文件：{}", ossObject);
            byte[] buffer = new byte[BUFF_CACHE_SIZE]; // 创建缓冲区数组
            int bytesRead = -1; // 用于记录读取的字节数，初始值为-1，表示还没有读取到数据

            BufferedOutputStream buffOs = new BufferedOutputStream(os);
            while ((bytesRead = fileIs.read(buffer)) != -1) { // 读取数据并写入到文件中，直到读取到文件末尾为止
                buffOs.write(buffer, 0, bytesRead); // 将数据写入到文件中
            }

            os.flush();
        } catch (IOException io) {
            log.error("", io);
            throw new ServiceException("下载失败");
        } catch (OSSException oe) {
            log.warn("阿里云上无该文件：{}", fileName);
            log.error("Error Message: {}", oe.getErrorMessage());
            log.error("Error Code: {}", oe.getErrorCode());
        }
    }

    @Override
    public OSS getOss() {
        return new OSSClientBuilder().build(endpoint, keyId, keySecret);
    }

    @Override
    public boolean checkFileExist(String fileUrl) {
        OSS oss = getOss();
        return oss.doesObjectExist(bucketName, fileUrl);
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
