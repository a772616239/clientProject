package server.handler.collection;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Collection;
import protocol.Collection.CS_CollectionInfo;
import protocol.Collection.SC_CollectionInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 处理客户端请求图鉴收集进度
 *
 * @author xiao_FL
 * @date 2019/9/6
 */
@MsgId(msgId = MsgIdEnum.CS_CollectionInfo_VALUE)
public class CollectionInfoInitHandler extends AbstractBaseHandler<Collection.CS_CollectionInfo> {

    @Override
    protected CS_CollectionInfo parse(byte[] bytes) throws Exception {
        return CS_CollectionInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CollectionInfo req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_CollectionInfo.Builder msg = SC_CollectionInfo.newBuilder();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CollectionInfo_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, e -> {
            PlayerDB.DB_Collection collection = player.getDb_data().getCollection();
            msg.addAllCfgId(collection.getCfgIdList());
            msg.addAllCollectionExp(playerEntity.collectionCfgIds2CollectionExpList(collection.getCanClaimedPetExpIdList()));
            msg.setCollectLv(collection.getCollectionLv());
            msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            msg.setCurExp(collection.getCollectionExp());
            msg.addAllArtifactExp(player.getCanClaimArtifactExp());
            msg.addAllLinkExp(player.getLinkExp());
            msg.addAllCollectedLinkId(collection.getCollectedLinkIdList());

            gsChn.send(MsgIdEnum.SC_CollectionInfo_VALUE, msg);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetCollect;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_CollectionInfo_VALUE, SC_CollectionInfo.newBuilder().setResult(retCode));
    }


}
