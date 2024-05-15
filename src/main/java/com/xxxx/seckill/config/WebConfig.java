package com.xxxx.seckill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC配置类
 * 配置类拦截器、解析器等
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;

    //添加解析器以支持自定义控制器方法参数类型。
    //该方法可以用在对于Controller中方法参数传入之前对该参数进行处理。然后将处理好的参数在传给Controller中的方法。
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }

    //静态资源展示
    //想自定义静态资源映射目录的话，只需重写addResourceHandlers方法即可。
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    //注册拦截器
    //需要一个实现HandlerInterceptor接口的拦截器实例
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(accessLimitInterceptor);
    }

}