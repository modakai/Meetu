package com.sakura.meetu.service;

import com.sakura.meetu.utils.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author sakura
 * @date 2023/7/15 20:20:18 周六
 */
public interface OssService {

    Result uploadImg(MultipartFile file);
}
