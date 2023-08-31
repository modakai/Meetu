package com.sakura.meetu.controller;

import cn.hutool.core.util.ObjectUtil;
import com.sakura.meetu.service.OssService;
import com.sakura.meetu.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    @PostMapping("/download/{fileName}")
    public Result download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            return ossService.downFile(fileName, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
