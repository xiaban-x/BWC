package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Admin;

public interface AdminService extends IService<Admin> {

    /**
     * 定义服务类的分页方法
     * author cclucky
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Admin> getPage(int currentPage, int pageSize);

    /**
     * 定义服务类的分页方法
     * author cclucky
     * @param currentPage
     * @param pageSize
     * @param admin
     * @return
     */
    IPage<Admin> getPage(int currentPage, int pageSize, Admin admin);
}
