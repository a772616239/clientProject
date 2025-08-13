package model.battle;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import util.LogUtil;

/**
 * 战斗控制器池子
 *
 * @author huhan
 * @date 2020/04/28
 */
public abstract class AbstractControllerPool {
    private final Map<BattleSubTypeEnum, ConcurrentLinkedQueue<AbstractBattleController>> controllerQueue
            = new ConcurrentHashMap<>();

    /**
     * 当前池子的战斗类型
     *
     * @return
     */
    public abstract BattleTypeEnum getBattleType();

    public AbstractBattleController getControllerByType(BattleSubTypeEnum subTypeEnum) {
        if (subTypeEnum == null || subTypeEnum == BattleSubTypeEnum.BSTE_Null) {
            LogUtil.error("getControllerByType, param is error, param：" + subTypeEnum + "cur subType=" + getBattleType());
            return null;
        }

        ConcurrentLinkedQueue<AbstractBattleController> queue = controllerQueue.computeIfAbsent(subTypeEnum, p -> new ConcurrentLinkedQueue<>());
        AbstractBattleController poll = queue.poll();

        if (poll == null) {
            poll = createController(subTypeEnum);
        }
        return poll;
    }

    /**
     * 创建battleController
     *
     * @param subTypeEnum
     * @return
     */
    public abstract AbstractBattleController createController(BattleSubTypeEnum subTypeEnum);


    /**
     * 回收战斗控制器
     *
     * @param controller
     */
    public void recycleController(AbstractBattleController controller) {
        if (controller == null || controller.getBattleType() != getBattleType()) {
            LogUtil.error("AbstractControllerPool.cycleController， error params , params type: "
                    + (controller == null ? null : controller.getBattleType()) + ", pool type:" + getBattleType());
            return;
        }
        controller.clear();

        ConcurrentLinkedQueue<AbstractBattleController> queue =
                controllerQueue.computeIfAbsent(controller.getSubBattleType(), p -> new ConcurrentLinkedQueue<>());

        queue.add(controller);
    }
}
