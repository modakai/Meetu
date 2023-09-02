package com.sakura.meetu.service;

import com.aliyun.oss.OSS;
import com.sakura.meetu.entity.File;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author sakura
 * @date 2023/7/15 20:20:18 周六
 */
public interface OssService {

    void removeFile(String fileUrl);

    void removeFileBatch(List<String> fileUrls);

    File uploadImg(MultipartFile file);

    void downloadFile(String fileName, HttpServletResponse response);

    OSS getOss();

    boolean checkFileExist(String fileUrl);
}
