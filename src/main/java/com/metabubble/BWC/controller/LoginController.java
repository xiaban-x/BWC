package com.metabubble.BWC.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.UserConverter;
import com.metabubble.BWC.dto.UserDto;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.ConfigService;
import com.metabubble.BWC.service.TeamService;
import com.metabubble.BWC.service.UserService;
import com.metabubble.BWC.utils.CookieUtils;
import com.metabubble.BWC.utils.MobileUtils;
import com.metabubble.BWC.utils.SMSUtils;
import com.metabubble.BWC.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TeamService teamService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private ManageSession manageSession;

    String userKey = "userKey";
    String stringSession = "session";
    String userId = "userId";
    /**
     * 发送验证码
     * @param mobile    手机号
     * @param type      验证码用途
     * @param imgCode   图形验证码
     * @param request
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> findCode(String mobile , String type, String imgCode, HttpServletRequest request){
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(type)) {

            return R.error("请求参数不全");
        }

        mobile = mobile.trim();
        // 判断传入的手机号格式是否正确
        if (mobile.length() != 11 || !MobileUtils.isMobileNum(mobile)) {

            return R.error("手机号格式不正确");
        }
        HttpSession session = request.getSession();
        String checkCodeGen = (String)session.getAttribute("checkCodeGen");

        if (!imgCode.equalsIgnoreCase(checkCodeGen)){
            return R.error("图形验证码错误");
        }

        //生成验证码

        //String mobileCode = ValidateCodeUtils.generateValidateCode(4).toString();
        String mobileCode = "123456";
        log.info("验证码是："+mobileCode);

        String modelCode = null;

        switch (type) {
            case "register":// 发送注册的短信验证码

                //判断手机号是否注册
                if(userService.findUser(mobile)){//伪代码
                    return R.error("当前手机号已注册，请直接登录");
                }

                modelCode = "";
                break;

            case "reset":// 发送重置登录密码的短信验证码

                //判断手机号是否注册
                if(!userService.findUser(mobile)){//伪代码
                    return R.error("当前手机号未注册，请先注册");
                }

                modelCode = "";
                break;
            case "login":

                //判断手机号是否注册
                if (!userService.findUser(mobile)){
                    return R.error("当前手机号未注册，请先注册");
                }

                break;
            default:

                return R.error("非法请求");
        }

        //
        String mobileKey = type+"_mobile_" + mobile;
        //
        String todayKey = "today_mobile_code_times_" + mobile;

        Long times = redisTemplate.getExpire(mobileKey);
        if (times!=null) {
            if (times>=240){
                return R.error("距离您上次发送验证码不足一分钟，请一分钟后再尝试获取");
            } else if (times==-1){
                throw new CustomException("redis储存有误");
            }
        }

        // 判断当前手机号今天发送密码次数是否已达上线,每天15条（具体条数根据自己的需求调用）
        String todayTimes = (String) redisTemplate.opsForValue().get(todayKey);
        int todayCount = 1;
        if (todayTimes != null) {
            todayCount = new Integer(todayTimes);
            if (todayCount >= 15) {

                return R.error("当前手机号今日发送验证码已达上限，请明日再来");
            }
            todayCount++;
        }

        Boolean msg = null;//发送短信验证码是否成功与失败

//        try {

            //发送短信验证码，请求成功后返回指定标识，请求失败，可以返回失败的信息，
            //方便开发人员排查bug。此处使用的是阿里云的短信服务，
            //你也可以使用其他的短信服务，此处不做赘述
            //调用阿里云短信服务
            msg = SMSUtils.sendMessage("瑞吉外卖",modelCode,mobile,mobileCode);

            log.info("手机号:" + mobile + " 的验证码是：" + mobileCode);

//            if (msg) {

                // 保存验证码到redis
                redisTemplate.opsForValue().set(mobileKey, mobileCode, 60 * 5 + 5, TimeUnit.SECONDS);//redis中的code比实际要多5秒

                // 记录本号码发送验证码次数
                redisTemplate.opsForValue().set(todayKey, todayCount + "", MobileUtils.getSurplusTime(),TimeUnit.SECONDS);



//            } else {
//                return R.error("短信验证码发送失败");
//            }

//        } catch (Exception e) {
//            log.info("获取手机验证码异常：" + e.getMessage());
//            return R.error("获取短信验证码异常");
//        }

        return R.success("发送成功");
    }

    /**
     * 用户登录
     * @param mobile    手机号
     * @param password  密码
     * @param contents  手机验证码
     * @param type      登录方式
     * @param request
     * @return
     */
    @PostMapping()
    public R<UserDto> login(String mobile,String password,String contents,String type, HttpServletRequest request,HttpServletResponse response){
        // 判断请求参数是否正确
        if (StringUtils.isBlank(mobile)) {
            return R.error("缺少必要的参数");
        }

        mobile = mobile.trim();
        // 判断传入的手机号格式是否正确
        if (mobile.length() != 11 || !MobileUtils.isMobileNum(mobile)) {
            return R.error("传入的手机号格式不正确");
        }

        if (!userService.findUser(mobile)){
            return R.error("该用户未注册");
        }

        // 判断当前手机号的登录失败次数，防止有人暴力破解用户的密码
        String limitKey =  mobile + "_login_error_times";
        String limitTimes = (String) redisTemplate.opsForValue().get(limitKey);
        Integer times = 1;
        if (limitTimes != null) {
            if (new Integer(limitTimes).intValue() >= 6) {
                return R.error("当前账号今日登录失败次数超过6次，为保证您的账号安全，系统已锁定当前账号，您可明天再登录或立即重置密码后使用新密码登录！");
            }
            times = new Integer(limitTimes) + 1;
        }


        switch (type){
            case "password":
                if (StringUtils.isBlank(password)) {
                    return R.error("缺少必要的参数");
                }
                //1.密码进行md5加密处理
                password = DigestUtils.md5DigestAsHex(password.getBytes());
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getTel,mobile);
                User user = userService.getOne(queryWrapper);
                if (user.getPassword().equals(password)){
                    try {
                        HttpSession httpSession = manageSession.getManageSession().get(user.getId().toString());
                        if (httpSession!=null){
                            //当前session有值，说明1.此帐号处于已登录状态有人正在使用，2.session还在有效期未被销毁
                            log.info("用户:"+user.getId()+"再次登录！");
                            httpSession.invalidate();
                            manageSession.getManageSession().remove(user.getId().toString());
                        }
                    } catch (Exception e) {
                        log.info(e.toString()+"：无用报错");
                    }
                    //6.登陆成功，将员工id存入session
                    HttpSession session = request.getSession();
                    session.setAttribute("user",user.getId());
                    session.setMaxInactiveInterval(1296000);
                    manageSession.getManageSession().put(user.getId().toString(),session);
                    CookieUtils.setCookie(request,response,stringSession,session.getId(),60*60*24*14,true);
                    CookieUtils.setCookie(request,response,userId,user.getId().toString(),60*60*24*14,true);
                    redisTemplate.delete(limitKey);
                    UserDto userDto = UserConverter.INSTANCES.toUserRoleDto(user);
                    //redisTemplate.opsForValue().set(userKey+user.getId(),user,24,TimeUnit.HOURS);
                    return R.success(userDto);
                }else {
                    // 记录密码输入错误数
                    redisTemplate.opsForValue().set(limitKey, times + "", MobileUtils.getSurplusTime(),TimeUnit.SECONDS);

                    return R.error("密码错误");
                }

            case "code":
                if (StringUtils.isBlank(contents)) {
                    return R.error("缺少必要的参数");
                }
                String mobileKey = "login_mobile_" + mobile;
                String code = (String) redisTemplate.opsForValue().get(mobileKey);
                if (code == null) {
                    return R.error("当前验证码已失效，请获取最新验证码后再进行此操作");
                } else if (!code.equals(contents)) {
                    return R.error("您输入的验证码不正确，请重新输入（不用重新获取）");
                } else {
                    LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(User::getTel,mobile);
                    User user1 = userService.getOne(queryWrapper1);
                    try {
                        HttpSession httpSession = manageSession.getManageSession().get(user1.getId().toString());
                        if (httpSession!=null){
                            //当前session有值，说明1.此帐号处于已登录状态有人正在使用，2.session还在有效期未被销毁
                            log.info("用户:"+user1.getId()+"再次登录！");
                            httpSession.invalidate();
                            manageSession.getManageSession().remove(user1.getId().toString());
                        }
                    } catch (Exception e) {
                        log.info(e.toString()+"：无用报错");
                    }
                    //6.登陆成功，将员工id存入session
                    HttpSession session = request.getSession();
                    session.setAttribute("user",user1.getId());
                    session.setMaxInactiveInterval(1296000);
                    manageSession.getManageSession().put(user1.getId().toString(),session);
                    CookieUtils.setCookie(request,response,stringSession,session.getId(),60*60*24*14,true);
                    CookieUtils.setCookie(request,response,userId,user1.getId().toString(),60*60*24*14,true);
                    redisTemplate.delete(mobileKey);
                    redisTemplate.delete(limitKey);
                    UserDto userDto1 = UserConverter.INSTANCES.toUserRoleDto(user1);
                    //redisTemplate.opsForValue().set(userKey+user1.getId(),user1,24,TimeUnit.HOURS);
                    return R.success(userDto1);
                }

            default:
                return R.error("未知请求");
        }
    }


    /**
     * 重置密码
     * @param mobile    手机号
     * @param contents  手机验证码
     * @param password  新密码
     * @param request
     * @return
     */
    @PostMapping("/reset")
    public R<String> resetSecret(String mobile, String contents, String password,HttpServletRequest request,HttpServletResponse response){

        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(contents) || StringUtils.isBlank(password)) {
            return R.error("缺少必要的参数");
        }

        mobile = mobile.trim();
        // 判断传入的手机号格式是否正确
        if (mobile.length() != 11 || !MobileUtils.isMobileNum(mobile)) {
            return R.error("传入的手机号格式不正确");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getTel,mobile);
        List<User> list = userService.list(queryWrapper);
        if (list.size()>1){
            throw new CustomException("手机号注册多名用户");
        }else if (list==null){
            return R.error("此手机号未注册");
        }

        String mobileKey = "reset_mobile_"+mobile;// 存储到redis中的验证码的key

        // 校验短信验证码
        String code = (String) redisTemplate.opsForValue().get(mobileKey);
        if (code == null) {
            return R.error("当前验证码已失效，请获取最新验证码后再进行此操作");
        } else if (!code.equals(contents)) {
            return R.error("您输入的验证码不正确，请重新输入（不用重新获取）");
        }

        // 删除缓存的key
        redisTemplate.delete(mobileKey);
        // 删除用户今日登录失败次数的标识，如果有
        redisTemplate.delete( mobile + "_login_error_times");

        //1.密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        User user = list.get(0);
        user.setPassword(password);
        userService.updateById(user);
        redisTemplate.delete(userKey+user.getId());
        request.getSession().removeAttribute("user");

        try {
            HttpSession httpSession = manageSession.getManageSession().get(BaseContext.getCurrentId().toString());
            if (httpSession!=null){
                httpSession.invalidate();
            }
        } catch (Exception e) {
            log.info(e.toString()+"：无用报错");
        }finally {
            redisTemplate.delete(userKey+BaseContext.getCurrentId());

            //删除session中的账户信息
            request.getSession().removeAttribute("user");
            CookieUtils.deleteCookie(request,response,userId);
            CookieUtils.deleteCookie(request,response,stringSession);
        }

        return R.success("修改成功");
    }

    /**
     * 注册账户
     * @param mobile    手机号
     * @param contents  手机验证码
     * @param password  密码
     * @param request
     * @return
     */
    @PostMapping("/register")
    @Transactional
    public R<String> register(String mobile, String contents,String name, String password,HttpServletRequest request){

        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(contents) || StringUtils.isBlank(password)) {
            return R.error("缺少必要的参数");
        }

        mobile = mobile.trim();
        // 判断传入的手机号格式是否正确
        if (mobile.length() != 11 || !MobileUtils.isMobileNum(mobile)) {
            return R.error("传入的手机号格式不正确");
        }

        if (userService.findUser(mobile)){
            return R.error("该用户已注册");
        }


        String mobileKey = "register_mobile_" + mobile;
        String code = (String) redisTemplate.opsForValue().get(mobileKey);
        if (code == null) {
            return R.error("当前验证码已失效，请获取最新验证码后再进行此操作");
        } else if (!code.equals(contents)) {
            return R.error("您输入的验证码不正确，请重新输入（不用重新获取）");
        }

        redisTemplate.delete(mobileKey);

        //1.密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        User user = new User();
        user.setPassword(password);
        user.setTel(mobile);
        user.setName(name);
        user.setDownId(userService.createUUID());
        userService.save(user);
        teamService.save(user);
        return R.success("创建用户成功");
    }


    /**
     * 用户退出登录
     * author leitianyu999
     * @param request session中的管理员信息
     * @return 返回退出信息
     */
    @DeleteMapping("/logout")
    public R<String> logout(HttpServletRequest request,HttpServletResponse response){
        try {
            HttpSession httpSession = manageSession.getManageSession().get(BaseContext.getCurrentId().toString());
            if (httpSession!=null){
                httpSession.invalidate();
            }
        } catch (Exception e) {
            log.info(e.toString()+"：无用报错");
        }finally {
            redisTemplate.delete(userKey+BaseContext.getCurrentId());

            //删除session中的账户信息
            request.getSession().removeAttribute("user");
            CookieUtils.deleteCookie(request,response,userId);
            CookieUtils.deleteCookie(request,response,stringSession);
        }

        return R.success("退出成功");
    }


}
