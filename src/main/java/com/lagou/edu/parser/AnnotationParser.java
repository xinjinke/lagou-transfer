package com.lagou.edu.parser;


import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Service;
import com.lagou.edu.annotation.Transactional;
import com.lagou.edu.factory.BeanFactory;
import com.lagou.edu.factory.ProxyFactory;
import org.reflections.Reflections;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnnotationParser {

    public static Map<String,Object> map = new HashMap<>();// 存储对象

    static {
        try {
            Reflections reflections = new Reflections("com.lagou.edu");
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Service.class);
            for (Class<?> c : classes) {
                //通过反射获得对象
                Object object = c.newInstance();

                Service annotation = c.getAnnotation(Service.class);
                //如果注解没有名称
                if (StringUtils.isEmpty(annotation.value())) {
                    String[] names = c.getName().split("\\.");
                    map.put(names[names.length - 1], object);
                } else {
                    map.put(annotation.value(), object);
                }
            }

            //处理@Autoried依赖关系
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object o = entry.getValue();
                Class c = o.getClass();

                //获取所有的变量
                Field[] fields = c.getDeclaredFields();
                //遍历属性，若持有Autowired注解则注入
                for (Field field : fields) {
                    //判断是否是使用注解的参数
                    if (field.isAnnotationPresent(Autowired.class)
                            && field.getAnnotation(Autowired.class).required()) {
                        String[] names = field.getType().getName().split("\\.");
                        String name = names[names.length - 1];
                        //Autowired注解的位置需要set方法，方便c.getMethods()获取
                        Method[] methods = c.getMethods();
                        for (int j = 0; j < methods.length; j++) {
                            Method method = methods[j];
                            if (method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                                method.invoke(o, map.get(name));
                            }
                        }
                    }
                }
                // 把处理之后的object重新放到map中
                map.put(entry.getKey(),o);
            }


            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object o = entry.getValue();
                Class c = o.getClass();

                //判断对象类是否持有Transactional注解，若有则修改对象为代理对象
                if(c.isAnnotationPresent(Transactional.class)){
                    //获取代理工厂
                    ProxyFactory proxyFactory = (ProxyFactory) getBean("proxyFactory");
                    Class[] face = c.getInterfaces();//获取类c实现的所有接口
                    //判断对象是否实现接口
                    if(face != null && face.length > 0){
                        //实现使用JDK
                        o = proxyFactory.getJdkProxy(o);
                    }else{
                        //没实现使用CGLIB
                        o = proxyFactory.getCglibProxy(o);
                    }
                }
                map.put(entry.getKey(),o);
            }


        }catch(Exception e){

        }
    }

    public static  Object getBean(String id) {
        return map.get(id);
    }

}
