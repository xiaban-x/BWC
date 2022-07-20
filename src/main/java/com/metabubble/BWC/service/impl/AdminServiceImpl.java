package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.service.AdminService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
        implements AdminService {

    @Autowired
    private AdminMapper adminMapper;


    /**
     * 定义服务实体类的分页方法
     * author cclucky
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<Admin> getPage(int currentPage, int pageSize) {
        IPage page = new Page(currentPage, pageSize);
        return adminMapper.selectPage(page, null);

    }

    /**
     * 定义服务实体类的分页方法
     * author cclucky
     * @param currentPage
     * @param pageSize
     * @param admin
     * @return
     */
    @Override
    public IPage<Admin> getPage(int currentPage, int pageSize, Admin admin) {
        LambdaQueryWrapper<Admin> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(admin.getName()), Admin::getName, admin.getName());

        IPage page = new Page(currentPage, pageSize);
        return adminMapper.selectPage(page, lqw);
    }
}
