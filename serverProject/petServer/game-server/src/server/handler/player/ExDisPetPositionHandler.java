package server.handler.player;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import common.AbstractBaseHandler;
import hyzNet.message.MsgId;
import java.util.Map;
import java.util.Map.Entry;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerInfo.CS_ExDisPetPosition;
import protocol.PlayerInfo.DisplayPet;
import protocol.PlayerInfo.SC_ExDisPetPosition;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ExDisPetPosition_VALUE)
public class ExDisPetPositionHandler extends AbstractBaseHandler<CS_ExDisPetPosition> {
    @Override
    protected CS_ExDisPetPosition parse(byte[] bytes) throws Exception {
        return CS_ExDisPetPosition.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ExDisPetPosition req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int position1 = req.getPosition1();
        int position2 = req.getPosition2();
        int displayPetLimit = GameConfig.getById(GameConst.CONFIG_ID).getDisplayerpetlimit();

        SC_ExDisPetPosition.Builder resultBuilder = SC_ExDisPetPosition.newBuilder();
        if (position1 >= displayPetLimit || position2 >= displayPetLimit) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ExDisPetPosition_VALUE, resultBuilder);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if(player == null){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ExDisPetPosition_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_PlayerData.Builder builder = player.getDb_data();
            if (builder == null) {
                LogUtil.error("playerIdx[" + playerIdx + "] db is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ExDisPetPosition_VALUE, resultBuilder);
                return;
            }

            Map<Integer, String> displayPetMap = builder.getDisplayPetMap();
            String linkPetIdx_1 = displayPetMap.get(position1);
            String linkPetIdx_2 = displayPetMap.get(position2);

            builder.removeDisplayPet(position1);
            builder.removeDisplayPet(position2);

            if (linkPetIdx_2 != null) {
                builder.putDisplayPet(position1, linkPetIdx_2);
            }
            if (linkPetIdx_1 != null) {
                builder.putDisplayPet(position2, linkPetIdx_1);
            }


            for (Entry<Integer, String> entry : builder.getDisplayPetMap().entrySet()) {
                DisplayPet.Builder disPet = DisplayPet.newBuilder();
                disPet.setPosition(entry.getKey());
                disPet.setLinkPetIdx(entry.getValue());
                resultBuilder.addDisplayPet(disPet);
            }
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ExDisPetPosition_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
