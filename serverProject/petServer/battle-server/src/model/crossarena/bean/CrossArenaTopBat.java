package model.crossarena.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import protocol.CrossArenaDB.RedisCrossArenaTopBat;

public class CrossArenaTopBat {

    private String groupId;
    
    private Map<Long, Integer> batingMap = new HashMap<>();
    /**
     * 缓存玩家禁止战斗得ID
     */
    private Map<String, List<String>> notBattle = new ConcurrentHashMap<>();

    public CrossArenaTopBat(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public Map<String, List<String>> getNotBattle() {
        return notBattle;
    }

    public void setNotBattle(Map<String, List<String>> notBattle) {
        this.notBattle = notBattle;
    }

    public RedisCrossArenaTopBat addBattleAI(long battId, int[] res, String p1, String p2) {
    	RedisCrossArenaTopBat.Builder bating = RedisCrossArenaTopBat.newBuilder();
        bating.setRoomid(battId);
        bating.setPy1(p1);
        bating.setPy2(p2);
        bating.setCampWin(res[0]);
        bating.setEndTime(System.currentTimeMillis() + res[1] * 1000L);
        bating.setIsAI(1);
        batingMap.put(battId, 0);
        List<String> t1 = notBattle.computeIfAbsent(p1, k->new LinkedList<>());
        if (t1.size() > 2) {
            t1.remove(0);
            t1.add(p2);
        }
        List<String> t2 = notBattle.computeIfAbsent(p2, k->new LinkedList<>());
        if (t2.size() > 2) {
            t2.remove(0);
            t2.add(p1);
        }
        return bating.build();
    }

    public RedisCrossArenaTopBat addBattle(long battId, String p1, String p2) {
    	RedisCrossArenaTopBat.Builder bating = RedisCrossArenaTopBat.newBuilder();
        bating.setRoomid(battId);
        bating.setPy1(p1);
        bating.setPy2(p2);
        bating.setEndTime(System.currentTimeMillis() + 300000L);
        batingMap.put(battId, 0);
        List<String> t1 = notBattle.computeIfAbsent(p1, k->new LinkedList<>());
        if (t1.size() > 2) {
            t1.remove(0);
            t1.add(p2);
        }
        List<String> t2 = notBattle.computeIfAbsent(p2, k->new LinkedList<>());
        if (t2.size() > 2) {
            t2.remove(0);
            t2.add(p1);
        }
        return bating.build();
    }

    /**
     * 两个玩家是否可以战斗
     * @param p1
     * @param p2
     * @return
     */
    public boolean canBattlePlay(String p1, String p2) {
        return !notBattle.getOrDefault(p1, new ArrayList<>()).contains(p2);
    }

	public Map<Long, Integer> getBatingMap() {
		return batingMap;
	}

	public void setBatingMap(Map<Long, Integer> batingMap) {
		this.batingMap = batingMap;
	}

}
