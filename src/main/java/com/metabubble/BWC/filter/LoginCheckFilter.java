package com.metabubble.BWC.filter;

import com.alibaba.fastjson.JSON;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.utils.CookieUtils;
import com.sun.webkit.network.CookieManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 过滤器
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    private ManageSession manageSession;

    String stringSession = "session";
    String userId = "userId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("拦截到请求 : {}",request.getRequestURI());

        //如果未登录则返回未登录结果
        //获取本次请求的URI
        String requestURI = request.getRequestURI(); // /backend/login.html

        BaseContext.remove();

//        String cookieSessionId = CookieUtils.getCookieValue(request, this.stringSession, true);
//        String cookieUserId = CookieUtils.getCookieValue(request, this.userId, true);

        //定义不需要处理的请求路径
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");

            HttpSession publicSession = manageSession.getManageSession().get(userId.toString());

//            if (publicSession!=null&&publicSession.getId().equals(cookieSessionId)){
                BaseContext.setCurrentId(userId);

                filterChain.doFilter(request,response);
                BaseContext.remove();
                return;
//            }
//
//            request.getSession().invalidate();
//            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//            BaseContext.remove();
//            return;
        }



//        if (cookieUserId!=null&&cookieSessionId!=null) {
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

        String[] urls = new String[]{
                //书写功能阶段，停止拦截器，暂时让请求发出
                "/**",
                "/admin/**",
                "/common/**",
                "/config/**",
                "/cusservice/**",
                "/data/**",
                "/decoration/**",
                "/logs/**",
                "/user/**",
                "/backend/**",
                "/front/**",
                "/merchant/**",
                "/financeList/**"
        };
        //判断本次请求是否需要处理
        boolean check = check(urls,requestURI);
        //如果不需要处理，直接放行
        if (check){
            filterChain.doFilter(request,response);
            BaseContext.remove();
            return;
        }
        //4-1判断登录状态，如果已经登录直接放行
        if (request.getSession().getAttribute("employee")!=null){

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            BaseContext.remove();
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            BaseContext.remove();
            return;
        }

        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        BaseContext.remove();
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls 放行的路径
     * @param requestURI 网页请求的URI
     * @return 返回一个布尔值
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
