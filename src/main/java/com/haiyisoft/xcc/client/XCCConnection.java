package com.haiyisoft.xcc.client;

import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import io.nats.client.Connection;

/**
 * The Connection class is at the heart of the XCC Java client
 * Created by Chr.yl on 2023/7/20.
 *
 * @author Chr.yl
 */
public interface XCCConnection {

    void setVar(Connection nc, ChannelEvent channelEvent);

    void getState(Connection nc, ChannelEvent channelEvent);

    void accept(Connection nc, ChannelEvent channelEvent);

    /**
     * 应答
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    XCCEvent answer(Connection nc, ChannelEvent channelEvent);

    /**
     * 挂断
     *
     * @param nc
     * @param channelEvent
     */
    void hangup(Connection nc, ChannelEvent channelEvent);

    /**
     * 播放text
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent   内容
     */
    XCCEvent playTTS(Connection nc, ChannelEvent channelEvent, String ttsContent);

    /**
     * 播放file
     *
     * @param nc
     * @param channelEvent
     * @param file         file_path/png_file
     */
    void playFILE(Connection nc, ChannelEvent channelEvent, String file);

    /**
     * 语音识别,播放语音,收集语音(不收集按键)
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    XCCEvent detectSpeechPlayTTSNoDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent);

    /**
     * 不可打断
     * 语音识别,播放语音,收集语音(不收集按键)
     *
     * @param nc
     * @param channelEvent
     * @return
     */
    XCCEvent detectSpeechPlayTTSNoDTMFNoBreak(Connection nc, ChannelEvent channelEvent, String ttsContent);

    /**
     * 播报并收集按键(多位按键)
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent   播报内容
     * @param maxDigits    最大位长
     * @return
     */
    XCCEvent playAndReadDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent, int maxDigits);

    /**
     * 播报并收集按键(少位按键)
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent   播报内容
     * @param maxDigits    最大位长
     * @return
     */
    XCCEvent playAndReadDTMFChryl(Connection nc, ChannelEvent channelEvent, String ttsContent, int maxDigits);

    /**
     * 转人工测试
     * Transfer Artificial
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     */
    void handleTransferArtificial(Connection nc, ChannelEvent channelEvent, String ttsContent);

    /**
     * TODO
     * 转接内部分机
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @return
     */
    XCCEvent bridgeExtension(Connection nc, ChannelEvent channelEvent, String ttsContent);

    /**
     * 转接
     * 带消息头/原始呼叫
     *
     * @param nc
     * @param channelEvent
     * @param ttsContent
     * @return
     */
    XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr, String sipHeader, String callNumber);

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
    XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr);

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
    void writeLog(Connection nc, ChannelEvent channelEvent, String level, String data);

    /**
     * 广西-转接华为人工
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @param ngdEvent
     * @param callNumber
     * @return
     */
    XCCEvent bridgeArtificial(Connection nc, ChannelEvent channelEvent, String retValue, NGDEvent ngdEvent, String callNumber);

    /**
     * 广西-转接华为流程
     *
     * @param nc
     * @param channelEvent
     * @param retValue
     * @param ngdEvent
     * @param callNumber
     * @return
     */
    XCCEvent bridgeIVR(Connection nc, ChannelEvent channelEvent, String retValue, IVREvent ivrEvent, NGDEvent ngdEvent, String callNumber);

}
