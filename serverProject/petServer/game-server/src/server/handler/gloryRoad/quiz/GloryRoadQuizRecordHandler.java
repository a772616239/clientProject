package server.handler.gloryRoad.quiz;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.gloryroad.dbCache.gloryroadCache;
import model.gloryroad.entity.gloryroadEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_GloryRoadQuizRecord;
import protocol.GloryRoad.GloryRoadQuizRecord;
import protocol.GloryRoad.SC_GloryRoadQuizRecord;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/17
 */
@MsgId(msgId = MsgIdEnum.CS_GloryRoadQuizRecord_VALUE)
public class GloryRoadQuizRecordHandler extends AbstractBaseHandler<CS_GloryRoadQuizRecord> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_GloryRoadQuizRecord.Builder resultBuilder = SC_GloryRoadQuizRecord.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_GloryRoadQuizRecord_VALUE, resultBuilder);
    }

    @Override
    protected CS_GloryRoadQuizRecord parse(byte[] bytes) throws Exception {
        return CS_GloryRoadQuizRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GloryRoadQuizRecord req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        gloryroadEntity entity = gloryroadCache.getInstance().getEntity(playerIdx);

        SC_GloryRoadQuizRecord.Builder resultBuilder = SC_GloryRoadQuizRecord.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_GloryRoadQuizRecord_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, en -> {
            List<GloryRoadQuizRecord> recordList = entity.getDbBuilder().getRecordsList();
            if (CollectionUtils.isNotEmpty(recordList)) {
                resultBuilder.addAllRecords(recordList);
            }
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_GloryRoadQuizRecord_VALUE, resultBuilder);
    }
}
