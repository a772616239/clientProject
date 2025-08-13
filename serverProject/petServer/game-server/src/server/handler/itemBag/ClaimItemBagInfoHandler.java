package server.handler.itemBag;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import protocol.Bag.CS_ClaimItemBagInfo;
import protocol.Bag.ItemInfo;
import protocol.Bag.SC_ClaimItemBagInfo;
import protocol.Common.EnumFunction;
import protocol.ItemBagDB.DB_ItemBag;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimItemBagInfo_VALUE)
public class ClaimItemBagInfoHandler extends AbstractBaseHandler<CS_ClaimItemBagInfo> {
    @Override
    protected CS_ClaimItemBagInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimItemBagInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimItemBagInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimItemBagInfo.Builder resultBuilder = SC_ClaimItemBagInfo.newBuilder();

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] itemBag is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimItemBagInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(itemBag, entity -> {
            DB_ItemBag.Builder itemInfo = itemBag.getDb_data();

            //移除背包道具为 <= 0 的道具
            Set<Integer> clearIdx = new HashSet<>();

            for (Entry<Integer, Long> entry : itemInfo.getItemsMap().entrySet()) {
                if (entry.getValue() <= 0) {
                    clearIdx.add(entry.getKey());
                    continue;
                }
                ItemInfo.Builder item = ItemInfo.newBuilder();
                item.setItemCfgId(entry.getKey());
                item.setNewItemCount(entry.getValue());
                resultBuilder.addItems(item);
            }

            if (!clearIdx.isEmpty()) {
                itemBag.clearItem(clearIdx, null);
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimItemBagInfo_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ItemBag;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimItemBagInfo_VALUE, SC_ClaimItemBagInfo.newBuilder().setRetCode(retCode));
    }
}
