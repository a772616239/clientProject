package server.handler.cp.copy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import java.util.Map;
import model.cp.CpCopyManger;
import model.cp.CpTeamCache;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpCopyMapFloor;
import model.cp.entity.CpCopyMapPoint;
import model.cp.entity.CpDailyData;
import model.cp.entity.CpTeamCopyPlayerProgress;
import org.springframework.util.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_ClaimCpCopyInfo;
import protocol.CpFunction.SC_ClaimCpCopyInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 拉取副本详情
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimCpCopyInfo_VALUE)
public class ClaimCopyInfoHandler extends AbstractBaseHandler<CS_ClaimCpCopyInfo> {
    @Override
    protected CS_ClaimCpCopyInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimCpCopyInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCpCopyInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        CpCopyMap data = CpCopyManger.getInstance().findMapDataByPlayerId(playerIdx);

        SC_ClaimCpCopyInfo.Builder msg = setData(playerIdx, data);

        CpCopyManger.getInstance().loginCpTeamCopy(playerIdx);

        gsChn.send(MsgIdEnum.SC_ClaimCpCopyInfo_VALUE, msg);

    }

    private SC_ClaimCpCopyInfo.Builder setData(String playerIdx, CpCopyMap data) {
        SC_ClaimCpCopyInfo.Builder msg = SC_ClaimCpCopyInfo.newBuilder();
        if (data == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CP_CopyNotExists));
            return msg;
        }
        for (CpCopyMapFloor floorData : data.getFloors().values()) {
            msg.addFloors(buildFloorVo(floorData));
        }
        for (CpTeamCopyPlayerProgress progress : data.getProgress().values()) {
            msg.addPlayers(buildPlayerVo(progress, data.getPlayerState()));
        }
        List<Integer> claimedRewards = data.getAlreadyClaimRewardId().get(playerIdx);
        if (!CollectionUtils.isEmpty(claimedRewards)) {
            msg.addAllClaimedRewardIndex(claimedRewards);
        }

        CpDailyData playerDailyData = CpCopyManger.getInstance().findPlayerDailyData(playerIdx);

        msg.setFreeReviveTimes(playerDailyData.getFreeReviveNum());

        msg.setBuyReviveTimes(playerDailyData.getBuyReviveNum());

        msg.setExpireTime(CpTeamCache.getInstance().loadPlayerMapExpire(data.getMapId()));

        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        return msg;

    }

    private CpFunction.CpCopyPlayer.Builder buildPlayerVo(CpTeamCopyPlayerProgress progress, Map<String, CpFunction.CpCopyPlayerState> playerState) {
        CpFunction.CpCopyPlayer.Builder playerVo = CpFunction.CpCopyPlayer.newBuilder();
        playerVo.setPlayerIdx(progress.getPlayerIdx());
        playerVo.setHeader(progress.getHeader());
        playerVo.setBorderId(progress.getBorderId());
        playerVo.setStarScore(progress.getStarScore());
        playerVo.setPlayerName(progress.getPlayerName());
        playerVo.addAllPassPoint(progress.getPassPointIds());
        if (!CpFunctionUtil.isRobot(progress.getPlayerIdx())) {
            playerVo.setIsLeave(progress.isFinish());
        }
        CpFunction.CpCopyPlayerState state = playerState.get(progress.getPlayerIdx());
        if (state != null) {
            playerVo.setState(state);
        }
        playerVo.setReviveTimes(CpCopyManger.getInstance().queryPlayerReviveTime(playerVo.getPlayerIdx()));
        playerVo.setLimitReviveTimes(CpCopyManger.getInstance().queryLimitReviveTimes(playerVo.getPlayerIdx()));
        return playerVo;
    }

    private CpFunction.CpCopyFloor.Builder buildFloorVo(CpCopyMapFloor floorData) {
        CpFunction.CpCopyFloor.Builder floor = CpFunction.CpCopyFloor.newBuilder();
        floor.setFloor(floorData.getFloor());
        for (CpCopyMapPoint point : floorData.getPoints().values()) {
            addFloorPoint(floor, point);
        }
        return floor;
    }

    private void addFloorPoint(CpFunction.CpCopyFloor.Builder floor, CpCopyMapPoint point) {
        CpFunction.CpCopyPoint.Builder pointVo = CpFunction.CpCopyPoint.newBuilder();
        pointVo.setPointId(point.getId());
        pointVo.setEvent(CpFunction.CpFunctionEvent.forNumber(point.getPointType()));
        pointVo.setDifficult(point.getDifficulty());
        floor.addPoints(pointVo);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimCpCopyInfo_VALUE, SC_ClaimCpCopyInfo.newBuilder().setRetCode(retCode));
    }
}
