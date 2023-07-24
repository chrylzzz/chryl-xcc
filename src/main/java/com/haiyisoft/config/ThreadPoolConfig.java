package com.haiyisoft.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 重写异步线程
 * Created By Chr.yl on 2023-02-08.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class ThreadPoolConfig implements AsyncConfigurer {

    //获取当前机器的核数
    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setCorePoolSize(CPU_NUM * 5);//核心线程大小
//        taskExecutor.setMaxPoolSize(CPU_NUM * 10);//最大线程大小

//        taskExecutor.setCorePoolSize(CPU_NUM * 6);//核心线程大小
        taskExecutor.setMaxPoolSize(CPU_NUM * 12);//最大线程大小

        taskExecutor.setQueueCapacity(500);//队列最大容量
        // 线程池对拒绝任务的处理策略
        // 当提交的任务个数大于QueueCapacity，就需要设置该参数，但spring提供的都不太满足业务场景，可以自定义一个，也可以注意不要超过QueueCapacity即可
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);
        taskExecutor.setKeepAliveSeconds(60);
        //线程名前缀
        taskExecutor.setThreadNamePrefix("Chryl-Thread-");
        taskExecutor.initialize();
        return taskExecutor;
    }


}
