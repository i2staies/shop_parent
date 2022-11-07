package com.atguigu.utils;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@EnableConfigurationProperties(MinioProperties.class)
@Component
public class MinioUploader {

    //这句话先被执行
    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MinioClient minioClient;


    @Bean
    public MinioClient MinioClient() throws Exception {
        // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        MinioClient minioClient = new MinioClient(minioProperties.getEndpoint(), minioProperties.getAccessKey(), minioProperties.getSecretKey());
        // 检查存储桶是否已经存在
        boolean isExist = minioClient.bucketExists(minioProperties.getBucketName());
        if(isExist) {
            System.out.println("Bucket already exists.");
        } else {
            // 创建一个名为asiatrip的存储桶，用于存储照片的zip文件。
            minioClient.makeBucket(minioProperties.getBucketName());
        }
        return minioClient;
    }

    public String uploadFile(MultipartFile multipartFile) throws Exception {

            String fileName = UUID.randomUUID().toString()+multipartFile.getOriginalFilename();

            // 使用putObject上传一个文件到存储桶中。
            InputStream fileInputStream = multipartFile.getInputStream();

            //这个是文件上传的时候需要的参数 文件可用大小与文件上传多少
            PutObjectOptions options=new PutObjectOptions(fileInputStream.available(),-1);
            options.setContentType(multipartFile.getContentType());
            minioClient.putObject(minioProperties.getBucketName(),fileName,fileInputStream,options);
            System.out.println("上传成功");
            String retUrl = minioProperties.getEndpoint()+"/"+minioProperties.getBucketName()+"/"+fileName;
            return retUrl;
        }
    }
