package com.haiyisoft.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.haiyisoft.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * IVR业务交互实体
 * Created by Chr.yl on 2023/7/4.
 *
 * @author Chr.yl
 */
@Builder
@Data
@AllArgsConstructor
public class IVRModel {

    //------------------------------------------------通话相关数据
    /**
     * 来电号码
     */
    @JSONField(name = "ldhm")
    private String cidPhoneNumber;
    /**
     * fs caller id == channel id
     */
    @JSONField(name = "fscallid")
    private String fsCallerId;
    /**
     * icd caller id
     */
    @JSONField(name = "icdcallid")
    private String icdCallerId;
    /**
     * 来话开始时间
     */
    @JSONField(name = "ivrkssj")
    private String ivrStartTime;
    /**
     * 来话结束时间
     */
    @JSONField(name = "ivrjssj")
    private String ivrEndTime;
    //是否转人工:0否1是
    @JSONField(name = "sfzrg")
    private String artificialType;
    /**
     * 是否有效通话: 0否1是
     */
    @JSONField(name = "sfyx")
    private String ivrValidCallType;
    /**
     * 是否正常结束: 0否1是
     */
    @JSONField(name = "sfzcjs")
    private String ivrCallEndNormalType;
    /**
     * 意图对象
     */
    @JSONField(name = "ytdx")
    private String intent;
    /**
     * 指令
     */
    @JSONField(name = "zl")
    private String zl;
    /**
     * 地区编码
     */
    @JSONField(name = "dqbm")
    private String areaCode;
    /**
     * 供电单位编码
     */
    @JSONField(name = "gddwbm")
    private String orgCode;

    //------------------------------------------------通话相关数据

    public IVRModel() {
        this.ivrStartTime = DateUtil.getLocalDateTime();
    }

    /**
     * 来话意图实体
     *
     * @param cidPhoneNumber
     * @param fsCallerId
     * @param icdCallerId
     * @param ivrStartTime
     * @param intent
     * @param zl
     * @param areaCode
     * @param orgCode
     */
    public IVRModel(String cidPhoneNumber, String fsCallerId, String icdCallerId, String ivrStartTime, String intent, String zl, String areaCode, String orgCode) {
        this.cidPhoneNumber = cidPhoneNumber;
        this.fsCallerId = fsCallerId;
        this.icdCallerId = icdCallerId;
        this.ivrStartTime = ivrStartTime;
        this.ivrEndTime = DateUtil.getLocalDateTime();
        this.intent = intent;
        this.zl = zl;
        this.areaCode = areaCode;
        this.orgCode = orgCode;
    }

    /**
     * 通话数据实体
     *
     * @param cidPhoneNumber
     * @param fsCallerId
     * @param icdCallerId
     * @param ivrStartTime
     * @param artificialType
     * @param ivrValidCallType
     * @param ivrCallEndNormalType
     */
    public IVRModel(String cidPhoneNumber, String fsCallerId, String icdCallerId, String ivrStartTime, String artificialType, String ivrValidCallType, String ivrCallEndNormalType) {
        this.cidPhoneNumber = cidPhoneNumber;
        this.fsCallerId = fsCallerId;
        this.icdCallerId = icdCallerId;
        this.ivrStartTime = ivrStartTime;
        this.ivrEndTime = DateUtil.getLocalDateTime();
        this.artificialType = artificialType;
        this.ivrValidCallType = ivrValidCallType;
        this.ivrCallEndNormalType = ivrCallEndNormalType;
    }

    public static void main(String[] args) {
        IVRModel ivrModel = IVRModel.builder().cidPhoneNumber("12").fsCallerId("aaa-bbb-ccc").icdCallerId("1234-2244").ivrStartTime(DateUtil.getLocalDateTime()).ivrEndTime(DateUtil.getLocalDateTime()).artificialType("1").ivrValidCallType("1").ivrCallEndNormalType("1").intent("#sjdf").zl(null).areaCode(null).orgCode(null).build();

        IVRModel ivrModel2 = new IVRModel("13344563332", "anbc", "akc", DateUtil.getLocalDateTime(), DateUtil.getLocalDateTime(), "1", "1", "1", "a", "zl", "0401", "040100");
        IVRModel ivrModel3
                = new IVRModel("15567895678", "454666-2883d", "355-22883sh211", DateUtil.getLocalDateTime(),
                "#tdyt#djkf3#sjsjf", "", "", "");
        String s = JSON.toJSONString(ivrModel3);
        System.out.println(s);

    }

}
