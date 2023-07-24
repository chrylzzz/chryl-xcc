package com.haiyisoft.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.XCCEvent;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * XCC工具类
 * Created By Chr.yl on 2023-02-08.
 *
 * @author Chr.yl
 **/
@Slf4j
public class XCCUtil {

    /********************************************xswitch相关********************************************/

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
    public static void getState(Connection nc, ChannelEvent channelEvent) {
        RequestUtil request = new RequestUtil();
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        params.put("uuid", channelEvent.getUuid());
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequest(nc, service, XCCConstants.GET_STATE, params);
    }


    //接管话务
    public static void accept(Connection nc, ChannelEvent channelEvent) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelUuid = channelEvent.getUuid();
        params.put("uuid", channelUuid);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        RequestUtil.natsRequest(nc, service, XCCConstants.ACCEPT, params);
    }

    /**
     * 应答
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    public static XCCEvent answer(Connection nc, ChannelEvent channelEvent) {
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
    public static void hangup(Connection nc, ChannelEvent channelEvent) {
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
    public static XCCEvent playTTS(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        log.info("TTS播报内容为 : {}", ttsContent);
        JSONObject media = getPlayMedia(XCCConstants.PLAY_TTS, ttsContent);
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
    public void playFILE(Connection nc, ChannelEvent channelEvent, String file) {
        JSONObject params = new JSONObject();
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelUuid = channelEvent.getUuid();
        params.put("uuid", channelUuid);
        JSONObject media = getPlayMedia(XCCConstants.PLAY_FILE, file);
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
    public static XCCEvent detectSpeechPlayTTSNoDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        return detectSpeechPlayBody(nc, channelEvent, ttsContent, getSpeech());
    }

    /**
     * 不可打断
     * 语音识别,播放语音,收集语音(不收集按键)
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    public static XCCEvent detectSpeechPlayTTSNoDTMFNoBreak(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        return detectSpeechPlayBody(nc, channelEvent, ttsContent, getSpeechNoBreak());
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
    public static XCCEvent playAndReadDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent, int maxDigits) {
//        播放一个语音并获取用户按键信息，将在收到满足条件的按键后返回。
//        data：播放的媒体，可以是语音文件或TTS。
//        返回结果：
//        dtmf：收到的按键。
//        terminator：结束符，如果有的话。
//        本接口将在收到第一个DTMF按键后打断当前的播放。
        JSONObject params = getDTMF(maxDigits);
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        JSONObject media = getPlayMedia(XCCConstants.PLAY_TTS, ttsContent);
        params.put("media", media);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByReadDTMF(nc, service, XCCConstants.READ_DTMF, params, null);
    }


    /**
     * 转人工测试
     * Transfer Artificial
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     */
    public static void handleTransferArtificial(Connection nc, ChannelEvent channelEvent, String ttsContent) {
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
    public static XCCEvent bridgeExtension(Connection nc, ChannelEvent channelEvent, String ttsContent) {

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

        JSONObject params = convertBridgeParams(channelEvent, "user/1001", "555555555555555", "13287983898");
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
    public static XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr, String sipHeader, String callNumber) {
        //正在转接,请稍后
        playTTS(nc, channelEvent, ttsContent);
        JSONObject params = convertBridgeParams(channelEvent, dialStr, sipHeader, callNumber);
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
    public static XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr) {
        //正在转接,请稍后
        playTTS(nc, channelEvent, ttsContent);
        JSONObject params = convertBridgeParams(channelEvent, dialStr);
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
    public static void writeLog(Connection nc, ChannelEvent channelEvent, String level, String data) {
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

    /********************************************xswitch相关********************************************/

    /********************************************请求体*************************************************/

    /**
     * 获取媒体对象
     *
     * @param playType play类型
     * @param content  播报内容,可为string,file
     * @return JSONObject
     */
    public static JSONObject getPlayMedia(String playType, String content) {
        JSONObject media = new JSONObject();
        /**
         *  type：枚举字符串，文件类型，
         *        FILE：文件
         *        TEXT：TTS，即语音合成
         *        SSML：TTS，SSML格式支持（并非所有引擎都支持SSML）
         */
        media.put("type", playType);
        media.put("data", "[" + IVRInit.CHRYL_CONFIG_PROPERTY.getXttsS() + "]" + content);
//        media.put("data", content);
        //引擎TTS engine,若使用xswitch配置unimrcp,则为unimrcp:profile
        media.put("engine", IVRInit.CHRYL_CONFIG_PROPERTY.getTtsEngine());
        //嗓音Voice-Name，由TTS引擎决定，默认为default。
        media.put("voice", IVRInit.CHRYL_CONFIG_PROPERTY.getTtsVoice());
        return media;
    }

    /**
     * 获取按键对象
     *
     * @param maxDigits 最大位长
     * @return
     */
    public static JSONObject getDTMF(int maxDigits) {
        JSONObject dtmf = new JSONObject();
        dtmf.put("min_digits", 1);//min_digits：最小位长。
        dtmf.put("max_digits", maxDigits);//max_digits：最大位长。
        dtmf.put("timeout", IVRInit.CHRYL_CONFIG_PROPERTY.getDtmfNoInputTimeout());//timeout：超时，默认5000ms。
        dtmf.put("digit_timeout", IVRInit.CHRYL_CONFIG_PROPERTY.getDigitTimeout());//digit_timeout：位间超时，默认2000ms。
        dtmf.put("terminators", XCCConstants.DTMF_TERMINATORS);//terminators：结束符，如#。
        return dtmf;
    }

    /**
     * 获取语音识别对象
     *
     * @param nobreak
     * @return
     */
    public static JSONObject getSpeechBody(String nobreak) {
        JSONObject speech = new JSONObject();
        //默认传，default为docker grammar file: /usr/local/freeswitch/grammar/default.gram
//        speech.put("grammar", "default");
        speech.put("grammar", "builtin:grammar/boolean?language=zh-CN;y=1;n=2 builtin");
        //引擎ASR engine,若使用xswitch配置unimrcp,则为unimrcp:profile.
        speech.put("engine", IVRInit.CHRYL_CONFIG_PROPERTY.getAsrEngine());
        //禁止打断。用户讲话不会打断放音。
//        speech.put("nobreak", XCCConstants.NO_BREAK);
        speech.put("nobreak", nobreak);
        //正整数，未检测到语音超时，默认为5000ms
//        speech.put("no_input_timeout", 5 * 1000);
        speech.put("no_input_timeout", IVRInit.CHRYL_CONFIG_PROPERTY.getSpeechNoInputTimeout());
        //语音超时，即如果对方讲话一直不停超时，最大只能设置成6000ms，默认为6000ms。
//        speech.put("speech_timeout", 8 * 1000);
        //正整数，语音最大超时，和参数speech_timeout作用相同，如果max_speech_timeout的值大于speech_timeout，则以max_speech_timeout为主，用于一些特殊场景的语音时长设置。
//        speech.put("max_speech_timeout", 8 * 1000);
        speech.put("max_speech_timeout", IVRInit.CHRYL_CONFIG_PROPERTY.getMaxSpeechTimeout());
        //是否返回中间结果
        speech.put("partial_event", true);
        //默认会发送Event.DetectedData事件，如果为true则不发送。
        speech.put("disable_detected_data_event", true);
        return speech;
    }

    /**
     * 获取语音对象
     *
     * @return JSONObject
     */
    public static JSONObject getSpeech() {
        return getSpeechBody(IVRInit.CHRYL_CONFIG_PROPERTY.getNoBreak());
    }

    /**
     * 获取语音对象
     * 不可打断
     *
     * @return
     */
    public static JSONObject getSpeechNoBreak() {
        return getSpeechBody("true");
    }

    /**
     * detectSpeechPlay请求体
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @param speech
     * @return
     */
    public static XCCEvent detectSpeechPlayBody(Connection nc, ChannelEvent channelEvent, String ttsContent, JSONObject speech) {
        JSONObject params = new JSONObject();
        //ctrl_uuid:ctrl_uuid
        params.put("ctrl_uuid", "chryl-ivvr");
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        log.info("TTS播报内容为 : {}", ttsContent);
        JSONObject media = getPlayMedia(XCCConstants.PLAY_TTS, ttsContent);
        params.put("media", media);
        //如果不需要同时检测DTMF，可以不传该参数。
//        params.put("dtmf", null);
//        JSONObject speech = getSpeech();
        params.put("speech", speech);
        String service = IVRInit.CHRYL_CONFIG_PROPERTY.getXnodeSubjectPrefix() + channelEvent.getNodeUuid();
        return RequestUtil.natsRequestFutureByDetectSpeech(nc, service, XCCConstants.DETECT_SPEECH, params, null);
    }

    /********************************************请求体***********************************************/

    /********************************************数据处理********************************************/

    /**
     * 转人工和转精准ivr
     * sofia/default/1001@10.194.38.38:5060
     *
     * @param number 转接号
     * @return
     */
    public static String convertDialStr(String number) {
        StringBuilder append = new StringBuilder("sofia/default/").append(number).append("@");
        String sipIp = "";
        if (IpUtil.INTERNET_IP.equals(XCCConstants.IP_201)) {
            sipIp = XCCConstants.IP_92;
        } else if (IpUtil.INTERNET_IP.equals(XCCConstants.IP_203)) {
            sipIp = XCCConstants.IP_102;
        } else {
            sipIp = XCCConstants.IP_92;
        }
        String toString = append.append(sipIp).toString();
        log.info("转接 dial_string : {}", toString);
        return toString;
    }

    /**
     * 组装 bridge:
     * sip header
     * caller id number
     *
     * @param channelEvent
     * @param dialStr
     * @param sipHeader
     * @param cidPhoneNumber
     * @return
     */
    public static JSONObject convertBridgeParams(ChannelEvent channelEvent, String dialStr, String sipHeader, String cidPhoneNumber) {
        //全局参数
//        JSONObject globalParam = new JSONObject();

        //组装call params arr
        JSONObject callParamArr = new JSONObject();
        callParamArr.put("leg_timeout", "20");
        //sip header
        callParamArr.put(XCCConstants.SIP_HEADER_USER2USER, sipHeader);
        callParamArr.put("find_sip_device_only", "false");

        //呼叫参数
        JSONObject callParam = new JSONObject();
        callParam.put("uuid", IdGenerator.fastSimpleUUID());
        callParam.put("dial_string", dialStr);
        //Caller ID Number
        callParam.put("cid_number", cidPhoneNumber);
        callParam.put("params", callParamArr);
        //[{},{}]
        JSONArray callParamArray = new JSONArray();
        callParamArray.add(callParam);
        //组装destination
        JSONObject destination = new JSONObject();
//        destination.put("global_params", globalParam);
        destination.put("call_params", callParamArray);

        JSONObject params = new JSONObject();
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        params.put("ctrl_uuid", "chryl-ivvr");
        params.put("flow_control", XCCConstants.ANY);
        params.put("ringall", "false");
        params.put("destination", destination);
        log.info("转接 params : {}", params);
        return params;
    }

    /**
     * @param channelEvent
     * @param dialStr
     * @return
     */
    public static JSONObject convertBridgeParams(ChannelEvent channelEvent, String dialStr) {
        //全局参数
//        JSONObject globalParam = new JSONObject();

        //组装call params arr
        JSONObject callParamArr = new JSONObject();
        callParamArr.put("leg_timeout", "20");
        callParamArr.put("find_sip_device_only", "false");

        //呼叫参数
        JSONObject callParam = new JSONObject();
        callParam.put("uuid", IdGenerator.fastSimpleUUID());
        callParam.put("dial_string", dialStr);
        callParam.put("params", callParamArr);
        //[{},{}]
        JSONArray callParamArray = new JSONArray();
        callParamArray.add(callParam);
        //组装destination
        JSONObject destination = new JSONObject();
//        destination.put("global_params", globalParam);
        destination.put("call_params", callParamArray);

        JSONObject params = new JSONObject();
        //当前channel 的uuid
        String channelId = channelEvent.getUuid();
        params.put("uuid", channelId);
        params.put("ctrl_uuid", "chryl-ivvr");
        params.put("flow_control", XCCConstants.ANY);
        params.put("ringall", "false");
        params.put("destination", destination);

        return params;
    }

    /********************************************数据处理***********************************************/

}
