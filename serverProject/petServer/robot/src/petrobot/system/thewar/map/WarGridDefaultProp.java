package petrobot.system.thewar.map;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import petrobot.util.LogUtil;
import protocol.TheWarDefine.CellDefaultPropertiesMap;
import protocol.TheWarDefine.TheWarPropertyMap;

public class WarGridDefaultProp {
    private final static Map<String, Map<Integer, Long>> defaultPropMap = new HashMap<>();

    public static boolean loadWarMapDefaultProp() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("cfg/DefaultPropertyMap_Server.bytes");
            CellDefaultPropertiesMap propMapData = CellDefaultPropertiesMap.parseFrom(fis);
            for (int i = 0; i < propMapData.getKeysCount(); i++) {
                String type = propMapData.getKeys(i);
                Map<Integer, Long> mapData = defaultPropMap.get(type);
                if (mapData == null) {
                    mapData = new HashMap<>();
                    defaultPropMap.put(type, mapData);
                }
                TheWarPropertyMap propValues = propMapData.getValues(i);
                for (int j = 0; j < propValues.getKeysCount(); j++) {
                    int propType = propValues.getKeysValue(j);
                    long propVal = propValues.getValues(j);
                    mapData.put(propType, propVal);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtil.printStackTrace(e);
                }
            }
        }
    }

    public static Map<Integer, Long> getDefaultPropMap(String objType) {
        return defaultPropMap.get(objType);
    }

    public static long getDefaultPropVal(String gridName, int propType) {
        Map<Integer, Long> defaultMap = getDefaultPropMap(gridName);
        if (defaultMap == null) {
            return 0;
        }
        Long val = defaultMap.get(propType);
        return val != null ? val : 0;
    }
}
