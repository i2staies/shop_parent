package com.atguigu.minio;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;

import java.io.FileInputStream;

public class FileUploader {
    public static void main(String[] args) throws Exception {
        try {
            // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
            MinioClient minioClient = new MinioClient("http://192.168.88.131:9000", "enjoy6288", "enjoy6288");
            // 检查存储桶是否已经存在
            boolean isExist = minioClient.bucketExists("java0518");
            if(isExist) {
                System.out.println("Bucket already exists.");
            } else {
                // 创建一个名为asiatrip的存储桶，用于存储照片的zip文件。
                minioClient.makeBucket("java0518");
            }
            // 使用putObject上传一个文件到存储桶中。
            FileInputStream fileInputStream = new FileInputStream("E:\\life\\尚硅谷线下课程资料\\项目\\电商\\day03\\资料\\图片资源\\苹果手机\\绿色4.jpg");
            //这个是文件上传的时候需要的参数 文件可用大小与文件上传多少
            PutObjectOptions options=new PutObjectOptions(fileInputStream.available(),-1);
            minioClient.putObject("java0518","new.jpg",fileInputStream,options);
            System.out.println("上传成功");

        } catch(MinioException e) {
            System.out.println("Error occurred: " + e);
        }
    }
}