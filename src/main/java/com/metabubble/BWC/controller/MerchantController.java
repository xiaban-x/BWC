package com.metabubble.BWC.controller;


import com.metabubble.BWC.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant")
public class MerchantController {
    @Autowired
    private MerchantMapper merchantMapper;
    //111
}
