package common;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import util.GameUtil;
import util.LogUtil;

abstract public class AbstractBaseHandler<T> extends AbstractHandler<T> {

    public boolean checkValid(GameServerTcpChannel gsChn) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        return player.isOnline();
    }

    @Override
    public void doAction(GameServerTcpChannel channel, int codeNum) {
        try {
            super.doAction(channel, codeNum);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * 返回改handler所属于的function
     *
     * @return
     */
    abstract public EnumFunction belongFunction();

    /**
     * 用于处理当功能关闭时的逻辑
     *
     * @param gsChn
     * @param codeNum
     */
    abstract public void doClosedActive(GameServerTcpChannel gsChn, int codeNum);
}
