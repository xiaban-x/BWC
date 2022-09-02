package com.metabubble.BWC.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.TeamConverter;
import com.metabubble.BWC.dto.TeamDto;
import com.metabubble.BWC.entity.Team;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Autowired
    private TeamService teamService;

    /**
     * 获取团队信息
     * @author leitianyu999
     * @return
     */
    @GetMapping
    public R<TeamDto> get(){
        Long id = BaseContext.getCurrentId();

        LambdaQueryWrapper<Team> queryWrapper1  = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Team::getUserId,id);
        Team one = teamService.getOne(queryWrapper1);
        TeamDto team1 = TeamConverter.INSTANCES.TeamToTeamDto(one);

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",id);

        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,-24);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int days = calendar.get(Calendar.DAY_OF_MONTH);

        queryWrapper.apply("year(create_time)="+year);
        queryWrapper.apply("month(create_time)="+month);
        queryWrapper.apply("day(create_time)="+days);
        queryWrapper.select("sum(total_withdrawn_amount) as totalWithdrawnAmount");

        Team team = teamService.getOne(queryWrapper);


        if (team!=null){
            team1.setYesterdayWithdrawn(team.getTotalWithdrawnAmount());
        }else {
            team1.setYesterdayWithdrawn(new BigDecimal(0));
        }

        AtomicInteger atomicInteger1 = new AtomicInteger(team1.getDownUser01Amount());
        int allMember = atomicInteger1.addAndGet(team1.getDownUser02Amount());

        team1.setAllMember(allMember);

        return R.success(team1);
    }



}
