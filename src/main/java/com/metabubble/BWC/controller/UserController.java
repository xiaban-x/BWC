package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.UserConverter;
import com.metabubble.BWC.dto.UserDto;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.service.TeamService;
import com.metabubble.BWC.service.UserService;
import com.metabubble.BWC.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private LogsService logsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ManageSession manageSession;


    String stringSession = "session";
    String userId = "userId";
    String userKey = "userKey";

    /**
     * 管理端添加用户
     * @param user  用户对象
     * @return
     * @author leitianyu999
     */
    @PostMapping
    public R<String> add(@RequestBody User user){
        userService.save(user);
        return R.success("添加成功");
    }


    /**
     * 管理端分页查询
     * @param offset 页码
     * @param limit 条数
     * @param wxId 微信号
     * @param grade 会员信息
     * @param tel 电话号
     * @return
     * @author leitianyu999
     */
    @GetMapping("/page")
    public R<Page> page(int offset, int limit,String wxId,String grade,String tel){

        //分页构造器
        Page<User> pageSearch = new Page(offset,limit);
        //条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(wxId),User::getWxId,wxId);
        queryWrapper.like(StringUtils.isNotEmpty(tel),User::getTel,tel);
        queryWrapper.eq(StringUtils.isNotEmpty(grade),User::getGrade,grade);
        //添加排序条件
        queryWrapper.orderByDesc(User::getCreateTime);

        userService.page(pageSearch,queryWrapper);


        return R.success(pageSearch);

    }

    /**
     * 管理端修改用户数据
     * @param user  修改的用户属性
     * @return
     * @author leitianyu999
     */
    @PutMapping
    public R<String> update(@RequestBody User user){

        userService.updateById(user);
        logsService.saveLog("修改用户", "管理员“"+BaseContext.getCurrentId()+"”修改了"+user.getName()+"的基本信息");
        return R.success("修改成功");

    }

    /**
     * 管理端根据主键id查询用户
     * @param id 用户id
     * @return
     * @author leitianyu999
     */
    @GetMapping()
    public R<User> getById(@RequestParam Long id){
        User user = userService.getById(id);

        if (user!=null) {
            return R.success(user);
        }
        return R.error("没有查询到对应员工信息");
    }



    /**
     * 管理端根据主键id删除用户
     * @param id 用户id
     * @return
     * @author leitianyu999
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long id){
        User byId = userService.getById(id);
        userService.removeById(id);
        logsService.saveLog("删除用户","管理员”"+BaseContext.getCurrentId()+"“删除了"+byId.getName()+"用户");
        return R.success("删除成功");
    }

    /**
     * 管理端管理员封禁用户账号
     * @param id    用户id
     * @param reason    封禁理由
     * @return
     */
    @DeleteMapping("/ban")
    public R<String> banUser(Long id,String reason){
        User user = new User();
        user.setStatus(1);
        user.setReason(reason);
        user.setId(id);
        userService.updateById(user);
        logsService.saveLog("用户封禁","管理员”"+BaseContext.getCurrentId()+"封禁用户"+id+"，理由是："+reason);
        return R.success("封禁成功");
    }

    /**
     * 管理端管理员解封用户账号
     * @param id
     * @param reason
     * @return
     */
    @PutMapping("/unban")
    public R<String> unbanUser(Long id,String reason){
        User user = new User();
        user.setStatus(0);
        user.setReason(reason);
        user.setId(id);
        userService.updateById(user);
        logsService.saveLog("用户解封","管理员”"+BaseContext.getCurrentId()+"解封用户"+id+"，理由是："+reason);
        return R.success("封禁成功");
    }


    /**
     * 用户端查询用户信息
     * @return
     * @author leitianyu999
     */
    @GetMapping("/getuser")
    public R<UserDto> getByIdForUser(HttpServletResponse response, HttpServletRequest request){
        Long id = BaseContext.getCurrentId();
        User o = (User) redisTemplate.opsForValue().get(userKey+id);
        if (o!=null){
            UserDto userDto = UserConverter.INSTANCES.toUserRoleDto(o);
            return R.success(userDto);
        }
        //条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        //添加用户id比对
        queryWrapper.eq(User::getId,id);
        User user = userService.getOne(queryWrapper);

        if (user!=null) {

            if (user.getStatus()==1){
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
                return R.error("您的账号已被封禁，理由是："+user.getReason());
            }

            UserDto userDto = UserConverter.INSTANCES.toUserRoleDto(user);
            //redisTemplate.opsForValue().set(userKey+id,user,24, TimeUnit.HOURS);
            return R.success(userDto);
        }
        return R.error("没有查询到对应用户信息");
    }


    /**
     * 用户端修改用户数据
     * @param userDto1  修改的用户属性
     * @return
     * @author leitianyu999
     */
    @PutMapping("/getuser")
    public R<String> updateForUser(@RequestBody UserDto userDto1){
        Long id = BaseContext.getCurrentId();
        UserDto userDto = new UserDto();
        userDto.setId(id);
        if (userDto1.getName()!=null) {
            userDto.setName(userDto1.getName());
        }
        if (userDto1.getAvatar()!=null){
            userDto.setAvatar(userDto1.getAvatar());
        }
        User user = UserConverter.INSTANCES.toUserDtoRoleUser(userDto);
        userService.updateById(user);
        redisTemplate.delete(userKey+id);
        return R.success("修改成功");

    }


    /**
     * 用户端根据邀请码添加上级
     * @param invitation 邀请码
     * @return
     * @author leitianyu999
     */
    @PutMapping("/user/invitation")
    @Transactional
    public R<String> addTeam(String invitation){
        Long id = BaseContext.getCurrentId();
        User user = (User) redisTemplate.opsForValue().get(userKey+id);
        if (user==null){
            //查询用户对象
            user = userService.getById(id);
        }
        //判断邀请码是否为用户本身邀请码
        if (user.getDownId().equals(invitation)){
            return R.error("邀请码错误");
        }
        //判断用户是否为有上级用户
        if (user.getUpId()==null) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //添加验证码对比
            queryWrapper.eq(User::getDownId,invitation);
            //查询上级对象
            User userFirst = userService.getOne(queryWrapper);
            //判断是否有上级对象
            if (userFirst==null){
                return R.error("查无此邀请码");
            }

            //团队添加上级
            teamService.addTeamTop(user,userFirst);

            //用户添加上级邀请码
            user.setUpId(invitation);
            userService.updateById(user);
            redisTemplate.delete(userKey+id);
            return R.success("添加成功");
        }
        return R.error("已填写邀请码");
    }

    /**
     * 测试用添加用户
     * @param user 用户资料
     * @return
     * @author leitianyu999
     */
    @PutMapping("/creatuser")
    @Transactional
    public R<String> creatUser(@RequestBody User user){

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //添加id条件判断
        queryWrapper.eq(User::getTel,user.getTel());
        //查询是否有该用户
        User one = userService.getOne(queryWrapper);
        if (one==null) {
            //生成邀请码
            String uuid = userService.createUUID();
            //添加邀请码
            user.setDownId(uuid);
            //保存用户信息
            userService.save(user);
            User serviceOne = userService.getOne(queryWrapper);
            //创建团队表
            teamService.save(serviceOne);
            return R.success("添加成功");
        }
        return R.error("已有用户");
    }

    /**
     * 测试用添加用户
     * @RequsetBody wxId 用户资料
     * @return
     * @author Kenlihankun
     */

    @PostMapping("/bindWX")
    @Transactional
    public R<String> bindWX(@RequestBody User user){
        Long userId = BaseContext.getCurrentId();
        User user1 = userService.getById(userId);
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        if (user1.getWxId() == null){
            user1.setWxId(user.getWxId());
            userService.save(user1);
        }
        if (user1.getWxId()!=null) {
            userUpdateWrapper.eq("id",userId);
            user1.setWxId(user.getWxId());
            userService.update(user1,userUpdateWrapper);
        }


        return R.success("success");
    }

}
