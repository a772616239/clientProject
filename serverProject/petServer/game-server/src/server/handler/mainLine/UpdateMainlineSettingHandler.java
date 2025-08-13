package server.handler.mainLine;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import protocol.Common;
import protocol.MainLine;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_UpdateMainlineSetting_VALUE)
public class UpdateMainlineSettingHandler extends AbstractBaseHandler<MainLine.CS_UpdateMainlineSetting> {
    @Override
    protected MainLine.CS_UpdateMainlineSetting parse(byte[] bytes) throws Exception {
        return MainLine.CS_UpdateMainlineSetting.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MainLine.CS_UpdateMainlineSetting req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        MainLine.SC_UpdateMainlineSetting.Builder resultBuilder = MainLine.SC_UpdateMainlineSetting.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateMainlineSetting_VALUE, resultBuilder);
            return;
        }

        if (req.getClosePlot() == entity.getDBBuilder().getClosePlot()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateMainlineSetting_VALUE, resultBuilder);
            return;
        }


        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDBBuilder().setClosePlot(req.getClosePlot());
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_UpdateMainlineSetting_VALUE, resultBuilder);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.MainLine;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_UpdateMainlineSetting_VALUE, MainLine.SC_UpdateMainlineSetting.newBuilder().setRetCode(retCode));
    }
}
