package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Cooperation;
import com.metabubble.BWC.service.CooperationService;
import com.metabubble.BWC.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 渠道合作
 */
@RestController
@RequestMapping("/cooperation")
@Slf4j
public class CooperationController {
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
    public R<String> save(@RequestBody Cooperation cooperation) {
        // 渠道合作新增日志
        logsService.saveLog("新增渠道合作", "增加了” " + cooperation.getName() + " “的渠道合作");

        cooperationService.save(cooperation);

        return R.success("添加成功");
    }

    /**
     * 修改渠道合作数据信息
     * author cclucky
     * @param cooperation
     * @return
     */
    @PutMapping
    public R<String> update(Cooperation cooperation) {

        // 渠道合作新增日志
        logsService.saveLog("修改渠道合作信息", "“ " + cooperation.getName() + " ” 的信息");

        cooperationService.updateById(cooperation);

        return R.success("修改数据成功");
    }

    /**
     * 分页查询 + 条件查询
     * author cclucky
     * @param offset
     * @param limit
     * @param condition
     * @return
     */
    @GetMapping
    public R<Page> page(Integer offset, Integer limit, String condition) {
        // 构建分页构造器
        Page<Cooperation> pageInfo = new Page(offset, limit);

        // 构建条件构造器
        LambdaQueryWrapper<Cooperation> cooperationLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据名称
        cooperationLambdaQueryWrapper.like(condition != null, Cooperation::getName, condition);
        // 根据电话
        cooperationLambdaQueryWrapper.like(condition != null, Cooperation::getTel, condition);

        // 查询结果
        cooperationService.page(pageInfo, cooperationLambdaQueryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 删除渠道合作
     * author cclucky
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        // 查询id相关信息
        Cooperation cooperation = cooperationService.getById(id);
        // 渠道合作删除日志
        logsService.saveLog("删除渠道合作", "删除了” " + cooperation.getName() + " ”的渠道合作信息");

        // 执行删除操作
        cooperationService.removeById(id);

        return R.success("删除成功");
    }
}
















