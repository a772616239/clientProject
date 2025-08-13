package server.handler.barrage;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.barrage.BarrageDTO;
import model.barrage.BarrageManager;
import org.apache.commons.lang.StringUtils;
import protocol.Barrage;
import protocol.Common;
import protocol.MessageId;

import static protocol.MessageId.MsgIdEnum.SC_ClaimBarrageInfo_VALUE;

/**
 * 拉取弹幕信息(全量)
 */
@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimBarrageInfo_VALUE)
public class ClaimBarrageInfoHandler extends AbstractBaseHandler<Barrage.CS_ClaimBarrageInfo> {
    @Override
    protected Barrage.CS_ClaimBarrageInfo parse(byte[] bytes) throws Exception {
        return Barrage.CS_ClaimBarrageInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Barrage.CS_ClaimBarrageInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }
        List<BarrageDTO> initMsg = BarrageManager.getInstance().getInitMsg(req.getFunction(), req.getModuleId());
        Barrage.SC_ClaimBarrageInfo.Builder msg = Barrage.SC_ClaimBarrageInfo.newBuilder();
        for (BarrageDTO data : initMsg) {
            msg.addBarrage(data.getMessage());
            msg.addPlayerIdx(data.getPlayerIdx());
        }
        gsChn.send(SC_ClaimBarrageInfo_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
