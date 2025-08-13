package petrobot.system.patrol;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_EnterFight;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_BuyPatrolGoods;
import protocol.Patrol.CS_PatrolExplore;
import protocol.Patrol.CS_PatrolIfFinish;
import protocol.Patrol.CS_PatrolInit;
import protocol.Patrol.CS_PatrolTrigger;
import protocol.Patrol.PatrolPoint;
import protocol.Patrol.PatrolSearchEvent;
import protocol.Patrol.SaleManGoods;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
//@Controller
public class PatrolManager {
    @Index(IndexConst.PATROL_FINISH_CHECK)
    public void patrolFinishCheck(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_PatrolIfFinish_VALUE, CS_PatrolIfFinish.newBuilder());
    }

    @Index(IndexConst.PATROL_INIT)
    public void init(Robot robot) {
        if (!robot.getData().isPatrolFinish()){
            robot.getClient().send(MsgIdEnum.CS_PatrolFinish_VALUE, CS_PatrolIfFinish.newBuilder());
        }
        robot.getClient().send(MsgIdEnum.CS_PatrolInit_VALUE, CS_PatrolInit.newBuilder());
    }

    @Index(IndexConst.PATROL_PLAY)
    public void play(Robot robot) {
        PatrolPoint location = robot.getData().getPatrolStatus().getLocation();
        boolean next = false;
        for (PatrolPoint patrolPoint : robot.getData().getPatrolMap().getMainLine().getPointList()) {
            if (location.getX() == patrolPoint.getX() && location.getY() == location.getY()) {
                next = true;
                continue;
            }
            if (patrolPoint.getExplored() == 1) {
                continue;
            }
            if (next) {
                switch (patrolPoint.getMapEnum()) {
                    case EXPLORE:
                        robot.getClient().send(MsgIdEnum.CS_PatrolExplore_VALUE, CS_PatrolExplore.newBuilder().setLocation(patrolPoint).setEvent(PatrolSearchEvent.explore2));
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case BASTARD:
                    case BOSS:
                        List<String> strings = new ArrayList<>();
                        strings.add(String.valueOf(BattleSubTypeEnum.BSTE_Patrol));
                        strings.add(String.valueOf(0));
                        strings.add(String.valueOf(patrolPoint.getX()));
                        strings.add(String.valueOf(patrolPoint.getY()));
                        robot.getClient().send(MsgIdEnum.CS_EnterFight_VALUE, CS_EnterFight.newBuilder().addAllParamList(strings).setType(BattleSubTypeEnum.BSTE_Patrol));
                        try {
                            Thread.sleep(30 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        robot.getClient().send(MsgIdEnum.CS_BattleResult_VALUE, CS_BattleResult.newBuilder().setIsGMEnd(true).setBattleId(robot.getData().getBattleId()).setWinnerCamp(1));

                        break;
                    case BOSS_KEY:
                        robot.getClient().send(MsgIdEnum.CS_PatrolTrigger_VALUE, CS_PatrolTrigger.newBuilder().setLocation(patrolPoint));
                    case TRAVELING_SALESMAN:
                        robot.getClient().send(MsgIdEnum.CS_PatrolTrigger_VALUE, CS_PatrolTrigger.newBuilder().setLocation(patrolPoint));
                        List<SaleManGoods> goodsListList = robot.getData().getPatrolStatus().getSaleMan().getGoodsListList();
                        for (SaleManGoods goods : goodsListList) {
                            try {
                                Thread.sleep(2 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            robot.getClient().send(MsgIdEnum.CS_BuyPatrolGoods_VALUE, CS_BuyPatrolGoods.newBuilder().setGoodsId(goods.getGoodsId()));
                        }
                        break;
                    default:
                        break;
                }
                //更新地图信息
                robot.getClient().send(MsgIdEnum.CS_PatrolInit_VALUE, CS_PatrolInit.newBuilder());
            }
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
