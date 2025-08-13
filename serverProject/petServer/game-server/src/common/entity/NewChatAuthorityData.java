package common.entity;

import com.alibaba.fastjson.JSONObject;
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
public class NewChatAuthorityData {
    private String roleId;
    private boolean appPermission;
    private String appPermissionMsg;

    public Map<String, Object> toTreeMap() {
        TreeMap<String, Object> result = new TreeMap<>();
        result.put("roleId", roleId);
        result.put("appPermission", appPermission);
        result.put("appPermissionMsg", appPermissionMsg);
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

    public String toJson() {
        return JSONObject.toJSONString(this);
    }

}
