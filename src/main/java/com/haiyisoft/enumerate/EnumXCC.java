package com.haiyisoft.enumerate;

/**
 * Created by Chr.yl on 2023/6/6.
 *
 * @author Chr.yl
 */
public enum EnumXCC implements CommonXCC {

    /**
     * NGD是否通过身份校验流程
     * YES 验证成功
     * NO 验证失败
     */
    USER_OK("userOK", "YES"),
    USER_NO("userOK", "NO"),
    /**
     * NGD错误码
     */
    NGD_REQUEST_TO_MUCH("4000019", "用户请求过于频繁，请稍后再试"),
    NGD_REQUEST_FAIL("4002012", "服务器处理该请求失败，请检查请求是否合法"),
    NGD_BOT_TOKEN_ERROR("4002409", "bot token错误"),
    NGD_BOT_RES_ERROR("4100", "知识库返回异常"),
    /**
     * 报表
     * 是否有效通话
     * 0 否
     * 1 是
     */
    IVR_VALID_CALL_FALSE("sfyx", "0"),
    IVR_VALID_CALL_TRUE("sfyx", "1"),
    /**
     * 报表
     * 是否转人工
     * 0 否
     * 1 是
     */
    IVR_ARTIFICIAL_FALSE("sfzrg", "0"),
    IVR_ARTIFICIAL_TRUE("sfzrg", "1"),
    /**
     * 报表
     * 通话是否正常结束
     * 0 否
     * 1 是
     */
    IVR_FINISH_FALSE("sfzcjs", "0"),
    IVR_FINISH_TRUE("sfzcjs", "1"),
    /**
     * 报表
     * 0 满意
     * 1 不满意
     * 2 未评价
     */
    IVR_RATE_TRUE("myd", "0"),
    IVR_RATE_FALSE("myd", "1"),
    IVR_RATE_NEUTRAL("myd", "2"),
    /**
     * 意图：其他
     * 意图为空时，全部设置为其他
     */
    IVR_INTENT_QT("QT", "#QT"),
    /**
     * 转人工队列编码
     * 0 投诉
     * 1 非投诉
     * 2 故障报修
     */
    IVR_TRANSFER_CODE_COMPLAINT("transferCode", "0"),
    IVR_TRANSFER_CODE_NO_COMPLAINT("transferCode", "1"),
    IVR_TRANSFER_CODE_REPAIR("transferCode", "2"),

    //
    ;

    private String property;
    private String value;


    EnumXCC(String property, String value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public String getProperty() {
        return this.property;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public int keyParseIntValue() {
        return Integer.parseInt(this.property);
    }

    @Override
    public int valueParseIntValue() {
        return Integer.parseInt(this.value);
    }

}
