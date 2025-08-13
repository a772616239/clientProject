package model.warpServer.battleServer.handler;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battlerecord.entity.battlerecordEntity;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaTopManager;
import model.stoneRift.StoneRiftWorldMapManager;
import protocol.*;
import util.LogUtil;

import static protocol.Forward.ForwardMsgIdEnum.*;
import static protocol.MessageId.MsgIdEnum.*;

@MsgId(msgId = MessageId.MsgIdEnum.BS_GS_TransferGSTOGSMsg_VALUE)
public class GSToGSTransferHandler extends AbstractHandler<ServerTransfer.BS_GS_TransferGSTOGSMsg> {

    @Override
    protected ServerTransfer.BS_GS_TransferGSTOGSMsg parse(byte[] bytes) throws Exception {
        return ServerTransfer.BS_GS_TransferGSTOGSMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, ServerTransfer.BS_GS_TransferGSTOGSMsg req, int i) {
        try {
            switch (req.getMsgId()) {
                case BS_GS_CrossArenaTopAward_VALUE:
                    topPlayAward(req.getMsgData());
                    break;
                case BS_GS_CrossArenaRefInfo_VALUE:
                    refTableInfo(req.getMsgData());
                    break;
                case FM_StealStoneRiftRes_VALUE:
                    stealStoneRiftPlayerRes(req.getMsgData());
                    break;
                case FM_SaveBattleRecord_VALUE:
                    saveBattleRecord(req.getMsgData());
                    break;    

            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }

    private void saveBattleRecord(ByteString msgData) {
        try {
            Forward.SaveBattleRecord record = Forward.SaveBattleRecord.parseFrom(msgData);
            battlerecordEntity newEntity = new battlerecordEntity();
            newEntity.setBattleid(record.getBattleId());
            newEntity.setVersion(record.getVersion());
            BattleRecordDB.DB_ServerBattleRecord data = BattleRecordDB.DB_ServerBattleRecord.parseFrom(record.getData());
            newEntity.setServerBattleRecordBuilder(data.toBuilder());
            newEntity.transformDBData();
            newEntity.putToCache();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
        }
    }

    private void stealStoneRiftPlayerRes(ByteString msgData) {
        try {
            StoneRift.FG_StealStoneRiftRes msg = StoneRift.FG_StealStoneRiftRes.parseFrom(msgData);

            StoneRiftWorldMapManager.getInstance().dealStolen(msg.getPlayerId(),msg.getFactoryId());


        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    private void topPlayAward(ByteString msgData) throws InvalidProtocolBufferException {
        ServerTransfer.BS_GS_CrossArenaTopAward tomsg = ServerTransfer.BS_GS_CrossArenaTopAward.parseFrom(msgData);
        CrossArenaTopManager.getInstance().sendPlayerAward(tomsg);
    }

    private void refTableInfo(ByteString msgData) throws InvalidProtocolBufferException {
        ServerTransfer.BS_GS_CrossArenaRefInfo msg10 = ServerTransfer.BS_GS_CrossArenaRefInfo.parseFrom(msgData);
        CrossArenaManager.getInstance().tableChangeAfter(msg10.getTableInfo());
    }

}
