package common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author huhan
 * @date 2020/07/27
 */
@Getter
@Setter
public class ChatAuthorityData {
    private String roleId;
    private String userId;
    private int rightType;
    private String extInfo;
    private String msg;
    private String serverIndex;


    public Map<String, Object> toTreeMap() {
        TreeMap<String, Object> result = new TreeMap<>();
        result.put("roleId", roleId);
        result.put("userId", userId);
        result.put("rightType", rightType);
        result.put("extInfo", extInfo);
        result.put("msg", msg);
        result.put("serverIndex", serverIndex);
        return result;
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (Entry<String, Object> entry : toTreeMap().entrySet()) {
            list.add(entry.getKey() + "=" + entry.getValue());
        }
        return StringUtils.join(list, "&");
    }
}
