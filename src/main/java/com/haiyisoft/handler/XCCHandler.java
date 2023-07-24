package com.haiyisoft.handler;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import com.haiyisoft.util.NumberUtil;
import com.haiyisoft.util.XCCUtil;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * xcc业务处理
 * Created by Chr.yl on 2023/3/30.
 *
 * @author Chr.yl
 */
@Slf4j
public class XCCHandler {


    /**
     * 处理一挂机的用户
     * xcc code :
     * 100: 未碰到
     * 200  成功
     * 202  按键
     * 400: 未碰到
     * 404: 1.uuid参数错误2.用户主动挂机时调用XSwitch
     * 410: 1.发生在放音或ASR检测过程中用户侧挂机的情况
     * 500: xswitch出错
     * 6xx: 系统错误
     * 555: 自定义错误码
     *
     * @param xccEvent
     * @return true 执行挂机,false 继续执行
     */
    public static boolean handleSomeHangup(XCCEvent xccEvent, String channelId) {
        Integer code = xccEvent.getCode();
        if (XCCConstants.JSONRPC_CANNOT_LOCATE_SESSION_BY_UUID == code//404
                || XCCConstants.JSONRPC_USER_HANGUP == code//410
                || XCCConstants.JSONRPC_CLIENT_ERROR == code//400
                || XCCConstants.JSONRPC_SERVER_ERROR == code//500
                || XCCConstants.CHRYL_ERROR_CODE == code//555
        ) {
            log.info("handleXccAgent 执行挂机 channelId : [{}] , code : [{}]  ,", channelId, code);
            return true;
        }
        //校验为开头
        if (NumberUtil.compareNum(code)) {
            return true;
        }
        return false;
    }

    /**
     * 处理是否输入
     *
     * @param xccEvent
     * @return true 识别输入,false 未输入
     */
    public static boolean handleXccInput(XCCEvent xccEvent) {
        boolean xccInput = xccEvent.isXccInput();
        if (xccInput) {//识别到结果
            return true;
        } else {//未识别到结果
            return false;
        }
    }

    /**
     * 备份
     *
     * @param ivrEvent
     * @param xccEvent
     * @param channelEvent
     * @param nc
     */
    public static void beifen(IVREvent ivrEvent, XCCEvent xccEvent, ChannelEvent channelEvent, Connection nc) {
        Integer code = xccEvent.getCode();
        String type = xccEvent.getType();
        String error = xccEvent.getError();
        String xccRecognitionResult = xccEvent.getXccRecognitionResult();
        String method = xccEvent.getXccMethod();
        boolean xccInput = xccEvent.isXccInput();
        if (XCCConstants.OK == code) {//200
            if (XCCConstants.RECOGNITION_TYPE_SPEECH_END.equals(type)) {
                //Speech.End; asr已完成
            }
            if (XCCConstants.RECOGNITION_TYPE_ERROR.equals(type)) {
//                当 type = ERROR 时, error = no_input//没说话
            }
        } else if (XCCConstants.JSONRPC_TEMP == code) {//100
            //还没碰到
        } else if (XCCConstants.JSONRPC_NOTIFY == code) {//202
            //dtmf 按键和没按键都是202
        } else if (XCCConstants.JSONRPC_CLIENT_ERROR == code) {//400
            //还没碰到
        } else if (XCCConstants.JSONRPC_CANNOT_LOCATE_SESSION_BY_UUID == code) {//404
            //uuid参数错误或者用户主动挂机时调用xswitch
        } else if (XCCConstants.JSONRPC_USER_HANGUP == code) {//410
            //发生在放音或ASR检测过程中用户侧挂机的情况
        } else if (XCCConstants.JSONRPC_SERVER_ERROR == code) {//500
            //xswitch出错
        } else if (XCCConstants.CHRYL_ERROR_CODE == code) {//555
            //自定义错误码
        } else if (XCCConstants.JSONRPC_CODE_SYSTEM_ERROR == code) {//6xx
            //系统错误
        } else {

        }


        if (XCCConstants.OK == code) {//200
            if (XCCConstants.RECOGNITION_TYPE_ERROR.equals(type)) {//当 type = ERROR 时 , error = no_input
                //"YYSR#检测到您没说话, 您请说"
            }
        } else if (XCCConstants.JSONRPC_NOTIFY == code) {//202, 没按键输入
            if (XCCConstants.READ_DTMF.equals(method)) {
                //没按键
                //"YYSR#检测到您未输入, 请输入"
            }
        }

    }


    /**
     * XSwitch有识别结果
     * 赋值xcc返回数据
     *
     * @param code
     * @param message
     * @param xccRecognitionResult
     * @param type
     * @param error
     * @param xccMethod
     * @param cause
     * @return
     */
    public static XCCEvent xccEventSetVar(Integer code, String message, String xccRecognitionResult,
                                          String type, String error, String xccMethod, String cause) {
        log.info("xccEventSetVar 有识别结果入参 code : [{}] , message : [{}] , xccRecognitionResult : [{}] , type : [{}] , error : [{}] , xccMethod : [{}] , cause : [{}]",
                code, message, xccRecognitionResult, type, error, xccMethod, cause);
        XCCEvent xccEvent = new XCCEvent();
        xccEvent.setCode(code);
        xccEvent.setMessage(message);
        xccEvent.setXccRecognitionResult(xccRecognitionResult);
        xccEvent.setType(type);
        xccEvent.setError(error);
        xccEvent.setXccMethod(xccMethod);
        xccEvent.setCause(cause);
        //根据识别内容判断是否输入
        if (StringUtils.isBlank(xccRecognitionResult)) {
            //no_input
            xccEvent.setXccInput(false);
        } else {
            //已识别
            xccEvent.setXccInput(true);
        }
        log.info("xccEventSetVar 有识别结果出参 xccEvent: {}", xccEvent);
        return xccEvent;
    }

    /**
     * XSwitch无识别结果
     * 赋值xcc返回数据
     *
     * @param code
     * @param message
     * @param type
     * @param error
     * @param xccMethod
     * @param cause
     * @return
     */
    public static XCCEvent xccEventSetVar(Integer code, String message, String type,
                                          String error, String xccMethod, String cause) {
        log.info("xccEventSetVar 无识别结果入参 code : [{}] , message : [{}] , type : [{}] , error : [{}] , xccMethod : [{}] , cause : [{}]",
                code, message, type, error, xccMethod, cause);
        XCCEvent xccEvent =
                new XCCEvent(code, message, type, error, cause, "", xccMethod, true);
        log.info("xccEventSetVar 无识别结果出参 xccEvent: {}", xccEvent);
        return xccEvent;
    }


    /**
     * 应答
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    public static XCCEvent answer(Connection nc, ChannelEvent channelEvent) {
        return XCCUtil.answer(nc, channelEvent);
    }

    /**
     * 挂机
     *
     * @param nc
     * @param channelEvent
     */
    public static void hangup(Connection nc, ChannelEvent channelEvent) {
        XCCUtil.hangup(nc, channelEvent);
    }

    /**
     * 收音 不可按键
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @return
     */
    public static XCCEvent detectSpeechPlayTTSNoDTMF(Connection nc, ChannelEvent channelEvent, String retValue) {
        return XCCUtil.detectSpeechPlayTTSNoDTMF(nc, channelEvent, retValue);
    }

    /**
     * 收音 不可按键 不可打断
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @return
     */
    public static XCCEvent detectSpeechPlayTTSNoDTMFNoBreak(Connection nc, ChannelEvent channelEvent, String retValue) {
        return XCCUtil.detectSpeechPlayTTSNoDTMFNoBreak(nc, channelEvent, retValue);
    }

    /**
     * 播报 收按键
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @param maxDigits
     * @return
     */
    public static XCCEvent playAndReadDTMF(Connection nc, ChannelEvent channelEvent, String retValue, int maxDigits) {
        return XCCUtil.playAndReadDTMF(nc, channelEvent, retValue, maxDigits);
    }

    /**
     * 测试-转分机
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @return
     */
    public static XCCEvent bridgeExtension(Connection nc, ChannelEvent channelEvent, String retValue) {
        return XCCUtil.bridgeExtension(nc, channelEvent, retValue);
    }

    /**
     * 转人工
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @param ngdEvent
     * @param callNumber
     * @return
     */
    public static XCCEvent bridgeArtificial(Connection nc, ChannelEvent channelEvent, String retValue, NGDEvent ngdEvent, String callNumber) {
        //呼叫字符串
        String dialStr = XCCUtil.convertDialStr(XCCConstants.HUAWEI_ARTIFICIAL_NUMBER);
        String handleSipHeader = ChannelHandler.handleSipHeader(ngdEvent, channelEvent);
        return XCCUtil.bridge(nc, channelEvent, retValue, dialStr, handleSipHeader, callNumber);
    }

    /**
     * 放音
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @return
     */
    public static XCCEvent playTTS(Connection nc, ChannelEvent channelEvent, String ttsContent) {
        return XCCUtil.playTTS(nc, channelEvent, ttsContent);
    }

    /**
     * 转接到精准IVR
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @param ivrEvent
     * @param ngdEvent
     * @param callNumber
     * @return
     */
    public static XCCEvent bridgeIVR(Connection nc, ChannelEvent channelEvent, String retValue, IVREvent ivrEvent, NGDEvent ngdEvent, String callNumber) {
        //呼叫字符串,不使用4001,使用后缀码
        String phoneAdsCode = ivrEvent.getPhoneAdsCode();
        String dialStr = XCCUtil.convertDialStr(phoneAdsCode);
//        String dialStr = XCCUtil.convertDialStr(XCCConstants.HUAWEI_IVR_NUMBER);
        String handleSipHeader = ChannelHandler.handleSipHeader(ngdEvent, channelEvent);
        return XCCUtil.bridge(nc, channelEvent, retValue, dialStr, handleSipHeader, callNumber);
    }
}
