package com.haiyisoft.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * xcc channel model
 * Created by Chr.yl on 2023/3/7.
 *
 * @author Chr.yl
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelEvent {

    //xswitch node id
    private String nodeUuid;
    //channel id
    private String uuid;
    //Channel state
    private String state;
    /**
     * 传入sip_h_User-to-User(User-to-User):
     * <p>
     * icd传入: callid | 来电手机号 | 来话手机所对应的后缀码
     */
    private String sipReqHeaderU2U;
    /**
     * 返回sip_h_User-to-User(User-to-User):
     * fs返回: callid | 来电手机号 | 来话手机所对应的后缀码 | 用户编号(若返回空) | 转人工业务类型
     */
    private String sipResHeaderU2U;

}


