package com.haiyisoft.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.haiyisoft.advice.IVRExceptionAdvice;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.XCCEvent;
import com.haiyisoft.handler.XCCHandler;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Future;

/**
 * Created By Chr.yl on 2023-02-08.
 *
 * @author Chr.yl
 */
@Slf4j
public class RequestUtil {


    /**
     * 构造JSON-RPC对象
     *
     * @param method 方法名
     * @return JSONObject
     */
    public static JSONObject getJsonRpc(String method, JSONObject params) {
        /*JSON-RPC 2.0格式定义
        {
            "jsonrpc": "2.0",
            "id": "0",
            "method": "XNode.NativeApp",
            "params": {
                "ctrl_uuid": "ctrl_uuid",
                "uuid": "channel_uuid",
                "cmd": "playback",
                "args": "/tmp/welcome.wav"
            }
        }*/
        JSONObject jsonRpc = new JSONObject();
        //JSON-RPC 2.0版本
        jsonRpc.put("jsonrpc", "2.0");
        //JSON-RPC id,每个请求一个，保证唯一. XSwitch要求该id必须是一个字符串类型
        jsonRpc.put("id", IdGenerator.fastSimpleUUID());
        jsonRpc.put("method", method);
        jsonRpc.put("params", params);
        return jsonRpc;
    }

    /**
     * 无数据返回
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @param timeout 超时时间
     * @return
     */
    public static void natsRequestTimeOut(Connection con, String service, String method, JSONObject params, Duration timeout) {
        log.info("{} 执行开始", method);
        JSONObject jsonRpc = getJsonRpc(method, params);
        byte[] bytes = jsonRpc.toString().getBytes(StandardCharsets.UTF_8);
        log.info("{} 请求信息 service:[{}], Serializer json:{}", method, service, JSON.toJSONString(jsonRpc, JSONWriter.Feature.PrettyFormat));
        try {
            Message msg = con.request(service, bytes, timeout);
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            log.info("{} 返回信息:{}", method, response);
            JSONObject result = JSONObject.parseObject(response).getJSONObject("result");
            Integer code = result.getInteger("code");//统一返回
            String message = result.getString("message");//统一返回
            String type = "";//type=ERROR时才有
            String error = "";//type=ERROR时才有
            String cause = result.getString("cause");
            log.error("XSwitch Invoke method : [{}]  code : [{}] , message : [{}] , type : [{}] , error : [{}] , cause : [{}]",
                    method, code, message, type, error, cause);
        } catch (Exception e) {
            log.error("xcc handleException 发生异常：{} , {}", method, e);
        }
        log.info("{} 执行结束", method);
    }

    /**
     * 无数据返回
     * nats 请求超时时间默认十分钟
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @return
     */
    public static void natsRequest(Connection con, String service, String method, JSONObject params) {
        log.info("{} 执行开始", method);
        JSONObject jsonRpc = getJsonRpc(method, params);
        byte[] bytes = jsonRpc.toString().getBytes(StandardCharsets.UTF_8);
        log.info("{} 请求信息 service:[{}], Serializer json:{}", method, service, JSON.toJSONString(jsonRpc, JSONWriter.Feature.PrettyFormat));
        try {
            //默认请求超时时间10分钟 NatsRequestCompletableFuture
            Future<Message> incoming = con.request(service, bytes);
            Message msg = incoming.get();
//            Message msg = incoming.get(1L, TimeUnit.MILLISECONDS);
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            log.info("{} 返回信息:{}", method, response);
            JSONObject result = JSONObject.parseObject(response).getJSONObject("result");
            Integer code = result.getInteger("code");//统一返回
            String message = result.getString("message");//统一返回
            String type = "";//type=ERROR时才有
            String error = "";//type=ERROR时才有
            String cause = result.getString("cause");
            log.error("XSwitch Invoke method : [{}]  code : [{}] , message : [{}] , type : [{}] , error : [{}] , cause : [{}]",
                    method, code, message, type, error, cause);
        } catch (Exception e) {
            log.error("xcc handleException 发生异常：{} , {}", method, e);
        }
        log.info("{} 执行结束", method);
    }

    /**
     * Hangup
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @return
     */
    public static XCCEvent natsRequestFutureByHangup(Connection con, String service, String method, JSONObject params) {
        return natsRequestFutureByAnswer(con, service, method, params);
    }


    /**
     * PlayTTS
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @return
     */
    public static XCCEvent natsRequestFutureByPlayTTS(Connection con, String service, String method, JSONObject params) {
        return natsRequestFutureByAnswer(con, service, method, params);
    }

    /**
     * Answer
     * nats 请求超时时间默认十分钟
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @return
     */
    public static XCCEvent natsRequestFutureByAnswer(Connection con, String service, String method, JSONObject params) {
        log.info("{} 执行开始", method);
        JSONObject jsonRpc = getJsonRpc(method, params);
        byte[] bytes = jsonRpc.toString().getBytes(StandardCharsets.UTF_8);
        log.info("{} 请求信息 service:[{}], Serializer json:{}", method, service, JSON.toJSONString(jsonRpc, JSONWriter.Feature.PrettyFormat));
        XCCEvent xccEvent;
        try {
            Future<Message> incoming = con.request(service, bytes);
            Message msg = incoming.get();
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            log.info("{} 返回信息:{}", method, response);
            JSONObject result = JSONObject.parseObject(response).getJSONObject("result");
            Integer code = result.getInteger("code");//统一返回
            String message = result.getString("message");//统一返回
            String type = "";//type=ERROR时才有
            String error = "";//type=ERROR时才有
            String cause = result.getString("cause");
            xccEvent = XCCHandler.xccEventSetVar(code, message, type, error, method, cause);
            log.info("{} xccEvent: {}", method, xccEvent);
        } catch (Exception e) {
            log.error("xcc handleException 发生异常：{} , {}", method, e);
            xccEvent = IVRExceptionAdvice.handleException(method, e);
        }
        log.info("{} 执行结束", method);
        return xccEvent;
    }

    /**
     * DetectSpeech
     *
     * @param con          connection
     * @param service      node uuid
     * @param method       xcc method
     * @param params       rpc-json params
     * @param milliSeconds 毫秒
     * @return
     */
    public static XCCEvent natsRequestFutureByDetectSpeech(Connection con, String service, String method, JSONObject params, Long milliSeconds) {
        log.info("{} 执行开始", method);
        JSONObject jsonRpc = getJsonRpc(method, params);
        byte[] bytes = jsonRpc.toString().getBytes(StandardCharsets.UTF_8);
        log.info("{} 请求信息 service:[{}], Serializer json:{}", method, service, JSON.toJSONString(jsonRpc, JSONWriter.Feature.PrettyFormat));
        XCCEvent xccEvent;
        try {
            Future<Message> incoming = con.request(service, bytes);
            Message msg = incoming.get();
//            Message msg = incoming.get(milliSeconds, TimeUnit.MILLISECONDS);
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            log.info("{} 返回信息:{}", method, response);
            JSONObject result = JSONObject.parseObject(response).getJSONObject("result");
            Integer code = result.getInteger("code");//统一返回
            String message = result.getString("message");//统一返回
            String type = "";//type=ERROR时才有
            String error = "";//type=ERROR时才有
            String cause = result.getString("cause");
            //识别返回数据,调用失败默认为""
            String utterance = "";
            JSONObject jsonData = result.getJSONObject("data");
            if (jsonData == null) {

            } else {
                if (XCCConstants.OK == code) {//200
                    if (IVRInit.CHRYL_CONFIG_PROPERTY.isHandleEngineData()) {//手动解析
                        String xmlStr = jsonData.getString("engine_data");
                        utterance = Dom4jUtil.parseAsrResXml(xmlStr);
                        if (StringUtils.isBlank(utterance)) {
                            //未识别话术,参考深度解析
                            type = XCCConstants.RECOGNITION_TYPE_ERROR;
                            error = XCCConstants.RECOGNITION_ERROR_NO_INPUT;
                        }
                    } else {//已深度解析
                        utterance = jsonData.getString("text");
                        type = jsonData.getString("type");
                        error = jsonData.getString("error");
                    }
                } else {//其他编码, 这里不作处理
                    type = jsonData.getString("type");
                    error = jsonData.getString("error");
                }
            }
            xccEvent = XCCHandler.xccEventSetVar(code, message, utterance, type, error, method, cause);
            log.info("{} 识别返回数据 utterance: {}", method, utterance);
            log.info("{} xccEvent: {}", method, xccEvent);
        } catch (Exception e) {
            log.error("xcc handleException 发生异常：{} , {}", method, e);
            xccEvent = IVRExceptionAdvice.handleException(method, e);
        }
        log.info("{} 执行结束", method);
        return xccEvent;
    }


    /**
     * ReadDTMF
     *
     * @param con          connection
     * @param service      node uuid
     * @param method       xcc method
     * @param params       rpc-json params
     * @param milliSeconds 毫秒
     * @return
     */

    public static XCCEvent natsRequestFutureByReadDTMF(Connection con, String service, String method, JSONObject params, Long milliSeconds) {
        log.info("{} 执行开始时间为", method);
        JSONObject jsonRpc = getJsonRpc(method, params);
        byte[] bytes = jsonRpc.toString().getBytes(StandardCharsets.UTF_8);
        log.info("{} 请求信息 service:[{}], Serializer json:{}", method, service, JSON.toJSONString(jsonRpc, JSONWriter.Feature.PrettyFormat));
        XCCEvent xccEvent;
        try {
            Future<Message> incoming = con.request(service, bytes);
            Message msg = incoming.get();
//            Message msg = incoming.get(milliSeconds, TimeUnit.MILLISECONDS);
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            log.info("{} 返回信息:{}", method, response);
            JSONObject result = JSONObject.parseObject(response).getJSONObject("result");
            Integer code = result.getInteger("code");//统一返回
            String message = result.getString("message");//统一返回
            String type = "";//type=ERROR时才有
            String error = "";//type=ERROR时才有
            String cause = result.getString("cause");
            //识别返回数据,调用失败默认为""
            String dtmf = "";
            if (code == XCCConstants.OK) {
                //收到的按键
                dtmf = result.getString("dtmf");
            } else if (code == XCCConstants.JSONRPC_NOTIFY) {
                //收到的按键
                dtmf = result.getString("dtmf");
                if (StringUtils.isBlank(dtmf)) {//未识别话术,参考深度解析返回event
                    type = XCCConstants.RECOGNITION_TYPE_ERROR;
                    error = XCCConstants.RECOGNITION_ERROR_NO_INPUT;
                }
            } else {//其他编码, 这里不作处理

            }
            xccEvent = XCCHandler.xccEventSetVar(code, message, dtmf, type, error, method, cause);
            log.info("{} 识别返回数据 dtmf: {}", method, dtmf);
            log.info("{} xccEvent: {}", method, xccEvent);
        } catch (Exception e) {
            log.error("xcc handleException 发生异常：{} , {}", method, e);
            xccEvent = IVRExceptionAdvice.handleException(method, e);
        }
        log.info("{} 执行结束", method);

        return xccEvent;
    }

    /**
     * Bridge
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @param timeout 超时时间
     * @return
     */
    public static XCCEvent natsRequestFutureByBridge(Connection con, String service, String method, JSONObject params, Duration timeout) {
        log.info("{} 执行开始时间为", method);
        JSONObject jsonRpc = getJsonRpc(method, params);
        byte[] bytes = jsonRpc.toString().getBytes(StandardCharsets.UTF_8);
        log.info("{} 请求信息 service:[{}], Serializer json:{}", method, service, JSON.toJSONString(jsonRpc, JSONWriter.Feature.PrettyFormat));
        XCCEvent xccEvent;
        try {
            //默认超时时间10分钟 NatsRequestCompletableFuture
//            Future<Message> incoming = con.request(service, bytes);
//            Message msg = incoming.get();
//            Message msg = incoming.get(milliSeconds, TimeUnit.MILLISECONDS);
//            Message msg = incoming.get(1L, TimeUnit.HOURS);

            Message msg = con.request(service, bytes, timeout);
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            log.info("{} 返回信息:{}", method, response);
            JSONObject result = JSONObject.parseObject(response).getJSONObject("result");
            Integer code = result.getInteger("code");//统一返回
            String message = result.getString("message");//统一返回
            String type = "";//type=ERROR时才有
            String error = "";//type=ERROR时才有
            String cause = result.getString("cause");
            xccEvent = XCCHandler.xccEventSetVar(code, message, type, error, method, cause);
            log.info("{} xccEvent: {}", method, xccEvent);
        } catch (Exception e) {
            log.error("xcc handleException 发生异常：{} , {}", method, e);
            xccEvent = IVRExceptionAdvice.handleException(method, e);
        }
        log.info("{} 执行结束", method);

        return xccEvent;
    }

    /**
     * Log
     *
     * @param con     connection
     * @param service node uuid
     * @param method  xcc method
     * @param params  rpc-json params
     * @return
     */
    public static void natsRequestFutureByLog(Connection con, String service, String method, JSONObject params) {
        natsRequest(con, service, method, params);
    }
}
