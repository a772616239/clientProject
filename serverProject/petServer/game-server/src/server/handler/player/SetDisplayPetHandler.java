package server.handler.player;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerInfo.CS_SetDisplayPet;
import protocol.PlayerInfo.DisplayPet;
import protocol.PlayerInfo.SC_SetDisplayPet;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_SetDisplayPet_VALUE)
public class SetDisplayPetHandler extends AbstractBaseHandler<CS_SetDisplayPet> {
    @Override
    protected CS_SetDisplayPet parse(byte[] bytes) throws Exception {
        return CS_SetDisplayPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SetDisplayPet req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        List<String> linkPetIdx = req.getLinkPetIdxList();
        //去掉重复ID,验证宠物idx是否存在
        Set<String> linkPetSet = new HashSet<>();
        for (String petIdx : linkPetIdx) {
            if (petCache.getInstance().getPetById(playerIdx, petIdx) == null) {
                continue;
            }
            linkPetSet.add(petIdx);
        }

        SC_SetDisplayPet.Builder resultBuilder = SC_SetDisplayPet.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if(player == null){
            LogUtil.error("playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_SetDisplayPet_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_PlayerData.Builder builder = player.getDb_data();
            if(builder == null){
                LogUtil.error("playerIdx[" + playerIdx + "] DBData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_SetDisplayPet_VALUE, resultBuilder);
                return;
            }

            builder.clearDisplayPet();
            for (int j = 0; j < GameConfig.getById(GameConst.CONFIG_ID).getDisplayerpetlimit(); j++) {
                if (j >= linkPetSet.size()) {
                    break;
                }

                builder.putDisplayPet(j, linkPetIdx.get(j));

                DisplayPet.Builder disPet = DisplayPet.newBuilder();
                disPet.setPosition(j);
                disPet.setLinkPetIdx(linkPetIdx.get(j));
                resultBuilder.addDisplayPet(disPet);
            }
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_SetDisplayPet_VALUE, resultBuilder);
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
