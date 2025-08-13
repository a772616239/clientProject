package model.thewar.warmap.config;

import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import datatool.StringHelper;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import model.thewar.warmap.grid.WarGridDefaultProp;
import protocol.TheWarDefine.TheWarCell;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarMapData;
import util.LogUtil;

public class TotalWarMapCfgData {
    private static final Map<String, WarMapConfig> totalMapData = new HashMap<>();

    public static boolean init() {
        if (!WarGridDefaultProp.loadWarMapDefaultProp()) {
            return false;
        }

        for (Entry<String, TheWarMapConfigObject> entry : TheWarMapConfig._ix_mapname.entrySet()) {
            WarMapConfig warMapConfig = loadWarMapConfig(entry.getValue().getMapfilename());
            if (warMapConfig == null) {
                return false;
            }
            totalMapData.put(entry.getKey(), warMapConfig);
        }
        return true;
    }

    public static WarMapConfig loadWarMapConfig(String mapFileName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("cfg/" + mapFileName);
            TheWarMapData warMapConfigData = TheWarMapData.parseFrom(fis);
            WarMapConfig warMapConfig = new WarMapConfig();
            warMapConfig.setMapName(warMapConfigData.getMapName());
            warMapConfig.setLength(Math.abs(warMapConfigData.getMax().getX() - warMapConfigData.getMin().getX()));
            warMapConfig.setHeight(Math.abs(warMapConfigData.getMax().getY() - warMapConfigData.getMin().getY()));
            for (TheWarCell cell : warMapConfigData.getCellListList()) {
                int gridType = (int) WarGridDefaultProp.getDefaultPropVal(cell.getName(), TheWarCellPropertyEnum.TWCP_CellType_VALUE);
                if (gridType < 0) {
                    continue;
                }
                if (TheWarCellPropertyEnum.forNumber(gridType) == null) {
                    LogUtil.error("Load warMap error, gridType error,mapName=" + warMapConfig.getMapName() + ",cellName=" + cell.getName());
                    return null;
                }
                warMapConfig.addWarCell(cell.getPos(), cell);
            }
            return warMapConfig;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
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


    public static WarMapConfig getInitMapData(String mapName) {
        if (StringHelper.isNull(mapName)) {
            return null;
        }
        return totalMapData.get(mapName);
    }
}
