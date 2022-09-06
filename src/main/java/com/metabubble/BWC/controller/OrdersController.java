package com.metabubble.BWC.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.OrdersConverter;
import com.metabubble.BWC.dto.Imp.PageConverter;
import com.metabubble.BWC.dto.OrdersDo;
import com.metabubble.BWC.dto.OrdersDto;
import com.metabubble.BWC.dto.OrdersListDto;
import com.metabubble.BWC.entity.*;
import com.metabubble.BWC.service.*;
import com.metabubble.BWC.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private LogsService logsService;
    @Autowired
    private ManageSession manageSession;
    @Autowired
    private RedisTemplate redisTemplate;

    String normal = "normal";
    String right = "right";
    String stringSession = "session";
    String userId = "userId";
    String userKey = "userKey";

    /**
     * 用户端查看全部订单（根据状态查询）
     * @param offset 页码
     * @param limit 分页条数
     * @param status 订单状态
     * @return
     * @author leitianyu999
     */
    @GetMapping("/user/page")
    @Transactional
    public R<Page> userPage(int offset, int limit,@RequestParam List<String> status){
        Long id = BaseContext.getCurrentId();
        //分页构造器
        Page<Orders> pageSearch = new Page(offset,limit);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(!String.valueOf(id).equals(""),Orders::getUserId,id);
        if (!(status.size()==1&&status.get(0).equals("9"))) {
            if (status.size()!=0&&status!=null) {
                queryWrapper.and(ordersLambdaQueryWrapper -> {
                    for (String o : status) {
                        int i = Integer.parseInt(o);
                        ordersLambdaQueryWrapper.or().eq(Orders::getStatus,i);
                    }
                });
            }
        }

        //添加排序条件
        queryWrapper.orderByDesc(Orders::getUpdateTime);
        ordersService.page(pageSearch,queryWrapper);

        List<OrdersListDto> collect = pageSearch.getRecords().stream().map(item -> {
            Orders orders = ordersService.updateStatusFormExpiredTimeAndReturn(item);
            Long merchantId = orders.getMerchantId();
            OrdersListDto ordersListDto = OrdersConverter.INSTANCES.OrdersToOrdersListDto(orders);
            Merchant merchant = merchantService.getById(merchantId);
            ordersListDto.setMerchantPic(merchant.getPic());
            return ordersListDto;
        }).collect(Collectors.toList());

        Page page = PageConverter.INSTANCES.PageToPage(pageSearch);
        page.setRecords(collect);

        return R.success(page);
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
        Task task = taskService.getById(orders.getTaskId());
        OrdersDto ordersDto = OrdersConverter.INSTANCES.OrdersToMerOrdersDto(orders);
        ordersDto.setRequirement(task.getRequirement());
        ordersDto.setRemark(task.getRemark());
        return R.success(ordersDto);
    }

    /**
     * 用户端接取任务创建订单
     * @param taskId 任务id
     * @return
     * @author leitianyu999
     */
    @PostMapping("/user/add")
    @Transactional
    public R<String> add(Long taskId, HttpServletResponse response, HttpServletRequest request){
        Map<String, String> status = userService.findStatus(BaseContext.getCurrentId());
        if (!(status.get(normal)!=null&&status.get(normal).equals(right))){
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
                return R.error("您的账号已被封禁，理由是："+status.get("ban"));
            }
        }
        //查询任务是否启用
        if (taskService.checkTaskStatus(taskId)) {
            Long userId = BaseContext.getCurrentId();

            User user = userService.getById(userId);
            //查找任务
            Task task = taskService.getById(taskId);
            if (merchantService.checkBlackList(user.getTel(),task.getMerchantId())){
                return R.error("您进入了该商家的黑名单，不可接取此任务");
            }
            if (!taskService.checkOrders(userId,task)){
                return R.error("用户"+task.getTimeInterval()+"日内已接过此任务");
            }
            if (taskService.checkTime(task)){
                task.setStatus(0);
                taskService.updateById(task);
                return R.error("该任务不在可接取时间范围！");
            }
            //更新任务数量
            taskService.updateAmount(taskId);
            Orders orders = new Orders();
            //添加用户id
            orders.setUserId(userId);
            //添加任务id
            orders.setTaskId(taskId);
            //添加订单状态0
            orders.setStatus(0);
            //订单添加任务信息
            orders.setTaskName(task.getName());
            //查找商家
            Merchant merchant = merchantService.getById(task.getMerchantId());
            //添加商家id
            orders.setMerchantId(merchant.getId());
            orders.setMerchantName(merchant.getName());
            //查询用户是否为会员
            Boolean aBoolean = userService.checkGrade(userId);
            if (aBoolean){
                //添加返现金额
                orders.setRebate(task.getRebateB());
                //添加最低消费金额
                orders.setMinConsumption(task.getMinConsumptionB());
                //添加平台类型
                orders.setPlatform(task.getPlatform());
                //添加订单会员信息
                orders.setGrade(1);
            }else {
                //添加返现金额
                orders.setRebate(task.getRebateA());
                //添加最低消费金额
                orders.setMinConsumption(task.getMinConsumptionA());
                //添加平台类型
                orders.setPlatform(task.getPlatform());
                //添加订单会员信息
                orders.setGrade(0);
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
    @PutMapping("/user/firstaudit")
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
        orders.setUserId(BaseContext.getCurrentId());
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
            orders.setStatus(2);
            orders = ordersService.addExpiredTime(orders);
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
    @PutMapping("/user/secondaudit")
    public R<String> secondAudit(@RequestBody Orders orders){
        //查询订单是否过期
        if (!ordersService.updateStatusFormExpiredTime(orders.getId())) {
            return R.error("订单已过期");
        }
        orders.setUserId(BaseContext.getCurrentId());
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
    public R<Page> page(String name,int offset,int limit,String merchantName,@RequestParam List<String> status,String orderNumber,String tel){
        Page<Orders> page = new Page(offset,limit);

        LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper();

        LambdaQueryWrapper<Merchant> queryWrapper2 = new LambdaQueryWrapper();

        LambdaQueryWrapper<Orders> queryWrapper3 = new LambdaQueryWrapper<>();
        //判断name或tel是否为空
        if (StringUtils.isNotEmpty(name) ||StringUtils.isNotEmpty(tel)){
            queryWrapper1.like(StringUtils.isNotEmpty(name),User::getName,name);
            queryWrapper1.like(StringUtils.isNotEmpty(tel),User::getTel,tel);
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

        //
        if (!(status.size()==1&&status.get(0).equals("9"))) {
            if (status.size()!=0&&status!=null) {
                queryWrapper3.and(ordersLambdaQueryWrapper -> {
                    for (String o : status) {
                        int i = Integer.parseInt(o);
                        ordersLambdaQueryWrapper.or().eq(Orders::getStatus,i);
                    }
                });
            }
        }
        //添加orderNumber
        queryWrapper3.like(StringUtils.isNotEmpty(orderNumber),Orders::getOrderNumber,orderNumber);
        //根据创建时间排序
        queryWrapper3.orderByDesc(Orders::getUpdateTime);



        ordersService.page(page,queryWrapper3);

        List<Orders> records = page.getRecords();
        List<OrdersDo> collect = records.stream().map(item -> {
            Long userId = item.getUserId();
            User byId = userService.getById(userId);
            OrdersDo ordersDo = OrdersConverter.INSTANCES.OrdersToOrdersDo(item);
            ordersDo.setTel(byId.getTel());
            return ordersDo;
        }).collect(Collectors.toList());

        Page pageToPage = PageConverter.INSTANCES.PageToPage(page);
        pageToPage.setRecords(collect);


        return R.success(pageToPage);
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
    public R<String> Audit(Long id){
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
            logsService.saveLog("订单审核","管理员”"+BaseContext.getCurrentId()+"”通过"+orders.getUserId()+"用户一审");
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
            //任务完成数量加一
            taskService.addCompleted(orders);
            //用户返现金额到账
            userService.cashback(orders);
            //获取用户对象
            User user = userService.getById(orders.getUserId());
            //给上一级返现
            if (team.getUpUser01Id()!=null) {
                teamService.cashbackForUserFromFirst(team.getUpUser01Id(),user.getTel());
            }
            //给上二级返现
            if (team.getUpUser02Id()!=null){
                teamService.cashbackForUserFromSecond(team.getUpUser02Id(),user.getTel());
            }
            Long admin = BaseContext.getCurrentId();
            //添加审核人id
            orders.setReviewerIdB(admin);
            //更新订单状态
            ordersService.updateById(orders);
            logsService.saveLog("订单审核","管理员”"+admin+"”通过"+orders.getUserId()+"用户二审");
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
            logsService.saveLog("订单审核","管理员”"+BaseContext.getCurrentId()+"”不通过"+orders.getUserId()+"用户一审");
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
            logsService.saveLog("订单审核","管理员”"+BaseContext.getCurrentId()+"”不通过"+orders.getUserId()+"用户一审");
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
