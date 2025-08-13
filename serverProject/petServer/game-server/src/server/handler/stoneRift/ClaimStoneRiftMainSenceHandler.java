package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.stoneRift.StoneRiftUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRift;
import model.stoneRift.entity.DbStoneRiftEvent;
import model.stoneRift.entity.StoneRiftScience;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.*;
import util.GameUtil;
import util.MapUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimStoneRiftMain_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimStoneRiftMain_VALUE)
public class ClaimStoneRiftMainSenceHandler extends AbstractBaseHandler<CS_ClaimStoneRiftMain> {

    @Override
    protected CS_ClaimStoneRiftMain parse(byte[] bytes) throws Exception {
        return CS_ClaimStoneRiftMain.parseFrom(bytes);
    }

    @Override
    public void execute(GameServerTcpChannel gsChn, CS_ClaimStoneRiftMain req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimStoneRiftMain.Builder msg = buildMsg(playerId);

        GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneRiftMain_VALUE, msg);

    }

    private SC_ClaimStoneRiftMain.Builder buildMsg(String playerId) {
        SC_ClaimStoneRiftMain.Builder msg = SC_ClaimStoneRiftMain.newBuilder();

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        if (!player.functionUnLock(EnumFunction.StoneRift)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            return msg;
        }
        stoneriftEntity stonerift = stoneriftCache.getByIdx(playerId);
        if (stonerift == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }

        combinePlayerInfo(player, msg);

        combineStoneRiftInf(stonerift, msg);

        combineFactoryVo(stonerift, msg);


        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));

        return msg;
    }

    private void combineFactoryVo(stoneriftEntity entity, SC_ClaimStoneRiftMain.Builder msg) {
        entity.getDB_Builder().getFactoryMap().forEach((k, v) -> {
            msg.addStoneFactory(StoneRiftUtil.toFactoryVo(entity,k));
        });

    }

    private void combineStoneRiftInf(stoneriftEntity stonerift, SC_ClaimStoneRiftMain.Builder msg) {
        DbStoneRift db = stonerift.getDB_Builder();
        msg.setLevel(db.getLevel());
        msg.setExp(db.getExp());
        msg.setOverload(StoneRiftUtil.toOverLoadInfo(db));
        msg.setEventVo(DbEvent2Vo(db.getEvent()));
        msg.setMapId(stonerift.getDB_Builder().getMapId());
        msg.setPlayerScience(MapUtil.map2IntMap(db.getDbScience().getSkillLvMap()));
        msg.setNextRefreshTime(db.getNextSettleTime());
    }

    private StoneRiftEventVo.Builder DbEvent2Vo(DbStoneRiftEvent event) {
        StoneRiftEventVo.Builder vo = StoneRiftEventVo.newBuilder();
        vo.setEventValue(event.getEvent());
        vo.setCanTrigger(event.isAlreadyTrigger());
        vo.setExpireTime(event.getExpireTime());
        return vo;
    }

    private void combinePlayerInfo(playerEntity player, SC_ClaimStoneRiftMain.Builder msg) {
        msg.setHeader(player.getAvatar());
        msg.setPlayerId(player.getIdx());
        msg.setBorderId(player.getBorderId());
        msg.setPlayerName(player.getName());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
