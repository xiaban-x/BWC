package com.metabubble.BWC.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *  全局异常处理器
 */
@ControllerAdvice(annotations = {RestController.class,Controller.class}) // 捕获带有这些注解的异常
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 示例：异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException.class) //拦截这类异常
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }

}
