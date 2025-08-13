package server.handler.mainLine;

import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mainLine.util.MainLineUtil;
import protocol.Common.EnumFunction;
import protocol.MainLine.CS_InputPassword;
import protocol.MainLine.MainLineProgress;
import protocol.MainLine.SC_InputPassword;
import protocol.MainLineDB.DB_MainLine.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_InputPassword_VALUE)
public class InputPasswordHandler extends AbstractBaseHandler<CS_InputPassword> {
    @Override
    protected CS_InputPassword parse(byte[] bytes) throws Exception {
        return CS_InputPassword.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_InputPassword req, int i) {
        SC_InputPassword.Builder resultBuilder = SC_InputPassword.newBuilder();

        int psw = req.getPswOrder();

        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("ClaimMainLineHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            Builder dbBuilder = entity.getDBBuilder();
            if (dbBuilder == null) {
                LogUtil.error("ClaimMainLineHandler, playerIdx[" + playerIdx + "] dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
                return;
            }

            MainLineProgress.Builder mainLineProBuilder = dbBuilder.getMainLineProBuilder();
            int curCheckPoint = mainLineProBuilder.getCurCheckPoint();

            if (mainLineProBuilder.getProgressList().contains(psw)) {
                LogUtil.info("repeated input the same psw");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_InputTheSamePsw));
                gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
                return;
            }

            MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(curCheckPoint);
            if (checkPointCfg == null) {
                LogUtil.error("InputPasswordHandler, MainLineCheckPoint cfg is null, id = " + curCheckPoint);
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
                return;
            }

            if (checkPointCfg.getType() != 1) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_CheckPointTypeMissMatch));
                gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
                return;
            }

            if (!isInPswCheckPointNode(checkPointCfg.getNodelist(), psw)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_InputNotPswNode));
                gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
                return;
            }

            mainLineProBuilder.addProgress(psw);

            if (isFinishInputPsw(checkPointCfg.getNodelist(), mainLineProBuilder.getProgressList())) {
                entity.updatePswRecord(curCheckPoint, mainLineProBuilder.getProgressList());

                resultBuilder.setIsFinish(true);
                resultBuilder.setIsRight(MainLineUtil.pswIsRight(curCheckPoint, mainLineProBuilder.getProgressList()));
                entity.passCurCheckPoint();
                entity.updateRanking(psw);
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, resultBuilder);
            entity.sendRefreshMainLineMsg();
        });
    }

    /**
     * 检查节点是不是在关卡中,且是密码节点
     *
     * @return
     */
    public boolean isInPswCheckPointNode(int[] checkPointNodeList, int psw) {
        if (!ArrayUtil.intArrayContain(checkPointNodeList, psw)) {
            return false;
        }

        MainLineNodeObject byId = MainLineNode.getById(psw);
        if (byId != null && byId.getNodetype() == 3) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否已经完全输入了密码
     *
     * @param orderList
     * @param progress
     * @return
     */
    public boolean isFinishInputPsw(int[] orderList, List<Integer> progress) {
        if (orderList == null || progress == null) {
            return false;
        }

        for (int i : orderList) {
            MainLineNodeObject byId = MainLineNode.getById(i);
            if (byId != null && byId.getNodetype() == 3) {
                if (!progress.contains(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_InputPassword_VALUE, SC_InputPassword.newBuilder().setRetCode(retCode));
    }
}
