package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.mapper.ConfigMapper;
import com.metabubble.BWC.service.ConfigService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config>
        implements ConfigService {
    /**
     * 一个根据id获取内容的静态类，不设置接口，可调用
     * @param id
     * @return
     */
    public R<String> getContentById(Long id) {

        Config config = this.getById(id);
        String content = config.getContent();

        return R.success(content);
    }

    @Override
    public String getOnlyContentById(Long id) {
        return this.getById(id).getContent();
    }

    /**
     * 一个根据id获取内容的静态类，不设置接口，可调用
     * @param id
     * @return
     * 晴天小杰
     */
    public R<List> getContentsById(Long id) {

        Config config = this.getById(id);
        String content = config.getContent();
        String contents = config.getContents();

        List<String> lists = new ArrayList<>();
        lists.add(content);
        lists.add(contents);
        return R.success(lists);
    }
}
