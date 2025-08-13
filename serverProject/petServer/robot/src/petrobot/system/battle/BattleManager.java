package petrobot.system.battle;

import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import java.util.List;
import java.util.Random;
import petrobot.robot.PlayerData;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.CS_EnterFight.Builder;
import protocol.GM.CS_GM;
import protocol.MainLine.MainLineProgress;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.ResCopy;

@Controller
public class BattleManager {

    @Index(value = IndexConst.BATTLE_LAST_STEP)
    public void battleLastStep(Robot robot) {
        if (robot == null) {
            return;
        }
        /**
         * // 无尽尖塔：第一个参数层数
         * 	// 外敌入侵：生成的Idx
         * 	// 主线闯关：第一个参数为NodeId
         * 	// 巡逻队：第一个参数为类型，第二个参数为是否激怒：0否1是，第三个x坐标，第四个y坐标
         * 	// 勇气试炼：第一个参数为选择关卡进度
         * 	// 资源副本：第一个参数为副本类型，第二个参数为关卡内关卡ID
         * 	// 积分副本：第一个参数为挑战的任务Id
         */
        Builder builder = CS_EnterFight.newBuilder();
        Random random = new Random();
        SyncExecuteFunction.executeConsumer(robot, r -> {
            PlayerData data = robot.getData();
            int battleType = 0;
            switch (battleType) {
                case 0: {
                    builder.setType(BattleSubTypeEnum.BSTE_EndlessSpire);
                    if (data.getCurSpireLv() > 500) {
                        CS_GM.Builder gmBuilder = CS_GM.newBuilder();
                        gmBuilder.setStr("spireLv|1");
                        robot.getClient().send(MsgIdEnum.CS_GM_VALUE, gmBuilder);
                        builder.addParamList(String.valueOf(1));
                    } else {
                        builder.addParamList(String.valueOf(data.getCurSpireLv() + 1));
                    }
                    break;
                }
                case 1: {
                    builder.setType(BattleSubTypeEnum.BSTE_ForeignInvasion);
                    break;
                }
                case 2: {
                    builder.setType(BattleSubTypeEnum.BSTE_MainLineCheckPoint);
                    MainLineProgress.Builder mainLinePro = data.getMainLinePro();
                    if (mainLinePro == null) {
                        builder.addParamList(String.valueOf(random.nextInt(100)));
                    } else {
                        builder.addParamList(String.valueOf(mainLinePro.getUnlockNodesList().get(random.nextInt(mainLinePro.getUnlockNodesCount()))));
                    }
                    break;
                }
                case 3: {
                    builder.setType(BattleSubTypeEnum.BSTE_Patrol);
                    break;
                }
                case 4: {
                    builder.setType(BattleSubTypeEnum.BSTE_BreaveChallenge);
                    break;
                }
                case 5: {
                    builder.setType(BattleSubTypeEnum.BSTE_ResourceCopy);
                    List<ResCopy> resCopies = data.getResCopies();
                    if (resCopies != null && !resCopies.isEmpty()) {
                        ResCopy resCopy = resCopies.get(random.nextInt(resCopies.size()));
                        builder.addParamList(String.valueOf(resCopy.getTypeValue()));
                        builder.addParamList(String.valueOf(resCopy.getUnlockProgressList().get(random.nextInt(resCopy.getProgressCount()))));
                    } else {
                        builder.addParamList(String.valueOf(random.nextInt(5)));
                        builder.addParamList(String.valueOf(random.nextInt(10)));
                    }
                    break;
                }
                case 6: {
                    builder.setType(BattleSubTypeEnum.BSTE_PointCopy);
                    builder.addParamList(String.valueOf(random.nextInt(10)));
                    break;
                }
                case 7: {
                    builder.setType(BattleSubTypeEnum.BSTE_BossTower);
                    BossTowerConfigObject object = BossTowerConfig.randomGet();
                    if (object != null) {
                        builder.addParamList(String.valueOf(object.getId()));
                        builder.addParamList(String.valueOf(object.getFightmakeid()));
                    }
                }
                default: {
                    break;
                }
            }
        });
        robot.getClient().send(MsgIdEnum.CS_EnterFight_VALUE, builder);
    }
}
