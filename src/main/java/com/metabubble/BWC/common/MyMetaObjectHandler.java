package com.metabubble.BWC.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 创建时间和更新时间的插入操作自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insert");
        boolean updateTime = metaObject.hasSetter("updateTime");
        metaObject.setValue("createTime", LocalDateTime.now());
        if (updateTime){
            metaObject.setValue("updateTime", LocalDateTime.now());
        }
    }

    /**
     * 更新操作自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("update");
        metaObject.setValue("updateTime", LocalDateTime.now());
    }


}
