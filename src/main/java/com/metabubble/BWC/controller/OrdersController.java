package com.metabubble.BWC.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.OrdersConverter;
import com.metabubble.BWC.dto.OrdersDto;
import com.metabubble.BWC.entity.*;
import com.metabubble.BWC.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TeamService teamService;

    /**
     * 用户端查看全部订单（根据状态查询）
     * @param id 用户id
     * @param offset 页码
     * @param limit 分页条数
     * @param status 订单状态
     * @return
     * @author leitianyu999
     */
    @GetMapping("/user/page")
    public R<Page> userPage(int id,int offset, int limit,String status){

        //分页构造器
        Page<Orders> pageSearch = new Page(offset,limit);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();

        if (StringUtils.isNotEmpty(status)) {
            int i = Integer.parseInt(status);
            queryWrapper.eq(Orders::getStatus,i);
        }
        queryWrapper.eq(!String.valueOf(id).equals(""),Orders::getUserId,id);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getUpdateTime);
        ordersService.page(pageSearch,queryWrapper);
        return R.success(pageSearch);
    }

    /**
     * 用户端根据订单id查询订单详情
     * @param id 订单id
     * @return
     * @author leitianyu999
     */
    @GetMapping("/user")
    public R<OrdersDto> selectByOrdersId(int id){
        Orders orders = ordersService.getById(id);
        OrdersDto ordersDto = OrdersConverter.INSTANCES.OrdersToMerOrdersDto(orders);
        return R.success(ordersDto);
    }

    /**
     * 用户端接取任务创建订单
     * @param userId 用户id
     * @param taskId 任务id
     * @return
     * @author leitianyu999
     */
    @PostMapping("add")
    @Transactional
    public R<String> add(Long userId,Long taskId){
        //查询任务是否启用
        if (taskService.checkTaskStatus(taskId)) {
            //更新任务数量
            taskService.updateAmount(taskId);
            Orders orders = new Orders();
            //添加用户id
            orders.setUserId(userId);
            //添加任务id
            orders.setTaskId(taskId);
            //添加订单状态0
            orders.setStatus(0);
            //查找任务
            Task task = taskService.getById(taskId);
            //查找商家
            Merchant merchant = merchantService.getById(task.getMerchantId());
            //添加商家id
            orders.setMerchantId(merchant.getId());
            //查询用户是否为会员
            Boolean aBoolean = userService.checkGrade(userId);
            if (aBoolean){
                //添加返现金额
                orders.setRebate(task.getRebateB());
            }else {
                //添加返现金额
                orders.setRebate(task.getRebateA());
            }
            //添加订单过期时间
            orders = ordersService.addExpiredTime(orders);
            //保存orders
            ordersService.save(orders);

            return R.success("订单生成成功");
        }
        return R.error("订单生成错误");
    }

    /**
     * 用户端提交一审核资料
     * @param orders 审核资料
     * @return
     * @author leitianyu999
     */
    @PutMapping("/firstaudit")
    public R<String> firstAudit(@RequestBody Orders orders){
        //判断PicOrder是否为空
        if (orders.getPicOrder()==null){
            return R.error("无订单截图");
        }
        //判断Amount是否为空
        if (orders.getAmount()==null){
            return R.error("无订单金额");
        }
        //判断OrderNumber是否为空
        if (orders.getOrderNumber()==null){
            return R.error("无订单编号");
        }
        //判断订单是否过期
        if (!ordersService.updateStatusFormExpiredTime(orders.getId())) {
            return R.error("订单已过期");
        }

        Orders orders1 = ordersService.getById(orders);
        //添加订单金额
        orders1.setAmount(orders.getAmount());
        orders.setStatus(orders1.getStatus());
        //判断订单金额是否符合要求
        if (!ordersService.checkByMachine(orders1)) {
            return R.error("订单金额不对");
        }
        //判断订单状态
        if (orders.getStatus()==0||orders.getStatus()==3) {
            //更改订单状态为一审待审核
            orders.setStatus(1);

            ordersService.updateById(orders);
            return R.success("上传成功");
        }
        return R.error("订单状态出错");
    }

    /**
     * 用户提交二审资料
     * @param orders 二审资料
     * @return
     * @author leitianyu999
     */
    @PutMapping("/secondaudit")
    public R<String> secondAudit(@RequestBody Orders orders){
        //查询订单是否过期
        if (!ordersService.updateStatusFormExpiredTime(orders.getId())) {
            return R.error("订单已过期");
        }

        Orders orders1 = ordersService.getById(orders);
        //添加订单状态
        orders.setStatus(orders1.getStatus());
        if (orders.getStatus()==2||orders.getStatus()==5) {
            //跟新状态
            orders.setStatus(4);

            ordersService.updateById(orders);
            return R.success("上传成功");
        }
        return R.error("订单出错");
    }

    /**
     * 管理端分页查询
     * @param name 用户名称
     * @param offset    页码
     * @param limit 分页条数
     * @param merchantName  商家名称
     * @param status 状态
     * @return
     * @author leitianyu999
     */
    @GetMapping("/page")
    @Transactional
    public R<Page> page(String name,int offset,int limit,String merchantName,String status){
        Page<Orders> page = new Page(offset,limit);

        LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper();

        LambdaQueryWrapper<Merchant> queryWrapper2 = new LambdaQueryWrapper();

        LambdaQueryWrapper<Orders> queryWrapper3 = new LambdaQueryWrapper<>();
        //判断name是否为空
        if (name!=null){
            queryWrapper1.like(User::getName,name);
            List<User> list = userService.list(queryWrapper1);
            //判断name在用户表查的数据是否存在
            if (list!=null&&list.size()!=0){
                queryWrapper3.and(ordersLambdaQueryWrapper -> {
                    for (User user : list) {
                        ordersLambdaQueryWrapper.or().eq(Orders::getUserId,user.getId());
                    }
                });
            }else {
                return R.error("查询无此用户");
            }
        }
        //判断merchantName是否为空
        if (merchantName!=null){
            queryWrapper2.like(Merchant::getName,merchantName);
            List<Merchant> list1 = merchantService.list(queryWrapper2);
            //判断merchantName在商家表查的数据是否存在
            if (list1!=null&&list1.size()!=0){
                queryWrapper3.and(ordersLambdaQueryWrapper -> {
                    for (Merchant merchant : list1) {
                        ordersLambdaQueryWrapper.or().eq(Orders::getMerchantId,merchant.getId());
                    }
                });
            }else {
                return R.error("查询无此商户");
            }
        }

        //添加状态判断
        queryWrapper3.eq(StringUtils.isNotEmpty(status),Orders::getStatus,status);
        //根据创建时间排序
        queryWrapper3.orderByDesc(Orders::getUpdateTime);



        ordersService.page(page,queryWrapper3);
        return R.success(page);
    }

    /**
     * 管理端根据订单id查询订单详情
     * @param id 订单id
     * @return
     * @author leitianyu999
     */
    @GetMapping
    public R<Orders> get(int id){
        Orders byId = ordersService.getById(id);
        return R.success(byId);
    }

    /**
     * 后台审核订单
     * @param id
     * @return
     * @author leitianyu999
     */
    @PostMapping("/commit")
    @Transactional
    public R<String> firstAudit(Long id){
        Orders orders = ordersService.getById(id);
        Integer status = orders.getStatus();
        //待一审
        if (status==1) {
            //添加一审成功状态
            orders.setStatus(2);
            //更改过期时间
            orders = ordersService.addExpiredTime(orders);
            //添加审核人id
            orders.setReviewerIdA(BaseContext.getCurrentId());
            ordersService.updateById(orders);
            return R.success("一审成功");
        }
        //待二审
        if (status==4){
            //查询用户会员等级并跟新订单金额
            orders = ordersService.updateRebate(orders);
            //获取团队资料
            LambdaQueryWrapper<Team> queryWrapper123 = new LambdaQueryWrapper<>();
            queryWrapper123.eq(Team::getUserId,orders.getUserId());
            Team team = teamService.getOne(queryWrapper123);
            //订单状态改成完成
            orders.setStatus(6);
            //用户返现金额到账
            userService.cashback(orders);
            //给上一级返现
            if (team.getUpUser01Id()!=null) {
                teamService.cashbackForUserFromFirst(team.getUpUser01Id());
            }
            //给上二级返现
            if (team.getUpUser02Id()!=null){
                teamService.cashbackForUserFromSecond(team.getUpUser02Id());
            }
            //添加审核人id
            //orders.setReviewerIdB(BaseContext.getCurrentId());
            //更新订单状态
            ordersService.updateById(orders);
            return R.success("二审成功");
        }
        return R.error("订单状态错误");
    }

    /**
     * 后台审核不通过
     * @param orders
     * @return
     * @author leitianyu999
     */
    @PutMapping("audit")
    public R<String> auditFailed(@RequestBody Orders orders){
        Orders orders1 = ordersService.getById(orders);
        //待一审
        if (orders1.getStatus()==1){
            orders1.setStatus(3);
            //添加理由
            orders1.setReason(orders.getReason());
            //添加审核人id
            orders.setReviewerIdA(BaseContext.getCurrentId());
            ordersService.updateById(orders1);
            return R.success("更改成功");
        }
        //待二审
        if (orders1.getStatus()==4){
            orders1.setStatus(5);
            //添加理由
            orders1.setReason(orders.getReason());
            //添加审核人id
            orders.setReviewerIdB(BaseContext.getCurrentId());
            ordersService.updateById(orders1);
            return R.success("更改成功");
        }
        return R.error("订单状态错误");
    }

    /**
     * 用户取消订单
     * @param id
     * @return
     * @author leitianyu999
     */
    @DeleteMapping("/user")
    public R<String> cancelOrders(Long id){
        Orders orders = ordersService.getById(id);
        Integer status = orders.getStatus();
        //判断订单是否可取消
        if (status==0||status==1||status==2||status==3||status==4||status==5) {
            orders.setStatus(7);
            ordersService.updateById(orders);
            return R.success("取消订单成功");
        }
        //判断订单是否已完成
        if (status==6) {
            return R.error("订单已完成");
        }
        //判断订单是否已过期
        if (status==8) {
            return R.error("订单已过期");
        }
        //订单已取消
        return R.error("订单已取消");

    }
}
