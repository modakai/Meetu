package com.sakura.meetu.controller;

import cn.hutool.core.util.ObjectUtil;
import com.sakura.meetu.service.OssService;
import com.sakura.meetu.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author sakura
 * @date 2023/7/15 16:16:10 周六
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final OssService ossService;

    public FileController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (ObjectUtil.isEmpty(file)) {
            return Result.error(Result.CODE_ERROR_400, "请选择上传文件!");
        }
        return ossService.uploadImg(file);
    }
}
