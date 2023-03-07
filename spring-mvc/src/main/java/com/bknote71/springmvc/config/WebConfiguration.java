package com.bknote71.springmvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(myArgumentResolver());
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(myReturnValueHandlers());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(myMessageConverter());
    }


    // @EnableWebMvc --> no WebMvcAutoConfiguration
    // - 이 메서드를 적용시키기 위해서는 필수인가? 그런듯 하다....
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl cacheControl = CacheControl.noCache();
        registry.addResourceHandler("resources/**")
                .addResourceLocations("classpath:/myfile/")
                .setCacheControl(cacheControl);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        WebMvcConfigurer.super.addCorsMappings(registry);
    }

    @Bean
    public HttpMessageConverter<?> myMessageConverter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(formatter);
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(formatter);
        javaTimeModule.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(javaTimeModule);
        return new MyHttpMessageConverter(objectMapper);
    }


    @Bean
    public MyArgumentResolver myArgumentResolver() {
        return new MyArgumentResolver();
    }

    @Bean
    public HandlerMethodReturnValueHandler myReturnValueHandlers() {
        return new MyReturnValueHandler();
    }

    static class MyArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return true;
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            return "new Object()";
        }
    }

    static class MyReturnValueHandler implements HandlerMethodReturnValueHandler {

        @Override
        public boolean supportsReturnType(MethodParameter returnType) {
            return true;
        }

        @Override
        public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                      ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
            HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
            response.getWriter().println("custom handle return value^^");
            response.flushBuffer();
        }
    }

    static class MyHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

        protected MyHttpMessageConverter(ObjectMapper objectMapper) {
            super(objectMapper);
        }
    }
}
