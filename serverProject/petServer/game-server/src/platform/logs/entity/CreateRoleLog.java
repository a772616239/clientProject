package platform.logs.entity;

import com.alibaba.fastjson.JSONObject;
import common.HttpRequestUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import protocol.LoginProto.ClientData;

@Getter
@Setter
@NoArgsConstructor
public class CreateRoleLog extends AbstractPlayerLog {
    private String deviceName;
    private String nation;
    private String city;

    public CreateRoleLog(String playerIdx, ClientData clientData, String ip) {
        super(playerIdx);
        if (clientData != null) {
            this.setPlatform(clientData.getPlatform());
            this.setChannel(clientData.getChannel());
            this.deviceName = clientData.getDevice();
            this.setSourceId(String.valueOf(clientData.getSourceId()));
        }
        JSONObject result = HttpRequestUtil.queryIpInfo(ip);
        if (result.containsKey("nation")) {
            this.nation = result.getString("nation");
        }
        if (result.containsKey("city")) {
            this.city = result.getString("city");
        }
    }
}
