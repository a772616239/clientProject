package server.handler.resRecycle.impl;

import protocol.Common;
import server.handler.resRecycle.ResourceRecycle;
import server.handler.resRecycle.ResourceRecycleHelper;
import server.handler.resRecycle.ResourceRecycleInterface;

/**
 * 生命遗迹资源回收
 */
@ResourceRecycle(function = Common.EnumFunction.RelicsRes)
public class RelicsResRecycle implements ResourceRecycleInterface {

    @Override
    public void resourceRecycle(String playerId, int settleInterval) {
        ResourceRecycleHelper.resourceCopyRecycle(playerId, Common.EnumFunction.RelicsRes, settleInterval);
    }


}
