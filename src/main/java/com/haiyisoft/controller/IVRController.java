package com.haiyisoft.controller;

import com.alibaba.fastjson2.JSONObject;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.handler.IVRHandler;
import com.haiyisoft.service.IVRService;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * IVR Control
 * Created By Chr.yl on 2023-02-08.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class IVRController {

    @Autowired
    private IVRService ivrService;

    /**
     * waiting for incoming call
     */
    public void domain() {
        try {
            //获取nats连接
            Connection nc = Nats.connect(IVRInit.CHRYL_CONFIG_PROPERTY.getNatsUrl());
            //从nats获取订阅主题
            Subscription sub = nc.subscribe(IVRInit.CHRYL_CONFIG_PROPERTY.getXctrlSubject());
            log.info("Ivr Controller started");
            while (true) {
                //订阅消息
                Message subMsg = sub.nextMessage(Duration.ofMillis(50000));
                if (subMsg == null) {
                    log.warn(" this subMsg is null ");
                } else {

                    //订阅事件
                    //XNode收到呼叫后，向NATS广播来话消息（Event.Channel（state = START）），Ctrl收到后进行处理。
                    String eventStr = new String(subMsg.getData(), StandardCharsets.UTF_8);
                    JSONObject eventJson = JSONObject.parseObject(eventStr);

                    //log.info("订阅事件 eventJson:{}", eventJson);

                    //event状态:Event.Channel（state=START）
                    String method = eventJson.getString("method");
                    if (XCCConstants.EVENT_CHANNEL.equals(method)) {
                        JSONObject params = eventJson.getJSONObject("params");
                        //convert param
                        ChannelEvent event = IVRHandler.convertParams(params);
                        //asr domain
                        ivrService.handlerChannelEvent(nc, event);

                    } else if (XCCConstants.EVENT_DETECTED_FACE.equals(method)) {
                        log.info("事件 event======Event-Name : {}", "Event.DetectedFace");
                    } else if (XCCConstants.EVENT_NATIVE_EVENT.equals(method)) {
                        log.info("事件 event======Event-Name : {}", "EVENT_NATIVE_EVENT");
                    }

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            log.error("IVRController 发生异常：{}", e);
        }
    }
}
