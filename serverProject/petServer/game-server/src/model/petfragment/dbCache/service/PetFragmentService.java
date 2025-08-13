package model.petfragment.dbCache.service;

import model.petfragment.entity.FragmentUseResult;
import model.petfragment.entity.petfragmentEntity;
import platform.logs.ReasonManager.Reason;

import java.util.Map;

/**
 * 宠物碎片业务接口
 *
 * @author xiao_FL
 * @date 2019/5/22
 */
public interface PetFragmentService {
    /**
     * 查询碎片
     *
     * @param playerId 玩家id
     * @return 碎片list
     */
    petfragmentEntity getFragmentByPlayer(String playerId);

    /**
     * 合成宠物
     *
     * @param fragmentId 碎片图鉴id
     * @param playerId   申请玩家
     * @param amount     合成数量
     * @param showReward
     * @param useAll
     * @return 操作结果：-1合成失败，否则成功并返回剩余碎片数量
     */
    FragmentUseResult useFragment(String fragmentId, String playerId, int amount, boolean showReward, boolean useAll);

    /**
     * 获得碎片，发出消息
     *
     * @param playerId    玩家id
     * @param fragmentMap 碎片图鉴id，碎片数量
     * @param reason      原因
     * @return 操作结果
     */
    boolean playerObtainFragment(String playerId, Map<Integer, Integer> fragmentMap, Reason reason);

    /**
     * 删除碎片，发出消息
     *
     * @param playerId    玩家id
     * @param fragmentMap 碎片图鉴，碎片数量
     * @return 操作结果
     */
    boolean removeFragment(String playerId, Map<Integer, Integer> fragmentMap);
}
