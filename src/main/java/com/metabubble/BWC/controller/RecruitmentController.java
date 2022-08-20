package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Recruitment;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.service.RecruitmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商家入驻
 */
@RestController
@RequestMapping("/recruitment")
@Slf4j
public class RecruitmentController {
    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private LogsService logsService;

    /**
     * 添加商家入驻
     * author cclucky
     * @param recruitment
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Recruitment recruitment) {
        recruitmentService.save(recruitment);

        // 商家入驻新增日志
        logsService.saveLog("商家入驻", "“ " + recruitment.getShopName() + " ” 商家入驻了");

        return R.success("添加成功");
    }

    /**
     * 修改商家入驻信息
     * author cclucky
     * @param recruitment
     * @return
     */
    @PutMapping
    public R<String> updateRecruitment(@RequestBody Recruitment recruitment) {
        // 商家入驻新增日志
        logsService.saveLog("修改商家入驻信息", "“ " + recruitment.getShopName() + " ” 商家信息");

        recruitmentService.updateById(recruitment);

        return R.success("数据修改成功");
    }

    /**
     * 分页查询 + 条件查询
     * author cclucky
     * @param offset
     * @param limit
     * @param condition 商家名称
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer offset, Integer limit, String condition) {
        // 构建分页构造器
        Page<Recruitment> pageInfo = new Page(offset, limit);

        // 构建条件构造器
        LambdaQueryWrapper<Recruitment> recruitmentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recruitmentLambdaQueryWrapper.like(condition != null, Recruitment::getShopName, condition);

        // 添加排序
        recruitmentLambdaQueryWrapper.orderByAsc(Recruitment::getUpdateTime);

        // 查询结果
        recruitmentService.page(pageInfo, recruitmentLambdaQueryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 删除商家入驻
     * author cclucky
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        // 查询id相关的信息
        Recruitment recruitment = recruitmentService.getById(id);

        // 商家入驻新增日志
        logsService.saveLog("删除商家入驻", "删除了” " + recruitment.getShopName() + " “入驻");

        // 根据id删除商家入驻
        recruitmentService.removeById(id);

        return R.success("删除成功");
    }
}

















