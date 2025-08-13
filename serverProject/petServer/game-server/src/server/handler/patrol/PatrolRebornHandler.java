package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol;
import protocol.Patrol.CS_PatrolReborn;
import protocol.Patrol.SC_PatrolReborn;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;


/**
 * 处理巡逻队失败后重新游戏请求
 *
 * @author xiao_FL
 * @date 2019/8/15
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolReborn_VALUE)
public class PatrolRebornHandler extends AbstractBaseHandler<CS_PatrolReborn> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolReborn parse(byte[] bytes) throws Exception {
        return CS_PatrolReborn.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolReborn csPatrolReborn, int i) {
        LogUtil.info("recv patrolReborn msg:" + csPatrolReborn.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            Patrol.SC_PatrolReborn.Builder result = Patrol.SC_PatrolReborn.newBuilder();
            patrolService.reborn(playerId, result);
            gameServerTcpChannel.send(MsgIdEnum.SC_PatrolReborn_VALUE, result);

        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Patrol;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PatrolReborn_VALUE, SC_PatrolReborn.newBuilder().setResult(retCode));
    }
}
