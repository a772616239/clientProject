package server.handler.cp.copy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpCopyManger;
import model.cp.CpTeamCache;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpCopyMapPoint;
import org.apache.commons.lang.StringUtils;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_QueryPointMonster;
import protocol.CpFunction.SC_QueryPointMonster;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_QueryPointMonster_VALUE;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_PointAlreadyExplore;
import static protocol.RetCodeId.RetCodeEnum.RCE_ErrorParam;

/**
 * 查询战斗点怪物
 */
@MsgId(msgId = MsgIdEnum.CS_QueryPointMonster_VALUE)
public class QueryPointMonsterHandler extends AbstractBaseHandler<CS_QueryPointMonster> {
    @Override
    protected CS_QueryPointMonster parse(byte[] bytes) throws Exception {
        return CS_QueryPointMonster.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryPointMonster req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_QueryPointMonster.Builder msg = SC_QueryPointMonster.newBuilder();
        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(playerIdx);
        if (StringUtils.isEmpty(mapId)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CP_CopyNotExists));
            gsChn.send(SC_QueryPointMonster_VALUE, msg);
            return;
        }

        CpCopyMap copyMapData = CpCopyManger.getInstance().findCopyMapData(mapId);
        if (copyMapData == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CP_CopyNotExists));
            gsChn.send(SC_QueryPointMonster_VALUE, msg);
            return;
        }
        CpCopyMapPoint point = copyMapData.queryPointById(req.getPointId());
        if (point == null || point.getPointType() != CpFunction.CpFunctionEvent.CFE_Battle_VALUE) {
            msg.setRetCode(GameUtil.buildRetCode(RCE_ErrorParam));
            gsChn.send(SC_QueryPointMonster_VALUE, msg);
            return;
        }
        if (!StringUtils.isEmpty(point.getPlayerIdx())) {
            msg.setRetCode(GameUtil.buildRetCode(RCE_CP_PointAlreadyExplore));
            gsChn.send(SC_QueryPointMonster_VALUE, msg);
            return;
        }

        for (PetMessage.Pet monster : point.getMonsters()) {
            msg.addPets(CpFunctionUtil.toPetVo(monster));
        }
        msg.setAbility(point.getAbility());
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_QueryPointMonster_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_QueryPointMonster_VALUE, SC_QueryPointMonster.newBuilder().setRetCode(retCode));
    }
}
