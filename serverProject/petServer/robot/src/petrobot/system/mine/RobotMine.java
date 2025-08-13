package petrobot.system.mine;

import lombok.Getter;
import lombok.Setter;
import petrobot.robot.Robot;
import protocol.MineFight.FriendHelpDisplayInfo;
import protocol.MineFight.MineGiftObj;
import protocol.MineFight.MineInfo;
import protocol.MineFight.MineRewardInfo;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class RobotMine {
    private Robot owner;

    private int freeExploitScroll;
    private Map<String, MineInfo.Builder> mineInfoMap;
    private Map<Integer, MineGiftObj> mineGiftObjMap;

    private Map<Integer, MineRewardInfo> mineRewardMap;
    private Map<Integer, String> ownedMineInfo;
    private String attackingMineIdx;

    private long helpExpireTime;
    private FriendHelpDisplayInfo currentBeHelpingInfo;

    private List<FriendHelpDisplayInfo> friendHelpList;

    public RobotMine(Robot robot) {
        this.owner = robot;
        mineInfoMap = new ConcurrentHashMap<>();
        mineGiftObjMap = new ConcurrentHashMap<>();
        mineRewardMap = new ConcurrentHashMap<>();
        ownedMineInfo = new ConcurrentHashMap<>();
        friendHelpList = new Vector<>();
    }

    public void clear() {
        freeExploitScroll = 0;
        mineInfoMap.clear();
        mineGiftObjMap.clear();
        mineRewardMap.clear();
        ownedMineInfo.clear();
    }
}
