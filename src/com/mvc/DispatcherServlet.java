package com.mvc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServlet extends HttpServlet {
    private Handler handler = new Handler();

    public DispatcherServlet() {
    }

    public void init(ServletConfig config) {
        boolean flag = this.handler.loadPropertiesFile();
        String packageNames = null;
        if (!flag) {
            packageNames = config.getInitParameter("scanPackage");
        } else {
            packageNames = this.handler.getScanPackageName();
        }

        this.handler.scanAnnotation(packageNames);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            //首先获取浏览器的请求的uri
            String uri = request.getRequestURI();
            //原先我们把这个流程的很多事情都是放在这个里面写 但是后来发现不好 因为不符合Java这种编程思想 所以我们把这个东西单独踢了出去把这些
            //东西交由handler去处理，所以在这个地方我们进行创建handler
            //这里我们就使用handler去解析字符串
            String requestContent = this.handler.parseURI(uri);
            //这个地方我们接收到了请求 所携带的要访问的方法名 我们获取它主要还是通过这个再去寻找到配置文件 然后读取全类名 最后进行反射实现方法的寻找与执行
            String methodName = request.getParameter("method");
            if (methodName == null) {
                methodName = requestContent.substring(0, requestContent.indexOf("."));
            }
            //寻找类
            Object obj = this.handler.findObject(requestContent);
            //寻找方法
            Method method = this.handler.findMethod(obj, methodName);
            //将我们获得的值注入进去
            Object[] finalParamValue = this.handler.injectionParameters(method, request, response);
            Object methodResult = method.invoke(obj, finalParamValue);
            this.handler.finalResolver(obj, method, methodResult, request, response);
        } catch (ClassNotFoundException var10) {
            var10.printStackTrace();
        } catch (IllegalAccessException var11) {
            var11.printStackTrace();
        } catch (InstantiationException var12) {
            var12.printStackTrace();
        } catch (NoSuchMethodException var13) {
            var13.printStackTrace();
        } catch (InvocationTargetException var14) {
            var14.printStackTrace();
        } catch (java.lang.NoSuchMethodException var15) {
            var15.printStackTrace();
        }

    }
}
