package model.crossarena.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrossArenaTopHis {

    Map<Integer, CrossArenaTopHisSub> his = new ConcurrentHashMap<>();

    public Map<Integer, CrossArenaTopHisSub> getHis() {
        return his;
    }

    public void setHis(Map<Integer, CrossArenaTopHisSub> his) {
        this.his = his;
    }
}
