package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Team;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.TeamMapper;
import com.metabubble.BWC.service.*;
import com.metabubble.BWC.utils.MobileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {



    @Autowired
    private UserService userService;
    @Autowired
    private TeamMsgService teamMsgService;
    @Autowired
    private UserMsgService userMsgService;
    @Autowired
    private ConfigService configService;

    /**
     * 团队向上一级返现
     * @param id 返现对象id
     * @author leitianyu999
     */
    @Override
    public BigDecimal cashbackForUserFromFirst(Long id,String tel) {
        LambdaQueryWrapper<Team> queryWrapper123 = new LambdaQueryWrapper<>();
        queryWrapper123.eq(Team::getUserId,id);
        Team team = this.getOne(queryWrapper123);

        //手机号脱敏处理
        String phone = MobileUtils.blurPhone(tel);

        BigDecimal bigDecimal = null;
        //查询是否为会员
        if (userService.checkGrade(id)) {
            BigDecimal bigDecimalForFirstWithVip = BigDecimal.valueOf(Double.parseDouble(configService.getOnlyContentById(Long.parseLong("18"))));
            bigDecimal = bigDecimalForFirstWithVip;
            if (!bigDecimalForFirstWithVip.equals(0)) {
                //会员返现
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForFirstWithVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"一级成员会员返现"+bigDecimalForFirstWithVip);
                userMsgService.addCashback(id,tel,"一级成员会员返现"+bigDecimalForFirstWithVip);
            }
        }else {
            BigDecimal bigDecimalForFirstWithNtoVip = BigDecimal.valueOf(Double.parseDouble(configService.getOnlyContentById(Long.parseLong("19"))));
            bigDecimal = bigDecimalForFirstWithNtoVip;
            if (!bigDecimalForFirstWithNtoVip.equals(0)) {
                //非会员返现
                BigDecimal add = team.getTotalWithdrawnAmount().add(bigDecimalForFirstWithNtoVip);
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForFirstWithNtoVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"一级成员非会员返现"+bigDecimalForFirstWithNtoVip);
                userMsgService.addCashback(id,tel,"一级成员非会员返现"+bigDecimalForFirstWithNtoVip);
            }
        }
        this.update(team,queryWrapper123);
        return bigDecimal;
    }

    /**
     * 团队向上二级返现
     * @param id 返现对象id
     * @author leitianyu999
     */
    @Override
    public BigDecimal cashbackForUserFromSecond(Long id,String tel) {
        LambdaQueryWrapper<Team> queryWrapper123 = new LambdaQueryWrapper<>();
        queryWrapper123.eq(Team::getUserId,id);
        Team team = this.getOne(queryWrapper123);

        BigDecimal bigDecimal = null;
        //查询是否为会员
        if (userService.checkGrade(id)) {
            BigDecimal bigDecimalForSecondWithVip = BigDecimal.valueOf(Double.parseDouble(configService.getOnlyContentById(Long.parseLong("20"))));
            bigDecimal = bigDecimalForSecondWithVip;
            if (!bigDecimalForSecondWithVip.equals(0)) {
                //会员返现
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForSecondWithVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"二级成员会员返现"+bigDecimalForSecondWithVip);
                userMsgService.addCashback(id,tel,"二级成员会员返现"+bigDecimalForSecondWithVip);
            }
        }else {
            BigDecimal bigDecimalForSecondWithNtoVip = BigDecimal.valueOf(Double.parseDouble(configService.getOnlyContentById(Long.parseLong("21"))));
            bigDecimal = bigDecimalForSecondWithNtoVip;
            if (!bigDecimalForSecondWithNtoVip.equals(0)) {
                //非会员返现
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForSecondWithNtoVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"二级成员非会员返现"+bigDecimalForSecondWithNtoVip);
                userMsgService.addCashback(id,tel,"二级成员非会员返现"+bigDecimalForSecondWithNtoVip);
            }
        }
        this.update(team,queryWrapper123);
        return bigDecimal;
    }

    /**
     * 创建团队表
     * @param user 用户对象
     * @author leitianyu999
     */
    @Override
    public void save(User user) {
        Team team = new Team();
        team.setDownUser01Amount(0);
        team.setDownUser02Amount(0);
        team.setUserId(user.getId());
        this.save(team);
    }

    /**
     * 创建团队表同时添加上级用户
     * @param user
     * @param invitation
     */
    @Override
    public void save(User user, String invitation) {
        Team team = new Team();
        team.setDownUser01Amount(0);
        team.setDownUser02Amount(0);
        team.setUserId(user.getId());
        this.save(team);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //添加验证码对比
        queryWrapper.eq(User::getDownId,invitation);
        //查询上级对象
        User userFirst = userService.getOne(queryWrapper);
        //判断是否有上级对象
        if (userFirst!=null){
            //团队添加上级
            this.addTeamTop(user,userFirst);
            //用户添加上级邀请码
            user.setUpId(invitation);
            userService.updateById(user);
        }

    }

    /**
     * 团队添加上一级和上二级
     * @param user
     * @author leitianyu999
     */
    @Override
    public void addTeamTop(User user,User topUser) {

        LambdaQueryWrapper<Team> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Team::getUserId,user.getId());
        //本机用户
        Team team = this.getOne(queryWrapper1);

        LambdaQueryWrapper<Team> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Team::getUserId,topUser.getId());
        //上级用户
        Team teamTop = this.getOne(queryWrapper2);

        //添加团队上一级id
        team.setUpUser01Id(topUser.getId());

        int downUser01Amount = teamTop.getDownUser01Amount();
        AtomicInteger atomicInteger1 = new AtomicInteger(downUser01Amount);
        int top = atomicInteger1.incrementAndGet();
        //修改下级一级成员数量
        teamTop.setDownUser01Amount(top);
        //团队信息添加
        teamMsgService.add(teamTop.getUserId(),user.getTel(),"一级成员");
        //判断是否有上二级成员
        if (teamTop.getUpUser01Id()!=null){
            team.setUpUser02Id(teamTop.getUpUser01Id());

            LambdaQueryWrapper<Team> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(Team::getUserId,teamTop.getUpUser01Id());
            //上二级用户
            Team teamTopTop = this.getOne(queryWrapper3);

            int downUser02Amount = teamTopTop.getDownUser02Amount();
            AtomicInteger atomicInteger2 = new AtomicInteger(downUser02Amount);
            int topTop = atomicInteger2.incrementAndGet();
            //修改下级二级成员数量
            teamTopTop.setDownUser02Amount(topTop);
            //团队信息添加
            teamMsgService.add(teamTopTop.getUserId(),user.getTel(),"二级成员");

            this.update(teamTopTop,queryWrapper3);
        }
        this.update(team,queryWrapper1);
        this.update(teamTop,queryWrapper2);
    }

    /**
     * 团队返现驳回
     * @param orders
     * @param user
     */
    @Override
    public void overruleCashback(Orders orders, User user) {
        //查询用户团队信息
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId,orders.getUserId());
        Team team = this.getOne(queryWrapper);

        Long upUser01Id = team.getUpUser01Id();
        //是否有上一级用户
        if (upUser01Id!=null&&orders.getRebate01()!=null){
            //查询上一级用户团队信息
            LambdaQueryWrapper<Team> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Team::getUserId,upUser01Id);
            Team teamTop = this.getOne(queryWrapper1);
            //修改上一级团队返现信息
            teamTop.setTotalWithdrawnAmount(teamTop.getTotalWithdrawnAmount().subtract(orders.getRebate01()));
            this.update(teamTop,queryWrapper1);
            //记录信息
            teamMsgService.addCashback(upUser01Id,user.getTel(),"一级成员订单返现驳回"+orders.getRebate01());
            userMsgService.overruleCashback(upUser01Id,user.getTel(),orders.getRebate01().toString());

            Long upUser02Id = team.getUpUser02Id();

            //查询是否有上二级用户
            if (upUser02Id!=null&&orders.getRebate02()!=null){
                //查询上二级用户团队信息
                LambdaQueryWrapper<Team> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper2.eq(Team::getUserId,upUser02Id);
                Team teamTopTop = this.getOne(queryWrapper2);
                //修改上二级团队信息
                teamTopTop.setTotalWithdrawnAmount(teamTopTop.getTotalWithdrawnAmount().subtract(orders.getRebate02()));
                this.update(teamTopTop,queryWrapper2);
                //记录日志
                teamMsgService.addCashback(upUser02Id,user.getTel(),"二级成员订单返现驳回"+orders.getRebate02());
                userMsgService.overruleCashback(upUser02Id,user.getTel(),orders.getRebate02().toString());
            }
        }

    }
}
