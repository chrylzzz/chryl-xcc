package com.haiyisoft.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.util.DateUtil;
import com.haiyisoft.util.HttpClientUtil;
import com.haiyisoft.util.IdGenerator;
import com.haiyisoft.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Webhook业务处理
 * Created by Chr.yl on 2023/6/30.
 *
 * @author Chr.yl
 */
@Slf4j
public class WebHookHandler {

    /**
     * 发送短信
     *
     * @param ivrEvent
     * @param dxnr     短信内容
     */
    public static void sendMessage(IVREvent ivrEvent, String dxnr) {
        //来电号码
        String cidPhoneNumber = ivrEvent.getCidPhoneNumber();
        JSONObject context = new JSONObject();
        context.put("jssjh", cidPhoneNumber);
        context.put("dxnr", dxnr);
        JSONObject params = convertWebHookReqBody(XCCConstants.SEND_MESSAGE, context);
        log.info("sendMessage,WebHook接口入参:{}", JSON.toJSONString(params, JSONWriter.Feature.PrettyFormat));
        String resData = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getWebHookUrl(), params.toJSONString());
        log.info("sendMessage,WebHook接口出参:{}", resData);
    }

    /**
     * 会话记录
     * Save Call Detail Recording
     *
     * @param ivrEvent
     */
    public static void saveCDR(IVREvent ivrEvent) {
        String channelId = ivrEvent.getChannelId();
        JSONArray metadataArray = ivrEvent.getNgdNodeMetadataArray();
        log.info("cdr array :{}", metadataArray);
        //营销接口会话记录入参
        String cdr = "";
        if (metadataArray != null && metadataArray.size() != 0) {
//                metadataArray.forEach(metadata -> {
//                    JSONObject jsonObject = (JSONObject) JSON.toJSON(metadata);
//                    String query = jsonObject.getString("query");
//                    String queryTime = jsonObject.getString("queryTime");
//                    String answer = jsonObject.getString("answer");
//                    String answerTime = jsonObject.getString("answerTime");
//                    cdr = cdr + (XCCConstants.B + queryTime + query + XCCConstants.H + answerTime + answer);
//                });
            /**
             * 前导流程在ngd
             */
            cdr = convertCDRFromNGD(metadataArray);

            /**
             * 前导流程在fs
             */
//            cdr = convertCDRFromIVR(metadataArray);
        }

        //标准格式:[#B:2018-11-20 20:00:00欢迎致电95598.#H:2018-11-20 20:00:00你好我要查电费。#B:2018-11-20 20:00:00请A请按键输入您的用户编号。]
        log.info("[{}]================ CDR:[{}] ", channelId, cdr);

        //来电号码
        String cidPhoneNumber = ivrEvent.getCidPhoneNumber();
        //华为会话标识
        String icdCallerId = ivrEvent.getIcdCallerId();
        //号码归属地
        String phoneAdsCode = ivrEvent.getPhoneAdsCode();
        JSONObject context = new JSONObject();
        context.put("callid", icdCallerId);
        context.put("ldhm", cidPhoneNumber);
        context.put("hhjl", cdr);
        //这里送后缀码
        String areaCode = NumberUtil.convertPhoneAdsCode2AreaCode(phoneAdsCode);
        context.put("dqbm", areaCode);
        context.put("gddwbm", areaCode);
        JSONObject params = convertWebHookReqBody(XCCConstants.I_HJZX_BCDHNR, context);
        log.info("I_HJZX_BCDHNR,WebHook接口入参:{}", JSON.toJSONString(params, JSONWriter.Feature.PrettyFormat));
        String resData = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getWebHookUrl(), params.toJSONString());
        log.info("I_HJZX_BCDHNR,WebHook接口出参:{}", resData);
    }

    /**
     * WebHook请求体
     *
     * @param action
     * @param context
     * @return
     */
    public static JSONObject convertWebHookReqBody(String action, JSONObject context) {
        JSONObject params = new JSONObject();
        params.put("action", action);
        params.put("context", context);
        return params;
    }

    /**
     * 处理会话记录
     * 前导流程在ngd
     */
    public static String convertCDRFromNGD(JSONArray metadataArray) {
        JSONObject welcomeJsonData = metadataArray.getJSONObject(0);
        String welcomeStr = welcomeJsonData.getString("answer");
        String welcomeTime = welcomeJsonData.getString("answerTime");
        String cdr = XCCConstants.B + welcomeTime + welcomeStr;
        for (int i = 1; i < metadataArray.size(); i++) {
            JSONObject jsonObject = metadataArray.getJSONObject(i);
            String query = jsonObject.getString("query");
            String queryTime = jsonObject.getString("queryTime");
            String answer = jsonObject.getString("answer");
            String answerTime = jsonObject.getString("answerTime");
//                cdr = cdr + (XCCConstants.B + queryTime + query + XCCConstants.H + answerTime + answer);
            cdr = cdr + (XCCConstants.H + queryTime + query + XCCConstants.B + answerTime + answer);
        }
        return cdr;
    }

    /**
     * 处理会话记录
     * 前导流程在fs
     *
     * @param metadataArray
     * @return
     */
    public static String convertCDRFromIVR(JSONArray metadataArray) {
        //欢迎语处理格式
        String cdr = XCCConstants.B + DateUtil.getLocalDateTime() + XCCConstants.TEMP_WELCOME_TEXT;

        for (int i = 0; i < metadataArray.size(); i++) {
            JSONObject jsonObject = metadataArray.getJSONObject(i);
            String query = jsonObject.getString("query");
            String queryTime = jsonObject.getString("queryTime");
            String answer = jsonObject.getString("answer");
            String answerTime = jsonObject.getString("answerTime");
//                cdr = cdr + (XCCConstants.B + queryTime + query + XCCConstants.H + answerTime + answer);
            cdr = cdr + (XCCConstants.H + queryTime + query + XCCConstants.B + answerTime + answer);
        }
        return cdr;
    }

    /**
     * 本次通话结束打点
     *
     * @param ivrEvent
     */
    public static void ivrEndPoint(IVREvent ivrEvent) {
        //通话开始时间
        String startTime = ivrEvent.getStartTime();
        //fs id
        String channelId = ivrEvent.getChannelId();
        //来电号码
        String cidPhoneNumber = ivrEvent.getCidPhoneNumber();
        //华为会话标识
        String icdCallerId = ivrEvent.getIcdCallerId();
        //号码归属地
        String phoneAdsCode = ivrEvent.getPhoneAdsCode();

        JSONObject context = new JSONObject();
        context.put("ivr_node_start_time", startTime);
        context.put("ivr_fs_caller_id", channelId);
        context.put("ivr_icd_caller_id", icdCallerId);
        context.put("ivr_phone", cidPhoneNumber);
        context.put("ivr_phone_ads_code", phoneAdsCode);

        JSONObject params = convertWebHookReqBody(XCCConstants.IVR_END_POINT, context);
        log.info("IVR_END_POINT,WebHook接口入参:{}", JSON.toJSONString(params, JSONWriter.Feature.PrettyFormat));
        String resData = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getWebHookUrl(), params.toJSONString());
        log.info("IVR_END_POINT,WebHook接口出参:{}", resData);
    }

}
