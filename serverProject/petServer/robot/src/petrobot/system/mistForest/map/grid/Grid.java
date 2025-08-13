package petrobot.system.mistForest.map.grid;

import lombok.Getter;
import lombok.Setter;
import petrobot.system.mistForest.obj.MistObj;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Grid {
    public Grid(int gridType) {
        this.gridType = gridType;
        this.gridObjMap = new HashMap<>();
    }

    private int gridType;
    private boolean blocked;
    private boolean safeRegion;

    private boolean dynamicBlock;

    private Map<Long, MistObj> gridObjMap;

    public boolean isGridBlocked() {
        return blocked || dynamicBlock;
    }
}
