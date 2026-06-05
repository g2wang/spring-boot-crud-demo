package com.example.cruddemo.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServlet(
            @Value("${spring.h2.console.path:/h2-console}") String consolePath) {
        String path = consolePath.endsWith("/") ? consolePath.substring(0, consolePath.length() - 1) : consolePath;
        ServletRegistrationBean<JakartaWebServlet> registration =
                new ServletRegistrationBean<>(new JakartaWebServlet(), path, path + "/*");
        registration.setName("h2Console");
        return registration;
    }
}
