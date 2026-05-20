package com.acleda.bsonlineshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String API_PREFIX = "/api";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_PREFIX, handler ->
                handler.isAnnotationPresent(RestController.class)
                        && handler.getPackageName().startsWith("com.acleda.bsonlineshop.controller"));
    }
}
