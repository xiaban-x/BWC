package com.metabubble.BWC.filter;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.UserService;
import com.metabubble.BWC.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户端的过滤器
 */
@Order(10)
@WebFilter(filterName = "userFilter", urlPatterns = "/*")
@Slf4j
public class UserFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    private UserService userService;


    @Autowired
    private ManageSession manageSession;

    String stringSession = "session";
    String userId = "userId";

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("拦截到请求 : {}",request.getRequestURI());

        // 获取请求方式
        String method = request.getMethod();

        // 获取本次请求的URI
        String requestURI = request.getRequestURI();

        //判断管理员是否已登录
//        if (request.getSession().getAttribute("admin") != null) {
            //管理员退出,可放行
//            if (requestURI.equals("/admin/logout")) {
//                filterChain.doFilter(request, response);
//                BaseContext.remove();
//                return;
//            }
//
//            response.getWriter().write(JSON.toJSONString(R.error("管理端已登录")));
//            BaseContext.remove();
//            return;
//        }

        // 用户未登录便能放行的请求路径
        String[] urls1 = new String[]{
                "/login/sendMsg",
                "/login/reset",
                "/common/checkCodeGen",
                "/decoration/type",
                "/task/home",
                "/task/detail",
                "/merchant/getAddress",
                "apis.map.qq.com/ws/geocoder/v1/",
                // 新增
                "/common/upload",
                "/admin/login"
        };

        // 用户未登录才能放行的请求路径
        String[] urls2 = new String[]{
                "/login/"
        };

        // 用户登录后才能放行的请求路径
        String[] urls3 = new String[]{
                "/user/getuser/**",
                "/user/user/**",
                "/teamMsg/**",
                "/team/**",
                "/orders/user/**",
                "/login/resetfirst",
                "/login/resetsecond",
                "/login/logout",
                "/recruitment",
                "/cooperation",
                // 以下为自行整理
                "/financeList/insertRechargeInfo",
                "/financeList/insertCashableInfo",
                "/cusservice",
                "/task/pickTask",
                // 新增
                "/admin/logout",
                "/data/**",
                "/logs/page",
                "/decoration/**",
                "/cusservice/**"
        };

        boolean check1 = check(urls1, requestURI);
        boolean check2 = check(urls2, requestURI);
        boolean check3 = check(urls3, requestURI);

        // 登录与否都可放行
        if (check1) {
            log.info("登录与否都可放行的路径 : {}", requestURI);

            filterChain.doFilter(request, response);
            BaseContext.remove();
            return;
        }

        // 未登录才能放行
        if (check2) {
            // 判断用户登录状态
            if (request.getSession().getAttribute("user") == null) {
                log.info("用户未登录");

                filterChain.doFilter(request, response);
                BaseContext.remove();
                return;
            }

            // 用户已登陆，通过输出流方式向客户端页面响应数据
            response.setContentType("text/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JSON.toJSONString(R.error("请先退出登录")));

            BaseContext.remove();
            return;
        }

        String cookieSessionId = CookieUtils.getCookieValue(request, this.stringSession, true);
        String cookieUserId = CookieUtils.getCookieValue(request, this.userId, true);

        // 登录后才能放行
        if (check3) {

            // 判断/recruitment，/cooperation的请求方式
            if (requestURI.equals("/recruitment") || requestURI.equals("/cooperation")) {
                // 判断是否为管理员登录，为管理员登录则放行
                if (request.getSession().getAttribute("admin") != null) {
                    // 为管理员登录，放行
                    filterChain.doFilter(request, response);
                    BaseContext.remove();
                    return;
                }

                // 用户登录则判断请求方式
                if (method.equals("DELETE")){
                    throw new CustomException("无权限访问");
                }
            }

            // 判断用户登录状态
            if (request.getSession().getAttribute("user") != null) {
                log.info("用户已登录");

                Long userId = (Long) request.getSession().getAttribute("user");

                HttpSession publicSession = manageSession.getManageSession().get(userId.toString());

//            if (publicSession!=null&&publicSession.getId().equals(cookieSessionId)){
                BaseContext.setCurrentId(userId);

                filterChain.doFilter(request, response);
                BaseContext.remove();
                return;
//            }
//
//            request.getSession().invalidate();
//            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//            BaseContext.remove();
//            return;
          }


//            if (cookieUserId!=null&&cookieSessionId!=null) {
//            HttpSession publicSession = manageSession.getManageSession().get(cookieUserId);
//            if (publicSession!=null&&publicSession.getId().equals(cookieSessionId)){
//                HttpSession session = request.getSession();
//                session.setAttribute("user",Long.parseLong(cookieUserId));
//                session.setMaxInactiveInterval(publicSession.getMaxInactiveInterval());
//
//                BaseContext.setCurrentId(Long.parseLong(cookieUserId));
//
//                filterChain.doFilter(request,response);
//                BaseContext.remove();
//                return;
//            }
//        }


            // 未登录，通过输出流方式向客户端页面响应数据
            response.setContentType("text/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JSON.toJSONString(R.error("用户未登录")));

            BaseContext.remove();
            return;
        }

        // 不需要处理
        if (request.getSession().getAttribute("user") == null) {
            filterChain.doFilter(request, response);
            BaseContext.remove();
            return;
        }

        throw new CustomException("无权限访问");
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls 不许放行的路径
     * @param requestURI 网页请求的URI
     * @return 返回一个布尔值
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }

}

















