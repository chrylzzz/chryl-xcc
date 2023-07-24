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

        String formatSipHeader = "";
        if (ngdEvent.isUserOk()) {
            //用户编号
            String uid = ngdEvent.getUid();
            if (StringUtils.isBlank(uid)) {
                //不处理使用,只加 |
                formatSipHeader = sipReqHeaderU2U + XCCConstants.SIP_HEADER_SEPARATOR;
            } else {
                //处理,替换用户编号
                //当前使用1业务类型
                //res
                String sipResHeaderU2U = sipReqHeaderU2U + XCCConstants.RES_SIP_SUFFIX;
                formatSipHeader = String.format(sipResHeaderU2U, uid);
            }
        } else {
            //不处理使用,只加 |
            formatSipHeader = sipReqHeaderU2U + XCCConstants.SIP_HEADER_SEPARATOR;
        }
        log.info("转接 sip header : {}", formatSipHeader);
        return formatSipHeader;
    }


    public static void main(String[] args) {
//        String s = "callid | 来电手机号 | 来话手机所对应的后缀码 | %s | 转人工业务类型";
        String s = "callid | 来电手机号 | 来话手机所对应的后缀码 | 100100001 | 转人工业务类型";
        StringBuilder stringBuffer = new StringBuilder();
        String format = String.format(s, "5555555");
        System.out.println(format);
    }
}
