package server.handler.ancientCall;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import java.util.Map.Entry;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.MapUtils;
import protocol.AncientCall.AncientMustGetDrawTimes;
import protocol.AncientCall.CS_ClaimAncient;
import protocol.AncientCall.SC_ClaimAncient;
import protocol.AncientCall.SC_ClaimAncient.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/06/02
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimAncient_VALUE)
public class ClaimAncientHandler extends AbstractBaseHandler<CS_ClaimAncient> {
    @Override
    protected CS_ClaimAncient parse(byte[] bytes) throws Exception {
        return CS_ClaimAncient.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAncient req, int i) {
        Builder resultBuilder = SC_ClaimAncient.newBuilder();
        playerEntity player = playerCache.getByIdx(String.valueOf(gsChn.getPlayerId1()));
        if (player != null) {
            resultBuilder.setTodayCallTimes(player.getDb_data().getTodayCallTimes());
            Map<Integer, Integer> timesMap = player.getDb_data().getAncientAltar().getMustGetDrawTimesMap();
            if (MapUtils.isNotEmpty(timesMap)) {
                for (Entry<Integer, Integer> entry : timesMap.entrySet()) {
                    AncientMustGetDrawTimes.Builder builder = AncientMustGetDrawTimes.newBuilder().setAltarType(entry.getKey()).setCurTimes(entry.getValue());
                    resultBuilder.addMustGetDrawTimes(builder);
                }
            }
        }
        gsChn.send(MsgIdEnum.SC_ClaimAncient_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ClaimAncient_VALUE, SC_ClaimAncient.newBuilder());
    }
}
