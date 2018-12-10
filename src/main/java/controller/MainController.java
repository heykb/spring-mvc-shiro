package controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
public class MainController {


    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public String hello(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request,
                        @RequestParam(value = "rememberMe",required = false) Boolean rememberMe){
        Subject subject = SecurityUtils.getSubject();
        //System.out.println(username+": "+password);
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
        token.setRememberMe(rememberMe!=null?true:false);

        try{
            subject.login(token);
            subject.checkRole("user");
            subject.checkPermission("user:update");
        }catch (Exception e){
            return e.getMessage();
        }
        String url = WebUtils.getSavedRequest(request).getRequestUrl();
        System.out.println(url);
        return "success";
    }

    @RequestMapping(value = "/checkRoles",method = RequestMethod.GET)
    @ResponseBody
    @RequiresRoles("user")
    public String checkRoles(){
       return "checkRoles success";
    }
}
