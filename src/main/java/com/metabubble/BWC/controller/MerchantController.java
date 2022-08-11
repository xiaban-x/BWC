package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.Condition;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.MerchantConverter;
import com.metabubble.BWC.dto.MerchantDto;
import com.metabubble.BWC.entity.Merchant;
import com.metabubble.BWC.service.LogsService;
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
import java.util.*;

@RestController
@RequestMapping("/merchant")
@Slf4j
public class MerchantController {

    @Autowired
    MerchantService merchantService;
    @Autowired
    LogsService logsService;

    public static final String KEY_1 = "XEABZ-GFERQ-GVY5M-GZCOR-EJGOT-OWBOP";

    /**
     * 查询商家信息
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
     * 新增商家
     * @param merchant
     * @Author 看客
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Merchant merchant) {
        //获取经纬度
        BigDecimal lng = merchant.getLng();
        BigDecimal lat = merchant.getLat();
        if (lng == null  || lat == null){
            lng = getGeocoderLatitude(merchant.getAddress()).get("lng");
            lat = getGeocoderLatitude(merchant.getAddress()).get("lat");
        }

        merchant.setLng(lng);
        merchant.setLat(lat);
        //保存
        boolean flag = merchantService.save(merchant);
        if (flag){
            logsService.saveLog("新增商家","新增了\""+merchant.getName()+"\"商家");
            return R.success("新增成功");
        }

       return R.error("新增失败");
    }

    /**
     * 修改商家信息
     * @param merchant
     * @Author 看客
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Merchant merchant) {
        BigDecimal lng = getGeocoderLatitude(merchant.getAddress()).get("lng");
        BigDecimal lat = getGeocoderLatitude(merchant.getAddress()).get("lat");
        //获取修改前的商家名字
        String merchantName = merchantService.getById(merchant.getId()).getName();
        boolean flag = merchantService.updateById(merchant);
        if (flag){
            logsService.saveLog("修改商家","修改了\""+merchantName+"\"商家的基本信息");
            return R.success("修改成功");
        }
        return R.error("修改失败");

    }

    /**
     * 删除商家
     * @param id
     * @Author 看客
     * @return
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable("id") Long id){
        String name = merchantService.getById(id).getName();
        boolean flag = merchantService.removeById(id);
        if(flag){
            logsService.saveLog("删除商家","删除了\""+name+"\"商家");
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
    public static Map<String, BigDecimal> getGeocoderLatitude(String address) {
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
            Map<String, BigDecimal> map = null;
            if (StringUtils.isNotEmpty(str)) {
                int lngStart = str.indexOf("lng\":");
                int lngEnd = str.indexOf(",\"lat");
                int latEnd = str.indexOf("}", lngEnd + 8);
                if (lngStart > 0 && lngEnd > 0 && latEnd > 0) {
                    String lngStr = str.substring(lngStart + 5, lngEnd);
                    String latStr = str.substring(lngEnd + 7, latEnd);
                    BigDecimal lng = BigDecimal.valueOf(Double.parseDouble(lngStr));
                    BigDecimal lat = BigDecimal.valueOf(Double.parseDouble(latStr));
                    map = new HashMap<>();
                    map.put("lng", lng);
                    map.put("lat", lat);
                    return map;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

