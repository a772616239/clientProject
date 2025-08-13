package server.handler.resRecycle;

public interface ResourceRecycleInterface {

    /**
     * 回收资源
     * @param playerId 玩家id
     * @param settleInterval 这里每一天都是按照刷新时间为一天,刷新时间前24小时内都算作一天
     */
    void resourceRecycle(String playerId, int settleInterval);
}
