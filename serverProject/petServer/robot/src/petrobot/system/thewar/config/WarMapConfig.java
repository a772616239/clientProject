package petrobot.system.thewar.config;

import java.util.HashMap;
import java.util.Map;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCell;

public class WarMapConfig {
    private String mapName;
    private int length;
    private int height;
    private Map<Position, TheWarCell> warMapInitGrids = new HashMap<>();

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void addWarCell(Position pos, TheWarCell cell) {
        warMapInitGrids.put(pos, cell);
    }

    public Map<Position, TheWarCell> getInitGrids() {
        return warMapInitGrids;
    }
}
