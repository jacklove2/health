package com.itheima.job;

import com.itheima.dao.OrderSettingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @atuthor JackLove
 * @date 2019-09-27 22:05
 * @Package com.itheima.job
 */
public class ClearOrderSetting {
    @Autowired
    private OrderSettingDao orderSettingDao;

    public void doClear() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formatdate = sdf.format(date);
        orderSettingDao.delOrderSetting(formatdate);

    }

    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-clearorderset-quarz.xml");
    }
}
