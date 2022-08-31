package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public void cashbackForUserFromFirst(Long id,String tel) {
        LambdaQueryWrapper<Team> queryWrapper123 = new LambdaQueryWrapper<>();
        queryWrapper123.eq(Team::getUserId,id);
        Team team = this.getOne(queryWrapper123);

        //手机号脱敏处理
        String phone = MobileUtils.blurPhone(tel);
        //查询是否为会员
        if (userService.checkGrade(id)) {
            BigDecimal bigDecimalForFirstWithVip = BigDecimal.valueOf(Integer.parseInt(configService.getOnlyContentById(Long.parseLong("18"))));
            if (!bigDecimalForFirstWithVip.equals(0)) {
                //会员返现
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForFirstWithVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"一级成员会员返现"+bigDecimalForFirstWithVip);
                userMsgService.addCashback(id,tel,"一级成员会员返现"+bigDecimalForFirstWithVip);
            }
        }else {
            BigDecimal bigDecimalForFirstWithNtoVip = BigDecimal.valueOf(Integer.parseInt(configService.getOnlyContentById(Long.parseLong("19"))));
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
    }

    /**
     * 团队向上二级返现
     * @param id 返现对象id
     * @author leitianyu999
     */
    @Override
    public void cashbackForUserFromSecond(Long id,String tel) {
        LambdaQueryWrapper<Team> queryWrapper123 = new LambdaQueryWrapper<>();
        queryWrapper123.eq(Team::getUserId,id);
        Team team = this.getOne(queryWrapper123);


        //查询是否为会员
        if (userService.checkGrade(id)) {
            BigDecimal bigDecimalForSecondWithVip = BigDecimal.valueOf(Integer.parseInt(configService.getOnlyContentById(Long.parseLong("20"))));
            if (!bigDecimalForSecondWithVip.equals(0)) {
                //会员返现
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForSecondWithVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"二级成员会员返现"+bigDecimalForSecondWithVip);
                userMsgService.addCashback(id,tel,"二级成员会员返现"+bigDecimalForSecondWithVip);
            }
        }else {
            BigDecimal bigDecimalForSecondWithNtoVip = BigDecimal.valueOf(Integer.parseInt(configService.getOnlyContentById(Long.parseLong("21"))));
            if (!bigDecimalForSecondWithNtoVip.equals(0)) {
                //非会员返现
                team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(bigDecimalForSecondWithNtoVip));
                //团队信息计入
                teamMsgService.addCashback(id,tel,"二级成员非会员返现"+bigDecimalForSecondWithNtoVip);
                userMsgService.addCashback(id,tel,"二级成员非会员返现"+bigDecimalForSecondWithNtoVip);
            }
        }
        this.update(team,queryWrapper123);
    }

    /**
     * 创建团队表
     * @param user 用户对象
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
     * 团队添加上一级和上二级
     * @param user
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
}
