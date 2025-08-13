package server.handler.itemBag;

import cfg.Item;
import cfg.ItemObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.itembag.BlindBoxManager;
import model.itembag.ItemConst;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import protocol.Bag;
import protocol.Bag.CS_BlindBoxInfo;
import protocol.Bag.SC_BlindBoxInfo;
import protocol.Common.EnumFunction;
import protocol.ItemBagDB;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.Collection;

@MsgId(msgId = MsgIdEnum.CS_BlindBoxInfo_VALUE)
public class ClaimBindBoxInfoHandler extends AbstractBaseHandler<CS_BlindBoxInfo> {
    @Override
    protected CS_BlindBoxInfo parse(byte[] bytes) throws Exception {
        return CS_BlindBoxInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BlindBoxInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        int itemCfgId = req.getItemCfgId();

        SC_BlindBoxInfo.Builder resultBuilder = SC_BlindBoxInfo.newBuilder();
        ItemObject itemCfg = Item.getById(itemCfgId);
        if (itemCfg == null || ItemConst.ItemType.Blind_Box != itemCfg.getSpecialtype()) {
            LogUtil.error("BlindBoxInfoHandler,blindBox from itemId is null:{}", itemCfgId);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BlindBoxInfo_VALUE, resultBuilder);
            return;
        }
        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] itemBag is null");
            return;
        }

        SyncExecuteFunction.executeConsumer(itemBag, entity -> {
            ItemBagDB.DB_BlindBoxReward db_blindBoxReward = itemBag.getDb_data().getBlindBoxesMap().get(itemCfgId);
            if (db_blindBoxReward == null) {
                Collection<Bag.BlindBoxReward> blindBoxRewards = BlindBoxManager.getInstance().randomBlindBoxShowRewards(itemCfgId);
                ItemBagDB.DB_BlindBoxReward.Builder builder = ItemBagDB.DB_BlindBoxReward.newBuilder();
                builder.addAllRewardList(blindBoxRewards);
                resultBuilder.addAllRewardList(blindBoxRewards);
                itemBag.getDb_data().putBlindBoxes(itemCfgId, builder.build());
                return;
            }
            resultBuilder.addAllRewardList(db_blindBoxReward.getRewardListList());
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_BlindBoxInfo_VALUE, resultBuilder);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ItemBag;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BlindBoxInfo_VALUE, SC_BlindBoxInfo.newBuilder().setRetCode(retCode));
    }
}
