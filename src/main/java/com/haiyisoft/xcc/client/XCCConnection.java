package com.haiyisoft.xcc.client;

import com.haiyisoft.entry.ChannelEvent;
import com.haiyisoft.entry.IVREvent;
import com.haiyisoft.entry.NGDEvent;
import com.haiyisoft.entry.XCCEvent;
import io.nats.client.Connection;

/**
 * Created by Chr.yl on 2023/7/20.
 *
 * @author Chr.yl
 */
public interface XCCConnection {
    void setVar(Connection nc, ChannelEvent channelEvent);

    void getState(Connection nc, ChannelEvent channelEvent);

    void accept(Connection nc, ChannelEvent channelEvent);

    XCCEvent answer(Connection nc, ChannelEvent channelEvent);

    void hangup(Connection nc, ChannelEvent channelEvent);

    XCCEvent playTTS(Connection nc, ChannelEvent channelEvent, String ttsContent);

    void playFILE(Connection nc, ChannelEvent channelEvent, String file);


    XCCEvent detectSpeechPlayTTSNoDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent);


    XCCEvent detectSpeechPlayTTSNoDTMFNoBreak(Connection nc, ChannelEvent channelEvent, String ttsContent);

    XCCEvent playAndReadDTMF(Connection nc, ChannelEvent channelEvent, String ttsContent, int maxDigits);

    void handleTransferArtificial(Connection nc, ChannelEvent channelEvent, String ttsContent);

    XCCEvent bridgeExtension(Connection nc, ChannelEvent channelEvent, String ttsContent);

    XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr, String sipHeader, String callNumber);

    XCCEvent bridge(Connection nc, ChannelEvent channelEvent, String ttsContent, String dialStr);

    void writeLog(Connection nc, ChannelEvent channelEvent, String level, String data);

    XCCEvent bridgeArtificial(Connection nc, ChannelEvent channelEvent, String retValue, NGDEvent ngdEvent, String callNumber);

    XCCEvent bridgeIVR(Connection nc, ChannelEvent channelEvent, String retValue, IVREvent ivrEvent, NGDEvent ngdEvent, String callNumber);
}
