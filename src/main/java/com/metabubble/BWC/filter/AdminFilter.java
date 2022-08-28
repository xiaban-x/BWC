package com.metabubble.BWC.filter;

import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 不同类型admin的判断过滤器
 */
@Order(5)
@WebFilter(filterName = "adminFilter", urlPatterns = "/*")
@Slf4j
public class AdminFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    private AdminService adminService;

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("拦截到请求 : {}",request.getRequestURI());

        // 获取本次请求的URI
        String method = request.getMethod();
        System.out.println(method);
        String requestURI = request.getRequestURI();

        // type为0和1
        String[] urls1 = new String[]{
                "/financeList/updateWithdrawInfo",
                "/financeList/updateAgreeInfo"
        };

        // type为0和2
        String[] urls2 = new String[]{
                "/admin",   // 人员管理
                "/orders/commit",    // 订单审核
                "/orders/audit" ,   // 订单审核
                "/task/**"
        };

        //判断本次请求是否需要处理
        boolean check1 = check(urls1, requestURI);
        boolean check2 = check(urls2,requestURI);

        // 需要处理
        if (check1) {
            Long adminId = (Long) request.getSession().getAttribute("admin");
            BaseContext.setCurrentId(adminId);
            Admin admin = adminService.getById(adminId);

            if (admin.getType() == 0 || admin.getType() == 1) {
                filterChain.doFilter(request,response);
                BaseContext.remove();
                return;
            } else {
                throw new CustomException("无权限访问！");
            }
        }

        if (check2) {
            // 判断是否为/task/**
            if (requestURI.equals("/task")){
                if (method.equals("GET")) {
                    log.info("该请求为查询请求 ： {}", method);

                    // 放行
                    filterChain.doFilter(request,response);
                    BaseContext.remove();
                    return;
                }
            }
            Long adminId = (Long) request.getSession().getAttribute("admin");
            BaseContext.setCurrentId(adminId);
            Admin admin = adminService.getById(adminId);

            if (admin.getType() == 0 || admin.getType() == 2) {
                filterChain.doFilter(request,response);
                BaseContext.remove();
                return;
            } else {
                throw new CustomException("无权限访问！");
            }
        }

        // 不需要处理
        filterChain.doFilter(request, response);
        BaseContext.remove();
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls 不许放行的路径
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















