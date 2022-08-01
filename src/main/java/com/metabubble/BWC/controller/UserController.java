package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.UserConverter;
import com.metabubble.BWC.dto.UserDto;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.TeamService;
import com.metabubble.BWC.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TeamService teamService;

    /**
     * 后台添加用户
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
     * 分页查询
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
     * 修改用户数据
     * @param user  修改的用户属性
     * @return
     * @author leitianyu999
     */
    @PutMapping
    public R<String> update(@RequestBody User user){

        userService.updateById(user);
        return R.success("修改成功");

    }

    /**
     * 根据主键id查询用户
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
     * 根据主键id删除用户
     * @param id 用户id
     * @return
     * @author leitianyu999
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long id){
        userService.removeById(id);
        return R.success("删除成功");
    }


    /**
     * 用户端根据用户id查询用户信息
     * @param id 用户id
     * @return
     * @author leitianyu999
     */
    @GetMapping("/getuser")
    public R<UserDto> getByIdForUser(@RequestParam Long id){
        //条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(User::getId,id);
        User user = userService.getOne(queryWrapper);

        if (user!=null) {

            UserDto userDto = UserConverter.INSTANCES.toUserRoleDto(user);
            System.out.println(user);
            System.out.println(userDto);
            UserDto userDto1 = userDto;
            return R.success(userDto1);
        }
        return R.error("没有查询到对应员工信息");
    }


    /**
     * 用户端修改用户数据
     * @param userDto  修改的用户属性
     * @return
     * @author leitianyu999
     */
    @PutMapping("/getuser")
    public R<String> updateForUser(@RequestBody UserDto userDto){
        //条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        User user = UserConverter.INSTANCES.toUserDtoRoleUser(userDto);
        queryWrapper.eq(User::getId,user.getId());
        userService.update(user,queryWrapper);
        return R.success("修改成功");

    }


    /**
     * 用户端根据邀请码添加上级
     * @param invitation 邀请码
     * @param id    用户id
     * @return
     * @author leitianyu999
     */
    @PutMapping("/user/invitation")
    @Transactional
    public R<String> addTeam(String invitation ,int id){
        //查询用户对象
        User user = userService.getById(id);
        if (user.getUpId()==null) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //添加验证码对比
            queryWrapper.eq(User::getUpId,invitation);
            //查询上级对象
            User userFirst = userService.getOne(queryWrapper);
            if (userFirst==null){
                return R.error("查无此邀请码");
            }
            //团队添加上级
            teamService.addTeamTop(user,userFirst);

            //用户添加上级邀请码
            user.setUpId(invitation);
            userService.updateById(user);
            return R.success("添加成功");
        }
        return R.error("已填写邀请码");
    }


    @PutMapping("/creatuser")
    @Transactional
    public R<String> creatUser(@RequestBody User user){

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(User::getTel,user.getTel());

        User one = userService.getOne(queryWrapper);
        if (one==null) {
            String uuid = userService.createUUID();
            user.setDownId(uuid);
            userService.save(user);
            User serviceOne = userService.getOne(queryWrapper);
            teamService.save(serviceOne.getId());
            return R.success("添加成功");
        }
        return R.error("已有用户");
    }

}
