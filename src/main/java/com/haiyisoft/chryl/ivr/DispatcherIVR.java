package com.haiyisoft.chryl.ivr;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import com.haiyisoft.handler.IVRHandler;
import com.haiyisoft.handler.WebHookHandler;
import com.haiyisoft.xcc.client.XCCConnection;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Central dispatcher for IVR request handlers/controllers
 * Created by Chr.yl on 2023/6/4.
 *
 * @author Chr.yl
 */
@Slf4j
@Component
public class DispatcherIVR {

    @Autowired
    private XCCConnection xccConnection;

    /**
     * IVR INVOKE
     *
     * @param nc
     * @param channelEvent
     * @param retKey
     * @param retValue
     * @param ngdEvent
     * @param ivrEvent
     * @param callerIdNumber
     * @return
     */
    public XCCEvent doDispatch(Connection nc, ChannelEvent channelEvent, String retKey, String retValue, IVREvent ivrEvent, NGDEvent ngdEvent, String callerIdNumber) {
        XCCEvent xccEvent;
        if (XCCConstants.YYSR.equals(retKey)) {//播报收音
            xccEvent = xccConnection.detectSpeechPlayTTSNotDTMF(nc, channelEvent, retValue);
        } else if (XCCConstants.AJSR.equals(retKey)) {//收集按键，多位按键
            xccEvent = xccConnection.playAndReadDTMF(nc, channelEvent, retValue, 18);
        } else if (XCCConstants.YWAJ.equals(retKey)) {//收集按键，一位按键
            xccEvent = xccConnection.playAndReadDTMFChryl(nc, channelEvent, retValue, 1);
        } else if (XCCConstants.YYGB.equals(retKey)) {//语音广播,不可打断
            xccEvent = xccConnection.detectSpeechPlayTTSNoDTMFNoBreak(nc, channelEvent, retValue);
        } else if (XCCConstants.RGYT.equals(retKey)) {//转人工
            //测试-分机
//            xccEvent = XCCHandler.bridgeExtension(nc, channelEvent, retValue);

            //设置转人工标志
            ivrEvent.setTransferFlag(true);
            //保存通话数据
            IVRHandler.beforeTransfer(ivrEvent, ngdEvent);
            //转人工
            xccEvent = xccConnection.bridgeArtificial(nc, channelEvent, retValue, ngdEvent, callerIdNumber);
        } else if (XCCConstants.JZLC.equals(retKey)) {//转精准IVR
            xccEvent = xccConnection.bridgeIVR(nc, channelEvent, retValue, ivrEvent, ngdEvent, callerIdNumber);
        } else if (XCCConstants.DXFS.equals(retKey)) {//短信发送
            WebHookHandler.sendMessage(ivrEvent, retValue);
            xccEvent = xccConnection.detectSpeechPlayTTSNotDTMF(nc, channelEvent, XCCConstants.FAQ_SEND_MESSAGE_TEXT);
        } else if (XCCConstants.PLAY.equals(retKey)) {//语音播报,不收集
            xccEvent = xccConnection.playTTS(nc, channelEvent, retValue);
        } else {
            log.error("严格根据配置的指令开发");
            xccEvent = new XCCEvent();
        }
        return xccEvent;
    }


}
