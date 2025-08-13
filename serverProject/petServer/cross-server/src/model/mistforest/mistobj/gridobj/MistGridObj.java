package model.mistforest.mistobj.gridobj;

import model.mistforest.map.grid.Grid;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;

public class MistGridObj extends MistObject {
    public MistGridObj(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void clear() {
        Grid grid = room.getWorldMap().getGridByPos(getPos().getX(), getPos().getY());
        if (grid != null && grid.getGridObj() != null && grid.getGridObj().getId() == getId()) {
            grid.setGridObj(null);
        }
        super.clear();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        Grid grid = room.getWorldMap().getGridByPos(getPos().getX(), getPos().getY());
        if (grid != null) {
            grid.setGridObj(this);
        }
    }

    public boolean isGridBlock(MistFighter fighter) {
        return false;
    }

    public void onPlayerEnter(MistFighter fighter) {
    }

    public void onPlayerLeave(MistFighter fighter) {
    }
}
