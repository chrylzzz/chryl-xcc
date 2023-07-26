package com.haiyisoft.service.impl;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import com.haiyisoft.handler.IVRHandler;
import com.haiyisoft.handler.NGDHandler;
import com.haiyisoft.handler.XCCHandler;
import com.haiyisoft.service.IVRService;
import com.haiyisoft.model.NGDNodeMetaData;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * V1版本:
 * 可不依托于百度知识库配置,全部手动处理
 * 流程做复杂处理
 * <p>
 * Created By Chr.yl on 2023-02-08.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class IVRServiceV1 implements IVRService {

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
                String retKey = XCCConstants.YYSR;
                String retValue = XCCConstants.WELCOME_TEXT;
                while (true) {

                    xccEvent = IVRHandler.domain(nc, channelEvent, retKey, retValue, ivrEvent, ngdEvent, callerIdNumber);

                    //处理是否已挂机
                    boolean handleHangup = XCCHandler.handleSomeHangup(xccEvent, channelId);
                    if (handleHangup) {//挂机
                        //TODO 记录已挂机的IVR对话日志
                        log.info("挂断部分");
                        break;
                    } else {//正常通话

                        log.info("正常通话");
                        //处理是否识别
                        boolean xccInput = XCCHandler.handleXccInput(xccEvent);
                        //判断是否识别到(dtmf/speech)
                        if (xccInput) {//xcc已识别

                            log.info("已识别到数据");
                            //xcc识别数据
                            String xccRecognitionResult = xccEvent.getXccRecognitionResult();
                            //获取指令和话术
                            ngdEvent = NGDHandler.handler(xccRecognitionResult, channelId, callerIdNumber, icdCallerId, phoneAdsCode, ngdEvent);

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
                                    //TODO 记录已挂机的IVR对话日志
                                    //转人工后挂机
                                    break;
                                }

                            }

                            retKey = ngdEvent.getRetKey();
                            retValue = ngdEvent.getRetValue();

                        } else {//xcc未识别

                            log.info("未识别到数据");
                            //触发转人工规则
                            ivrEvent = IVRHandler.transferRule(ivrEvent, channelEvent, nc, ngdEvent, callerIdNumber);
                            if (ivrEvent.isTransferFlag()) {
                                //TODO 记录已挂机的IVR对话日志
                                //转人工后挂机
                                break;
                            }
                            if (XCCConstants.DETECT_SPEECH.equals(xccEvent.getXccMethod())) {
                                retKey = "YYSR";
                                retValue = "已检测到您没说话, 您请说";
                            } else if (XCCConstants.READ_DTMF.equals(xccEvent.getXccMethod())) {
                                retKey = "AJSR";
                                retValue = "已检测到您未输入, 请输入";
                            }
                        }

                        //记录IVR对话日志
                        NGDNodeMetaData ngdNodeMetaData = ngdEvent.getNgdNodeMetaData();
                        ivrEvent.getNgdNodeMetadataArray().add(ngdNodeMetaData);

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
            log.info("hangup ivrEvent data: {}", ivrEvent);

            //挂断双方
            XCCHandler.hangup(nc, channelEvent);
            log.info("hangup this call channelId: {} ", channelId);
        }
    }


}
