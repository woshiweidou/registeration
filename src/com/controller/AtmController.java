package com.controller;

import com.domain.User;
import com.mvc.ModelAndView;
import com.mvc.RequestMapping;
import com.mvc.ResponseBody;
import com.mvc.SessionAttributes;
import com.service.AtmService;

import java.util.List;

@RequestMapping("AtmController.do")
@SessionAttributes({"name"})
public class AtmController {
    private AtmService service = new AtmService();

    public AtmController() {
    }

    @RequestMapping("login.do")
    public ModelAndView login(User user) {
        ModelAndView mv = new ModelAndView();
        String result = this.service.login(user);
        if ("success".equals(result)) {
            mv.addObject("name", user.getName());
            mv.setViewName("welcome.jsp");
        } else {
            mv.addObject("result", result);
            mv.setViewName("index.jsp");
        }

        return mv;
    }

    @ResponseBody
    @RequestMapping("query.do")
    public List<User> query() {
        System.out.println("我是query的Controller");
        return null;
    }
}
