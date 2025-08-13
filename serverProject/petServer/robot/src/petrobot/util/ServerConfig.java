package petrobot.util;

import lombok.ToString;
import servertool.ServerCommonConfig;

@ToString
public class ServerConfig extends ServerCommonConfig {
    private static ServerConfig instance = new ServerConfig();

    private ServerConfig() {
        setJsonPath();
    }

    public static ServerConfig getInstance() {
        return instance;
    }

    private String jsonPath;

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath() {
        this.jsonPath = "cfg";
//        this.jsonPath = Config.get("jsonPath");
    }

}
