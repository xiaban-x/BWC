package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Cooperation;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.CooperationService;
import com.metabubble.BWC.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 渠道合作
 */
@RestController
@RequestMapping("/cooperation")
@Slf4j
public class CooperationController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private CooperationService cooperationService;

    @Autowired
    private LogsService logsService;

    /**
     * 添加渠道合作
     * author cclucky
     * @param cooperation
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Cooperation cooperation, HttpServletRequest request) {

        // 判断登录状态
        if (request.getSession().getAttribute("user") == null) {
            return R.error("用户未登录");
        }

        // 渠道合作新增日志
        //logsService.saveLog("新增渠道合作", "增加了” " + cooperation.getName() + " “的渠道合作");

        cooperationService.save(cooperation);

        return R.success("添加成功");
    }

    /**
     * 修改渠道合作数据信息
     * author cclucky
     * @param cooperation
     * @return
     */
    /**
     * 暂时废除
    @PutMapping
    public R<String> update(@RequestBody Cooperation cooperation) {

        // 渠道合作新增日志
        logsService.saveLog("修改渠道合作信息", "“ " + cooperation.getName() + " ” 的信息");

        cooperationService.updateById(cooperation);

        return R.success("修改数据成功");
    }**/


    /**
     * * 分页查询 + 条件查询
     * author cclucky
     * @param offset
     * @param limit
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int offset, int limit, String name) {
        // 构建分页构造器
        Page<Cooperation> pageInfo = new Page(offset, limit);

        // 构建条件构造器
        LambdaQueryWrapper<Cooperation> cooperationLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据名称
        cooperationLambdaQueryWrapper.like(name != null, Cooperation::getName, name);
        // 排序条件
        cooperationLambdaQueryWrapper.orderByAsc(Cooperation::getUpdateTime);

        // 查询结果
        cooperationService.page(pageInfo, cooperationLambdaQueryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除渠道合作数据
     * author cclucky
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id, HttpServletRequest request) {
        // 判读登录信息
        Long adminId = (Long) request.getSession().getAttribute("admin");

        if (adminId == null) {
            return R.error("非管理员，无权限操作");
        }

        // 查询id相关信息
        Cooperation cooperation = cooperationService.getById(id);

        // 日志信息
        Logs log = new Logs();
        Admin admin = adminService.getById(adminId);
        log.setAdminName(admin.getName());
        log.setAdminId(adminId);
        log.setName("删除渠道合作");
        log.setContent("删除了” " + cooperation.getName() + " ”的渠道合作信息");

        // 执行删除操作
        if (cooperationService.removeById(id)) {
            // 渠道合作删除日志
            logsService.save(log);

            return R.success("删除成功");
        }

        return R.error("网络错误，请稍后再试");
    }
}
















