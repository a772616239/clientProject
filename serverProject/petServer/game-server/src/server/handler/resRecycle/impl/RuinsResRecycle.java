package server.handler.resRecycle.impl;

import protocol.Common;
import server.handler.resRecycle.ResourceRecycle;
import server.handler.resRecycle.ResourceRecycleHelper;
import server.handler.resRecycle.ResourceRecycleInterface;

/**
 * 觉醒遗迹生命回收
 */
@ResourceRecycle(function = Common.EnumFunction.RuinsRes)
public class RuinsResRecycle implements ResourceRecycleInterface {

    @Override
    public void resourceRecycle(String playerId, int settleInterval) {
        ResourceRecycleHelper.resourceCopyRecycle(playerId, Common.EnumFunction.RuinsRes, settleInterval);
    }


}
