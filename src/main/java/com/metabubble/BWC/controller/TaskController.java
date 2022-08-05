package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.Condition;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.HomeDto;
import com.metabubble.BWC.dto.Imp.HomeConverter;
import com.metabubble.BWC.dto.Imp.TaskConverter;
import com.metabubble.BWC.dto.Imp.TaskDetailConverter;
import com.metabubble.BWC.dto.TaskDetailDto;
import com.metabubble.BWC.dto.TaskDto;
import com.metabubble.BWC.entity.Merchant;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.MerchantService;
import com.metabubble.BWC.service.TaskService;
import com.metabubble.BWC.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    TaskService taskService;
    @Autowired
    MerchantService merchantService;
    @Autowired
    UserService userService;
    /**
     * 默认地球半径
     */
    private static final double EARTH_RADIUS = 6371000;//赤道半径(单位m)

    /**
     * 查询全部
     *
     * @param offset
     * @param limit
     * @return
     * @Author 看客
     */
    @GetMapping(value = {"/{offset}/{limit}"})
    public R<List<TaskDto>> getAll(@PathVariable Integer offset, @PathVariable Integer limit) {

        Page<Task> page = new Page<>(offset, limit);
        List<Task> records = taskService.page(page).getRecords();
        List<TaskDto> taskDtos = new ArrayList<>();
        if (records != null) {
            for (Task record : records) {
                if (record != null) {
                    TaskDto taskDto = TaskConverter.INSTANCES.TaskToTaskDto(record);
                    taskDtos.add(taskDto);
                }
            }
        }
        return R.success(taskDtos);
    }

    /**
     * 修改
     *
     * @param task
     * @return
     * @Author 看客
     */
    @PutMapping
    public R<String> update(@RequestBody Task task) {
        //前端如果没传任务总数 说明后台管理的没填 任务总数与剩余量，完成量默认恢复为刚发布任务的状态
        //如果穿了，则使任务剩余量设置为和新设置的amount一致，且任务完成量清零
        if (task.getAmount() != null){
            task.setCompleted(0);
            task.setTaskLeft(task.getAmount());
        }else{
            Integer amount = taskService.getById(task.getId()).getAmount();
            task.setAmount(amount);
            task.setCompleted(0);
            task.setTaskLeft(amount);
        }
        boolean flag = taskService.updateById(task);
        if (flag) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     * @Author 看客
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        boolean flag = taskService.removeById(id);
        if (flag) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    /**
     * 新增
     *
     * @param task
     * @return
     * @Author 看客
     */
    @PostMapping
    public R<String> save(@RequestBody Task task) {
        task.setTaskLeft(task.getAmount());
        task.setCompleted(0);
        boolean flag = taskService.save(task);
        if (flag) {
            return R.success("保存成功");
        } else {
            return R.error("保存失败");
        }
    }

    @PutMapping("/pickTask")
    public R<String> updateTask(@RequestBody(required = false) Condition condition){
        Task task = taskService.getById(condition.getId());
        task.setCompleted(task.getCompleted()+1);
        task.setTaskLeft(task.getTaskLeft()-1);
        boolean flag = taskService.updateById(task);
        if (flag) {
            return R.success("更新成功");
        } else {
            return R.error("更新失败");
        }
    }
    @GetMapping("/detail")
    public R<TaskDetailDto> getTaskDetail(@RequestBody(required = false) Condition condition){
        //通过任务编号查找被点击的任务
        Task task = taskService.getById(condition.getId());
        //通过id查询点击任务的用户
        User user = userService.getById(condition.getUserId());
        //获取用户的经纬度
        BigDecimal userLng = user.getLng();
        BigDecimal userLat = user.getLat();
        //获取任务所属商家的经纬度
        Merchant merchant = merchantService.getById(task.getMerchantId());
        BigDecimal merchantLng = merchant.getLng();
        BigDecimal merchantLat = merchant.getLat();
        //获取用户到商家的距离
        BigDecimal userToMerchantDistance = getDistance(userLng, userLat, merchantLng, merchantLat);
        //获取商家地址
        String merchantAddress = merchant.getAddress();
        //获取商家名字
        String merchantName = merchant.getName();
        TaskDetailDto taskDetailDto = TaskDetailConverter.INSTANCES.TaskToTaskDetailDto(task);
        //设置进TaskDetailDto
        taskDetailDto.setMerchantName(merchantName);
        taskDetailDto.setMerchantAddress(merchantAddress);
        taskDetailDto.setUserToMerchantDistance(userToMerchantDistance);
        return R.success(taskDetailDto);
    }
    /**
     * 首页展示任务
     * 根据订单名字/商家名字 任务类型 任务要求 任务是否需要评价 平台类型脾虚
     * @param condition
     * @param limit
     * @param offset
     * @Author 看客
     * @return
     */
    @GetMapping("/home/{offset}/{limit}")
    public R<List<HomeDto>> getByCondition(@RequestBody(required = false) Condition condition, @PathVariable Integer limit, @PathVariable Integer offset) {
        Page<Task> taskPage = new Page<>(offset, limit);
        LambdaQueryWrapper<Task> mLqw = new LambdaQueryWrapper<>();
        //通过id获取用户
        User user = userService.getById(condition.getId());
        //获取所有商家
        List<Merchant> merchants = merchantService.list();
        //得到每个商家与用户的距离
        Map<Merchant, BigDecimal> merchantBigDecimalMap = distanceToMerchant(merchants, user.getLng(), user.getLat());

        //任务要求排序：0为人气高，1为距离近，2为最省钱，3为新商家
        if (condition.getConstraint() != null) {
            if (condition.getConstraint() == 0) {
                mLqw.orderByDesc(Task::getCompleted);
            } else if (condition.getConstraint() == 1) {
                //任务的条件构造器
                LambdaQueryWrapper<Task> mLqw2 = new LambdaQueryWrapper<>();
                ArrayList<Task> taskList = new ArrayList<>();
                //获取所有订单
                //添加过滤条件
                //通过名字搜索
                mLqw2.like(StringUtils.isNotEmpty(condition.getName()), Task::getName, condition.getName());

                //任务类型筛选：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
                if (condition.getType() != null) {
                    mLqw2.eq(Task::getType, condition.getType());
                }
                //此处进行筛选评价和平台类型
                if (condition.getComment() != null){
                    mLqw2.eq(Task::getComment,condition.getComment());
                }
                if (condition.getPlatform() != null){
                    mLqw2.eq(Task::getPlatform,condition.getPlatform());
                }
                //筛选出被启用的订单，即status == 1
                mLqw2.eq(Task::getStatus,1);
                //查询所有符合条件的任务
                List<Task> tasks = taskService.list(mLqw2);
                //获取从近到远排序的商家
                Set<Merchant> merchantsOrder = calculationOfConstraints(merchants, user);
                for (Merchant merchant : merchantsOrder) {
                    //获取商家id
                    Long id = merchant.getId();
                    for (Task task : tasks) {
                        //如果商家id等于订单中的商家id 说明此时是同一个商家
                        if (task.getMerchantId().equals(id)) {
                            //则放入到taskList中 得到最终的结果————订单按距离进行排序 从近到远
                            taskList.add(task);
                        }
                    }
                }
                List<HomeDto> homeDtoList = new ArrayList<>();
                for (Task record : taskList) {
                    if (record != null) {
                        HomeDto homeDto = HomeConverter.INSTANCES.TaskToHomeDto(record);
                        //获取商家
                        Merchant merchant = merchantService.getById(record.getMerchantId());
                        //获取商家名字
                        String merchantName = merchant.getName();
                        //获取商家与用户之间的距离
                        BigDecimal distance = merchantBigDecimalMap.get(merchant);
                        //设置进homeDto
                        homeDto.setMerchantName(merchantName);
                        homeDto.setUserToMerchantDistance(distance);
                        homeDtoList.add(homeDto);
                    }
                }
                //分页返回
                ArrayList<HomeDto> homeDtos = new ArrayList<>();
                //如果少于五条数据则返回homeDtoList的全部 多于五条返回limit
                int realLimit;
                if (homeDtoList.size() < limit){
                    realLimit = homeDtoList.size();
                }else{
                    realLimit = limit;
                }
                for (int i = 0; i < realLimit; i++) {
                    //2 5
                    if (realLimit < limit){
                        homeDtos.add(homeDtoList.get(i));
                    }else{
                        homeDtos.add(homeDtoList.get(i+(offset-1)*limit));
                    }
                }
                return R.success(homeDtos);
            }else if (condition.getConstraint() == 2){
                mLqw.orderByAsc(Task::getRebateA);
            }else if (condition.getConstraint() == 3){
                mLqw.orderByDesc(Task::getCreateTime);
            }
        }
        //添加过滤条件
        //通过名字搜索
        mLqw.like(StringUtils.isNotEmpty(condition.getName()), Task::getName, condition.getName());
        //任务类型筛选：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
        if (condition.getType() != null) {
            mLqw.eq(Task::getType, condition.getType());
        }
        //任务评价筛选 0
        if (condition.getComment() != null) {
            mLqw.eq(Task::getComment, condition.getComment());
        }
        //平台类型筛选
        if (condition.getPlatform() != null) {
            mLqw.eq(Task::getPlatform, condition.getPlatform());
        }
        //筛选出被启用的订单，即status == 1
        mLqw.eq(Task::getStatus,1);

        taskService.page(taskPage, mLqw);
        //得到按条件查询的任务
        List<Task> records = taskPage.getRecords();

        List<HomeDto> homes = new ArrayList<>();
        if (records != null) {
            for (Task record : records) {
                if (record != null) {
                    HomeDto homeDto = HomeConverter.INSTANCES.TaskToHomeDto(record);
                    //获取任务对应的商家
                    Merchant merchant = merchantService.getById(record.getMerchantId());
                    //获取商家名字
                    String merchantName = merchant.getName();
                    //获取商家与用户之间的距离
                    BigDecimal distance = merchantBigDecimalMap.get(merchant);
                    //设置进homeDto
                    homeDto.setMerchantName(merchantName);
                    homeDto.setUserToMerchantDistance(distance);
                    homes.add(homeDto);
                }
            }
        }
        return R.success(homes);
    }

    /**
     * 给商家按距离远近排序 从近到远
     *
     * @param merchants
     * @param user
     * @return
     * @Author 看客
     */
    public Set<Merchant> calculationOfConstraints(List<Merchant> merchants, User user) {
        //距离近
        Map<Merchant, BigDecimal> map = new HashMap<>();
        //商家与用户之间的距离
        ArrayList<BigDecimal> distanceList = new ArrayList<>();
        //商家按距离排序结果  采用LinkedHashSet存储  有序不可重复  避免两个暑假与用户距离相同时产生错误
        Set<Merchant> merchantsOrder = new LinkedHashSet<>();
//        ArrayList<Merchant> merchantsOrder = new ArrayList<>();
        //获取用户的经纬度
        BigDecimal userLng = user.getLng();
        BigDecimal userLat = user.getLat();

        for (Merchant merchant : merchants) {
            //获取商家的经纬度
            BigDecimal merchantLat = merchant.getLat();
            BigDecimal merchantLng = merchant.getLng();
            //得到商家与用户之间的距离
            BigDecimal distance = getDistance(userLng, userLat, merchantLng, merchantLat);
            //将商家与用户之间的距离存储起来
            distanceList.add(distance);
            //将商家和 商家与用户之间的距离 作为一对键值对存储起来
            map.put(merchant, distance);
        }

        //冒泡排序对 商家与用户之间的距离 从小到大排序
        BigDecimal temp;
        for (int i = distanceList.size() - 1; i > 0 ; i--) {
            for (int j = 0; j < i; j++) {
                if (distanceList.get(j).compareTo(distanceList.get(j + 1)) > 0) {
                    temp = distanceList.get(j);
                    distanceList.set(j, distanceList.get(j + 1));
                    distanceList.set(j + 1, temp);
                }
            }
        }
        //依据 distanceList 的排序结果 对 商家进行小到大排序
        for (BigDecimal decimal : distanceList) {
            for (int j = 0; j < map.size(); j++) {
                //遍历所有商家的距离值
                BigDecimal bigDecimal = map.get(merchants.get(j));

                if (bigDecimal.compareTo(decimal) == 0) {
                    //此时商家的距离值与distanceList里的第i个数据相等 则得到响应排序
                    //例如第一次遍历就是为了找到距离最小值 商家遍历一次 得到 一个响应排位
                    merchantsOrder.add(merchants.get(j));//待处理：两个商家距离相等时会出点问题
                }
            }

        }
        return merchantsOrder;
    }

    /**
     * 得到每个商家与用户之间的距离
     * @param merchants
     * @param userLng
     * @param userLat
     * @return
     */
    public static Map<Merchant,BigDecimal> distanceToMerchant(List<Merchant> merchants,BigDecimal userLng,BigDecimal userLat){

        Map<Merchant, BigDecimal> map = new HashMap<>();

        for (Merchant merchant : merchants) {
            //获取商家的经纬度
            BigDecimal merchantLat = merchant.getLat();
            BigDecimal merchantLng = merchant.getLng();
            //得到商家与用户之间的距离
            BigDecimal distance = getDistance(userLng, userLat, merchantLng, merchantLat);
            //将商家和 商家与用户之间的距离 作为一对键值对存储起来
            map.put(merchant, distance);
        }
        return map;
    }
    /**
     * 获取弧度
     * @param d
     * @return
     * @Author 看客
     */
    private static BigDecimal rad(double d) {
        return BigDecimal.valueOf(d * Math.PI / 180.0);
    }

    /**
     * 分别传入两地经纬度得到距离
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     * @Author 看客
     */
    public static BigDecimal getDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        BigDecimal radLat1 = rad(Double.parseDouble(String.valueOf(lat1)));
        BigDecimal radLat2 = rad(Double.parseDouble(String.valueOf(lat2)));
        BigDecimal a = radLat1.subtract(radLat2);
        BigDecimal b = rad(Double.parseDouble(String.valueOf(lng1))).subtract(rad(Double.parseDouble(String.valueOf(lng2))));
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(Double.parseDouble(String.valueOf(a.divide(BigDecimal.valueOf(2.0))))), 2)
                + Math.cos(Double.parseDouble(String.valueOf(radLat1)))
                * Math.cos(Double.parseDouble(String.valueOf(radLat2)))
                * Math.pow(Math.sin(Double.parseDouble(String.valueOf(b.divide(BigDecimal.valueOf(2))))), 2)));
        BigDecimal s1 = BigDecimal.valueOf(s);
        s1 = s1.multiply(BigDecimal.valueOf(EARTH_RADIUS));
        double s2 = Math.round(Double.parseDouble(String.valueOf(s1.multiply(BigDecimal.valueOf(10000))))) / 10000;
        return BigDecimal.valueOf(s2);
    }
}
