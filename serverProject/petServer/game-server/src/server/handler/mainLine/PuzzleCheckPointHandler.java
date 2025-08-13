package server.handler.mainLine;

import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mainLine.util.MainLineUtil;
import protocol.Common.EnumFunction;
import protocol.MainLine.CS_PuzzleCheckPoint;
import protocol.MainLine.MainLineProgress;
import protocol.MainLine.OperationTypeEnum;
import protocol.MainLine.SC_PuzzleCheckPoint;
import protocol.MainLineDB.DB_MainLine.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_PuzzleCheckPoint_VALUE)
public class PuzzleCheckPointHandler extends AbstractBaseHandler<CS_PuzzleCheckPoint> {
    @Override
    protected CS_PuzzleCheckPoint parse(byte[] bytes) throws Exception {
        return CS_PuzzleCheckPoint.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PuzzleCheckPoint req, int i) {

        SC_PuzzleCheckPoint.Builder resultBuilder = SC_PuzzleCheckPoint.newBuilder();

        int nodeId = req.getNodeId();
        MainLineNodeObject nodeCfg = MainLineNode.getById(nodeId);
        if (nodeCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_NodeCfgIsNull));
            gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("PuzzleCheckPointHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
            return;
        }


        SyncExecuteFunction.executeConsumer(entity, e -> {
            Builder db_data = entity.getDBBuilder();
            if (db_data == null) {
                LogUtil.error("PuzzleCheckPointHandler, playerIdx[" + playerIdx + "] dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
                return;
            }

            MainLineProgress.Builder mainLineProBuilder = db_data.getMainLineProBuilder();
            int curCheckPoint = mainLineProBuilder.getCurCheckPoint();

            MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(curCheckPoint);
            if (checkPointCfg == null) {
                LogUtil.error("PuzzleCheckPointHandler, MainLineCheckPoint cfg is null, id = " + curCheckPoint);
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
                return;
            }

            //判断当前节点是否属于当前关卡，判断当前位置是否能够到达该节点
            if (!ArrayUtil.intArrayContain(checkPointCfg.getNodelist(), nodeId)
                    || !MainLineUtil.nodeCanReach(mainLineProBuilder.getUnlockNodesList(), nodeId, false)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_NodeCanNotReach));
                gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
                return;
            }

            switch (nodeCfg.getNodetype()) {
                case 0:
                    resultBuilder.setOperation(OperationTypeEnum.OTE_Null);
                    mainLineProBuilder.addProgress(nodeId);
                    entity.addUnlockNode(nodeCfg.getAfternodeid(), true);
                    break;
                case 1:
                case 2:
                    if (mainLineProBuilder.getProgressList().contains(nodeId)) {
                        entity.addUnlockNode(nodeCfg.getAfternodeid(), true);
                        resultBuilder.setOperation(OperationTypeEnum.OTE_Null);
                    } else {
                        resultBuilder.setOperation(OperationTypeEnum.OTE_EnterBattle);
                    }
                    break;
                case 3:
                    LogUtil.error("Unsupported nodeType, checkPointId:" + curCheckPoint + ", nodeId: " + nodeId);
                    break;
                case 4:
                    int transferTarget = nodeCfg.getParam();
                    entity.addProgress(nodeId);
                    //标记传送节点位置
                    mainLineProBuilder.setLastTransferNode(nodeId);


                    MainLineNodeObject targetNodeCfg = MainLineNode.getById(transferTarget);
                    if (targetNodeCfg == null) {
                        LogUtil.error("MainLineNodeCfg is null, node Id = " + transferTarget);
                        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                        gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
                        return;
                    }
                    resultBuilder.setTransferTarget(transferTarget);
                    switch (targetNodeCfg.getNodetype()) {
                        case 0:
                            resultBuilder.setOperation(OperationTypeEnum.OTE_Transfer);

                            entity.addProgress(transferTarget);
                            entity.addUnlockNode(targetNodeCfg.getAfternodeid(), true);

                            mainLineProBuilder.setCurNode(mainLineProBuilder.getLastOperationtNode());
                            //标记上次操作的节点到传送目标点
                            mainLineProBuilder.setLastOperationtNode(transferTarget);
                            break;
                        default:
                            LogUtil.error("Unsupported transfer target, node type = " + targetNodeCfg.getNodetype());
                            break;
                    }
                default:
                    break;
            }
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setMainLinePro(mainLineProBuilder);
            gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PuzzleCheckPoint_VALUE, SC_PuzzleCheckPoint.newBuilder().setRetCode(retCode));
    }
}
