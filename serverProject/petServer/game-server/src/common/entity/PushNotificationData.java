package common.entity;

import com.alibaba.fastjson.JSONObject;
import common.GameConst.PushSourceType;
import common.GameConst.PushType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description 详细使用说明 http://192.168.0.202/index.php?title=%E6%8E%A8%E9%80%81
 * @Author hanx
 * @Date2020/8/6 0006 10:48
 **/
@Data
@NoArgsConstructor
public class PushNotificationData implements Serializable {
    private String title;
    private String body;
    private String multimedia;
    //可选 （DEVICE,USERID,TAG,ALL）
    private String targetType;

    private String targetValue;
    //1平台。2 SDK
    private int sourceType = PushSourceType.Platform.value();
    //必填：1即时推送，2定时推送
    private int pushType= PushType.INSTANT;

    @Override
    public String toString() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title",title);
        jsonObject.put("body",body);
        jsonObject.put("multimedia",multimedia);
        jsonObject.put("targetType",targetType);
        jsonObject.put("targetValue",targetValue);
        jsonObject.put("sourceType",sourceType);
        jsonObject.put("pushType",pushType);


        return jsonObject.toJSONString();
    }
}
