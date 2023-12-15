package com.haiyisoft.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.enumerate.EnumXCC;
import com.haiyisoft.model.IVRModel;
import com.haiyisoft.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 营销系统业务处理
 * Created by Chr.yl on 2023/7/11.
 *
 * @author Chr.yl
 */
@Slf4j
public class PMSHandler {

    /**
     * 保存来话意图信息
     *
     * @param ivrEvent
     * @param ngdEvent
     */
    public static void saveIntent(IVREvent ivrEvent, NGDEvent ngdEvent) {
        String ivrStartTime = ivrEvent.getIvrStartTime();
        String cidPhoneNumber = ivrEvent.getCidPhoneNumber();
        String fsCallerId = ivrEvent.getChannelId();
        String icdCallerId = ivrEvent.getIcdCallerId();
        String intent = ngdEvent.getIntent();
        if (StringUtils.isBlank(intent)) {
            intent = EnumXCC.IVR_INTENT_QT.getValue();
        }

        IVRModel ivrModel = new IVRModel(cidPhoneNumber, fsCallerId, icdCallerId, ivrStartTime, intent, "", "", "");
        String jsonParam = JSON.toJSONString(ivrModel);
        log.info("SAVE_INTENT, pms接口入参:{}", jsonParam);
        String postJson = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + XCCConstants.SAVE_INTENT_URL, jsonParam);
        log.info("SAVE_INTENT, pms接口出参:{}", postJson);
    }

    /**
     * 保存通话数据信息
     *
     * @param ivrEvent
     * @param ngdEvent
     */
    public static void saveCallInfo(IVREvent ivrEvent, NGDEvent ngdEvent) {
        String phoneAdsCode = ivrEvent.getPhoneAdsCode();
        String ivrStartTime = ivrEvent.getIvrStartTime();
        String cidPhoneNumber = ivrEvent.getCidPhoneNumber();
        String fsCallerId = ivrEvent.getChannelId();
        String icdCallerId = ivrEvent.getIcdCallerId();
        boolean transferFlag = ivrEvent.isTransferFlag();
        int artificialType, ivrValidCallType, ivrCallEndNormalType;//是否转人工,是否有效通话,是否正常结束: 0否1是

        if (transferFlag) {
            artificialType = EnumXCC.IVR_ARTIFICIAL_TRUE.valueParseIntValue();
        } else {
            artificialType = EnumXCC.IVR_ARTIFICIAL_FALSE.valueParseIntValue();
        }
        ivrValidCallType = EnumXCC.IVR_VALID_CALL_TRUE.valueParseIntValue();
        ivrCallEndNormalType = EnumXCC.IVR_FINISH_TRUE.valueParseIntValue();
        IVRModel ivrModel = new IVRModel(cidPhoneNumber, fsCallerId, icdCallerId, ivrStartTime, artificialType, ivrValidCallType, ivrCallEndNormalType, phoneAdsCode);
        String jsonParam = JSON.toJSONString(ivrModel);
        log.info("SAVE_CALL_DATA, pms接口入参:{}", jsonParam);
        String postJson = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + XCCConstants.SAVE_CALL_DATA_URL, jsonParam);
        log.info("SAVE_CALL_DATA, pms接口出参:{}", postJson);
    }

    /**
     * 保存满意度
     *
     * @param ivrEvent
     * @param ngdEvent
     */
    public static void saveRate(IVREvent ivrEvent, NGDEvent ngdEvent) {
        String cidPhoneNumber = ivrEvent.getCidPhoneNumber();
        String fsCallerId = ivrEvent.getChannelId();
        String icdCallerId = ivrEvent.getIcdCallerId();
        String rate = ngdEvent.getRate();
        if (StringUtils.isBlank(rate)) {
            rate = EnumXCC.IVR_RATE_NEUTRAL.getValue();
        }
        IVRModel ivrModel = new IVRModel(cidPhoneNumber, fsCallerId, icdCallerId, "", "", rate);
        String jsonParam = JSON.toJSONString(ivrModel);
        log.info("SAVE_RATE_DATA_URL, pms接口入参:{}", jsonParam);
        String postJson = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + XCCConstants.SAVE_RATE_DATA_URL, jsonParam);
        log.info("SAVE_RATE_DATA_URL, pms接口出参:{}", postJson);

    }

    /**
     * 查询欢迎语
     */
    public static String welcomeText() {
        IVRModel ivrModel = new IVRModel("19");
        String jsonParam = JSON.toJSONString(ivrModel);
        log.info("QUERY_BBHS__URL, pms接口入参:{}", jsonParam);
        String postJson = HttpClientUtil.doPostJson(IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + XCCConstants.QUERY_BBHS__URL, jsonParam);
        log.info("QUERY_BBHS__URL, pms接口出参:{}", postJson);
        JSONObject jsonObject = JSON.parseObject(postJson);
        JSONArray data = jsonObject.getJSONArray("data");
        String hsnr = "";
        if (data != null) {
            JSONObject dataJSONObject = data.getJSONObject(0);
            String hsbh = dataJSONObject.getString("hsbh");
            if ("99".equals(hsbh)) {//欢迎语话术编号为99
                hsnr = dataJSONObject.getString("hsnr");
            }
        }
        //欢迎语赋值,若失败,赋值默认
        if (StringUtils.isBlank(hsnr)) {
            hsnr = XCCConstants.DEFAULT_WELCOME_TEXT;
        }

        log.info("QUERY_BBHS__URL, welcomeText: {}", hsnr);
        return hsnr;
    }


    /******************************************** 终验接口需求 ********************************************/

    public static void main(String[] args) {
        queryGrayscale(null);
//        queryWhiteList(null);
    }

    /**
     * 查询手机号码是否为白名单用户
     *
     * @param phone
     * @return
     */
    public static Map<String, String> queryWhiteList(String phone) {
        String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + "/interface/queryBmd/QueryWhiteListForZnivr";
        JSONObject param = new JSONObject();
        param.put("LDHM", phone);
        log.info("queryWhiteList 入参: {}", param);
        String postJson = HttpClientUtil.doPostJson(url, param.toJSONString());
        log.info("queryWhiteList 出参: {}", postJson);
//        String postJson = "{\"LDHM\":\"19178273071\",\"SFBMD\":\"该用户不在白名单中\",\"resultCode\":\"1\"}";
        JSONObject jsonObject = JSONObject.parseObject(postJson);
        final String sfbmd = jsonObject.getString("SFBMD");
        Map<String, String> context = new HashMap<>();
        if ("该用户在白名单中".equals(sfbmd)) {//0在白名单 1不在白名单
            context.put("ivr_white_status", "0");
            context.put("ivr_msg", "该用户在白名单中");
        } else {
            context.put("ivr_white_status", "1");
            context.put("ivr_msg", "该用户不在白名单中");
        }
        log.info("queryWhiteList return: {}", context);
        return context;
    }

    /**
     * 根据手机号(用户类型)码查询欢迎语
     *
     * @param phone
     * @return
     */
    public static Map<String, String> queryWelMsgByUserType(String phone) {
        String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + "/interface/queryDtcdbbxx/QueryUserTypeWelcomeMessageForZnivr";

        JSONObject param = new JSONObject();
        param.put("LDHM", phone);
        log.info("queryWelMsgByUserType 入参: {}", param);
        String postJson = HttpClientUtil.doPostJson(url, param.toJSONString());
        log.info("queryWelMsgByUserType 出参: {}", postJson);
        JSONObject jsonObject = JSONObject.parseObject(postJson);
        final String hyy = jsonObject.getString("HYY");
        Map<String, String> context = new HashMap<>();
        if (StringUtils.isBlank(hyy)) {//1无欢迎语
            context.put("ivr_code", "1");
            context.put("ivr_msg", "未查询到欢迎语");
        } else {//0有欢迎语
            context.put("ivr_code", "0");
            context.put("ivr_msg", "查询成功");
            context.put("ivr_hyy", hyy);
        }
        log.info("queryWelMsgByUserType return: {}", context);
        return context;
    }

    /**
     * 根据手机号码查询是否允许接入智能IVR
     *
     * @param phone
     * @return
     */
    public static Map<String, String> queryGrayscale(String phone) {
        String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + "/interface/queryHdkz/QueryGrayscaleForZnivr";
        JSONObject param = new JSONObject();
        param.put("LDHM", phone);
        log.info("queryGrayscale 入参: {}", param);
        String postJson = HttpClientUtil.doPostJson(url, param.toJSONString());
        log.info("queryGrayscale 出参: {}", postJson);

//        String postJson="{\"LDHM\":\"19178273071\",\"code\":\"1\"}";
//        String postJson = "{\"LDHM\":\"19977188606\",\"data\":[],\"code\":\"1\"}";
//        String postJson="{\"LDHM\":\"19178273071\",\"data\":[{\"DHHM\":\"19178273071\",\"YHLB\":\"曾有最终答复意见客户来电\",\"KSSJ\":null,\"JSSJ\":null,\"SFJR\":\"是\"}],\"code\":\"1\"}";
        JSONObject jsonObject = JSONObject.parseObject(postJson);
        Map<String, String> context = new HashMap<>();
        JSONArray dataArr = jsonObject.getJSONArray("data");
        if (dataArr != null && dataArr.size() > 0) {
            JSONObject data = dataArr.getJSONObject(0);
            if (data != null) {//0在 1不在
                final String sfjr = data.getString("SFJR");
                if ("是".equals(sfjr)) {//允许接入
                    context.put("ivr_code", "0");
                    context.put("ivr_msg", "查询成功,可接入");
                    context.put("ivr_sfjr_code", "0");
                    context.put("ivr_sfjr_msg", sfjr);
                } else {//不可接入
                    context.put("ivr_code", "1");
                    context.put("ivr_msg", "未查询该用户信息");
                    context.put("ivr_sfjr_code", "1");
                    context.put("ivr_sfjr_msg", sfjr);
                }
            } else {//未查询到信息
                context.put("ivr_code", "1");
                context.put("ivr_msg", "该用户数据不存在");
                context.put("ivr_sfjr_code", "1");
                context.put("ivr_sfjr_msg", "否");
            }
        } else {//未查询到信息
            context.put("ivr_code", "1");
            context.put("ivr_msg", "数据不存在");
            context.put("ivr_sfjr_code", "1");
            context.put("ivr_sfjr_msg", "否");
        }
        log.info("queryGrayscale return: {}", context);
        return context;
    }

    /**
     * 根据手机号码、后缀码新增陌生号码
     *
     * @param phone
     * @param hzm
     * @return
     */
    public static Map<String, String> saveUnknowNumber(String phone, String hzm) {
        String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + "/interface/saveMshm/UnkonwnNumberForZnivr";
        JSONObject param = new JSONObject();
        param.put("LDHM", phone);
        param.put("HZM", hzm);
        log.info("saveUnknowNumber 入参: {}", param);
        String postJson = HttpClientUtil.doPostJson(url, param.toJSONString());
        log.info("saveUnknowNumber 出参: {}", param);
        JSONObject jsonObject = JSONObject.parseObject(postJson);
        final String msg = jsonObject.getString("msg");
        //{"LDHM":"18866660713","HZM":"95598040100","msg":"手机号已存在！！","code":"0"}
        //{"LDHM":"18866660714","HZM":"95598040100","code":"1"}
        Map<String, String> context = new HashMap<>();
        if (StringUtils.isBlank(msg)) {
            context.put("ivr_code", "0");
            context.put("ivr_msg", "添加陌生号码成功");
        } else {
            context.put("ivr_code", "1");
            context.put("ivr_msg", msg);
        }
        log.info("saveUnknowNumber return: {}", context);
        return context;
    }

    /**
     * 根据手机号码、日期查询是否来电，来电意图
     *
     * @param phone
     * @param rqxz  日期选择
     * @return
     */
    public static Map<String, String> queryPhoneCalls(String phone, String rqxz) {
        String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + "/interface/queryCfldxx/QueryPhoneCallsForZnivr";
        JSONObject param = new JSONObject();
        param.put("LDHM", phone);
        param.put("RQXZ", rqxz);
        log.info("queryPhoneCalls 入参: {}", param);
        String postJson = HttpClientUtil.doPostJson(url, param.toJSONString());
        log.info("queryPhoneCalls 出参: {}", postJson);
        JSONObject jsonObject = JSONObject.parseObject(postJson);
        final String LDCS = jsonObject.getString("LDCS");//来电次数
        final String SFLD = jsonObject.getString("SFLD");//是否来电
        final String ytdx = jsonObject.getString("ytdx");//意图,#分割
        Map<String, String> context = new HashMap<>();
        context.put("ivr_ldcs", LDCS);
        context.put("ivr_sfld", SFLD);//Y是,N否
        context.put("ivr_ytdx", ytdx);
        log.info("queryPhoneCalls return: {}", context);
        return context;
    }

    /**
     * 呼损接口-查询当日是否有转人工需求
     *
     * @param phone
     * @param rqxz
     * @return
     * @throws IOException
     */
    public static Map<String, String> queryCallLoss(String phone, String rqxz) {
        String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + "/interface/queryHsxx/QueryCallLossForZnivr";
        com.alibaba.fastjson.JSONObject param = new com.alibaba.fastjson.JSONObject();
        param.put("LDHM", phone);
        param.put("RQXZ", rqxz);
        log.info("queryCallLoss 入参: {}", param);
        String postJson = HttpClientUtil.doPostJson(url, param.toJSONString());
        log.info("queryCallLoss 出参: {}", param);
        JSONObject jsonObject = JSONObject.parseObject(postJson);
        final String SFHS = jsonObject.getString("SFHS");//是否呼损
        Map<String, String> context = new HashMap<>();
        context.put("ivr_sfhs", SFHS);//Y是,N否
        log.info("queryCallLoss return: {}", context);
        return context;
    }

    /******************************************** 终验接口需求 ********************************************/
}
