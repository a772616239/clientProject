package model.warpServer.crossServer.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import platform.logs.LogService;
import platform.logs.entity.thewar.TheWarAttackGridLog;
import platform.logs.entity.thewar.TheWarJobTileLog;
import platform.logs.entity.thewar.TheWarLevelUpTechLog;
import platform.logs.entity.thewar.TheWarTroopsGridLog;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateTheWarTargetPro;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

/**
 * @author huhan
 * @date 2020/11/30
 */
@MsgId(msgId = MsgIdEnum.CS_GS_UpdateTheWarTargetPro_VALUE)
public class UpdateTheWarTargetProHandler extends AbstractHandler<CS_GS_UpdateTheWarTargetPro> {
    @Override
    protected CS_GS_UpdateTheWarTargetPro parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateTheWarTargetPro.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateTheWarTargetPro req, int i) {
        EventUtil.triggerUpdateTargetProgress(req.getPlayerIdx(), req.getTargetType(), req.getAddPro(), req.getParam());
        addTheWarLog(req.getPlayerIdx(), req.getTargetType(), req.getAddPro(), req.getParam(), req.getLogParamList());
    }

    protected void addTheWarLog(String playerIdx, TargetTypeEnum targetType, int addPro, int param, List<String> extraParam) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String mapName = TheWarManager.getInstance().getMapName();
        switch (targetType.getNumber()) {
            case TargetTypeEnum.TTE_TheWar_JobTileLvReach_VALUE: {
                LogService.getInstance().submit(new TheWarJobTileLog(player, mapName, addPro));
                break;
            }
            case TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_Common_VALUE: {
                LogService.getInstance().submit(new TheWarTroopsGridLog(player, TheWarManager.getInstance().getMapName(), "普通领地", param));
                break;
            }
            case TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_WarGold_VALUE: {
                LogService.getInstance().submit(new TheWarTroopsGridLog(player, TheWarManager.getInstance().getMapName(), "远征币领地", param));
                break;
            }
            case TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_HolyWater_VALUE: {
                LogService.getInstance().submit(new TheWarTroopsGridLog(player, TheWarManager.getInstance().getMapName(), "圣水领地", param));
                break;
            }
            case TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_DpResource_VALUE: {
                LogService.getInstance().submit(new TheWarTroopsGridLog(player, TheWarManager.getInstance().getMapName(), "开门资源领地", param));
                break;
            }
            case TargetTypeEnum.TTE_TheWar_CumuStationTroops_BossGrid_VALUE: {
                LogService.getInstance().submit(new TheWarTroopsGridLog(player, TheWarManager.getInstance().getMapName(), "要塞领地", param));
                break;
            }
            case TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_Common_VALUE: {
                if (extraParam.size() > 4) {
                    String posX = extraParam.get(0);
                    String posY = extraParam.get(1);
                    String ownerName = extraParam.get(2);
                    String isTroops = extraParam.get(3);
                    String attackResult = extraParam.get(4);
                    LogService.getInstance().submit(new TheWarAttackGridLog(player, mapName, "普通领地",
                            param, posX, posY, ownerName, isTroops, attackResult));
                }
                break;
            }
            case TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_WarGold_VALUE: {
                if (extraParam.size() > 4) {
                    String posX = extraParam.get(0);
                    String posY = extraParam.get(1);
                    String ownerName = extraParam.get(2);
                    String isTroops = extraParam.get(3);
                    String attackResult = extraParam.get(4);
                    LogService.getInstance().submit(new TheWarAttackGridLog(player, mapName, "远征币领地",
                            param, posX, posY, ownerName, isTroops, attackResult));
                }
                break;
            }
            case TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_HolyWater_VALUE: {
                if (extraParam.size() > 4) {
                    String posX = extraParam.get(0);
                    String posY = extraParam.get(1);
                    String ownerName = extraParam.get(2);
                    String isTroops = extraParam.get(3);
                    String attackResult = extraParam.get(4);
                    LogService.getInstance().submit(new TheWarAttackGridLog(player, mapName, "圣水领地",
                            param, posX, posY, ownerName, isTroops, attackResult));
                }
                break;
            }
            case TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_DpResource_VALUE: {
                if (extraParam.size() > 4) {
                    String posX = extraParam.get(0);
                    String posY = extraParam.get(1);
                    String ownerName = extraParam.get(2);
                    String isTroops = extraParam.get(3);
                    String attackResult = extraParam.get(4);
                    LogService.getInstance().submit(new TheWarAttackGridLog(player, mapName, "开门资源领地",
                            param, posX, posY, ownerName, isTroops, attackResult));
                }
                break;
            }
            case TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_BossGrid_VALUE: {
                if (extraParam.size() > 4) {
                    String posX = extraParam.get(0);
                    String posY = extraParam.get(1);
                    String ownerName = extraParam.get(2);
                    String isTroops = extraParam.get(3);
                    String attackResult = extraParam.get(4);
                    LogService.getInstance().submit(new TheWarAttackGridLog(player, mapName, "要塞领地",
                            param, posX, posY, ownerName, isTroops, attackResult));
                }
                break;
            }

            case TargetTypeEnum.TTE_TheWar_TechLevelUp_VALUE: {
                LogService.getInstance().submit(new TheWarLevelUpTechLog(player, mapName, param, addPro));
                break;
            }
            default:
                break;
        }
    }
}
