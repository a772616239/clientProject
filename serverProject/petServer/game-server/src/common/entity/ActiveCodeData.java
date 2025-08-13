package common.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/27
 */
@Getter
@Setter
public class ActiveCodeData {
    private String roleId;
    private String aCode;
    private String userId;
    private int serverIndex;

    @Override
    public String toString() {
        return "{" +
                "\"roleId\":" + "\"" + roleId + "\"" +
                ",\"aCode\":" + "\"" + aCode + "\"" +
                ",\"userId\":" + "\"" + userId + "\"" +
                ",\"serverIndex\":" + serverIndex +
                '}';
    }
}
