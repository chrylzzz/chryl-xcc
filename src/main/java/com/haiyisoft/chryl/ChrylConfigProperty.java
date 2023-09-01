package com.haiyisoft.chryl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;
//import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 可配置
 * XCC和NGD等常量
 * Created by Chr.yl on 2023/3/28.
 *
 * @author Chr.yl
 */
@Data
@ConfigurationProperties(prefix = "chryl")
//@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
@JSONType(naming = PropertyNamingStrategy.KebabCase)
public class ChrylConfigProperty {

    /******************************************** XCC ********************************************/

    // nats url
    private String natsUrl;

    // ctrl subject
    private String xctrlSubject;

    // node subject prefix
    private String xnodeSubjectPrefix;

    public void setXnodeSubjectPrefix(String xnodeSubjectPrefix) {
        this.xnodeSubjectPrefix = xnodeSubjectPrefix + ".";
    }

    // xcc tts no break
    private String noBreak;

    // cluster true or false
    private boolean cluster;

    // parse engine xml data
    private boolean handleEngineData;

    // nats list
    private List<JSONObject> natsList;

    // DTMF 输入超时
    private int dtmfNoInputTimeout;

    // DTMF 按键输入超时(收集少位按键)
    private int dtmfChrylNoInputTimeout;

    // DTMF 位间超时
    private int digitTimeout;

    // speech 未检测到语音超时
    private int speechNoInputTimeout;

    // speech 语音超时，即如果对方讲话一直不停超时，最大只能设置成6000ms，默认为6000ms。
    private int speechTimeout;

    // speech 语音最大超时，和参数speech_timeout作用相同，如果max_speech_timeout的值大于speech_timeout，则以max_speech_timeout为主，用于一些特殊场景的语音时长设置。
    private int maxSpeechTimeout;

    // 动态语音配置(因fs存在识别问题:ASR识别成功,但是fs返回超时)
    private boolean dynamicSpeech;

    public void setDtmfNoInputTimeout(int dtmfNoInputTimeout) {
        this.dtmfNoInputTimeout = dtmfNoInputTimeout * 1000;
    }

    public void setDtmfChrylNoInputTimeout(int dtmfChrylNoInputTimeout) {
        this.dtmfChrylNoInputTimeout = dtmfChrylNoInputTimeout * 1000;
    }

    public void setDigitTimeout(int digitTimeout) {
        this.digitTimeout = digitTimeout * 1000;
    }

    public void setSpeechNoInputTimeout(int speechNoInputTimeout) {
        this.speechNoInputTimeout = speechNoInputTimeout * 1000;
    }

    public void setSpeechTimeout(int speechTimeout) {
        this.speechTimeout = speechTimeout * 1000;
    }

    public void setMaxSpeechTimeout(int maxSpeechTimeout) {
        this.maxSpeechTimeout = maxSpeechTimeout * 1000;
    }

    /******************************************** XCC ********************************************/
    /******************************************** NGD ********************************************/

    // ngd query url
    private String ngdCoreQueryUrl;

    // ngd bot token auth
    private String ngdBotToken;

    // convert solved
    private boolean convertSolved;

    /******************************************** NGD ********************************************/
    /******************************************** TTS ********************************************/

    // tts engine
    private String ttsEngine;

    // tts engine
    private String ttsVoice;

    // 是否开启tts voice规则
    private boolean ttsVoiceRule;

    // tts voice list
    private List<String> ttsVoiceList;

    // tts语速
    private String xttsS;

    /******************************************** TTS ********************************************/
    /******************************************** ASR ********************************************/

    // asr engine
    private String asrEngine;

    /******************************************** ASR ********************************************/
    /****************************************** WebHook ******************************************/

    // webhook url
    private String webHookUrl;

    /****************************************** WebHook ******************************************/
    /******************************************** PMS ********************************************/

    // pms url
    private String pmsUrl;

    /******************************************** PMS ********************************************/
}
