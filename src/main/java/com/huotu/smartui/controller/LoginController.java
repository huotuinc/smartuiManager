package com.huotu.smartui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huotu.smartui.utils.ResultUtil;

@Controller
public class LoginController {

    /**
     * 设置权限
     * @param user
     * @param appkey
     * @return
     */
    @RequestMapping(value="/login" ,method=RequestMethod.POST)
    @ResponseBody
    public ModelMap login(String user,String appkey,HttpServletRequest request){
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60*60);
        session.setAttribute("user", user);
        session.setAttribute("appkey", appkey);
        return ResultUtil.success();
    }
}
