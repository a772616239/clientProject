package petrobot.system.mistForest.obj.dynamicobj;

import lombok.Getter;
import lombok.Setter;
import petrobot.system.mistForest.MistConst;
import petrobot.tick.GlobalTick;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ClientEventInvoke;
import protocol.MistForest.ClientEventEnum;
import protocol.MistForest.Event_AbsorbTreasureBag;
import protocol.MistForest.Event_SendSnapShot;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;
import protocol.MistForest.UnitSnapShot;

import java.util.List;
import java.util.Random;

@Getter
@Setter
public class MistFighter extends MistDynamicObj {
    protected ProtoVector.Builder nextPos;

    protected long moveTimeStamp;

    protected boolean loadFinished;

    @Override
    public void init(UnitMetadata metadata) {
        super.init(metadata);
        loadFinished = true;
        LogUtil.debug("robot init mist finished,pos=" + pos.getX() + "," + pos.getY());
    }

    @Override
    public void clear() {
        super.clear();
        nextPos = null;
        moveTimeStamp = 0;
    }

    public boolean isBattling() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId) != 0;
    }

    public boolean isUnderControl() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_IsUnderControl) > 0;
    }

    public boolean isBornProtected() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected) > 0;
    }

    public void move() {
        if (isBattling() || isUnderControl() || getRobotMistForest() == null) {
            return;
        }
        if (getRobotMistForest().getFighter().getId() != getId()) {
            return;
        }
        if (getRobotMistForest().getOwner().getData().getBattleId() > 0) {
            return;
        }
        if (nextPos == null || MistConst.isSamePos(pos.getX() / 1000, pos.getY() / 1000, nextPos.getX(), nextPos.getY())) {
            calcNextPos();
//            touchObj();
        }
        if (nextPos != null) {
            toward = MistConst.calcStanderCoordVector(getPos().getX() / 1000, getPos().getY() / 1000, nextPos.getX(), nextPos.getY());
            long curTime = GlobalTick.getInstance().getCurrentTime();
            if (moveTimeStamp > 0) {
                long moveTime = curTime - moveTimeStamp;
                long speed = getAttribute(MistUnitPropTypeEnum.MUPT_Speed);
                speed = Math.min(speed, 1000);
                int moveX = (int) (moveTime * speed * toward.getX() / 10) / 1000;
                int moveY = (int) (moveTime * speed * toward.getY() / 10) / 1000;

                int oldX = getPos().getX();
                int oldY = getPos().getY();
                int oldSvrX = oldX / 1000;
                int oldSvrY = oldY / 1000;

                int maxX = getRobotMistForest().getWorldMap().getWeight();
                int maxY = getRobotMistForest().getWorldMap().getHeight();
                int newSvrX = Math.min(maxX - 1, Math.max(0, (oldX + moveX) / 1000));
                int newSvrY = Math.min(maxY - 1, Math.max(0, (oldY + moveY) / 1000));

                UnitSnapShot.Builder snapShot = UnitSnapShot.newBuilder();
                snapShot.setUnitId(getId());
                snapShot.getPosBuilder().setX(Math.min(maxX * 1000 - 1, Math.max(0, oldX + moveX)));
                snapShot.getPosBuilder().setY(Math.min(maxY * 1000 - 1, Math.max(0, oldY + moveY)));
                snapShot.setToward(toward);
                snapShot.setIsMoving(true);
                sendSnapShot(snapShot);

                if (oldSvrX != newSvrX || oldSvrY != newSvrY) {
                    moveToVecPos(snapShot.build());
                    touchObj();
                }

                StringBuilder strBuilder = new StringBuilder("Robot move");
                strBuilder.append(",name=" + getRobotMistForest().getOwner().getLoginName());
                strBuilder.append(",moveTime=" + moveTime);
                strBuilder.append(",towards=" + toward.getX() + "," + toward.getY());
                strBuilder.append("\ntargetPos=(" + nextPos.getX() + "," + nextPos.getY() + ")");
                strBuilder.append("\noldPos=(" + oldX + "," + oldY + "),");
                strBuilder.append("oldSvrPos=(" + oldSvrX + "," + oldSvrY + ")\n");
                strBuilder.append("newPos=(" + getPos().getX() + "," + getPos().getY() + "),");
                strBuilder.append("newSvrPos=(" + newSvrX + "," + newSvrY + "),");
                LogUtil.debug(strBuilder.toString());
            }
            moveTimeStamp = curTime;
        }
    }

    public void calcNextPos() {
        int toward = new Random().nextInt(8); // 8个方向
        nextPos = getNextValidPos(toward, 0);
        LogUtil.debug("calcNextPos = " + (nextPos != null ? nextPos.getX() + "," + nextPos.getY() : ""));
    }

    public ProtoVector.Builder getNextValidPos(int toward, int count) {
        if (count > 10) {
            toward = -1;
        }
        switch (toward) {
            case 0:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000 + 1, getPos().getY() / 1000)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000 + 1).setY(getPos().getY() / 1000);
                } else {
                    return getNextValidPos(1, count + 1);
                }
            case 1:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000 + 1, getPos().getY() / 1000 + 1)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000 + 1).setY(getPos().getY() / 1000 + 1);
                } else {
                    return getNextValidPos(2, count + 1);
                }
            case 2:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000, getPos().getY() / 1000 + 1)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000).setY(getPos().getY() / 1000 + 1);
                } else {
                    return getNextValidPos(3, count + 1);
                }
            case 3:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000 - 1, getPos().getY() / 1000 + 1)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000 - 1).setY(getPos().getY() / 1000 + 1);
                } else {
                    return getNextValidPos(4, count + 1);
                }
            case 4:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000 - 1, getPos().getY() / 1000)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000 - 1).setY(getPos().getY() / 1000);
                } else {
                    return getNextValidPos(5, count + 1);
                }
            case 5:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000 - 1, getPos().getY() / 1000 - 1)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000 - 1).setY(getPos().getY() / 1000 - 1);
                } else {
                    return getNextValidPos(6, count + 1);
                }
            case 6:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000, getPos().getY() / 1000 - 1)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000).setY(getPos().getY() / 1000 - 1);
                } else {
                    return getNextValidPos(7, count + 1);
                }
            case 7:
                if (getRobotMistForest().getWorldMap().isPosReachable(getPos().getX() / 1000 + 1, getPos().getY() / 1000 - 1)) {
                    return ProtoVector.newBuilder().setX(getPos().getX() / 1000 + 1).setY(getPos().getY() / 1000 - 1);
                } else {
                    return getNextValidPos(0, count + 1);
                }
            default:
                return null;
        }
    }

    public void touchObj() {
        if (isBornProtected()) {
            return;
        }
        //　暂时只处理资源点
        List<Long> bagList = getNearBagList();
        sendAbsorbBagList(bagList);
    }

    public List<Long> getNearBagList() {
        List<Long> bagList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000, getPos().getY() / 1000);
        List<Long> tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000 + 1, getPos().getY() / 1000);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000 + 1, getPos().getY() / 1000 + 1);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000, getPos().getY() / 1000 + 1);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000 - 1, getPos().getY() / 1000 + 1);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000 - 1, getPos().getY() / 1000);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000 - 1, getPos().getY() / 1000 - 1);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000, getPos().getY() / 1000 - 1);
        bagList = mergeBagList(bagList, tmpList);
        tmpList = getRobotMistForest().getWorldMap().getBagsByPos(getPos().getX() / 1000 + 1, getPos().getY() / 1000 - 1);
        bagList = mergeBagList(bagList, tmpList);
        return bagList;
    }

    protected List<Long> mergeBagList(List<Long> list1, List<Long> list2) {
        if (list1 == null) {
            return list2;
        } else if (list2 == null) {
            return list1;
        } else {
            list1.addAll(list2);
            return list1;
        }
    }

    public void sendSnapShot(UnitSnapShot.Builder snapShot) {
        CS_ClientEventInvoke.Builder builder = CS_ClientEventInvoke.newBuilder();
        builder.setEventType(ClientEventEnum.CET_SendSnapShot);
        builder.setEventData(Event_SendSnapShot.newBuilder().setSnapShot(snapShot).build().toByteString());
        getRobotMistForest().getOwner().getClient().send(MsgIdEnum.CS_ClientEventInvoke_VALUE, builder);
    }

    public void sendAbsorbBagList(List<Long> bagList) {
        if (bagList == null) {
            return;
        }
        CS_ClientEventInvoke.Builder builder = CS_ClientEventInvoke.newBuilder();
        builder.setEventType(ClientEventEnum.CET_AbsorbTreasureBag);

        Event_AbsorbTreasureBag.Builder absorbBagEvent = Event_AbsorbTreasureBag.newBuilder();
        absorbBagEvent.setTakerId(getId());
        absorbBagEvent.addAllAbsorbBagId(bagList);

        builder.setEventData(absorbBagEvent.build().toByteString());
        getRobotMistForest().getOwner().getClient().send(MsgIdEnum.CS_ClientEventInvoke_VALUE, builder);
    }

    @Override
    public void onTick() {
        super.onTick();
        move();
    }

}
