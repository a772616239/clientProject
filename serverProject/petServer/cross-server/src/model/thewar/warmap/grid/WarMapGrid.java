package model.thewar.warmap.grid;

import common.GameConst.EventType;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.obj.BaseObj;
import model.thewar.WarConst;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWarDB.GridCacheData;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.SC_UpdateWarGridProp;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarGridData;
import protocol.TheWarDefine.TheWarPropertyMap;
import server.event.Event;
import server.event.EventManager;

public class WarMapGrid extends BaseObj {
    protected String roomIdx;
    protected Position pos;
    protected String name;
    protected Map<Integer, Long> propMap; // 仅存非默认值，减少开销
    protected Set<Position> aroundGridSet;

    protected List<Integer> propMsgData;

    protected GridCacheData.Builder gridCacheData;

    public WarMapGrid() {
        this.propMap = new ConcurrentHashMap<>();
        this.aroundGridSet = new HashSet<>();
        this.propMsgData = new ArrayList<>();
        this.gridCacheData = GridCacheData.newBuilder();
    }

    @Override
    public String getBaseIdx() {
        return String.valueOf(pos.hashCode());
    }

    @Override
    public String getIdx() {
        return String.valueOf(pos.hashCode());
    }

    @Override
    public void setIdx(String idx) {

    }

    @Override
    public String getClassType() {
        return getClass().getSimpleName();
    }

    @Override
    public void putToCache() {
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            Event event = Event.valueOf(EventType.ET_TheWar_AddGridCache, this, warRoom);
            event.pushParam(buildGridCacheBuilder().build());
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    @Override
    public void transformDBData() {

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
        addUpdatePropType(propType);
    }

    public void setPropDefaultValue(int propType) {
        propMap.merge(propType, 0l, (oldVal, newVal) -> null);
        addUpdatePropType(propType);
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

    public void addUpdatePropType(int propType) {
        if (WarConst.isServerOnlyProp(propType)) {
            return;
        }
        propMsgData.add(propType);
    }

    public void broadcastPropData() {
        WarRoom room = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (room == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        SC_UpdateWarGridProp.Builder builder = SC_UpdateWarGridProp.newBuilder();
        builder.setUpdateTime(curTime);
        TheWarGridData.Builder gridBuilder = TheWarGridData.newBuilder();
        gridBuilder.setPos(getPos());
        gridBuilder.setName(getName());
        TheWarPropertyMap.Builder propBuilder = TheWarPropertyMap.newBuilder();
        propMsgData.forEach(propType -> propBuilder.addKeysValue(propType).addValues(getPropValue(propType)));
        gridBuilder.setProps(propBuilder);
        propMsgData.clear();
        builder.addWarGridsInfo(gridBuilder);
        room.broadcastMsg(MsgIdEnum.SC_UpdateWarGridProp_VALUE, builder, true);
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

    public GridCacheData.Builder buildGridCacheBuilder() {
        gridCacheData.clear();
        gridCacheData.setPos(getPos());
        gridCacheData.setGridName(getName());
        gridCacheData.putAllPropMap(getTotalProp());
        gridCacheData.addAllAroundGrids(getAroundGrids());
        return gridCacheData;
    }

    public void parseFromCacheData(GridCacheData cacheData) {
        setName(cacheData.getGridName());
        setPos(cacheData.getPos());
        setInitProp(cacheData.getPropMapMap());
        getAroundGrids().addAll(cacheData.getAroundGridsList());
    }
}
