package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.CheckGroup;
import com.itheima.pojo.Package;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @atuthor JackLove
 * @date 2019-09-26 12:12
 * @Package com.itheima.dao
 */
public interface PackageDao {
    //添加套餐的方法
    void addPackage(Package pkg);

    //向中间表插入数据
    void addcheckgroupId(@Param("package_Id") Integer package_Id, @Param("checkgroupId") Integer checkgroupId);

    //查询套餐
    List<Package> findAll();

    //查询套餐详情
    Package findById(Integer id);

    //查询套餐基本详情
    Package findPackageById(Integer id);

    //分页查询套餐信息

    
    Page<Package> findByPage(String queryString);

    
}
