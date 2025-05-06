package com.shaber.movieticket.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/uploading")
public class UpLoadingController {

    @Value("${movie.cover.upload.path}")
    private String uploadPath; // 在配置文件中定义的上传路径

    @PostMapping("/{type}")
    public ResponseEntity<Map<String, String>> uploadCover(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {

        try {
            // 验证文件类型
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "只允许上传图片文件");
                return ResponseEntity.badRequest().body(error);
            }

            // 验证封面类型参数
            if (!("horizon".equals(type) || "vertical".equals(type))) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "无效的封面类型，必须是'horizon'或'vertical'");
                return ResponseEntity.badRequest().body(error);
            }

            // 如果目录不存在则创建
            String dirPath = uploadPath + "/" + type;

            System.out.println("---------------------------\n" + dirPath + "---------------------------\n");

            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件到指定位置
            String filePath = dirPath + "/" + filename;
            System.out.println("---------------------------\n" + filePath + "---------------------------\n");
            File dest = new File(filePath);
            file.transferTo(dest);

            // 返回成功响应
            Map<String, String> response = new HashMap<>();
            response.put("message", "文件上传成功");
            response.put("path", "/movies/covers/" + type + "/" + filename);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
