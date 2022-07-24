package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.UserConverter;
import com.metabubble.BWC.dto.UserDto;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 分页查询
     * @param offset 页码
     * @param limit 条数
     * @param wxId 微信号
     * @param grade 会员信息
     * @param tel 电话号
     * @author leitianyu999
     * @return
     */
    @PostMapping("/page")
    public R<Page> page(int offset, int limit,String wxId,String grade,String tel){

        //分页构造器
        Page pageSearch = new Page(offset,limit);
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
     * @param id
     * @return
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
}
