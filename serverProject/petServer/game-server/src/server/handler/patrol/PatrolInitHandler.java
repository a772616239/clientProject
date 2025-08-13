package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolInitResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolInit;
import protocol.Patrol.SC_PatrolInit;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolInit_VALUE;

/**
 * 处理客户端打开巡逻队界面请求
 *
 * @author xiao_FL
 * @date 2019/7/29
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolInit_VALUE)
public class PatrolInitHandler extends AbstractBaseHandler<CS_PatrolInit> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolInit parse(byte[] bytes) throws Exception {
        return CS_PatrolInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolInit csPatrolInit, int i) {
        LogUtil.info("recv patrolInit msg:" + csPatrolInit.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            // 查询巡逻队
            PatrolInitResult returnResult = patrolService.patrolMapInit(playerId);
            // 返回
            SC_PatrolInit.Builder result = SC_PatrolInit.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            boolean success = returnResult.isSuccess();
            if (success) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
                result.setMap(returnResult.getPatrolMap());
                result.setStatus(returnResult.getPatrolStatus());
                result.setNewGame(returnResult.isNewGame());
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(SC_PatrolInit_VALUE, result);

            if (success) {
                //目标：进入秘境探索(巡逻队）
                EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_CumuJoinPatrol, 1, 0);
            }
            patrolService.sendPatrolInit(playerId);
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
        gsChn.send(MsgIdEnum.SC_PatrolInit_VALUE, SC_PatrolInit.newBuilder().setResult(retCode));
    }
}
