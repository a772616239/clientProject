package model.warpServer;

import datatool.StringHelper;

public class WarpServerConst {

    public static String parseIp(String ipPort) {
        if (StringHelper.isNull(ipPort)) {
            return "";
        }
        String[] strs = ipPort.split(":");
        if (strs == null || strs.length <= 0) {
            return "";
        }
        return strs[0];
    }
}
