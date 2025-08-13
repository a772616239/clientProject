package server.handler.resRecycle.impl;

import protocol.Common;
import server.handler.resRecycle.ResourceRecycle;
import server.handler.resRecycle.ResourceRecycleHelper;
import server.handler.resRecycle.ResourceRecycleInterface;

/**
 * 神器遗迹资源回收
 */
@ResourceRecycle(function = Common.EnumFunction.ArtifactRes)
public class ArtifactResRecycle implements ResourceRecycleInterface {

    @Override
    public void resourceRecycle(String playerId, int settleInterval) {
        ResourceRecycleHelper.resourceCopyRecycle(playerId, Common.EnumFunction.ArtifactRes,settleInterval);
    }

}
