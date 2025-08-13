package server.handler.resRecycle;

import cfg.FunctionOpenLvConfig;
import cfg.GameConfig;
import common.GameConst;
import common.GlobalData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Common;
import util.ClassUtil;
import util.LogUtil;

public class ResourceRecycleManager {

    @Getter
    private static ResourceRecycleManager instance = new ResourceRecycleManager();

    private static Map<Common.EnumFunction, ResourceRecycleInterface> recycleHandlerMap = new HashMap<>();

    static {
        List<Class<ResourceRecycleInterface>> classList = ClassUtil.getClassByInterface("server.handler.resRecycle.impl", ResourceRecycleInterface.class);

        for (Class<ResourceRecycleInterface> aClass : classList) {
            ResourceRecycle annotation = aClass.getAnnotation(ResourceRecycle.class);

            Common.EnumFunction function = annotation.function();

            if (Common.EnumFunction.NullFuntion == function) {
                LogUtil.error("class:{}  @ResourceRetrieve annotation cfg is null", aClass.getName());
                continue;
            }

            try {
                recycleHandlerMap.put(function, aClass.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error("class:{}  @ResourceRetrieve annotation cfg is error", aClass.getName());
            }

        }

    }

    public void recyclePlayerResource(String playerId) {
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            LogUtil.error("retrievePlayerResource error cause by player is null,playerId:{}", playerId);
            return;
        }
        int settleInterval = player.calculateResourceRecycleOfflineDay();
        if (!needRetrievePlayerResource(settleInterval)) {
            return;
        }
        for (Map.Entry<Common.EnumFunction, ResourceRecycleInterface> entry : recycleHandlerMap.entrySet()) {
            if (PlayerUtil.queryFunctionUnlock(playerId, entry.getKey())) {
                entry.getValue().resourceRecycle(playerId, settleInterval);
            }
        }
        if (GlobalData.getInstance().checkPlayerOnline(playerId)) {
            player.sendResourceRecycleInfo();
        }

    }

    private boolean needRetrievePlayerResource(int settleInterval) {
        return settleInterval <= GameConfig.getById(GameConst.CONFIG_ID).getResmaxrecycledays();
    }


    public void updateDailyData() {
        for (String playerId : playerCache.getInstance()._ix_id.keySet()) {
            recyclePlayerResource(playerId);
        }
    }
}
