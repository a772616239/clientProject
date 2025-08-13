package petrobot.system.mistForest.obj.dynamicobj;

import lombok.Getter;
import lombok.Setter;
import petrobot.system.mistForest.obj.MistObj;
import petrobot.util.LogUtil;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;
import protocol.MistForest.UnitSnapShot;

@Getter
@Setter
public class MistDynamicObj extends MistObj {
    ProtoVector.Builder toward;
    boolean isMoving;

    @Override
    public void init(UnitMetadata metadata) {
        super.init(metadata);
        toward = ProtoVector.newBuilder().mergeFrom(metadata.getSnapShotData().getToward());
        setMoving(metadata.getSnapShotData().getIsMoving());
    }

    @Override
    public void clear() {
        super.clear();
        toward.clear();
        isMoving = false;
    }

    public void moveToVecPos(UnitSnapShot snapShot) {
        if (snapShot == null) {
            return;
        }
        moveToVecPos(snapShot.getPos(), snapShot.getToward(), snapShot.getIsMoving(), false);
    }

    public void moveToVecPos(ProtoVector newPos, ProtoVector toward, boolean isMoving, boolean reviseMiniPos) {
        if (getPos().getX() == newPos.getX() && getPos().getY() == newPos.getY()) {
            return;
        }
        if (getRobotMistForest() != null && getRobotMistForest().getWorldMap() != null) {
            getRobotMistForest().getWorldMap().removePosObj(this);
        } else {
            LogUtil.error(getRobotMistForest() == null ? "robot Mist is null" : "robot mist map is null");
        }

        if (reviseMiniPos) {
            int x = newPos.getX() / 1000 * 1000 + 500;
            int y = newPos.getY() / 1000 * 1000 + 500;
            getPos().setX(x);
            getPos().setY(y);
        } else {
            getPos().setX(newPos.getX());
            getPos().setY(newPos.getY());
        }

        getToward().setX(toward.getX());
        getToward().setY(toward.getY());
        setMoving(isMoving);

        if (getRobotMistForest() != null && getRobotMistForest().getWorldMap() != null) {
            getRobotMistForest().getWorldMap().addPosObj(this);
        }
    }
}
