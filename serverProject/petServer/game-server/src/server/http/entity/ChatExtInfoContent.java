package server.http.entity;

import com.bowlong.third.FastJSON;

/**
 * 聊天服proto数据传输实体
 *
 * @author xiao_FL
 * @date 2019/12/26
 */
public class ChatExtInfoContent {
    byte[] content;

    public ChatExtInfoContent(byte[] bytes) {
        this.content = bytes;
    }

    @Override
    public String toString() {
        String extInfoStr = FastJSON.toJSONString(content);
        return "Test{" +
                "content=" + extInfoStr.substring(1, extInfoStr.length() - 1) +
                '}';
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
