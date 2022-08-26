package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.entity.Merchant;
import com.metabubble.BWC.entity.Recruitment;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.service.MerchantService;
import com.metabubble.BWC.service.RecruitmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import static com.metabubble.BWC.controller.MerchantController.getGeocoderLatitude;

/**
 * 商家入驻
 */
@RestController
@RequestMapping("/recruitment")
@Slf4j
public class RecruitmentController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private LogsService logsService;

    /**
     * 提交商家入驻申请
     * author cclucky
     * @param recruitment
     * @return
     */
    @PostMapping
    public R<String> submit(@RequestBody Recruitment recruitment, HttpServletRequest request) {
        // 判读登录状态
        if (request.getSession().getAttribute("user") == null) {
            return R.error("尚未登录");
        }

        // 完成申请操作
        if (recruitmentService.save(recruitment)) {
            // 日志
            //logsService.saveLog("商家入驻申请", recruitment.getShopName() + "申请入驻");

            return R.success("已发送申请，请耐心等待！");
        }
        return R.error("网络错误，清稍后再试");
    }

    /**
     * 处理商家入驻申请 : 驳回
     * author cclucky
     * @param id
     * @return
     */
    @PutMapping("/reject")
    public R<String> reject(Long id, HttpServletRequest request) {

        // 判断登录状态
        if (request.getSession().getAttribute("admin") == null) {
            return R.error("非管理员，无权限审核");
        }

        // 获取申请信息
        Recruitment recruitment = recruitmentService.getById(id);

        // 判断当前申请的状态
        if (recruitment.getStatus() == 1) {
            return R.error("该申请已被驳回，请勿重复操作");
        }

        if (recruitment.getStatus() == 2) {
            return R.error("该申请已通过");
        }

        // 驳回申请
        recruitment.setStatus(1);

        // 修改商家入驻表信息
        recruitmentService.updateById(recruitment);

        return R.success("已驳回该申请");
    }

    /**
     * 处理商家入驻申请 ： 通过
     * author cclucky
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/pass")
    public R<String> pass(Long id, HttpServletRequest request) {

        if (request.getSession().getAttribute("admin") == null) {
            return R.error("非管理员，无权限审核");
        }

        // 获取申请信息
        Recruitment recruitment = recruitmentService.getById(id);

        // 判断当前申请的状态
        if (recruitment.getStatus() == 2) {
            return R.error("该申请已通过，请勿重复操作");
        }

        if (recruitment.getStatus() == 1) {
            return R.error("该申请已被驳回");
        }

        // 通过该申请
        recruitment.setStatus(2);

        // 修改商家表信息
        recruitmentService.updateById(recruitment);

        //  1、新建商家信息
        Merchant merchant = new Merchant();
        merchant.setName(recruitment.getShopName());
        merchant.setTel(recruitment.getTel());
        merchant.setAddress(recruitment.getCity() + recruitment.getAddress());

        //获取经纬度
        BigDecimal lng = merchant.getLng();
        BigDecimal lat = merchant.getLat();
        if (lng == null  || lat == null){
            lng = getGeocoderLatitude(merchant.getAddress()).get("lng");
            lat = getGeocoderLatitude(merchant.getAddress()).get("lat");
        }

        merchant.setLng(lng);
        merchant.setLat(lat);

        //  2、将商家信息录入商家表
        boolean flag = merchantService.save(merchant);

        if (flag) {
            // 日志信息
            logsService.saveLog("商家入驻申请通过", recruitment.getShopName() + "的入驻申请已通过");

            return R.success("已通过该申请");
        }

        return R.error("网络错误，请稍后再试！");

    }

    /**
     * 分页查询 + 条件查询
     * author cclucky
     * @param offset
     * @param limit
     * @param shopName
     * @param tel
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int offset, int limit, String shopName, String tel) {
        // 构建分页构造器
        Page<Recruitment> pageInfo = new Page<>(offset, limit);

        // 构建查询构造器
        LambdaQueryWrapper<Recruitment> queryWrapper = new LambdaQueryWrapper<>();

        // 1、商家名称
        queryWrapper.like(shopName != null, Recruitment::getShopName, shopName);
        // 2、商家电话
        queryWrapper.like(tel != null, Recruitment::getTel, tel);

        // 添加排序
        queryWrapper.orderByAsc(Recruitment::getCreateTime);

        recruitmentService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除申请数据
     * author cclucky
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> deleteById(Long id, HttpServletRequest request) {

        // 判读登录状态
        Long adminId = (Long) request.getSession().getAttribute("admin");

        if (adminId == null) {
            return R.error("非管理员，无权限审核");
        }

        // 获取该条申请信息
        Recruitment recruitment = recruitmentService.getById(id);

        // 日志信息
        Admin admin = adminService.getById(adminId);

        Logs log = new Logs();
        log.setAdminId(admin.getId());
        log.setAdminName(admin.getName());
        log.setName("删除商家入驻申请数据");
        log.setContent(recruitment.getShopName() + "的申请已删除");

        if (recruitmentService.removeById(id)) {
            // 添加日志
            logsService.save(log);

            return R.success("删除成功");
        }

        return R.error("网络错误，请稍后再试");
    }
}

















