package com.haiyisoft.business;

import com.haiyisoft.chryl.ivr.DispatcherIVR;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.xcc.client.XCCConnection;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

/**
 * Created by Chr.yl on 2024/1/8.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class ConcurrentASR {
    @Autowired
    private XCCConnection xccConnection;
    @Autowired
    private DispatcherIVR dispatcherIvr;

    public void concurrentASR(Connection nc, ChannelEvent channelEvent, IVREvent ivrEvent, String callerIdNumber, String phoneAdsCode) {
        log.info("ConcurrentASR 开始并发测试");
        contextLoads(nc, channelEvent, ivrEvent, callerIdNumber, phoneAdsCode);
        //挂断双方
        log.info("ConcurrentASR 挂断双方");
        xccConnection.hangup(nc, channelEvent);
    }

    //模拟并发客户数
    private static final int USER_NUMS = 260;
    private static CountDownLatch COUNTDOWNLATCH = new CountDownLatch(USER_NUMS);

    /**
     * 测试并发
     */
    public void contextLoads(Connection nc, ChannelEvent channelEvent, IVREvent ivrEvent, String callerIdNumber, String phoneAdsCode) {
        for (int x = 0; x < USER_NUMS; x++) {

            FutureTask futureTask = new FutureTask(new ASRRequest(nc, channelEvent, ivrEvent, callerIdNumber, phoneAdsCode));
            Thread thread = new Thread(futureTask);

            log.info("ConcurrentASR contextLoads id: {}, name: {}", thread.getId(), thread.getName());

            thread.start();
            COUNTDOWNLATCH.countDown();
        }
    }

    public class ASRRequest implements Callable {

        private Connection nc;
        private ChannelEvent channelEvent;
        private IVREvent ivrEvent;
        private String callerIdNumber;
        private String phoneAdsCode;

        public ASRRequest() {
        }

        public ASRRequest(Connection nc, ChannelEvent channelEvent, IVREvent ivrEvent, String callerIdNumber, String phoneAdsCode) {
            this.nc = nc;
            this.channelEvent = channelEvent;
            this.ivrEvent = ivrEvent;
            this.callerIdNumber = callerIdNumber;
            this.phoneAdsCode = phoneAdsCode;
        }

        @Override
        public Object call() {
            try {
                COUNTDOWNLATCH.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //调用ASR服务
            dispatcherIvr.doDispatch(this.nc, this.channelEvent, "YYSR", "现在开始测试ASR并发,您请说", this.ivrEvent, new NGDEvent(), this.callerIdNumber);
            return null;
        }
    }

}
