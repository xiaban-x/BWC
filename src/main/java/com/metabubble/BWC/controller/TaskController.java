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
import com.metabubble.BWC.service.LogsService;
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
    @Autowired
    LogsService logsService;
    /**
     * 默认地球半径
     */
    private static final double EARTH_RADIUS = 6371000;//赤道半径(单位m)

    /**
     * 查询所有任务
     * @param offset
     * @param limit
     * @Author 看客
     * @return
     */
    @GetMapping
    public R<List<TaskDto>> getAll(Integer offset, Integer limit) {

        Page<Task> page = new Page<>(offset, limit);
        List<Task> records = taskService.page(page).getRecords();
        List<TaskDto> taskDtos = new ArrayList<>();
        if (records != null) {
            for (Task record : records) {
                if (record != null) {
                    Merchant merchant = merchantService.getById(record.getMerchantId());
                    TaskDto taskDto = TaskConverter.INSTANCES.TaskToTaskDto(record);
                    taskDto.setMerchantName(merchant.getName());
                    taskDtos.add(taskDto);
                }
            }
        }
        return R.success(taskDtos);
    }

    /**
     * 修改任务
     * @param task
     * @Author 看客
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Task task) {
        //前端如果没传任务总数 说明后台管理的没填 任务总数与剩余量，完成量默认恢复为刚发布任务的状态
        //如果穿了，则使任务剩余量设置为和新设置的amount一致，且任务完成量清零
        if (task.getAmount() != null){
            task.setCompleted(0);
            task.setTaskLeft(task.getAmount());
        }else{
            //获取任务总数
            Integer amount = taskService.getById(task.getId()).getAmount();
            task.setAmount(amount);
            task.setCompleted(0);
            task.setTaskLeft(amount);
        }
        //商家名称
        String name = merchantService.getById(task.getMerchantId()).getName();
        //获取修改任务前的信息
        String taskName = taskService.getById(task.getId()).getName();
        boolean flag = taskService.updateById(task);
        if (flag) {
            logsService.saveLog("修改任务","修改了\""+name+"\"的\""+taskName+"\"的任务");
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    /**
     * 删除任务
     * @param id
     * @Author 看客
     * @return
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        //获取任务
        Task task = taskService.getById(id);
        //获取任务名字
        String taskName = task.getName();
        //获取商家名字
        String merchantName = merchantService.getById(task.getMerchantId()).getName();
        boolean flag = taskService.removeById(id);
        if (flag) {
            logsService.saveLog("删除任务","删除了\""+merchantName+"\"的\""+taskName+"\"的任务");
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    /**
     * 添加订单
     * @param task
     * @Author 看客
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Task task) {
        //设置剩余量和总数一致
        task.setTaskLeft(task.getAmount());
        //设置任务完成量为0
        task.setCompleted(0);
        //商家名字
        String name = merchantService.getById(task.getMerchantId()).getName();
        boolean flag = taskService.save(task);
        if (flag) {
            logsService.saveLog("添加任务","添加了\""+name+"\"的\""+task.getName()+"\"的任务");
            return R.success("添加成功");
        } else {
            return R.error("添加失败");
        }
    }

    /**
     * 用户接任务后对任务数进行更新
     * @param condition
     * @Author 看客
     * @return
     */
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

    /**
     * 任务信息详情
     * @param id
     * @param userLng
     * @param userLat
     * @Author 看客
     * @return
     */
    @GetMapping("/detail")
    public R<TaskDetailDto> getTaskDetail(Integer id,BigDecimal userLng,BigDecimal userLat){
        //通过任务编号查找被点击的任务
        Task task = taskService.getById(id);
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
        //获取商家照片
        String merchantPic = merchant.getPic();
        TaskDetailDto taskDetailDto = TaskDetailConverter.INSTANCES.TaskToTaskDetailDto(task);
        //设置进TaskDetailDto
        taskDetailDto.setMerchantName(merchantName);
        taskDetailDto.setMerchantAddress(merchantAddress);
        taskDetailDto.setUserToMerchantDistance(userToMerchantDistance);
        taskDetailDto.setMerchantPic(merchantPic);

        return R.success(taskDetailDto);
    }

    /**
     * 首页展示任务
     * 根据订单名字/商家名字 任务类型 任务要求 任务是否需要评价 平台类型排序
     * @param limit
     * @param offset
     * @param name
     * @param type
     * @param constraint
     * @param comment
     * @param platform
     * @param userLng
     * @param userLat
     * @Author 看客
     * @return
     */
    @GetMapping("/home")
    public R<List<HomeDto>> getByCondition(Integer limit, Integer offset,String name,String merchantName,Integer type,Integer constraint,Integer comment,Integer platform,BigDecimal userLng,BigDecimal userLat) {
        Page<Task> taskPage = new Page<>(offset, limit);
        LambdaQueryWrapper<Task> mLqw = new LambdaQueryWrapper<>();
        //获取所有商家
        List<Merchant> merchants = merchantService.list();
        //得到每个商家与用户的距离
        Map<Merchant, BigDecimal> merchantBigDecimalMap = distanceToMerchant(merchants, userLng, userLat);

        //任务要求排序：0为人气高，1为距离近，2为最省钱，3为新商家
        if (constraint != null) {
            if (constraint == 0) {
                mLqw.orderByDesc(Task::getCompleted);
            } else if (constraint == 1) {
                //任务的条件构造器
                LambdaQueryWrapper<Task> mLqw2 = new LambdaQueryWrapper<>();
                ArrayList<Task> taskList = new ArrayList<>();
                //获取所有订单
                //添加过滤条件
                //通过名字搜索
                mLqw2.like(StringUtils.isNotEmpty(name), Task::getName,name);

                //任务类型筛选：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
                if (type != null) {
                    mLqw2.eq(Task::getType, type);
                }
                //此处进行筛选评价和平台类型
                if (comment != null){
                    mLqw2.eq(Task::getComment,comment);
                }
                if (platform != null){
                    mLqw2.eq(Task::getPlatform,platform);
                }
                //筛选出被启用的订单，即status == 1
                mLqw2.eq(Task::getStatus,1);
                //查询所有符合条件的任务
                List<Task> tasks = taskService.list(mLqw2);
                //获取从近到远排序的商家
                Set<Merchant> merchantsOrder = calculationOfConstraints(merchants, userLng,userLat);
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
                        if (merchantName == null){
                            merchantName = merchant.getName();
                        }
                        //获取商家照片
                        String merchantPic = merchant.getPic();
                        //获取商家与用户之间的距离
                        BigDecimal distance = merchantBigDecimalMap.get(merchant);
                        //设置进homeDto
                        homeDto.setMerchantName(merchantName);
                        homeDto.setUserToMerchantDistance(distance);
                        homeDto.setMerchantPic(merchantPic);
                        //保存进集合
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
            }else if (constraint == 2){
                mLqw.orderByAsc(Task::getRebateA);
            }else if (constraint == 3){
                mLqw.orderByDesc(Task::getCreateTime);
            }
        }
        //添加过滤条件
        //通过名字搜索
        mLqw.like(StringUtils.isNotEmpty(name), Task::getName, name);
        //任务类型筛选：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
        if (type != null) {
            mLqw.eq(Task::getType, type);
        }
        //任务评价筛选 0
        if (comment != null) {
            mLqw.eq(Task::getComment, comment);
        }
        //平台类型筛选
        if (platform != null) {
            mLqw.eq(Task::getPlatform, platform);
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
                    if (merchantName == null){
                        merchantName = merchant.getName();
                    }
                    //获取商家图片
                    String merchantPic = merchant.getPic();
                    //获取商家与用户之间的距离
                    BigDecimal distance = merchantBigDecimalMap.get(merchant);
                    //设置进homeDto
                    homeDto.setMerchantName(merchantName);
                    homeDto.setUserToMerchantDistance(distance);
                    homeDto.setMerchantPic(merchantPic);
                    //添加进集合
                    homes.add(homeDto);
                }
            }
        }
        return R.success(homes);
    }

    /**
     * 给商家按距离远近排序 从近到远
     * @param merchants
     * @param userLng
     * @param userLat
     * @Author 看客
     * @return
     */
    public Set<Merchant> calculationOfConstraints(List<Merchant> merchants, BigDecimal userLng, BigDecimal userLat) {
        //距离近
        Map<Merchant, BigDecimal> map = new HashMap<>();
        //商家与用户之间的距离
        ArrayList<BigDecimal> distanceList = new ArrayList<>();
        //商家按距离排序结果  采用LinkedHashSet存储  有序不可重复  避免两个暑假与用户距离相同时产生错误
        Set<Merchant> merchantsOrder = new LinkedHashSet<>();
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
            if (!distance.equals(BigDecimal.valueOf(-1))){
                map.put(merchant, distance);
            }
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
        if (lng2 == null || lat2 == null){
            return BigDecimal.valueOf(-1);
        }
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
