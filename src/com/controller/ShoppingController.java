package com.controller;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mvc.RequestMapping;

public class ShoppingController {
    public ShoppingController() {
    }

    @RequestMapping("kindQuery.do")
    public String kindQuery(HashMap<String, Object> map) {
        System.out.println("shoppingController的kindQuery方法");
        System.out.println(map);
        return "welcome.jsp";
    }

    @RequestMapping("kindInsert.do")
    public String kindInsert(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("shoppingController的kindInsert方法");
        System.out.println(request.getParameter("name"));
        System.out.println(request.getParameter("pass"));
        return "welcome.jsp";
    }
}
