package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/*
    认真成功处理 ,用来登录成功记录日志
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference(check = false)
    private LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        //登录成功后调用
        System.out.println("登录成功了.....");

        LoginLog loginLog = new LoginLog();
        loginLog.setLoginName(authentication.getName()); //当前登录用户名
        loginLog.setLoginTime(new Date());
        loginLog.setIp(httpServletRequest.getRemoteAddr()); //远程客户端ip
        loginLog.setLocation(WebUtil.getCityByIP(httpServletRequest.getRemoteAddr()));
        String agent = httpServletRequest.getHeader("user-agent");
        loginLog.setBrowserName(WebUtil.getBrowserName(agent)); //浏览器名称

        loginLogService.add(loginLog);

        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest,httpServletResponse);
    }
}
