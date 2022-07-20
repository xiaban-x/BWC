package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
    public R save(@RequestBody Admin admin) {
        adminService.save(admin);
        return R.success("添加成功");
    }

    /**
     * 更新管理员数据
     * author cclucky
     * @param admin
     * @return
     */
    @PutMapping
    public R update(@RequestBody Admin admin) {
        if (admin.getId() != null){
            return R.success(adminService.updateById(admin));
        } else {
            return R.error("不存在该用户！");
        }
    }

    /**
     * 删除管理员
     * author cclucky
     */
//    @DeleteMapping
//    public R delete(@RequestBody Admin admin) {
//        return R.success(adminService.removeById(admin.getId()));
//    }

    @DeleteMapping("{id}")
    public R delete(@PathVariable Long id) {
        return R.success(adminService.removeById(id));
    }

    /**
     * 查询所有
     * author cclucky
     * @return
     */
    @GetMapping
    public R getAll() {
        return R.success(adminService.list());
    }

    /**
     * 分页查询、根据管理员名字查询
     * author cclucky
     * @param currentPage
     * @param pageSize
     * @param admin
     * @return
     */
    @GetMapping("{currentPage}/{pageSize}")
    public R getPage(@PathVariable int currentPage, @PathVariable int pageSize,@RequestBody Admin admin) {
        IPage<Admin> page = adminService.getPage(currentPage, pageSize, admin);
        return R.success(page);
    }

}


