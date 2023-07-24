package com.haiyisoft.boot;

import com.haiyisoft.chryl.ChrylConfigProperty;
import com.haiyisoft.config.XCCConfiguration;
import com.haiyisoft.controller.IVRController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 初始化启动:方法执行时，项目已经初始化完毕
 * Created by Chr.yl on 2023/2/21.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class IVRInit {

    @Autowired
    private IVRController ivrController;

    @Bean
    public ApplicationRunner applicationRunner() {
        return applicationArguments -> {
            long startTime = System.currentTimeMillis();
            log.info("{} ：开始调用异步业务", Thread.currentThread().getName());
            ivrController.domain();
            long endTime = System.currentTimeMillis();
            log.info("{} ：调用异步业务结束，耗时：{}", Thread.currentThread().getName(), (endTime - startTime));
        };
    }

    public static ChrylConfigProperty CHRYL_CONFIG_PROPERTY;

    @Autowired
    private ChrylConfigProperty chrylConfigProperty;


    /**
     * 延迟初始化
     * 将初始化变量转为静态
     */
    @PostConstruct
    public void initialize() {
        XCCConfiguration.xccConfig(this.chrylConfigProperty);
        CHRYL_CONFIG_PROPERTY = this.chrylConfigProperty;
        log.info("初始化 CHRYL_CONFIG_PROPERTY 完成");
    }
}
