package model.warpServer.crossServer.handler.mistforest;

import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import platform.logs.entity.MistPlayTimeLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistAlchemyData;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_JoinMistForest;
import protocol.MistForest.SC_MistForestRoomInfo;
import protocol.ServerTransfer.CS_GS_JoinMistForest;
import protocol.ServerTransfer.EnumJoinMistForestType;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_JoinMistForest_VALUE)
public class JoinMistRoomHandler extends AbstractHandler<CS_GS_JoinMistForest> {
    @Override
    protected CS_GS_JoinMistForest parse(byte[] bytes) throws Exception {
        return CS_GS_JoinMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_JoinMistForest ret, int i) {
        String playerId = ret.getPlayerId();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }

        String ipPort = gsChn.channel.remoteAddress().toString().substring(1);
        if (ret.getRetCode() == MistRetCode.MRC_Success) {
            int serverIndex = CrossServerManager.getInstance().getServerIndexByCsAddr(ipPort);
            CrossServerManager.getInstance().addMistForestPlayer(playerId, ret.getMistRule(), serverIndex);
            int mistLevel = Math.max(0, ret.getMistForestLevel());
            if (mistLevel < 1000) {
                afterJoinMistAffairs(player, ret, mistLevel);
            }
        } else {
            if (ret.getJoinType() == EnumJoinMistForestType.EJFT_ExchangeJoin) {
                GlobalData.getInstance().sendMsg(
                        player.getIdx(), MsgIdEnum.SC_MistForestRoomInfo_VALUE, SC_MistForestRoomInfo.newBuilder());
            }
        }

        if (ret.getJoinType() == EnumJoinMistForestType.EJFT_InitJoin) {
            SC_JoinMistForest.Builder builder = SC_JoinMistForest.newBuilder();
            builder.setRetCode(ret.getRetCode());
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_JoinMistForest_VALUE, builder);
        }
    }

    public void afterJoinMistAffairs(playerEntity player, CS_GS_JoinMistForest ret, int mistLevel) {
        boolean removeTicket = false;
        int itemId = 0;
        RewardSourceEnum reason = RewardSourceEnum.RSE_MistForest;
        switch (ret.getMistRule()) {
            case EMRK_Common: {
                //成就：迷雾深林阶层
                EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_MistLevel, mistLevel, 0);
                //目标：累积进入x次迷雾深林
                EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_Mist_CumuEnterMist, 1, 0);
                //统计：玩法
                LogService.getInstance().submit(new GamePlayLog(player.getIdx(), EnumFunction.MistForest));

                SyncExecuteFunction.executeConsumer(player, entity -> {
                    if (!entity.getDb_data().getMistForestData().getFirstEnterMistFlag()) {
                        entity.getDb_data().getMistForestDataBuilder().setFirstEnterMistFlag(true);
                    }
                    if (mistLevel > entity.getDb_data().getMistForestData().getLastEnterMistLevel()) {
                        entity.getDb_data().getMistForestDataBuilder().setLastEnterMistLevel(mistLevel);
                        if (entity.getDb_data().getMistForestData().getAlchemyDataCount() > 0) {
                            List<Reward> rewardList = new ArrayList<>();
                            Reward.Builder reward = Reward.newBuilder();
                            for (MistAlchemyData data : entity.getDb_data().getMistForestData().getAlchemyDataList()) {
                                reward.setRewardType(RewardTypeEnum.RTE_Item).setId(data.getExchangeRewardId()).setCount(1);
                                rewardList.add(reward.build());
                            }
                            RewardManager.getInstance().doRewardByList(entity.getIdx(), rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest), false);
                            entity.getDb_data().getMistForestDataBuilder().clearAlchemyData();
                        }
                        entity.getDb_data().getMistForestDataBuilder().clearVipSkillData();
                        entity.getDb_data().getMistForestDataBuilder().addAllVipSkillData(ret.getVipSkillDataList());
                        entity.getDb_data().getMistForestDataBuilder().clearEliteMonsterRewardTimes();
                    }
                    entity.setLastEnterMistTime(GlobalTick.getInstance().getCurrentTime());
                    LogService.getInstance().submit(new MistPlayTimeLog(entity.getIdx(), mistLevel, entity.getDb_data().getMistForestData().getStamina(), true));
                });

                targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(player.getIdx());
                if (targetEntity != null) {
                    SyncExecuteFunction.executeConsumer(targetEntity, entity -> entity.updateMistTargetMissionWithLevelCheck(mistLevel));
                }
                return;
            }
            case EMRK_Maze: {
//                int ticket = player.getDb_data().getMazeData().getFreeTickets();
//                if (ticket > 0) {
//                    removeTicket = SyncExecuteFunction.executePredicate(player, entity -> {
//                        entity.getDb_data().getMazeDataBuilder().setFreeTickets(ticket - 1);
//                        return true;
//                    });
//                }
//                itemId = GameConst.MazeTicketItemId;

                removeTicket = true;
                reason = RewardSourceEnum.RSE_MistMaze;

                //统计：玩法
                LogService.getInstance().submit(new GamePlayLog(player.getIdx(), EnumFunction.MistMaze));
                break;
            }
            case EMRK_GhostBuster: {
                int ticket = player.getDb_data().getGhostBusterData().getFreeTickets();
                if (ticket > 0) {
                    removeTicket = SyncExecuteFunction.executePredicate(player, entity -> {
                        entity.getDb_data().getGhostBusterDataBuilder().setFreeTickets(ticket - 1);
                        return true;
                    });
                }
                itemId = GameConst.GhostBusterTicketItemId;
                reason = RewardSourceEnum.RSE_MistGhostBuster;

                //统计：玩法
                LogService.getInstance().submit(new GamePlayLog(player.getIdx(), EnumFunction.MistGhostBuster));
                break;
            }
            default:
                break;
        }
        if (removeTicket) {
            player.sendMistFreeTickets();
        } else {
            Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE, itemId, 1);
            if (!ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume,
                    ReasonManager.getInstance().borrowReason(reason, "迷雾森林入场"))) {
                LogUtil.error("MistForest consume error, remove MistForestTicketItem failed,rule=" + ret.getMistRule());
            }
        }
    }
}
