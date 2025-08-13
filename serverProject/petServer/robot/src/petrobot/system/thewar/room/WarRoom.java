package petrobot.system.thewar.room;

import io.netty.util.internal.ConcurrentSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import petrobot.system.thewar.map.WarMapData;
import petrobot.system.thewar.map.grid.WarMapGrid;
import petrobot.util.BaseObj;
import protocol.TheWar.TheWarPlayerBaseInfo;
import protocol.TheWarDB.WarCampInfo;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCellPropertyEnum;

@Getter
@Setter
public class WarRoom extends BaseObj {
    @Override
    public String getBaseIdx() {
        return roomIdx;
    }

    @Override
    public String getClassType() {
        return getClass().getSimpleName();
    }

    @Override
    public void putToCache() {

    }

    @Override
    public void transformDBData() {

    }

    protected String roomIdx;
    protected String mapName;
    protected Set<String> members;

    protected WarMapData warMap;
    protected Map<Integer, Map<String, TheWarPlayerBaseInfo>> totalCampData;
    private Map<Integer, WarCampInfo.Builder> totalCampInfo; // <WarCamp, CampInfo>

    public WarRoom(String roomIdx, String mapName) {
        this.roomIdx = roomIdx;
        this.mapName = mapName;
        this.members = new  ConcurrentSet<>();
        this.totalCampData = new ConcurrentHashMap<>();
        this.totalCampInfo = new ConcurrentHashMap<>();
        this.warMap = new WarMapData(roomIdx, mapName);
    }

    public void clear() {
        mapName = null;
        members.clear();
        warMap.clear();
        totalCampData.clear();
        totalCampInfo.clear();
    }

    public void updateMembers(List<TheWarPlayerBaseInfo> memberList) {
        if (CollectionUtils.isEmpty(memberList)) {
            return;
        }
        for (TheWarPlayerBaseInfo member : memberList) {
            Map<String, TheWarPlayerBaseInfo> campData = totalCampData.get(member.getCamp());
            if (campData == null) {
                campData = new HashMap<>();
                totalCampData.put(member.getCamp(), campData);
            }
            campData.put(member.getPlayerInfo().getPlayerId(), member);
            members.add(member.getPlayerInfo().getPlayerId());
        }
    }

    public Position getAvailableAttackPos(int camp, Position pos) {
        WarMapGrid grid = warMap.getGridMap().get(pos);
        if (grid == null || grid.isBlock()) {
            return null;
        }
        WarMapGrid aroundGrid;
        for (Position aroundPos : grid.getAroundGrids()) {
            aroundGrid = warMap.getMapGridByPos(aroundPos);
            if (aroundGrid != null && !aroundGrid.isBlock()
                    && aroundGrid.getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) <= 0
                    && aroundGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Camp_VALUE) != camp) {
                return aroundPos;
            }
        }
        return null;
    }

    public void initCampInfo() {

    }

    public void calcAddCampGrid() {

    }

    public void removeAddCampGrid() {

    }
}
