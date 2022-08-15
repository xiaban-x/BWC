package com.metabubble.BWC.utils;


import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * @ClassName: MobileUtil
 * @author leitianyu999
 */
public class MobileUtils {

    /**
     * 正则表达式：验证手机号
     */
    private static final String REGEX_MOBILE = "^((13[0-9])|(15[^4,\\D])|(18[0-3,5-9])|(17[0-9]))\\d{8}$";

    /**
     * 手机号脱敏筛选正则
     */
    public static final String PHONE_BLUR_REGEX = "(\\d{3})\\d{4}(\\d{4})";

    /**
     * 手机号脱敏替换正则
     */
    public static final String PHONE_BLUR_REPLACE_REGEX = "$1****$2";

    /**
     * 判断是否是手机号格式,如果传入的是空串，返回false
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isMobileNum(String mobile) {
        if(StringUtils.isBlank(mobile)){
            return false;
        }

        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    /**
     * 获取今日的剩余时间，返回值单位：秒
     * @return
     */
    public static Integer getSurplusTime(){
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long millis = c.getTimeInMillis() - now+2000;

        return (int)(millis/1000);
    }

    /**
     * 手机号脱敏处理
     * @param phone
     * @return
     */
    public static final String blurPhone(String phone) {
        return phone.replaceAll(PHONE_BLUR_REGEX, PHONE_BLUR_REPLACE_REGEX);
    }

}
