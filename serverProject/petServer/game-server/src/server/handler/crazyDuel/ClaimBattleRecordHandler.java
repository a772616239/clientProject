package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;

import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import org.springframework.util.StringUtils;
import protocol.Common;
import protocol.CrayzeDuel;
import protocol.CrayzeDuel.CS_ClaimCrazyBattleRecord;
import protocol.CrayzeDuel.SC_ClaimCrazyBattleRecord;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimCrazyBattleRecord_VALUE)
public class ClaimBattleRecordHandler extends AbstractBaseHandler<CS_ClaimCrazyBattleRecord> {
    @Override
    protected CS_ClaimCrazyBattleRecord parse(byte[] bytes) throws Exception {
        return CS_ClaimCrazyBattleRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCrazyBattleRecord req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_ClaimCrazyBattleRecord.Builder msg = SC_ClaimCrazyBattleRecord.newBuilder();
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimCrazyBattleRecord_VALUE, msg);
            return;
        }

        List<CrayzeDuel.CrazyBattleRecord> playerRecord = CrazyDuelManager.getInstance().findPlayerRecord(playerIdx);
        msg.addAllRecords(playerRecord);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));

        gsChn.send(MessageId.MsgIdEnum.SC_ClaimCrazyBattleRecord_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimCrazyBattleRecord_VALUE, SC_ClaimCrazyBattleRecord.newBuilder().setRetCode(retCode));
    }
}
