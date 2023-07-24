package com.haiyisoft.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每次调用XCC接收业务实体
 * Created by Chr.yl on 2023/6/3.
 *
 * @author Chr.yl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XCCEvent {

    //xcc返回code
    private Integer code;
    //xcc返回message
    private String message;
    //返回正常则无次字段,type=ERROR时返回
    //type：结果类型，有DTMF、Speech.Begin、Speech.Partial、Speech.End，Speech.Merged、ERROR等。
    private String type;
    //返回正常无此字段,type=ERROR时返回
    //error：错误，如no_input、speech_timeout等
    private String error;
    //正常不返回 code=500时返回
    private String cause;
    //xcc识别返回结果(语音和按键),包括 utterance/dtmf
    private String xccRecognitionResult;
    //xcc method
    private String xccMethod;
    //xcc input: xcc检测客户是否输入 : true 已输入
    //连续两次未输入转人工
    private boolean xccInput;
}
