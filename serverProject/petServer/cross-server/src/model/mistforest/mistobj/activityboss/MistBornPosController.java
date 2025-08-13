package model.mistforest.mistobj.activityboss;

import java.util.ArrayList;
import java.util.List;
import model.mistforest.MistConst;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.ProtoVector;

public class MistBornPosController {
    protected List<ProtoVector> emptyPosMap = new ArrayList<>();
    protected List<ProtoVector> usedPosMap = new ArrayList<>();

    public void init(int[][] posList) {
        if (null == posList || posList.length <= 0) {
            return;
        }
        for (int i = 0; i < posList.length; i++) {
            if (null == posList[i] || posList[i].length < 2) {
                continue;
            }
            ProtoVector.Builder posBuilder = ProtoVector.newBuilder();
            posBuilder.setX(posList[i][0]).setY(posList[i][1]);
            addEmptyPos(posBuilder.build());
        }
    }

    public void clear() {
        emptyPosMap.clear();
        usedPosMap.clear();
    }

    public int getEmptyPosCount() {
        return emptyPosMap.size();
    }

    public int getUsePosCount() {
        return usedPosMap.size();
    }

    public List<ProtoVector> getEmptyPosMap() {
        return emptyPosMap;
    }

    public void addEmptyPos(ProtoVector pos) {
        emptyPosMap.add(pos);
    }

    public ProtoVector getAndUseEmptyPos() {
        if (CollectionUtils.isEmpty(emptyPosMap)) {
            return null;
        }
        int rand = RandomUtils.nextInt(emptyPosMap.size());
        ProtoVector pos = emptyPosMap.get(rand);
        usedPosMap.add(pos);
        emptyPosMap.remove(rand);
        return pos;
    }

    public void returnUsedPos(ProtoVector pos) {
        emptyPosMap.add(pos);
        usedPosMap.removeIf(e -> MistConst.checkSamePos(e.getX(), e.getY(), pos.getX(), pos.getY()));
    }

    public void resetEmptyPos() {
        emptyPosMap.addAll(usedPosMap);
        usedPosMap.clear();
    }
}
