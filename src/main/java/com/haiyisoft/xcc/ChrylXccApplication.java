package com.haiyisoft.xcc;

import com.haiyisoft.chryl.ChrylConfigProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync(proxyTargetClass = true)//开启异步任务支持
@SpringBootApplication
@EnableConfigurationProperties({ChrylConfigProperty.class})
@ComponentScan("com.haiyisoft.**")
public class ChrylXccApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ChrylXccApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return super.configure(builder);
    }

}
