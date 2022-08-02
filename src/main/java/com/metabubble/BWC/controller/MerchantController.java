package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.Condition;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.MerchantConverter;
import com.metabubble.BWC.dto.MerchantDto;
import com.metabubble.BWC.entity.Merchant;
import com.metabubble.BWC.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
@Slf4j
public class MerchantController {

    @Autowired
    MerchantService merchantService;

    public static final String KEY_1 = "XEABZ-GFERQ-GVY5M-GZCOR-EJGOT-OWBOP";

    /**
     * 查询
     * @param condition
     * @param limit
     * @param offset
     * @Author 看客
     * @return
     */
    @GetMapping(value={"/{offset}/{limit}"})
    public R<List<MerchantDto>> getMerchantByPage(@RequestBody(required = false) Condition condition, @PathVariable Integer limit,@PathVariable Integer offset) {

        Page<Merchant> merchantPage = new Page<>(offset, limit);
        LambdaQueryWrapper<Merchant> mLqw = new LambdaQueryWrapper<>();
        //添加过滤条件
        mLqw.like(StringUtils.isNotEmpty(condition.getName()), Merchant::getName,condition.getName());

        mLqw.like(StringUtils.isNotEmpty(condition.getTel()),Merchant::getTel,condition.getTel());

        if (condition.getPlaType() != null){
            //mLqw.like(Merchant::getPlaType,condition.getPlaType());
        }
        //添加排序条件
        mLqw.orderByDesc(Merchant::getCreateTime);

        merchantService.page(merchantPage,mLqw);
        List<Merchant> records = merchantPage.getRecords();
        List<MerchantDto> merchants = new ArrayList<>();
        if (records != null){
            for(Merchant record:records){
                if (record !=null) {
                    MerchantDto merchantDto = MerchantConverter.INSTANCES.MerchantToMerchantDto(record);
                    merchants.add(merchantDto);
                }
            }
        }
        return R.success(merchants);
    }

    /**
     * 新增
     * @param merchant
     * @Author 看客
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Merchant merchant) {
        //获取经纬度
        merchant.setLng(BigDecimal.valueOf(Double.parseDouble(getGeocoderLatitude(merchant.getAddress()).get("lng"))));
        merchant.setLat(BigDecimal.valueOf(Double.parseDouble(getGeocoderLatitude(merchant.getAddress()).get("lat"))));
        //保存
        boolean flag = merchantService.save(merchant);
        if (flag){
            return R.success("保存成功");
        }
       return R.error("保存失败");
    }

    /**
     * 修改
     * @param merchant
     * @Author 看客
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Merchant merchant) {
        merchant.setLng(BigDecimal.valueOf(Double.parseDouble(getGeocoderLatitude(merchant.getAddress()).get("lng"))));
        merchant.setLat(BigDecimal.valueOf(Double.parseDouble(getGeocoderLatitude(merchant.getAddress()).get("lat"))));
        boolean flag = merchantService.updateById(merchant);
        if (flag){
            return R.success("修改成功");
        }
        return R.error("修改失败");

    }

    /**
     * 根据id删除
     * @param id
     * @Author 看客
     * @return
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable("id") Long id){
        boolean flag = merchantService.removeById(id);
        if(flag){
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    /**
     * 返回输入地址的经纬度坐标
     * key lng(经度),lat(纬度)
     * @param address
     * @Author 看客
     * @return
     */
    public static Map<String, String> getGeocoderLatitude(String address) {
        BufferedReader in = null;
        try {
            //将地址转换成utf-8的16进制
            address = URLEncoder.encode(address, "UTF-8");
            URL tirc = new URL("https://apis.map.qq.com/ws/geocoder/v1/?address=" + address + "&output=json&key=" + KEY_1);


            in = new BufferedReader(new InputStreamReader(tirc.openStream(), StandardCharsets.UTF_8));
            String res;
            StringBuilder sb = new StringBuilder("");
            while ((res = in.readLine()) != null) {
                sb.append(res.trim());
            }
            String str = sb.toString();
            Map<String, String> map = null;
            if (StringUtils.isNotEmpty(str)) {
                int lngStart = str.indexOf("lng\":");
                int lngEnd = str.indexOf(",\"lat");
                int latEnd = str.indexOf("}", lngEnd + 8);
                if (lngStart > 0 && lngEnd > 0 && latEnd > 0) {
                    String lng = str.substring(lngStart + 5, lngEnd);
                    String lat = str.substring(lngEnd + 7, latEnd);
                    map = new HashMap<String, String>();
                    map.put("lng", lng);
                    map.put("lat", lat);
                    return map;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

