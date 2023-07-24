package com.haiyisoft.model;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 详细对话dialog,这里只收集需要的信息
 * Created by Chr.yl on 2023/6/17.
 *
 * @author Chr.yl
 */
@Data
@AllArgsConstructor
public class NgdNodeDialog {

    //执行流程名
    private String processName;
    //节点名
    private String dialogNodeName;
    //回复内容相关
    private JSONObject values;

}
