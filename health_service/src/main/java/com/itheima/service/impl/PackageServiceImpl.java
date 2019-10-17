package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.dao.PackageDao;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.pojo.CheckGroup;
import com.itheima.pojo.Package;
import com.itheima.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.List;

/**
 * @atuthor JackLove
 * @date 2019-09-26 12:10
 * @Package com.itheima.service.impl
 */
@Service(interfaceClass = PackageService.class)
//ioc
public class PackageServiceImpl implements PackageService {
    //di
    @Autowired
    private PackageDao packageDao;
    @Autowired
    private JedisPool jedisPool;

    //添加套餐
    @Override
    //添加事务控制
    public void addPackage(Package pkg, Integer[] checkgroupIds) {
        //添加套餐
        packageDao.addPackage(pkg);
        //通过selectkey方法拿到pkgId
        Integer package_Id = pkg.getId();
        //遍历出检查组的id
        if (checkgroupIds != null) {
            for (Integer checkgroupId : checkgroupIds) {
                //调用业务层的方法向中间表插入数据
                packageDao.addcheckgroupId(package_Id, checkgroupId);
            }
        }
    }

    //查询套餐列表
    @Override
    public List<Package> findAll() throws IOException {
        //从redis获得套餐列表信息
        Jedis jedis = jedisPool.getResource();
        String packageStr = jedis.get("package");
        //创建ObjecMapper
        ObjectMapper mapper = new ObjectMapper();
        //判断从redis中获得的套餐列表信息json数据是否为空
        if (packageStr == null || "".equals(packageStr)) {
            //为空 表示redis中没有套餐列表信息
            //调用dao查询套餐列表信息
            List<Package> packageList = packageDao.findAll();
            //将packageList转换成json字符串数据存放到redis中
            packageStr = mapper.writeValueAsString(packageList);
            jedis.set("package", packageStr);
        }
        //关闭jedis
        jedis.close();
        //将jsonStr字符串转换成List<Package> 并返回
        List<Package> packageList = mapper.readValue(packageStr, new TypeReference<List<Package>>() {
        });
        return packageList;
    }

    //查询套餐详情
    @Override
    public Package findById(Integer id) throws IOException {
        //从redis获得套餐列表信息
        Jedis jedis = jedisPool.getResource();
        //通过键拿到值
        String packageDetailStr = jedis.get("packageDetail=" + id);
        //创建ObjecMapper
        ObjectMapper mapper = new ObjectMapper();
        Package pkg = null;
        //判断缓存是否有
        if (packageDetailStr == null || "".equals(packageDetailStr)) {
            //缓存里没有
            //通过dao查询
            pkg = packageDao.findById(id);
            //通过id判断是否是同一个套餐
            //存入redis
            //将Package对象转化字符串并且赋值给上面的值
            packageDetailStr = mapper.writeValueAsString(pkg);
            //Package的字符串
            jedis.set("packageDetail=" + id, packageDetailStr);
        }
        pkg = mapper.readValue(packageDetailStr, new TypeReference<Package>() {
        });
        //判断id是否相等
        if (pkg.getId() != id) {
            pkg = packageDao.findById(id);
            packageDetailStr = mapper.writeValueAsString(pkg);
            //Package的字符串
            jedis.set("packageDetail=" + id, packageDetailStr);
        }
        jedis.close();
        //返回pkg
        return pkg;

    }

    //查询套餐基本详情
    @Override
    public Package findPackageById(Integer id) throws IOException {
        //从redis获得套餐列表信息
        Jedis jedis = jedisPool.getResource();
        //通过键拿到值
        String packageInfoStr = jedis.get("packageInfo=" + id);
        //创建ObjecMapper
        ObjectMapper mapper = new ObjectMapper();
        Package pkg = null;
        if (packageInfoStr == null || "".equals(packageInfoStr)) {
            pkg = packageDao.findPackageById(id);
            packageInfoStr = mapper.writeValueAsString(pkg);
            jedis.set("packageInfo=" + id, packageInfoStr);
        }
        pkg = mapper.readValue(packageInfoStr, new TypeReference<Package>() {
        });
        //判断id是否相等
        if (pkg.getId() != id) {
            pkg = packageDao.findById(id);
            packageInfoStr = mapper.writeValueAsString(pkg);
            //Package的字符串
            jedis.set("packageInfo=" + id, packageInfoStr);
        }
        jedis.close();
        //返回pkg
        return pkg;
    }

    @Override
    public PageResult<Package> findByPage(QueryPageBean queryPageBean) {
        //拿到用户传过来的关键字
        //判断字符串是否为空,不为空则拼接
        if (!StringUtils.isEmpty(queryPageBean.getQueryString())) {
            queryPageBean.setQueryString("%" + queryPageBean.getQueryString() + "%");
        }
        Integer pageSize = queryPageBean.getPageSize();
        Integer currentPage = queryPageBean.getCurrentPage();
        //通过分页插件进行分页
        PageHelper.startPage(currentPage, pageSize);
        //调用dao层方法查询分页结果
        Page<Package> page = packageDao.findByPage(queryPageBean.getQueryString());
        //封装总记录数和分页数据
        PageResult<Package> pageResult = new PageResult<>(page.getTotal(), page.getResult());
        return pageResult;
    }


}

