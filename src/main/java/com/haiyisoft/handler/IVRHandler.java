package com.haiyisoft.handler;

import com.alibaba.fastjson2.JSONObject;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ivr业务处理
 * Created by Chr.yl on 2023/6/4.
 *
 * @author Chr.yl
 */
@Slf4j
public class IVRHandler {


    /**
     * IVR INVOKE
     *
     * @param nc
     * @param channelEvent
     * @param retKey
     * @param retValue
     * @param ngdEvent
     * @param ivrEvent
     * @param callerIdNumber
     * @return
     */
    public static XCCEvent domain(Connection nc, ChannelEvent channelEvent, String retKey, String retValue, IVREvent ivrEvent, NGDEvent ngdEvent, String callerIdNumber) {
        XCCEvent xccEvent;
        if (XCCConstants.YYSR.equals(retKey)) {//播报收音
            xccEvent = XCCHandler.detectSpeechPlayTTSNoDTMF(nc, channelEvent, retValue);
        } else if (XCCConstants.AJSR.equals(retKey)) {//收集按键，多位按键
            xccEvent = XCCHandler.playAndReadDTMF(nc, channelEvent, retValue, 18);
        } else if (XCCConstants.YWAJ.equals(retKey)) {//收集按键，一位按键
            xccEvent = XCCHandler.playAndReadDTMF(nc, channelEvent, retValue, 1);
        } else if (XCCConstants.YYGB.equals(retKey)) {//语音广播
            xccEvent = XCCHandler.detectSpeechPlayTTSNoDTMFNoBreak(nc, channelEvent, retValue);
        } else if (XCCConstants.RGYT.equals(retKey)) {//转人工
            //测试-分机
//            xccEvent = XCCHandler.bridgeExtension(nc, channelEvent, retValue);
            //转人工
            xccEvent = XCCHandler.bridgeArtificial(nc, channelEvent, retValue, ngdEvent, callerIdNumber);
        } else if (XCCConstants.JZLC.equals(retKey)) {//转精准IVR
            xccEvent = XCCHandler.bridgeIVR(nc, channelEvent, retValue, ivrEvent, ngdEvent, callerIdNumber);
        } else if (XCCConstants.DXFS.equals(retKey)) {//短信发送
            WebHookHandler.sendMessage(ivrEvent, retValue);
            xccEvent = XCCHandler.detectSpeechPlayTTSNoDTMF(nc, channelEvent, XCCConstants.FAQ_SEND_MESSAGE_TEXT);
        } else {
            log.error("严格根据配置的指令开发");
            xccEvent = new XCCEvent();
        }
        return xccEvent;
    }

    /**
     * 触发转人工规则,若使用bot配置管理控制,则不需要使用该方法
     * ngd: 机器回复一次+1,连续两次机器回复转人工
     * xcc: 未识别一次+1,连续两次未识别转人工
     */
    public static IVREvent transferRule(IVREvent ivrEvent, ChannelEvent channelEvent, Connection nc, NGDEvent ngdEvent, String callerIdNumber) {
        boolean transferFlag = ivrEvent.isTransferFlag();
        if (transferFlag) {//转人工
            XCCHandler.bridgeArtificial(nc, channelEvent, XCCConstants.ARTIFICIAL_TEXT, ngdEvent, callerIdNumber);
        } else {
            //累加次数
            int transferTime = ivrEvent.getTransferTime();
            int newTransferTime = transferTime + 1;
            if (newTransferTime >= XCCConstants.TRANSFER_ARTIFICIAL_TIME) {
                ivrEvent.setTransferFlag(true);
                log.info("transferRule 次数 {} 已累加至 {} , 转人工 channelId : {}", XCCConstants.DEFAULT_TRANSFER_TIME, XCCConstants.TRANSFER_ARTIFICIAL_TIME, ivrEvent.getChannelId());
                XCCHandler.bridgeArtificial(nc, channelEvent, XCCConstants.ARTIFICIAL_TEXT, ngdEvent, callerIdNumber);
            } else {
                log.info("transferRule 已累加 channelId : {} , transferTime : {} ,newTransferTime : {}", ivrEvent.getChannelId(), transferTime, newTransferTime);
                ivrEvent.setTransferTime(newTransferTime);
            }
        }
        return ivrEvent;
    }


    /**
     * 转人工规则清零
     *
     * @param ivrEvent
     * @return
     */
    public static IVREvent transferRuleClean(IVREvent ivrEvent) {
        ivrEvent.setTransferTime(XCCConstants.DEFAULT_TRANSFER_TIME);
        ivrEvent.setTransferFlag(false);
        log.info("transferRuleClean 已重置 channelId : {} , transferTime : {}", ivrEvent.getChannelId(), ivrEvent.getTransferTime());
        return ivrEvent;
    }


    /**
     * 获取xcc话务数据
     *
     * @param params
     * @return
     */
    public static ChannelEvent convertParams(JSONObject params) {
        ChannelEvent event = new ChannelEvent();
        try {
            String uuid = params.getString("uuid");
            String node_uuid = params.getString("node_uuid");
            //当前Channel的状态,如START--Event.Channel（state=START）
            String state = params.getString("state");
            //处理华为传递的SIP消息头
            JSONObject resParams = params.getJSONObject("params");
//            String u2u = resParams.getString(XCCConstants.SIP_HEADER_USER2USER)
//                    == null ? "" : resParams.getString(XCCConstants.SIP_HEADER_USER2USER) + XCCConstants.SIP_HEADER_SEPARATOR;
            String u2u = resParams.getString(XCCConstants.SIP_HEADER_USER2USER);

            event.setSipReqHeaderU2U(u2u);
            event.setUuid(uuid);
            event.setNodeUuid(node_uuid);
            event.setState(state);
            log.info("convertParams :{}", event);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("convertParams 发生异常：{}", e);
        }
        return event;
    }

    /**
     * 处理随路数据,放到ivrEvent
     *
     * @param channelEvent
     * @return
     */
    public static IVREvent convertIVREvent(ChannelEvent channelEvent) {
        //使用channelId作为callId,sessionId
        String channelId = channelEvent.getUuid();
        String u2U = channelEvent.getSipReqHeaderU2U();

        IVREvent ivrEvent = new IVREvent(channelId);

        if (StringUtils.isBlank(u2U)) {
            log.info("convertIVREvent sip header User-to-User in null");
        } else {
            log.info("convertIVREvent sip header User-to-User : [{}]", u2U);
            //icdCallId、手机号、来话手机所对应的后缀码
            String icd_call_id = "";
            String cid_phone_number = "";
            String phone_code = "";
            try {
                //aaa|ccc|bbb
                String[] splitU2U = u2U.split("\\|");
                if (splitU2U.length >= 3) {
                    icd_call_id = splitU2U[0];
                    cid_phone_number = splitU2U[1];
                    phone_code = splitU2U[2];
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("convertIVREvent sip header User-to-User 报错啦, 无法解析u2u");
            }
            ivrEvent.setIcdCallerId(icd_call_id);
            ivrEvent.setCidPhoneNumber(cid_phone_number);
            ivrEvent.setPhoneAdsCode(phone_code);
            log.info("convertIVREvent OK:{}", ivrEvent);
        }
        return ivrEvent;
    }

}
