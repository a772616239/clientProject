package model.warpServer.crossServer.handler.arena;

import cfg.ArenaRobotConfig;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.arena.ArenaUtil;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Activity;
import protocol.Arena;
import protocol.Arena.SC_ClaimArenaRanking;
import protocol.Common.LanguageEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TransArenaInfo;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/12
 */
@MsgId(msgId = MsgIdEnum.CS_GS_TransArenaInfo_VALUE)
public class TransArenaInfoHandler extends AbstractHandler<CS_GS_TransArenaInfo> {
    @Override
    protected CS_GS_TransArenaInfo parse(byte[] bytes) throws Exception {
        return CS_GS_TransArenaInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TransArenaInfo req, int i) {
        try {
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), req.getMsgId(), settleMsg(req));
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }


    private Builder<?> settleMsg(CS_GS_TransArenaInfo req) throws InvalidProtocolBufferException {
        if (req == null) {
            return null;
        }
        if (req.getMsgId() == MsgIdEnum.SC_ClaimRanking_VALUE) {
            return settleHonorWallRanking(req);
        }
        return null;
    }

    private Activity.SC_ClaimRanking.Builder settleHonorWallRanking(CS_GS_TransArenaInfo req) throws InvalidProtocolBufferException {
        SC_ClaimArenaRanking.Builder builder = settleArenaRanking(req);
        return rankingBuilder2CommonRankMsg(builder);
    }

    /**
     * 处理排行榜数据
     *
     * @param req
     * @return
     */
    private SC_ClaimArenaRanking.Builder settleArenaRanking(CS_GS_TransArenaInfo req) throws InvalidProtocolBufferException {
        SC_ClaimArenaRanking.Builder rankingBuilder = SC_ClaimArenaRanking.parseFrom(req.getMsgData()).toBuilder();
        //更新玩家排行信息
        arenaEntity entity = arenaCache.getByIdx(req.getPlayerIdx());
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                e.getDbBuilder().setRanking(rankingBuilder.getPlayerRanking());
                entity.getDbBuilder().setLastClaimRankingTime(GlobalTick.getInstance().getCurrentTime());

                //小于0是未上榜
                if (rankingBuilder.getPlayerRanking() > 0) {
                    EventUtil.triggerUpdateTargetProgress(req.getPlayerIdx(), TargetTypeEnum.TEE_Arena_DanReach, e.getDbBuilder().getDan(), rankingBuilder.getPlayerRanking());
                }
            });
        }
        //竞技场排行榜机器人多语言处理
        LanguageEnum language = PlayerUtil.queryPlayerLanguage(req.getPlayerIdx());
        if (language != LanguageEnum.LE_SimpleChinese) {
            Map<Integer, String> robotCfgIdNameMap = new HashMap<>();
            rankingBuilder.getRankingInfoBuilderList()
                    .forEach(e -> {
                        int robotCfgId = e.getSimpleInfo().getRobotCfgId();
                        if (language == null || ArenaRobotConfig.getById(robotCfgId) == null) {
                            return;
                        }

                        //获取名字
                        String newName = robotCfgIdNameMap.get(robotCfgId);
                        if (StringUtils.isEmpty(newName)) {
                            newName = ArenaUtil.getRobotName(robotCfgId, language);
                        }

                        if (StringUtils.isNotBlank(newName)) {
                            robotCfgIdNameMap.put(robotCfgId, newName);
                            e.getSimpleInfoBuilder().setName(newName);
                        }
                    });

        }
        return rankingBuilder;

    }

    private static Activity.SC_ClaimRanking.Builder rankingBuilder2CommonRankMsg(SC_ClaimArenaRanking.Builder rankingBuilder) {
        if (rankingBuilder == null) {
            return null;
        }
        Activity.SC_ClaimRanking.Builder builder = Activity.SC_ClaimRanking.newBuilder();
        builder.setRetCode(rankingBuilder.getRetCode());
        builder.setPlayerRanking(rankingBuilder.getPlayerRanking());
        builder.setPlayerScore(rankingBuilder.getPlayerScore());
        builder.setDan(rankingBuilder.getDan());
        builder.addAllRankingInfo(arenaRanking2PlayerRankingList(rankingBuilder.getRankingInfoList()));
        return builder;
    }

    private static List<Activity.PlayerRankingInfo> arenaRanking2PlayerRankingList(List<Arena.ArenaRankingPlayerInfo> rankingInfoList) {
        if (CollectionUtils.isEmpty(rankingInfoList)) {
            return Collections.emptyList();
        }
        List<Activity.PlayerRankingInfo> result = new ArrayList<>();
        for (Arena.ArenaRankingPlayerInfo arenaInfo : rankingInfoList) {
            Arena.ArenaPlayerSimpleInfo simpleInfo = arenaInfo.getSimpleInfo();
            Activity.PlayerRankingInfo.Builder builder = Activity.PlayerRankingInfo.newBuilder();
            builder.setPlayerIdx(simpleInfo.getPlayerIdx());
            builder.setPlayerName(simpleInfo.getName());
            builder.setPlayerLv(simpleInfo.getLevel());
            builder.setPlayerVipLv(simpleInfo.getVipLv());
            builder.setTitleId(simpleInfo.getTitleId());
            builder.setPlayerAvatar(simpleInfo.getAvatar());
            builder.setAvatarBorder(simpleInfo.getAvatarBorder());
            builder.setAvatarBorderRank(simpleInfo.getAvatarBorderRank());
            builder.setRanking(arenaInfo.getRanking());
            builder.setRankingScore(simpleInfo.getScore());
            builder.setServerIndex(simpleInfo.getServerIndex());
            builder.setNewTitleId(simpleInfo.getTitleId());
            Activity.RankingExInfo.Builder exInfo = Activity.RankingExInfo.newBuilder();
            Activity.ArenaRankingExInfo.Builder detail = Activity.ArenaRankingExInfo.newBuilder();
            detail.setDan(simpleInfo.getDan());
            detail.setFightAbility(simpleInfo.getFightAbility());
            detail.setShowPetId(simpleInfo.getShowPetId());
            exInfo.setExInfoEnum(Activity.RankingExInfoEnum.REIE_Arena);
            exInfo.setDetail(detail.build().toByteString());
            builder.setExInfo(exInfo);
            result.add(builder.build());
        }
        return result;
    }
}
