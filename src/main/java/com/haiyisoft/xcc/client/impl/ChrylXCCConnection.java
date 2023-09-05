package com.haiyisoft.xcc.client.impl;

import com.alibaba.fastjson2.JSONObject;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.chryl.ivr.DynamicSpeech;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import com.haiyisoft.handler.ChannelHandler;
import com.haiyisoft.util.RequestUtil;
import com.haiyisoft.util.XCCUtil;
import com.haiyisoft.xcc.client.XCCConnection;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chr.yl on 2023/7/20.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class ChrylXCCConnection implements XCCConnection {

    @Override
    public void setVar(Connection nc, ChannelEvent channelEvent) {
        RequestUtil request = new RequestUtil();
        JSONObject params = new JSONObject();
        Map<String, String> data = new HashMap<>();
        data.put("disable_img_fit", "true");
        params.put("ctrl_uuid", "chryl-ivvr");
        params.put("uuid", channelEvent.getUuid());
        params.put("data", data);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequest(nc, service, XCCConstants.SET_VAR, params);
    }


    //获取当前通道状态
    @Override
    public void getState(Connection nc, ChannelEvent channelEvent) {
        RequestUtil request = new RequestUtil();
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        params.put("uuid", channelEvent.getUuid());
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequest(nc, service, XCCConstants.GET_STATE, params);
    }


    //接管话务
    @Override
    public void accept(Connection nc, ChannelEvent channelEvent) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelUuid = channelEvent.getUuid();
        params.put("uuid", channelUuid);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequest(nc, service, XCCConstants.ACCEPT, params);
    }

    @Override
    public XCCEvent answer(Connection nc, ChannelEvent channelEvent) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByAnswer(nc, service, XCCConstants.ANSWER, params);
    }

    /**
     * 挂断
     *
     * @param nc
     * @param channelEvent
     */
    @Override
    public void hangup(Connection nc, ChannelEvent channelEvent) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        params.put("uuid", channelEvent.getUuid());
        //flag integer 值为: 0 挂断自己 , 1 挂断对方 , 2 挂断双方
        params.put("flag", 2);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequestFutureByHangup(nc, service, XCCConstants.HANGUP, params);
    }

    /**
     * 播放text
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent   内容
     */
    @Override
    public XCCEvent playTTS(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        log.info("TTS播报内容为 : {}", ttsContent);
        JSONObject media = XCCUtil.getPlayMedia(XCCConstants.PLAY_TTS, ttsContent, channelEvent.getCidVoiceName());
        params.put("media", media);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByPlayTTS(nc, service, XCCConstants.PLAY, params);
    }

    /**
     * 播放file
     *
     * @param nc
     * @param channelEvent
     * @param file         file_path/png_file
     */
    @Override
    public void playFILE(Connection nc, ChannelEvent channelEvent, String file) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelUuid = channelEvent.getUuid();
        params.put("uuid", channelUuid);
        JSONObject media = XCCUtil.getPlayMedia(XCCConstants.PLAY_FILE, file, channelEvent.getCidVoiceName());
        params.put("media", media);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequest(nc, service, XCCConstants.PLAY, params);
    }

    /**
     * 语音识别,播放语音,收集语音(不收集按键)
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    @Override
    public XCCEvent detectSpeechPlayTTSNoDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        return XCCUtil.detectSpeechPlayBody(nc, channelEvent, ttsContent, XCCUtil.getSpeech());
    }

    /**
     * 不可打断
     * 语音识别,播放语音,收集语音(不收集按键)
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    @Override
    public XCCEvent detectSpeechPlayTTSNoDTMFNoBreak(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        return XCCUtil.detectSpeechPlayBody(nc, channelEvent, ttsContent, XCCUtil.getSpeechNoBreak());
    }

    /**
     * 播报并收集按键
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent   播报内容
     * @param maxDigits    最大位长
     * @return
     */
    @Override
    public XCCEvent playAndReadDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent, int maxDigits) {
//        播放一个语音并获取用户按键信息，将在收到满足条件的按键后返回。
//        data：播放的媒体，可以是语音文件或TTS。
//        返回结果：
//        dtmf：收到的按键。
//        terminator：结束符，如果有的话。
//        本接口将在收到第一个DTMF按键后打断当前的播放。
        JSONObject params = XCCUtil.getDTMF(maxDigits);
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        JSONObject media = XCCUtil.getPlayMedia(XCCConstants.PLAY_TTS, ttsContent, channelEvent.getCidVoiceName());
        params.put("media", media);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByReadDTMF(nc, service, XCCConstants.READ_DTMF, params, 0);
    }

    /**
     * 播报并收集按键(少位)
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent   播报内容
     * @param maxDigits    最大位长
     * @return
     */
    public XCCEvent playAndReadDTMFChryl(Connection nc, ChannelEvent channelEvent, String ttsContent, int maxDigits) {
        JSONObject params = XCCUtil.getDTMFChryl(maxDigits);
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        JSONObject media = XCCUtil.getPlayMedia(XCCConstants.PLAY_TTS, ttsContent, channelEvent.getCidVoiceName());
        params.put("media", media);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByReadDTMF(nc, service, XCCConstants.READ_DTMF, params, 0);
    }

    /**
     * 转人工测试
     * Transfer Artificial
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     */
    @Override
    public void handleTransferArtificial(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        //转分机
        XCCEvent xccEvent = XCCUtil.bridgeExtension(nc, channelEvent, ttsContent);
        //转人工
//        XCCEvent xccEvent = XCCUtil.bridgeArtificial(nc, channelEvent, ttsContent);
    }

    /**
     * TODO
     * 转接内部分机
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @return
     */
    @Override
    public XCCEvent bridgeExtension(Connection nc, ChannelEvent channelEvent, String ttsContent) {

        /*
            {
              "jsonrpc": "2.0",
              "method": "XNode.Bridge",
              "id": "call1",
              "params": {
                "ctrl_uuid": "e808a500-4125-44c1-80f5-e73f86fb5a18",
                "uuid": "1500bbe9-3ffd-4d4a-8703-b2ce42bdd75a",
                "flow_control": "ANY",
                "ringall": false,
                "destination": {
                  "global_params": {},
                  "call_params": [
                    {
                      "uuid": "a255bd17-f5b1-4455-a6fd-a07e9c385adf",
                      "dial_string": "user/9001",
                      "params": {
                        "leg_timeout": "20",
                        "sip_h_User-to-User": "dsafdfdsfdsfdsf",
                        "find_sip_device_only": "false"
                      }
                    }
                  ]
                }
              }
            }

            ///
            https://docs.xswitch.cn/xcc-api/reference/#dial-string

            originate [origination_caller_id_number=9000]sofia/default/1001@10.194.38.38:5060 &echo
            sofia/default/4001@10.100.31.92:5060
        */
        //正在转接人工坐席,请稍后
        playTTS(nc, channelEvent, ttsContent);

        JSONObject params = XCCUtil.convertBridgeParams(channelEvent, "user/1001", "555555555555555", "13287983898");
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByBridge(nc, service, XCCConstants.BRIDGE, params, Duration.ofHours(1L));
    }

    /**
     * 转接
     * 带消息头/原始呼叫
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @return
     */
    @Override
    public XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr, String sipHeader, String callNumber) {
        //正在转接,请稍后
        playTTS(nc, channelEvent, ttsContent);
        JSONObject params = XCCUtil.convertBridgeParams(channelEvent, dialStr, sipHeader, callNumber);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByBridge(nc, service, XCCConstants.BRIDGE, params, Duration.ofHours(1L));
    }

    /**
     * 转接
     * https://docs.xswitch.cn/xcc-api/reference/#dial-string
     * originate [origination_caller_id_number=9000]sofia/default/1001@10.194.38.38:5060 &echo
     * sofia/default/4001@10.100.31.92:5060
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @param dialStr
     * @return
     */
    @Override
    public XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr) {
        //正在转接,请稍后
        playTTS(nc, channelEvent, ttsContent);
        JSONObject params = XCCUtil.convertBridgeParams(channelEvent, dialStr);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByBridge(nc, service, XCCConstants.BRIDGE, params, Duration.ofHours(1L));
    }

    /**
     * 日志打印。
     * 通过调用本接口可实现在XSwitch内打印想要输出的日志信息。该接口不依赖于通话，可在任意处进行调用。
     * <p>
     * level：日志级别。字符串类型。可从以下级别中任选其一。默认为DEBUG。
     * -------DISABLE、CONSOLE、ALERT、CRIT、ERR、WARNING、NOTICE、INFO、DEBUG
     * function：调用该接口代码所在函数名称。
     * file：调用该接口代码所在文件名称。
     * line：调用该接口代码行数。整型。
     * log_uuid：任意字符串。可选。建议为当前通话UUID。
     * data：想要打印的日志信息。
     * 注意，以上六个参数中，只有log_uuid为可选参数，其他均为必填
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    @Override
    public void writeLog(Connection nc, ChannelEvent channelEvent, String level, String data) {
//        XCCUtil.writeLog(nc, channelEvent);
        /*
        {
            "jsonrpc": "2.0",
            "method": "XNode.Log",
            "params": {
                "ctrl_uuid": "f8a02eed-3ea6-42a2-838d-856f529d3fbc",
                "level": "ALERT",
                "function": "xnode_status",
                "file": "log.js",
                "log_uuid": "",
                "line": 69,
                "data": "Hello, this is a test"
            },
            "id": "fake-log"
        }
         */
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        params.put("level", level);
        params.put("function", "xnode_status");
        params.put("file", "log.js");
        params.put("log_uuid", channelId);
        params.put("line", 69);
        params.put("data", data);

        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequestFutureByLog(nc, service, XCCConstants.LOG, params);
    }

    @Override
    public XCCEvent bridgeArtificial(Connection nc, ChannelEvent channelEvent, String retValue, NGDEvent ngdEvent, String callNumber) {
        String dialStr = XCCUtil.convertDialStr(XCCConstants.HUAWEI_ARTIFICIAL_NUMBER);
        String handleSipHeader = ChannelHandler.handleSipHeader(ngdEvent, channelEvent);
        return bridge(nc, channelEvent, retValue, dialStr, handleSipHeader, callNumber);
    }

    @Override
    public XCCEvent bridgeIVR(Connection nc, ChannelEvent channelEvent, String retValue, IVREvent ivrEvent, NGDEvent ngdEvent, String callNumber) {
        //呼叫字符串,不使用4001,使用后缀码
        String phoneAdsCode = ivrEvent.getPhoneAdsCode();
        String dialStr = XCCUtil.convertDialStr(phoneAdsCode);
//        String dialStr = XCCUtil.convertDialStr(XCCConstants.HUAWEI_IVR_NUMBER);
        String handleSipHeader = ChannelHandler.handleSipHeader(ngdEvent, channelEvent);
        return bridge(nc, channelEvent, retValue, dialStr, handleSipHeader, callNumber);
    }

    /********************************************xswitch相关********************************************/

}
