package server.handler.battle;

import common.AbstractBaseHandler;
import common.FunctionExclusion;
import common.GlobalData;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.FunctionManager;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.SC_EnterFight;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ReqOccupyGrid;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_EnterFight_VALUE)
public class EnterBattleHandler extends AbstractBaseHandler<CS_EnterFight> {

    @Override
    protected CS_EnterFight parse(byte[] bytes) throws Exception {
        return CS_EnterFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EnterFight req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.debug("EnterBattleHandler, playerIdx[ " + playerIdx + "Entity is null");
            return;
        }
        LogUtil.info("player:{} request enter fight ,req:{}", playerIdx, req);
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            SC_EnterFight.Builder retBuilder = SC_EnterFight.newBuilder();
            retBuilder.setRetCode(GameUtil.buildRetCode(FunctionExclusion.getInstance().getRetCodeByType(sf)));
            gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, retBuilder);
            return;
        }
        if (req.getType() != BattleSubTypeEnum.BSTE_TheWar) {
            BattleManager.getInstance().enterPveBattle(playerIdx, req);
        } else {
            if (!FunctionManager.getInstance().functionOpening(EnumFunction.TheWar)) {
                SC_EnterFight.Builder retBuilder = SC_EnterFight.newBuilder();
                retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
                gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, retBuilder);
                return;
            }
            if (!TheWarManager.getInstance().open()) {
                SC_EnterFight.Builder retBuilder = SC_EnterFight.newBuilder();
                retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_NotOpen));
                gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, retBuilder);
                return;
            }
            String roomIdx = player.getDb_data().getTheWarRoomIdx();
            if (StringHelper.isNull(roomIdx)) {
                SC_EnterFight.Builder retBuilder = SC_EnterFight.newBuilder();
                retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_NotJoinTheWar));
                gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, retBuilder);
                return;
            }
            if (req.getParamListCount() < 2) {
                SC_EnterFight.Builder retBuilder = SC_EnterFight.newBuilder();
                retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, retBuilder);
                return;
            }
            int posX = Integer.parseInt(req.getParamList(0));
            int posY = Integer.parseInt(req.getParamList(1));
            GS_CS_ReqOccupyGrid.Builder builder = GS_CS_ReqOccupyGrid.newBuilder();
            builder.setPlayerIdx(playerIdx);
            builder.getPosBuilder().setX(posX);
            builder.getPosBuilder().setY(posY);
            builder.setSkipBattle(req.getSkipBattle());
            if (!CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_ReqOccupyGrid_VALUE, builder)) {
                SC_EnterFight.Builder retBuilder = SC_EnterFight.newBuilder();
                retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_NotFoundWarServer));
                gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, retBuilder);
            }
        }

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_EnterFight_VALUE, SC_EnterFight.newBuilder().setRetCode(retCode));
    }
}
