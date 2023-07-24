package com.haiyisoft.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.enumerate.EnumXCC;
import com.haiyisoft.handler.NGDHandler;
import com.haiyisoft.model.NGDNodeMetaData;
import com.haiyisoft.model.NgdNodeDialog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 百度智能知识库
 * Created by Chr.yl on 2023/2/15.
 *
 * @author Chr.yl
 */
@Slf4j
public class NGDUtil {

    /**
     * 广西知识库接口
     * queryText 若为空时暂时不做判断,依赖百度NGD BOT配置/处理;若有需求再修改方法逻辑;
     *
     * @param queryText
     * @param sessionId    caller id
     * @param phone        caller number
     * @param icdCallerId  icd caller id
     * @param phoneAdsCode phone address code
     * @return NGDEvent
     */
    public static NGDEvent coreQuery(String queryText, String sessionId, String phone, String icdCallerId, String phoneAdsCode) {
        //package
        JSONObject param = coreQueryStruct(queryText, sessionId, phone, icdCallerId, phoneAdsCode);
        log.info("开始调用,百度知识库接口入参:{}", JSON.toJSONString(param, JSONWriter.Feature.PrettyFormat));
        //invoke
        String jsonStrResult = HttpClientUtil.doPostJsonForGxNgd(IVRInit.CHRYL_CONFIG_PROPERTY.getNgdCoreQueryUrl(), param.toJSONString());
        //res
        JSONObject parse = JSON.parseObject(jsonStrResult);
        log.info("结束调用,百度知识库接口返回: {}", parse);

        Integer code = parse.getIntValue("code");//统一返回
        String msg = parse.getString("msg");//统一返回

        NGDEvent ngdEvent;
        String answer = "";
        if (XCCConstants.OK == code) {
            JSONObject jsonData = parse.getJSONObject("data");
            //答复来源
            String source = jsonData.getString("source");
            //是否解决
            boolean solved = jsonData.getBooleanValue("solved");
            //处理answer
            answer = convertAnswer(jsonData, IVRInit.CHRYL_CONFIG_PROPERTY.isConvertSolved());
            ngdEvent = NGDHandler.ngdEventSetVar(sessionId, code, msg, answer, source, solved);
            log.info("百度知识库返回正常 code: {} , msg: {} , answer: {}", code, msg, answer);
            //保存流程信息
            NGDNodeMetaData ngdNodeMetaData = saveNgdNodeMateData(queryText, answer, jsonData);
            ngdEvent.setNgdNodeMetaData(ngdNodeMetaData);
            log.info("本次节点信息为:{}", ngdNodeMetaData);
        } else {
            log.error("百度知识调用异常 code: {} , msg: {}", code, msg);
            answer = XCCConstants.XCC_MISSING_TEXT;
            ngdEvent = NGDHandler.ngdEventSetErrorVar(sessionId, code, msg, answer);
        }

        JSONObject resContext = parse.getJSONObject("data").getJSONObject("context");//context
        //处理用户校验是否完成
        NGDEvent resNgdEvent = checkUser(resContext, ngdEvent);

        log.info("coreQueryNGD ngdEvent: {}", resNgdEvent);
        return resNgdEvent;
    }

    /**
     * 广西知识库接口
     * queryText 若为空时暂时不做判断,依赖百度NGD BOT配置/处理;若有需求再修改方法逻辑;
     *
     * @param queryText
     * @param sessionId    caller id
     * @param phone        caller number
     * @param icdCallerId  icd caller id
     * @param phoneAdsCode phone address code
     * @return JSONObject
     */
    public static JSONObject coreQueryJson(String queryText, String sessionId, String phone, String icdCallerId, String phoneAdsCode) {
        //package
        JSONObject param = coreQueryStruct(queryText, sessionId, phone, icdCallerId, phoneAdsCode);
        log.info("开始调用,百度知识库接口入参:{}", JSON.toJSONString(param, JSONWriter.Feature.PrettyFormat));
        //invoke
        String jsonStrResult = HttpClientUtil.doPostJsonForGxNgd(IVRInit.CHRYL_CONFIG_PROPERTY.getNgdCoreQueryUrl(), param.toJSONString());
        //res
        JSONObject parse = JSON.parseObject(jsonStrResult);
        log.info("结束调用,百度知识库接口返回: {}", parse);
        return parse;
    }

    /**
     * 保存用户意图
     *
     * @param context  context全局交互实体
     * @param ngdEvent
     * @return
     */
    public static NGDEvent handlerIntent(JSONObject context, NGDEvent ngdEvent) {
        String ytStr = context.getString(XCCConstants.IVR_YTDX);
        ngdEvent.setIntent(ytStr);
        return ngdEvent;
    }

    /**
     * TODO 未做异常处理
     * 处理ngd节点数据
     *
     * @param query
     * @param answer
     * @param result
     * @param ngdEvent
     * @return
     */
    public static NGDEvent convertNgdNodeMateData(String query, String answer, JSONObject result, NGDEvent ngdEvent) {
        JSONObject jsonData = result.getJSONObject("data");
        //保存流程信息
        NGDNodeMetaData ngdNodeMetaData = saveNgdNodeMateData(query, answer, jsonData);
        ngdEvent.setNgdNodeMetaData(ngdNodeMetaData);
        log.info("本次节点信息为:{}", ngdNodeMetaData);
        return ngdEvent;
    }

    /**
     * 根据百度知识库返回的数据取到合理的回复
     * 根据source和solved
     *
     * @param jsonData
     * @param convertSolved 是否手动处理未解决回复(system/none...)
     * @return
     */
    public static String convertAnswer(JSONObject jsonData, boolean convertSolved) {
        String answer = "";
        //根据 source 判断
        String source = jsonData.getString("source");
        //是否解决
        boolean solved = jsonData.getBooleanValue("solved");
        log.info("百度知识库命中 solved : {} , source : {}", solved, source);
        if (solved) {
            if (XCCConstants.SOURCE_TASK_BASED.equals(source)) {//task_based
                answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
            } else if (XCCConstants.SOURCE_FAQ.equals(source)) {//faq
                answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
            } else if (XCCConstants.CHITCHAT.equals(source)) {//chitchat
                answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
            } else if (XCCConstants.SOURCE_CLARIFY.equals(source)) {//clarify
                answer = jsonData.getJSONObject("clarifyQuestions")
                        .getJSONObject("voice")
                        .getJSONArray("questions")
                        .getString(0);
            } else {//此处自定义,待发现新类型继续补充
                answer = XCCConstants.XCC_MISSING_TEXT;
            }
        } else {
            if (convertSolved) {//处理,使用自定义话术
                answer = randomMissingMsg();
            } else {//不处理
                //当前测试source=system/none时,solved为false
                if (XCCConstants.SOURCE_SYSTEM.equals(source)) {//system
                    answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
                } else if (XCCConstants.SOURCE_NONE.equals(source)) {//none
                    answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
                } else {//此处自定义,待发现新类型继续补充
                    answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
                }
            }
        }
        log.info("百度知识库命中 answer: {}", answer);
        return answer;
    }

    /**
     * 无是否处理
     * 根据百度知识库返回的数据取到合理的回复
     * 根据source取answer
     *
     * @param jsonData
     * @return
     */
    public static String convertAnswer(JSONObject jsonData) {
        String answer = "";
        //根据 source 判断
        String source = jsonData.getString("source");
        if (XCCConstants.SOURCE_TASK_BASED.equals(source)) {//task_based
            answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
        } else if (XCCConstants.SOURCE_FAQ.equals(source)) {//faq
            answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
        } else if (XCCConstants.CHITCHAT.equals(source)) {//chitchat
            answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
        } else if (XCCConstants.SOURCE_CLARIFY.equals(source)) {//clarify
            answer = jsonData.getJSONObject("clarifyQuestions")
                    .getJSONObject("voice")
                    .getJSONArray("questions")
                    .getString(0);
        } else if (XCCConstants.SOURCE_SYSTEM.equals(source)) {//system
            answer = XCCConstants.XCC_MISSING_TEXT;
        } else if (XCCConstants.SOURCE_NONE.equals(source)) {//none
            answer = jsonData.getString(XCCConstants.SUGGEST_ANSWER);
        } else {
            answer = XCCConstants.XCC_MISSING_TEXT;
        }
        return answer;
    }

    private static ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    /**
     * 获取随机话术
     * 应用在none/system
     *
     * @return
     */
    public static String randomMissingMsg() {
        String MissingMsg;
        if (threadLocalRandom.nextBoolean()) {
            MissingMsg = XCCConstants.NGD_FIRST_UNDERSTAND_TEXT;
        } else {
            MissingMsg = XCCConstants.NGD_SECOND_UNDERSTAND_TEXT;
        }
        return MissingMsg;
    }

    /**
     * 获取ngd节点流程
     *
     * @param query    问
     * @param answer   答
     * @param jsonData ngd数据
     * @return
     */
    public static NGDNodeMetaData saveNgdNodeMateData(String query, String answer, JSONObject jsonData) {
        //答复来源
        String source = jsonData.getString("source");
        //是否解决
        boolean solved = jsonData.getBooleanValue("solved");
        //问答时间(百度返回时间带有毫秒,营销不需要毫秒,遂去掉- -)
        String queryTime = DateUtil.parseLocalDateTime(jsonData.getString("queryTime"));
        String answerTime = DateUtil.parseLocalDateTime(jsonData.getString("answerTime"));
        NGDNodeMetaData ngdNodeMetaData = new NGDNodeMetaData();

        if (XCCConstants.SOURCE_TASK_BASED.equals(source)) {//task_based:只有流程有dialog
            JSONObject answerDataJSONObject = jsonData.getJSONObject("answer");
            //记录ngd流程
            JSONArray dialogsArray = answerDataJSONObject.getJSONArray("dialogs");
            if (dialogsArray != null) {
                dialogsArray.forEach(dialog -> {
                    JSONObject jsonObject = (JSONObject) JSON.toJSON(dialog);
                    String processName = jsonObject.getString("processName");
                    String dialogNodeName = jsonObject.getString("dialogNodeName");
                    JSONObject values = jsonObject.getJSONObject("values");
                    NgdNodeDialog ngdNodeDialog = new NgdNodeDialog(processName, dialogNodeName, values);
                    ngdNodeMetaData.getDialogArray().add(ngdNodeDialog);
                });
            } else {
                //no dialogs 未处理 TODO
            }
        } else {
            //非流程未处理 TODO

        }

        ngdNodeMetaData.setQuery(query);
        ngdNodeMetaData.setQueryTime(queryTime);
        ngdNodeMetaData.setAnswer(answer);
        ngdNodeMetaData.setAnswerTime(answerTime);
        ngdNodeMetaData.setSource(source);
        ngdNodeMetaData.setSolved(solved);
        return ngdNodeMetaData;
    }

    /**
     * 将百度返回的文本字段处理为 指令和播报的内容
     * 若无指令默认使用 YYSR
     *
     * @param ngdEvent
     * @return
     */
    public static NGDEvent convertText(NGDEvent ngdEvent) {
        String retKey;//指令
        String retValue;//播报内容
        String todoText = ngdEvent.getAnswer();
        log.info("convertText todoText: {}", todoText);
        if (StringUtils.isBlank(todoText)) {//话术为空
            retKey = XCCConstants.YYSR;
            retValue = XCCConstants.XCC_MISSING_ANSWER_TEXT;
        } else {
            if (!todoText.contains(XCCConstants.NGD_SEPARATOR)) {//不带#的话术
                retKey = XCCConstants.YYSR;
                retValue = todoText;
            } else {//带#的话术
                String[] split = todoText.split(XCCConstants.NGD_SEPARATOR);
                retKey = split[0];//指令
                if (StringUtils.containsAnyIgnoreCase(retKey, XCCConstants.RET_KEY_STR_ARRAY)) {//有指令
                    retValue = split[1];//内容
                } else {//无指令
                    retKey = XCCConstants.YYSR;
                    retValue = XCCConstants.XCC_MISSING_ANSWER_TEXT;
                }
            }
        }
        //转大写
        ngdEvent.setRetKey(retKey.toUpperCase());
        ngdEvent.setRetValue(retValue);
        log.info("convertText retKey: {} , retValue: {}", retKey, retValue);
        return ngdEvent;
    }

    /**
     * 测试ngd接口
     *
     * @param queryText
     * @return
     */
    public static String testNGD(String queryText, String sessionId) {
        /*
        curl --location --request POST 'http://10.100.104.20:8304/api/v2/core/query' \
        --header 'Authorization: NGD b99612ed-8935-4215-98e2-dd96f05244b3' \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "queryText": "查电费",
            "context": {
                "channel": "智能IVR"
            }
        }'

        */
//        String ch = "1";
//        String params = "{\n" +
//                "  \"sessionId\" : \"" + sessionId + "\",\n" +
//                "  \"channel\" : \"" + channel + "\",\n" +
//                "  \"queryText\" : \"" + queryText + "\",\n" +
//                "  \"context\" : {\"channel\":\"" + ch + "\"},\n" +
//                "  \"ext\" : {\"exact\":true}\n" +
//                "}";

        JSONObject param = new JSONObject();
        JSONObject context = new JSONObject();
        JSONObject ext = new JSONObject();
        context.put("channel", XCCConstants.CHANNEL_IVR);
        param.put("queryText", queryText);//客户问题
        param.put("sessionId", sessionId);//会话id
        ext.put("exact", "true");
        param.put("ext", ext);//ext
        param.put("context", context);//渠道标识，智能IVR为广西智能ivr标识
        log.info("开始调用百度知识库接口");
        return HttpClientUtil.doPostJsonForGxNgd(IVRInit.CHRYL_CONFIG_PROPERTY.getNgdCoreQueryUrl(), param.toJSONString());
    }

    /**
     * 处理数字为中文汉字
     *
     * @param queryText
     * @return
     */
    public static String convertNumber2Ch(String queryText) {
        if ("0".equals(queryText)) {
            queryText = "零";
        } else if ("1".equals(queryText)) {
            queryText = "一";
        } else if ("2".equals(queryText)) {
            queryText = "二";
        } else if ("3".equals(queryText)) {
            queryText = "三";
        } else if ("4".equals(queryText)) {
            queryText = "四";
        } else if ("5".equals(queryText)) {
            queryText = "五";
        } else if ("6".equals(queryText)) {
            queryText = "六";
        } else if ("7".equals(queryText)) {
            queryText = "七";
        } else if ("8".equals(queryText)) {
            queryText = "八";
        } else if ("9".equals(queryText)) {
            queryText = "九";
        }
        return queryText;
    }

    /**
     * 组装core query
     *
     * @param queryText
     * @param sessionId
     * @param phone
     * @param icdCallerId  icd caller id
     * @param phoneAdsCode phone address code
     * @return
     */
    public static JSONObject coreQueryStruct(String queryText, String sessionId, String phone, String icdCallerId, String phoneAdsCode) {
        JSONObject param = new JSONObject();
        JSONObject context = new JSONObject();
        JSONObject ext = new JSONObject();
        param.put("channel", XCCConstants.CHANNEL_IVR);//渠道标识
        context.put(XCCConstants.IVR_PHONE, phone);
        context.put(XCCConstants.IVR_PHONE_ADS_CODE, phoneAdsCode);//后缀码
        context.put(XCCConstants.IVR_ICD_CALLER_ID, icdCallerId);//icd
        context.put(XCCConstants.IVR_FS_CALLER_ID, sessionId);//fs caller id = xcc channel id = session id
        param.put("queryText", queryText);//客户问题
        param.put("sessionId", sessionId);//会话id
        ext.put("exact", "true");
        param.put("ext", ext);//ext
        param.put("context", context);
        log.info("coreQueryStruct param : {}", param);
        return param;
    }

    /**
     * 身份校验配套流程,判断用户校验是否完成
     * userOK is true :身份校验已通过,用户编号已确定
     * userOK is false :身份校验未通过,用户编号未确定
     *
     * @param context
     * @param ngdEvent
     * @return
     */
    public static NGDEvent checkUser(JSONObject context, NGDEvent ngdEvent) {
        if (context != null) {
            //判断用户校验是否完成
            String userOK = context.getString(EnumXCC.USER_OK.getProperty());
            if (EnumXCC.USER_OK.getValue().equals(userOK)) {
                //已通过
                String uid = context.getString(XCCConstants.IVR_YHBH);
                ngdEvent.setUserOk(true);
                ngdEvent.setUid(uid);
            } else {
                //未通过
                ngdEvent.setUserOk(false);
                ngdEvent.setUid("");
            }
        } else {
            ngdEvent.setUserOk(false);
            ngdEvent.setUid("");
        }
        return ngdEvent;
    }

}
