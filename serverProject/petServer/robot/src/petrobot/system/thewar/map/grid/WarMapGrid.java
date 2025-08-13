package petrobot.system.thewar.map.grid;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import petrobot.system.thewar.map.WarGridDefaultProp;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarGridData;
import protocol.TheWarDefine.TheWarPropertyMap;

public class WarMapGrid {
    protected String roomIdx;
    protected Position pos;
    protected String name;
    protected Map<Integer, Long> propMap; // 仅存非默认值，减少开销
    protected Set<Position> aroundGridSet;

    public WarMapGrid() {
        this.propMap = new ConcurrentHashMap<>();
        this.aroundGridSet = new HashSet<>();
    }

    public boolean isBlock() {
        return getPropValue(TheWarCellPropertyEnum.TWCP_IsBlock_VALUE) > 0;
    }

    public String getRoomIdx() {
        return roomIdx;
    }

    public void setRoomIdx(String roomIdx) {
        this.roomIdx = roomIdx;
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInitProp(Map<Integer, Long> propMap) {
        if (propMap == null) {
            return;
        }
        this.propMap.clear();
        this.propMap.putAll(propMap);
    }

    public Map<Integer, Long> getTotalProp() {
        return propMap;
    }

    public void setPropValue(int propType, long value) {
        long defaultVal = WarGridDefaultProp.getDefaultPropVal(getName(), propType);
        propMap.merge(propType, value, (oldVal, newVal) -> newVal != defaultVal ? newVal : null);
    }

    public void setPropDefaultValue(int propType) {
        propMap.merge(propType, 0l, (oldVal, newVal) -> null);
    }

    public long getPropValue(int propType) {
        Long val = propMap.get(propType);
        return val != null ? val : WarGridDefaultProp.getDefaultPropVal(getName(), propType);
    }

    public void addAroundGrid(WarMapGrid aroundGrid) {
        if (aroundGrid != null) {
            aroundGridSet.add(aroundGrid.getPos());
        }
    }

    public Set<Position> getAroundGrids() {
        return aroundGridSet;
    }

    public boolean isAroundGrid(Position aroundPos) {
        return aroundGridSet.contains(aroundPos);
    }

    public TheWarGridData.Builder getWarGridData() {
        TheWarGridData.Builder builder = TheWarGridData.newBuilder();
        builder.setPos(getPos());
        builder.setName(getName());
        TheWarPropertyMap.Builder propBuilder = TheWarPropertyMap.newBuilder();
        for (Entry<Integer, Long> entry : getTotalProp().entrySet()) {
            propBuilder.addKeysValue(entry.getKey());
            propBuilder.addValues(entry.getValue());
        }
        builder.setProps(propBuilder);
        return builder;
    }
}
