package com.metabubble.BWC.controller;

import org.aspectj.lang.annotation.Pointcut;

public class AOPController {
    @Pointcut("execution()")
    public void pt(){};
}
