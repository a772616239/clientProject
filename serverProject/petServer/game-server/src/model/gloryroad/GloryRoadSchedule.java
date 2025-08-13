package model.gloryroad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import protocol.GameplayDB.EnumGloryRoadOperateType;
import protocol.GloryRoad.EnumGloryRoadSchedule;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2021/3/15
 */
@Getter
@Setter
@NoArgsConstructor
public class GloryRoadSchedule {
    private EnumGloryRoadSchedule schedule;
    private long startTime;
    private long endTime;

    /**
     * 待操作的列表
     */
    private List<EnumGloryRoadOperateType> operateList;

    public GloryRoadSchedule(EnumGloryRoadSchedule schedule, long startTime, long endTime) {
        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void addOperate(EnumGloryRoadOperateType operate) {
        if (operate == null || operate == EnumGloryRoadOperateType.EGROT_NULL) {
            return;
        }
        addAllOperate(Collections.singletonList(operate));
    }

    public synchronized void addAllOperate(List<EnumGloryRoadOperateType> operateList) {
        if (CollectionUtils.isEmpty(operateList)) {
            return;
        }

        if (this.operateList == null) {
            this.operateList = new ArrayList<>();
        }
        this.operateList.addAll(operateList);
    }

    public synchronized EnumGloryRoadOperateType popOperate() {
        if (operateIsEmpty()) {
//            LogUtil.info("GloryRoadSchedule.popOperate, cur schedule:" + this.schedule + "operate list is empty");
            return null;
        }
        EnumGloryRoadOperateType remove = this.operateList.remove(0);
//        LogUtil.info("GloryRoadSchedule.popOperate, cur schedule:" + this.schedule + ", pop operate:" + remove);
        return remove;
    }

    public boolean operateIsEmpty() {
        return this.operateList == null || this.operateList.isEmpty();
    }

    @Override
    public String toString() {
        String formatTimeZone = "UTC+8";
        return "GloryRoadSchedule{" +
                "schedule=" + schedule +
                ", startTime=" + TimeUtil.formatStampByZoneName(startTime, formatTimeZone) +
                ", endTime=" + TimeUtil.formatStampByZoneName(endTime, formatTimeZone) +
                ", operateList=" + GameUtil.collectionToString(operateList) +
                '}';
    }
}
