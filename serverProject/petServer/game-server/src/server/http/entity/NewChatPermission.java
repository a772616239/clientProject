package server.http.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/27
 */
@Getter
@Setter
@NoArgsConstructor
public class NewChatPermission {
    private int code;
    private String msg;
    private Object data;

    public String toJson() {
        return JSONObject.toJSONString(this);
    }

    public NewChatPermission(int code) {
        this.code = code;
    }

    public NewChatPermission(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
