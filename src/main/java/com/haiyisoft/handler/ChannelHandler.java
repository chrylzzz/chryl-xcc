package com.haiyisoft.handler;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.NGDEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Chr.yl on 2023/6/15.
 *
 * @author Chr.yl
 */
@Slf4j
public class ChannelHandler {

    /**
     * 处理 sip header
     * userOk is true: NGD 已校验完成身份验证,可对SIP HEADER处理
     * uid
     * userOk is false: NGD 未校验完成身份验证,不处理
     *
     * @param ngdEvent
     * @param channelEvent
     * @return
     */
    public static String handleSipHeader(NGDEvent ngdEvent, ChannelEvent channelEvent) {
        //校验通过处理sip header
        //req
        String sipReqHeaderU2U = channelEvent.getSipReqHeaderU2U();
//        String transferCode = ngdEvent.getTransferCode();
        //处理请求头,获得响应头前缀
        String resHeaderPrefix = StringUtils.substringBeforeLast(sipReqHeaderU2U, "|");

        String formatSipHeader = "";
        if (ngdEvent.isUserOk()) {
            //用户编号
            String uid = ngdEvent.getUid();
            if (StringUtils.isBlank(uid)) {
                //不处理使用,只加 |
                formatSipHeader = resHeaderPrefix + XCCConstants.SIP_HEADER_SEPARATOR;
//                formatSipHeader = sipReqHeaderU2U + XCCConstants.RES_SIP_NULL_UID_SUFFIX;
            } else {
                //处理,替换用户编号
                //当前使用1业务类型
                //投诉	0 非投诉	1 故障报修	2
                //res
                String sipResHeaderU2U = resHeaderPrefix + XCCConstants.RES_SIP_SUFFIX;
                formatSipHeader = String.format(sipResHeaderU2U, uid);
            }
        } else {
            //不处理使用,只加 |
            formatSipHeader = resHeaderPrefix + XCCConstants.SIP_HEADER_SEPARATOR;
//            formatSipHeader = sipReqHeaderU2U + XCCConstants.RES_SIP_NULL_UID_SUFFIX;
        }
        log.info("转接 sip header : {}", formatSipHeader);
        return formatSipHeader;
    }


    public static void main(String[] args) {
//        String resStr = "callid | 来电手机号 | 来话手机所对应的后缀码 | %resStr | 转人工业务类型";
        String resStr = "callid | 来电手机号 | 来话手机所对应的后缀码 | 100100001 | 转人工业务类型";
        StringBuilder stringBuffer = new StringBuilder();
        String format = String.format(resStr, "5555555");
        System.out.println(format);
        int i = resStr.lastIndexOf("|");
        System.out.println(i);
        String substring = resStr.substring(i);
        System.out.println(substring);
        String s1 = StringUtils.substringBeforeLast(resStr, "|");
        System.out.println(s1);
//        String[] splitU2U = resStr.split("\\|");
//        System.out.println(splitU2U[5]);

        String reqStr = "call_id | 来话手机号 | 来话手机号所属地后缀码 | 话务转接标识";
    }
}
