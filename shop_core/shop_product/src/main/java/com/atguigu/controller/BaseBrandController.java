package com.atguigu.controller;


import com.atguigu.entity.BaseBrand;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseBrandService;
import com.atguigu.utils.MinioUploader;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 品牌表 前端控制器
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-31
 */
@RestController
@RequestMapping("/product/brand")
public class BaseBrandController {
    @Autowired
    private BaseBrandService brandService;

    @Autowired
    private MinioUploader minioUploader;

    //1.分页查询品牌信息 http://192.168.16.218/product/brand/queryBrandByPage/1/10
    @GetMapping("/queryBrandByPage/{pageNum}/{pageSize}")
    public RetVal queryBrandByPage(@PathVariable Long pageNum,@PathVariable Long pageSize) {
        //分页查询
        IPage<BaseBrand> page = new Page<>(pageNum, pageSize);
        brandService.page(page,null);
        return RetVal.ok(page);
    }

    //http://127.0.0.1/product/brand
    //2.添加品牌
    @PostMapping
    public RetVal saveBrand(@RequestBody BaseBrand brand) {
        brandService.save(brand);
        return RetVal.ok();
    }

    //http://127.0.0.1/product/brand/4
    //3.根据id查询品牌信息
    @GetMapping("/{brandId}")
    public RetVal saveBrand(@PathVariable Long brandId) {
        BaseBrand brand = brandService.getById(brandId);
        return RetVal.ok(brand);
    }

    //4.更新品牌信息
    @PutMapping
    public RetVal updateBrand(@RequestBody BaseBrand brand) {
        brandService.updateById(brand);
        return RetVal.ok();
    }

    //5.删除品牌信息
    @DeleteMapping("/{brandId}")
    public RetVal remove(@PathVariable Long brandId) {
        brandService.removeById(brandId);
        return RetVal.ok();
    }

    //6.查询所有的品牌
    @GetMapping("/getAllBrand")
    public RetVal getAllBrand() {
        List<BaseBrand> brandList = brandService.list(null);
        return RetVal.ok(brandList);
    }



//    //7.文件上传  http://api.gmall.com/product/brand/fileUpload
//    @PostMapping("fileUpload")
//    public RetVal fileUpload1(MultipartFile file) throws Exception {
//        //需要一个配置文件告诉fastdfs在哪里
//        String configFilePath = this.getClass().getResource("/tracker.conf").getFile();
//        //初始化
//        ClientGlobal.init(configFilePath);
//        //创建trackerClient 客户端
//        TrackerClient trackerClient = new TrackerClient();
//        //用trackerClient获取连接
//        TrackerServer trackerServer = trackerClient.getConnection();
//        //创建StorageClient1
//        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
//        //对文件实现上传
//        String originalFilename = file.getOriginalFilename();
//        String extension = FilenameUtils.getExtension(originalFilename);
//        String path = storageClient1.upload_appender_file1(file.getBytes(), extension, null);
//        //这个前缀需要自己拼接 拼接的地址是有可能变化的 所以说最好写到配置文件 作业
//        return RetVal.ok(path);
//    }
//


    //8.文件上传  http://api.gmall.com/product/brand/fileUpload
    @PostMapping("/fileUpload")
    public RetVal fileUpload(MultipartFile[] file) throws Exception {
        String retUrl = null ;
        for (MultipartFile multipartFile : file) {
            retUrl = minioUploader.uploadFile(multipartFile);
        }
        return RetVal.ok(retUrl);
    }
}


