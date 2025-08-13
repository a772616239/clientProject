package server.handler.collection;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.redpoint.RedPointManager;
import model.redpoint.RedPointOptionEnum;
import protocol.Collection;
import protocol.Collection.CS_CollectCollectionExp;
import protocol.Collection.SC_CollectCollectionExp;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB;
import protocol.RedPointIdEnum;
import protocol.RedPointIdEnum.RedPointId;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;


/**
 * 收集图鉴经验
 */
@MsgId(msgId = MsgIdEnum.CS_CollectCollectionExp_VALUE)
public class CollectCollectionExpHandler extends AbstractBaseHandler<CS_CollectCollectionExp> {

    @Override
    protected CS_CollectCollectionExp parse(byte[] bytes) throws Exception {
        return CS_CollectCollectionExp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CollectCollectionExp req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        Collection.CollectionType collectionType = req.getCollectionType();
        SC_CollectCollectionExp.Builder resultBuilder = Collection.SC_CollectCollectionExp.newBuilder();
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CollectCollectionExp_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            int canClaimExp = player.calculateCanClaimCollectionExp(collectionType);
            if (canClaimExp <= 0) {
                resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_CollectCollectionExp_VALUE, resultBuilder);
                return;
            }
            LogUtil.info("player:{} claim collection exp by :{},canClaimExp:{}", playerId, collectionType, canClaimExp);
            PlayerDB.DB_Collection.Builder db_collection = player.getDb_data().getCollectionBuilder();
            updateDbInfo(playerId, canClaimExp, db_collection, collectionType);
            sendSuccessMsg(gsChn, resultBuilder, db_collection);
        });
    }

    private void updateDbInfo(String playerId, int canClaimExp, PlayerDB.DB_Collection.Builder db_collection, Collection.CollectionType collectionType) {
        db_collection.setCollectionExp(canClaimExp + db_collection.getCollectionExp());
        LogUtil.info("player:{} claim collection exp success,addExp:{},nowExp:{}", playerId, canClaimExp, db_collection.getCollectionExp());
        switch (collectionType) {
            case CT_Link:
                db_collection.clearCanClaimLinkExp();
                RedPointManager.getInstance().sendRedPoint(playerId, RedPointId.ALBUM_CHAIN_VALUE, RedPointOptionEnum.REMOVE);
                return;
            case CT_PET:
                db_collection.clearCanClaimedPetExpId();
                RedPointManager.getInstance().sendRedPoint(playerId, RedPointId.ALBUM_PET_VALUE, RedPointOptionEnum.REMOVE);
                return;
            case CT_Artifact:
                db_collection.clearCanClaimArtifactExp();
                RedPointManager.getInstance().sendRedPoint(playerId, RedPointId.ALBUM_ARTIFACT_VALUE, RedPointOptionEnum.REMOVE);
        }
    }

    private void sendSuccessMsg(GameServerTcpChannel gsChn, SC_CollectCollectionExp.Builder resultBuilder
            , PlayerDB.DB_Collection.Builder db_collection) {

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setCollectLv(db_collection.getCollectionLv());
        resultBuilder.setCurExp(db_collection.getCollectionExp());
        gsChn.send(MsgIdEnum.SC_CollectCollectionExp_VALUE, resultBuilder);

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetCollect;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CollectCollectionExp_VALUE, Collection.SC_CollectCollectionExp.newBuilder().setResult(retCode));
    }


}
