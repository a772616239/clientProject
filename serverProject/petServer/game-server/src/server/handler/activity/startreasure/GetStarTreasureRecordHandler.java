package server.handler.activity.startreasure;

import com.google.protobuf.InvalidProtocolBufferException;
import common.AbstractBaseHandler;
import common.GameConst.RedisKey;
import common.JedisUtil;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import protocol.Activity.CS_StarTreasureRecord;
import protocol.Activity.SC_StarTreasureRecord;
import protocol.Activity.StarTreasureRecord;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

/**
 * 获取星星宝藏活动信息
 */
@MsgId(msgId = MsgIdEnum.CS_StarTreasureRecord_VALUE)
public class GetStarTreasureRecordHandler extends AbstractBaseHandler<CS_StarTreasureRecord> {
    @Override
    protected CS_StarTreasureRecord parse(byte[] bytes) throws Exception {
        return CS_StarTreasureRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StarTreasureRecord req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_StarTreasureRecord.Builder resultBuilder = SC_StarTreasureRecord.newBuilder();
        if (!ActivityUtil.activityNeedDis(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_StarTreasureRecord_VALUE, resultBuilder);
            return;
        }

        List<byte[]> recordByteArrList = JedisUtil.jedis.lrange(RedisKey.getStarTreasureRecordKey(),-10,-1);
        if(recordByteArrList != null && !recordByteArrList.isEmpty()){
            for (byte[] recordByteArr :recordByteArrList) {
                try {
                    StarTreasureRecord recordBuild = StarTreasureRecord.parseFrom(recordByteArr);
                    if(recordBuild !=  null){
                        resultBuilder.addRecords(recordBuild);
                    }
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.error("StarTreasureRecord but redis byte arr parseFrom error!!!\n{}",e);
                }
            }
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_StarTreasureRecord_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StarTreasure;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }


}
