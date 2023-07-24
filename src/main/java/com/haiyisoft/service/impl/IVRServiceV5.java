package com.haiyisoft.service.impl;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import com.haiyisoft.handler.*;
import com.haiyisoft.model.NGDNodeMetaData;
import com.haiyisoft.service.IVRService;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * V5版本:
 * 欢迎语在百度ngd流程配置(先调用百度)
 * 未识话术别在XCC处理
 * <p>
 * Created By Chr.yl on 2023-02-08.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class IVRServiceV5 implements IVRService {

    @Override
    public void handlerChannelEvent(Connection nc, ChannelEvent channelEvent) {
        String state = channelEvent.getState();
        if (state == null) {
            log.error("state is null ");
        } else {

            //event
            IVREvent ivrEvent = IVRHandler.convertIVREvent(channelEvent);
            XCCEvent xccEvent = new XCCEvent();
            NGDEvent ngdEvent = new NGDEvent();
            //fs caller id
            String channelId = ivrEvent.getChannelId();
            //华为 caller id
            String icdCallerId = ivrEvent.getIcdCallerId();
            //来电号码
            String callerIdNumber = ivrEvent.getCidPhoneNumber();
            //后缀码
            String phoneAdsCode = ivrEvent.getPhoneAdsCode();
            log.info(" start this call channelId: {} , state :{} , IVREvent: {}", channelId, state, ivrEvent);

            if (XCCConstants.CHANNEL_START.equals(state)) {
                //开始接管,第一个指令必须是Accept或Answer
                XCCHandler.answer(nc, channelEvent);
                //
                while (true) {

                    //xcc识别数据
                    String xccRecognitionResult = xccEvent.getXccRecognitionResult();

                    //获取指令和话术
                    ngdEvent = NGDHandler.handler(xccRecognitionResult, channelId, callerIdNumber, icdCallerId, phoneAdsCode);

                    //记录IVR日志
                    NGDNodeMetaData ngdNodeMetaData = ngdEvent.getNgdNodeMetaData();
                    ivrEvent.getNgdNodeMetadataArray().add(ngdNodeMetaData);

                    //handle ngd agent
                    boolean handleSolved = NGDHandler.handleSolved(ngdEvent);
                    //判断是否为机器回复
                    if (handleSolved) {
                        log.info("人为回复");
                        ivrEvent = IVRHandler.transferRuleClean(ivrEvent);
                    } else {
                        log.info("机器回复");
                        //触发转人工规则
                        ivrEvent = IVRHandler.transferRule(ivrEvent, channelEvent, nc, ngdEvent, callerIdNumber);
                        if (ivrEvent.isTransferFlag()) {
                            //转人工后挂机
                            break;
                        }
                    }

                    String retKey = ngdEvent.getRetKey();
                    String retValue = ngdEvent.getRetValue();

                    xccEvent = IVRHandler.domain(nc, channelEvent, retKey, retValue, ivrEvent, ngdEvent, callerIdNumber);

                    //处理是否已挂机
                    boolean handleHangup = XCCHandler.handleSomeHangup(xccEvent, channelId);
                    if (handleHangup) {//挂机
                        //先存的IVR对话日志,这里挂机不需要单独处理
                        log.info("挂断部分");
                        break;
                    }
                    log.info("revert ivrEvent data: {}", ivrEvent);

                }
            } else if (XCCConstants.CHANNEL_CALLING.equals(state)) {
                log.info("CHANNEL_CALLING this call channelId: {}", channelId);
            } else if (XCCConstants.CHANNEL_RINGING.equals(state)) {
                log.info("CHANNEL_RINGING this call channelId: {}", channelId);
            } else if (XCCConstants.CHANNEL_BRIDGE.equals(state)) {
                log.info("CHANNEL_BRIDGE this call channelId: {}", channelId);
            } else if (XCCConstants.CHANNEL_READY.equals(state)) {
                log.info("CHANNEL_READY this call channelId: {}", channelId);
            } else if (XCCConstants.CHANNEL_MEDIA.equals(state)) {
                log.info("CHANNEL_MEDIA this call channelId: {}", channelId);
            } else if (XCCConstants.CHANNEL_DESTROY.equals(state)) {
                log.info("CHANNEL_DESTROY this call channelId: {}", channelId);
            }

            //保存会话记录
            log.info("ivrEvent completed: {}", ivrEvent);
            WebHookHandler.saveCDR(ivrEvent);
            //保存意图
            PMSHandler.saveIntent(ivrEvent, ngdEvent);
            //保存通话数据
            PMSHandler.saveCallData(ivrEvent, ngdEvent);
            //挂断双方
            XCCHandler.hangup(nc, channelEvent);
            log.info("hangup this call channelId: {} ", channelId);

        }
    }

}
