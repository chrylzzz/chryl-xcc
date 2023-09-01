package com.haiyisoft.chryl.ivr;

import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.util.NGDUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 动态语音配置(因fs存在识别问题:ASR识别成功,但是fs返回超时)
 * Created by Chr.yl on 2023/9/1.
 *
 * @author Chr.yl
 */
@Slf4j
public class DynamicSpeech {

    //是否开启多语音包规则
    private static boolean TTS_VOICE_RULE = IVRInit.CHRYL_CONFIG_PROPERTY.isTtsVoiceRule();

    /**
     * 根据播报内容获取播报时间
     * 播报时间=(播报时间+语音超时时间)
     *
     * @param ttsContent 播报内容
     * @return 播报时间
     */
    public static long convertPlayContentToMilliSeconds(String ttsContent) {
        int length = ttsContent.length();
//        long playContentMilliSeconds = ((length / 4) + 1) * 1000L;
        long playContentMilliSeconds = ((length / 4) + 5) * 1000L;
        long sec = playContentMilliSeconds + IVRInit.CHRYL_CONFIG_PROPERTY.getSpeechNoInputTimeout();
        log.info("本次语音收集等待时间: {} ms , 播报文本长度: {} , 播报话术所需时间: {} ms", sec, length, playContentMilliSeconds);
        return sec;
    }

    /**
     * 随机获取 TTS-VOICE-NAME
     * 根据是否开启tts多voice规则,返回tts-voice
     * 开启 true
     * 关闭 false
     */
    public static String returnVoiceElement() {
        if (TTS_VOICE_RULE) {
            List<String> ttsVoiceList = IVRInit.CHRYL_CONFIG_PROPERTY.getTtsVoiceList();
            return ttsVoiceList.get(NGDUtil.threadLocalRandom.nextInt(ttsVoiceList.size()));
        } else {
            return IVRInit.CHRYL_CONFIG_PROPERTY.getTtsVoice();
        }
    }

}
