package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_ProvideGoods;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_ProvideGoods_VALUE)
public class ProvideMistItemHandler extends AbstractHandler<GS_CS_ProvideGoods> {
    @Override
    protected GS_CS_ProvideGoods parse(byte[] bytes) throws Exception {
        return GS_CS_ProvideGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ProvideGoods req, int i) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            LogUtil.error("ProvideGoods Error, not fount player:" + req.getPlayerIdx());
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            LogUtil.error("ProvideGoods Error, not in mist room:" + req.getPlayerIdx());
            return;
        }
        SyncExecuteFunction.executeConsumer(room, mistRoom -> mistRoom.provideMistItem(player, req.getItemCfgId()));
    }
}
