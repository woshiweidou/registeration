package com.mvc;

import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Handler {
    //这个将作为我们请求名与他们真实的类名进行存储 因为我们想要在请求名的时候截取到全类名不仅对于用户的体验不好 也对于框架开发人员具有一定的约束
    private Map<String, String> realClassNameMap = new HashMap();
    //我们将类与其类名也作为一个存储
    private Map<String, Object> objectMap = new HashMap();
    private Map<Object, Map<String, Method>> objectMethodMap = new HashMap();
    private Map<String, String> methodRealClassNameMap = new HashMap();

    public Handler() {
    }

    String getScanPackageName() {
        return (String)this.realClassNameMap.get("scanPackage");
    }

    boolean loadPropertiesFile() {
        boolean flag = true;

        try {
            Properties properties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ApplicationContext.properties");
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();

            while(en.hasMoreElements()) {
                String key = (String)en.nextElement();
                String value = properties.getProperty(key);
                this.realClassNameMap.put(key, value);
            }

        } catch (IOException var7) {
            var7.printStackTrace();
        } catch (NullPointerException var8) {
            flag = false;
        }

        return flag;
    }

    void scanAnnotation(String packageNames) {
        if (packageNames != null) {
            String[] packages = packageNames.split(",");
            String[] var3 = packages;
            int var4 = packages.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String packageName = var3[var5];
                URL url = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", "\\"));
                if (url != null) {
                    String packagePath = url.getPath();
                    File packageFile = new File(packagePath);
                    File[] files = packageFile.listFiles((filex) -> {
                        return filex.isFile() && filex.getName().endsWith("class");
                    });
                    File[] var11 = files;
                    int var12 = files.length;

                    for(int var13 = 0; var13 < var12; ++var13) {
                        File file = var11[var13];
                        String simpleName = file.getName();
                        String fullName = packageName + "." + simpleName.substring(0, simpleName.indexOf("."));

                        try {
                            Class clazz = Class.forName(fullName);
                            RequestMapping classAnnotation = (RequestMapping)clazz.getAnnotation(RequestMapping.class);
                            if (classAnnotation != null) {
                                this.realClassNameMap.put(classAnnotation.value(), fullName);
                            }

                            Method[] methods = clazz.getDeclaredMethods();
                            Method[] var20 = methods;
                            int var21 = methods.length;

                            for(int var22 = 0; var22 < var21; ++var22) {
                                Method method = var20[var22];
                                RequestMapping methodAnnotation = (RequestMapping)method.getAnnotation(RequestMapping.class);
                                if (methodAnnotation == null) {
                                    throw new NoSuchMethodException("没有找到对应的方法 请检查注解");
                                }

                                this.methodRealClassNameMap.put(methodAnnotation.value(), fullName);
                            }
                        } catch (ClassNotFoundException var25) {
                            var25.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    String parseURI(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    Object findObject(String requestContent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //这个地方就是通过我们在 init的时候就去扫描文件  然后将我们获取到的请求方法名和这个集合当中的kv对匹配然后获取到类全名，
        Object obj = this.objectMap.get(requestContent);
        if (obj == null) {
            //这个地方判断我们获得的对象是否为空 为空那么就可能是真的没有扫描到 也有可能是初次创建这个对象 尚未有这个对象存在 所以接下来为了看看有没有这个对象我们就从
            //一开始扫描到的集合当中去寻找这个全类名看看是不是有这个类存在
            String fullClassName = (String)this.realClassNameMap.get(requestContent);
            if (fullClassName == null) {
                fullClassName = (String)this.methodRealClassNameMap.get(requestContent);
                if (fullClassName == null) {
                    throw new ControllerNotFoundException(requestContent + "不存在");
                }
            }

            //经过筛选之后，可以发现这个类是初次创建 所以我们从配置文件当中寻找到了相应的全类名 进行反射
            Class clazz = Class.forName(fullClassName);
            obj = clazz.newInstance();
            //我们把创建好的这个对象要放入集合中 以便于下次请求的查找
            this.objectMap.put(requestContent, obj);
            //做完以上的事情 我们就继续的去找到方法 但是这个时候不一样 由于我们想要去剔除我们所写的普通方法当中的参数 并且我们还想给那个
            //相应的方法上面的参数进行赋值。因此我们又需要首先把所有的方法名 我们给遍历出来然后用我们所拿到的请求方法名与这个当中的相对比

            //这一步我们就拿到了这个对象之下的所有方法名
            Method[] methods = clazz.getDeclaredMethods();
            Map<String, Method> methodMap = new HashMap();
            Method[] var7 = methods;
            int var8 = methods.length;
            //我们对拿到的方法名进行遍历 然后把拿到的方法名 与相应的方法放入集合当中去 以便于进一步的筛选查询
            for(int var9 = 0; var9 < var8; ++var9) {
                Method method = var7[var9];
                methodMap.put(method.getName(), method);
            }

            //最后这一步是要将这个一个对象 与他下面的方法名 方法相对应起来
            this.objectMethodMap.put(obj, methodMap);
        }

        return obj;
    }


    //至于为什么需要这个方法 在查找对象那里  其实已经可以找到那个方法了 但是我们现在有更大的野心 也希望用户体验更好 那就是他自己设置的那个参数 我们帮他进行DI
    Method findMethod(Object obj, String methodName) {
        Map<String, Method> methodMap = (Map)this.objectMethodMap.get(obj);
        return (Method)methodMap.get(methodName);
    }

    //当然对于进行DI注入 我们也还是需要注意 进行DI注入 我们需要什么条件 我们是要给方法的参数进行DI注入 那么我肯定需要这个方法 我肯定需要这个方法的参数
    //而且 我要求的不止这一点 我要拿到那个 方法的参数值  而且把那个参数值用请求拿到的值 注入给他
    //那么我最终想要结果就是 我帮他把请求的值注入了 这就是我要的 但是参数值可能不止一个 而且返回的值应该又是什么呢
    Object[] injectionParameters(Method method, HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, java.lang.NoSuchMethodException {
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length != 0) {
            Object[] finalParamValue = new Object[parameters.length];

            for(int i = 0; i < parameters.length; ++i) {
                Parameter parameter = parameters[i];
                RequestParam paramAnnotation = (RequestParam)parameter.getAnnotation(RequestParam.class);
                if (paramAnnotation != null) {
                    String key = paramAnnotation.value();
                    String value = request.getParameter(key);
                    if (value != null) {
                        Class paramClazz = parameter.getType();
                        if (paramClazz == String.class) {
                            finalParamValue[i] = value;
                        } else if (paramClazz != Integer.class && paramClazz != Integer.TYPE) {
                            if (paramClazz != Float.class && paramClazz != Float.TYPE) {
                                if (paramClazz == Double.class || paramClazz == Double.TYPE) {
                                    finalParamValue[i] = new Double(value);
                                }
                            } else {
                                finalParamValue[i] = new Float(value);
                            }
                        } else {
                            finalParamValue[i] = new Integer(value);
                        }
                    }
                } else {
                    Class paramClazz = parameter.getType();
                    if (paramClazz.isArray()) {
                        throw new ParameterTypeException("方法内数组参数无法处理");
                    }

                    if (paramClazz == HttpServletRequest.class) {
                        finalParamValue[i] = request;
                    } else if (paramClazz == HttpServletResponse.class) {
                        finalParamValue[i] = response;
                    } else {
                        if (paramClazz == Map.class || paramClazz == List.class) {
                            throw new ParameterTypeException("方法内集合不能传递接口 请提供具体类型");
                        }

                        Object paramObj = paramClazz.newInstance();
                        if (paramObj instanceof Map) {
                            Map<String, Object> paramMap = (Map)paramObj;
                            Enumeration en = request.getParameterNames();

                            while(en.hasMoreElements()) {
                                String key = (String)en.nextElement();
                                String value = request.getParameter(key);
                                paramMap.put(key, value);
                            }

                            finalParamValue[i] = paramMap;
                        } else {
                            if (!(paramObj instanceof Object)) {
                                throw new ParameterTypeException("未知类型 我处理不了啦 太累啦");
                            }

                            Field[] fields = paramClazz.getDeclaredFields();
                            Field[] var24 = fields;
                            int var25 = fields.length;

                            for(int var26 = 0; var26 < var25; ++var26) {
                                Field field = var24[var26];
                                field.setAccessible(true);
                                String key = field.getName();
                                String value = request.getParameter(key);
                                Class fieldType = field.getType();
                                Constructor fieldContructor = fieldType.getConstructor(String.class);
                                field.set(paramObj, fieldContructor.newInstance(value));
                            }

                            finalParamValue[i] = paramObj;
                        }
                    }
                }
            }

            return finalParamValue;
        } else {
            return null;
        }
    }

    private void parseResponseContent(String viewName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"".equals(viewName) && !"null".equals(viewName)) {
            String[] value = viewName.split(":");
            if (value.length == 1) {
                request.getRequestDispatcher(viewName).forward(request, response);
            } else if ("redirect".equals(value[0])) {
                response.sendRedirect(value[1]);
            }

        } else {
            System.out.println("不好好玩儿 我也不处理");
            throw new ViewNameFormatException("Controller响应的ViewName不能为空");
        }
    }

    private void parseModelAndView(Object obj, ModelAndView mv, HttpServletRequest request) {
        HashMap<String, Object> mvMap = mv.getAttributeMap();
        Set<String> keys = mvMap.keySet();
        Iterator it = keys.iterator();

        while(it.hasNext()) {
            String key = (String)it.next();
            Object value = mvMap.get(key);
            request.setAttribute(key, value);
        }

        SessionAttributes sattr = (SessionAttributes)obj.getClass().getAnnotation(SessionAttributes.class);
        if (sattr != null) {
            String[] attributeNames = sattr.value();
            if (attributeNames.length != 0) {
                HttpSession session = request.getSession();
                String[] var10 = attributeNames;
                int var11 = attributeNames.length;

                for(int var12 = 0; var12 < var11; ++var12) {
                    String attributeName = var10[var12];
                    session.setAttribute(attributeName, mvMap.get(attributeName));
                }
            }
        }

    }

    void finalResolver(Object obj, Method method, Object methodResult, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (methodResult != null) {
            if (methodResult instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView)methodResult;
                this.parseModelAndView(obj, mv, request);
                this.parseResponseContent(mv.getViewName(), request, response);
            } else {
                ResponseBody responseBody;
                if (methodResult instanceof String) {
                    responseBody = (ResponseBody)method.getAnnotation(ResponseBody.class);
                    if (responseBody != null) {
                        response.setContentType("text/html;charset=UTF-8");
                        response.getWriter().write((String)methodResult);
                    } else {
                        this.parseResponseContent((String)methodResult, request, response);
                    }
                } else {
                    responseBody = (ResponseBody)method.getAnnotation(ResponseBody.class);
                    if (responseBody != null) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("jsonObject", methodResult);
                        response.getWriter().write(jsonObject.toJSONString());
                    }
                }
            }

        }
    }
}
