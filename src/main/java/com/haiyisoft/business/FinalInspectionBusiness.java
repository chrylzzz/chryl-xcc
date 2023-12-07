package com.haiyisoft.business;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.handler.PMSHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 广西智能IVR终验需求
 * Created By Chryl on 2023-12-07.
 */
@Slf4j
public class FinalInspectionBusiness {

    /**
     * 终验需求
     *
     * @param phone 来话号码
     * @param hzm   后缀码
     * @return
     */
    public static String[] finalDomain(String phone, String hzm) {
        String key = "";
        String val = "";
        /**查询灰度控制,是否可接入智能IVR*/
        Map<String, String> grayscaleResMap = PMSHandler.queryGrayscale(phone);
        String ivr_sfjr_code = grayscaleResMap.getOrDefault("ivr_sfjr_code", "1");
        if ("0".equals(ivr_sfjr_code)) {//可接入
            /**查询是否在白名单中*/
            Map<String, String> whiteResMap = PMSHandler.queryWhiteList(phone);
            String ivr_white_status = whiteResMap.getOrDefault("ivr_white_status", "1");
            if ("0".equals(ivr_white_status)) {//白订单中正常服务
                /**查询呼损*/
                Map<String, String> callLossResMap = PMSHandler.queryCallLoss(phone, "1");
                String ivr_sfhs = callLossResMap.getOrDefault("ivr_sfhs", "N");
                if ("N".equals(ivr_sfhs)) {//无呼损的正常服务
                    /**动态菜单播报,根据用户类型查询欢迎语*/
                    Map<String, String> welMsgResMap = PMSHandler.queryWelMsgByUserType(phone);
                    String ivr_code = welMsgResMap.getOrDefault("ivr_code", "1");
                    if ("0".equals(ivr_code)) {
                        String ivr_hyy = welMsgResMap.getOrDefault("ivr_hyy", XCCConstants.WELCOME_TEXT);
                        key = XCCConstants.YYSR;
                        val = ivr_hyy;
                    }
                } else {//有呼损的直接转人工
                    key = XCCConstants.RGYT;
                    val = "正在为您转接人工服务,请稍后";
                }

            } else {//白名单外直接转按键服务
                key = XCCConstants.JZLC;
                val = "正在为您转接按键服务,请稍后";
            }
        } else {//不可接入,直接转按键
            //添加到陌生号码库
            PMSHandler.saveUnknowNumber(phone, hzm);
            //转按键
            key = XCCConstants.JZLC;
            val = "正在为您转接按键服务,请稍后";
        }

        String[] arr = {key, val};
        log.info("finalDomain: {}", arr);
        return arr;

    }
}
