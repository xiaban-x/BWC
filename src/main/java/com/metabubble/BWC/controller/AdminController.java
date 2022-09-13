package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.AdminDto;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.utils.CookieUtils;
import com.metabubble.BWC.utils.MobileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理员
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private ManageSession manageSession;

    @Autowired
    private RedisTemplate redisTemplate;

    String admin = "admin";

    /**
     * 管理员登录
     * author 晴天小杰
     * @param request 验证码信息
     * @param map 用户提交的登录信息
     * @return 返回登陆的信息
     */
    @PostMapping("/login")
    public R<AdminDto> login(HttpServletRequest request, @RequestBody Map map , HttpServletResponse response)
            throws Exception{
        //获取邮箱
        String email = map.get("email").toString();
        //获取用户提交的验证码
        String checkCode = map.get("checkCode").toString();
        //获取程序生成的验证码
        HttpSession session = request.getSession();
        String checkCodeGen = (String)session.getAttribute("checkCodeGen");
        //比对验证码
        if (!checkCodeGen.equalsIgnoreCase(checkCode)){
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
        // 判断当前管理员的登录失败次数，防止有人暴力破解用户的密码
        String limitKey =  adminMsg.getId() + "_login_error_times";
        String limitTimes = (String) redisTemplate.opsForValue().get(limitKey);
        Integer times = 1;
        if (limitTimes != null) {
            if (new Integer(limitTimes).intValue() >= 6) {
                return R.error("当前账号今日登录失败次数超过6次，为保证您的账号安全，系统已锁定当前账号，请找负责人联系！");
            }
            times = new Integer(limitTimes) + 1;
        }
        //查看员工状态；0==》禁用
        if (adminMsg.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //密码错误==》登陆失败
        if (!adminMsg.getPassword().equals(password)) {
            // 记录密码输入错误数
            redisTemplate.opsForValue().set(limitKey, times + "");

            return R.error("用户名或密码错误");
        }
        try {
            HttpSession httpSession = manageSession.getManageSession().get(admin+adminMsg.getId().toString());
            if (httpSession!=null){
                //当前session有值，说明1.此帐号处于已登录状态有人正在使用，2.session还在有效期未被销毁
                log.info("管理员:"+adminMsg.getId()+"再次登录！");
                httpSession.invalidate();
                manageSession.getManageSession().remove(admin+adminMsg.getId().toString());
            }
        } catch (Exception e) {
            log.info(e.toString()+"：无用报错");
        }
        //删除当前管理员的登录失败次数
        redisTemplate.delete(limitKey);
        //设置过期时间24h
        session.setMaxInactiveInterval(60*60*24);
        //登陆成功，id存入session
        request.getSession().setAttribute("admin", adminMsg.getId());
        //登陆成功，session存入
        manageSession.getManageSession().put(admin+adminMsg.getId().toString(),session);
        BaseContext.setCurrentId(adminMsg.getId());

        //Dto对象拷贝
        AdminDto adminDto = new AdminDto();
        BeanUtils.copyProperties(adminMsg,adminDto,"adminMsg");

        return R.success(adminDto);
    }


    /**
     * 删除管理员登陆失败次数
     * @param id
     * @return
     */
    @DeleteMapping("/limitkey")
    public R<String> removeLimitKey(Long id){
        String limitKey =  id + "_login_error_times";
        Boolean delete = redisTemplate.delete(limitKey);
        if (delete) {
            return R.success("恢复成功！");
        }else {
            return R.error("恢复失败，请尝试重新登录或联系技术人员！");
        }
    }

    /**
     * 管理员退出登录
     * author 晴天小杰
     * @param request session中的管理员信息
     * @return 返回退出信息
     */
    @DeleteMapping("/logout")
    public R<String> logout(HttpServletRequest request){

        Long id = BaseContext.getCurrentId();



        try {
            HttpSession httpSession = manageSession.getManageSession().get(admin+id.toString());
            if (httpSession!=null){
                httpSession.invalidate();
            }
        } catch (Exception e) {
            log.info(e.toString()+"：无用报错");
        }finally {
            //删除session中的账户信息
            request.getSession().removeAttribute("admin");
        }

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
        // 启用md5加密页面传来的密码
        String password = admin.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        admin.setPassword(password);

        if (admin.getType() != 0 && admin.getType() != 1 && admin.getType() != 2) {
            return R.error("信息错误");
        }

        if (adminService.save(admin)) {
            // 管理员新增日志
            logsService.saveLog("增加管理员", "增加了 “ " + admin.getName() + " ” 管理员");

            return R.success("添加成功");

        } else {
            return R.error("网络错误，请稍后再试");
        }

    }


    /**
     * 分页查询、根据管理员名字查询
     * author cclucky
     * @param offset
     * @param limit
     * @param condition
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int offset, int limit, String condition) {
        // 构建分页构造器
        Page<Admin> pageInfo = new Page(offset, limit);
        Page<AdminDto> adminDtoPage = new Page();

        // 构建条件构造器
        LambdaQueryWrapper<Admin> adminLambdaQueryWrapper = new LambdaQueryWrapper<>();
        adminLambdaQueryWrapper.like(condition != null, Admin::getName, condition);
        // 添加排序
        adminLambdaQueryWrapper.orderByAsc(Admin::getType);

        // 执行查询
        adminService.page(pageInfo, adminLambdaQueryWrapper);

        // 查询不显示密码
        List<Admin> records = pageInfo.getRecords();
        List<AdminDto> list = records.stream().map((item) -> {
            AdminDto adminDto = new AdminDto();
            BeanUtils.copyProperties(item, adminDto);

            Long adminId = item.getId();
            String name = item.getName();
            String email = item.getEmail();
            Integer type = item.getType();
            Integer status = item.getStatus();
            LocalDateTime createTime = item.getCreateTime();

            adminDto.setId(adminId);
            adminDto.setName(name);
            adminDto.setEmail(email);
            adminDto.setType(type);
            adminDto.setStatus(status);
            adminDto.setCreateTime(createTime);

            return adminDto;
        }).collect(Collectors.toList());

        adminDtoPage.setRecords(list);

        return  R.success(adminDtoPage);
    }

    /**
     * 更新管理员数据
     * author cclucky
     * @param admin
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Admin admin) {
        // 管理员更改日志
        logsService.saveLog("修改管理员", "修改 “ " + admin.getName() + " ”管理员的基本信息");

        boolean check1 = admin.getType().equals(0);
        boolean check2 = admin.getType().equals(1);
        boolean check3 = admin.getType().equals(2);
        if (check1 && check2 && check3) {
            return R.error("信息错误");
        }

        adminService.updateById(admin);

        return R.success("数据修改成功");
    }

    /**
     * 删除管理员
     * author cclucky
     */

    @DeleteMapping
    public R<String> delete(Long id) {

        Admin admin = adminService.getById(id);

        // 管理员删除日志
        logsService.saveLog("删除管理员", "删除 “ " + admin.getName() + " ” 管理员");

        adminService.removeById(id);

        return R.success("删除成功");
    }


    /**
     * 获取管理员信息
     * @return
     */
    @GetMapping
    public R<Admin> getAdmin(){
        return R.success(adminService.getById(BaseContext.getCurrentId()));
    }

}


