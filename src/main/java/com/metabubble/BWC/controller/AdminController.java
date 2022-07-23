package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 管理员
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     * author 晴天小杰
     * @param request 验证码信息
     * @param map 用户提交的登录信息
     * @return 返回登陆的信息
     */
    @PostMapping("/login")
    public R<Admin> login(HttpServletRequest request, @RequestBody Map map)
            throws Exception{
        //获取邮箱
        String email = map.get("email").toString();
        //获取用户提交的验证码
        String checkCode = map.get("checkCode").toString();
        //获取程序生成的验证码
        HttpSession session = request.getSession();
        String checkCodeGen = (String)session.getAttribute("checkCodeGen");
        //比对验证码
        if (!checkCodeGen.equals(checkCode)){
            //不允许注册
            return R.error("验证码错误");
        }
        //将页面提交的密码进行MD5加密处理
        String password = map.get("password").toString();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据email查数据库
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getEmail, email);
        Admin adminMsg = adminService.getOne(queryWrapper);
        //如果没有查询==》登陆失败
        if (adminMsg == null) {
            return R.error("用户名或密码错误");
        }
        //密码错误==》登陆失败
        if (!adminMsg.getPassword().equals(password)) {
            return R.error("用户名或密码错误");
        }
        //查看员工状态；0==》禁用
        if (adminMsg.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //登陆成功，id存入session
        request.getSession().setAttribute("admin", adminMsg.getId());

        return R.success(adminMsg);
    }

    /**
     * 管理员退出登录
     * author 晴天小杰
     * @param request session中的管理员信息
     * @return 返回退出信息
     */
    @DeleteMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //删除session中的账户信息
        request.getSession().removeAttribute("admin");
        return R.success("退出成功");
    }

    /**
     * 添加管理员
     * author cclucky
     * @param admin
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Admin admin) {
        adminService.save(admin);
        return R.success("添加成功");
    }

    // 不需要“查询所有”的功能
//    /**
//     * 查询所有
//     * author cclucky
//     * @return
//     */
//    @GetMapping
//    public R<List<Admin>> getAll() {
//        return R.success(adminService.list());
//    }

    /**
     * 分页查询、根据管理员名字查询
     * author cclucky
     * @param offset
     * @param limit
     * @param condition
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int offset, int limit,String condition) {
        // 构建分页构造器
        Page pageInfo = new Page(offset, limit);

        // 构建条件构造器
        LambdaQueryWrapper<Admin> adminLambdaQueryWrapper = new LambdaQueryWrapper<>();
        adminLambdaQueryWrapper.like(condition != null, Admin::getName, condition);
        // 添加排序
        adminLambdaQueryWrapper.orderByAsc(Admin::getType);

        // 执行查询
        adminService.page(pageInfo, adminLambdaQueryWrapper);

        return  R.success(pageInfo);
    }

    /**
     * 更新管理员数据
     * author cclucky
     * @param admin
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Admin admin) {


        adminService.updateById(admin);

        return R.success("数据修改成功");
    }

    /**
     * 删除管理员
     * author cclucky
     */

    @DeleteMapping
    public R<String> delete(Long id) {

        adminService.removeById(id);

        return R.success("删除成功");
    }

}


