package model.mainLine.manager;
/**
 * @function 保存玩家最近通关信息，和通关排行榜数据
 */

import cfg.GameConfig;
import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.PlayerLevelConfig;
import common.GameConst;
import common.entity.RankingQuerySingleResult;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.util.MainLineUtil;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.ranking.ranking.AbstractRanking;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity;
import util.ArrayUtil;
import util.LogUtil;

import java.util.List;
import java.util.Map;

public class MainLineManager {
    private static MainLineManager instance = new MainLineManager();

    public static MainLineManager getInstance() {
        if (instance == null) {
            synchronized (MainLineManager.class) {
                if (instance == null) {
                    instance = new MainLineManager();
                }
            }
        }
        return instance;
    }

    private MainLineManager() {
    }

    public boolean init() {
        return checkMainLineCfg();
    }

    /**
     * 检查主线关卡的配置是否正确
     *
     * @return
     */
    private boolean checkMainLineCfg() {
        Map<Integer, MainLineCheckPointObject> ix_id = MainLineCheckPoint._ix_id;
        if (ix_id == null || ix_id.isEmpty()) {
            LogUtil.error("MainLineCheckPoint cfg is null");
            return false;
        }

        for (MainLineCheckPointObject value : ix_id.values()) {
            //检查关卡解锁等级是否设置有误
            if (value.getUnlocklv() > PlayerLevelConfig.maxLevel) {
                LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] error cfg, unlockLv");
                return false;
            }

            int[] nodeList = value.getNodelist();
            //检查关卡中的nodeList，是否存在，以及每一个node的linkNode是否存在,以及nodeList是否有重复的元素
            if (!MainLineUtil.checkNodeList(nodeList) || ArrayUtil.isHaveRepeatedElement(nodeList)) {
                LogUtil.error("checkPoint nodeList Cfg error, checkPointId = " + value.getId());
                return false;
            }

            switch (value.getType()) {
                //普通关卡
                case 0:
                    if (nodeList.length != 1
                            || !MainLineUtil.checkNodeListType(value.getNodelist(), new int[]{1, 2, 5})) {
                        LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] lineUp cfg error");
                        return false;
                    }
                    break;
                //密码型
                case 1:
                    if (!MainLineUtil.checkNodeListType(value.getNodelist(), new int[]{0, 3})) {
                        LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] order or nodeList error");
                        return false;
                    }

                    //密码个数
                    int nodeLength_1 = value.getSubtype() + 1;

                    //此处检查order和lineUp的长度是否对应
                    if (MainLineUtil.getNodeTypeCount(value.getNodelist(), 3) != nodeLength_1
                            || !ArrayUtil.haveSameElementExclusionOrder(nodeList, value.getCorrectorder())) {
                        LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] correctOrder error");
                        return false;
                    }
                    break;
                //破阵型
                case 2:
                    if (value.getCorrectorder() == null
                            || !MainLineUtil.checkNodeListType(value.getNodelist(), new int[]{0, 1, 2})) {
                        LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] order error");
                        return false;
                    }

                    int orderLength_2 = 0;
                    switch (value.getSubtype()) {
                        case 1:
                            orderLength_2 = 3;
                            break;
                        case 2:
                            orderLength_2 = 4;
                            break;
                        case 3:
                            orderLength_2 = 5;
                            break;
                        case 4:
                            orderLength_2 = 6;
                            break;
                        case 5:
                            orderLength_2 = 9;
                            break;
                        default:
                            LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] unknown subType");
                            return false;
                    }

                    //此处检查order和lineUp的长度是否对应,只允许有一个boss类型
                    if (MainLineUtil.getNodeTypeCount(value.getNodelist(), new int[]{1, 2}) != orderLength_2
                            || MainLineUtil.getNodeTypeCount(value.getNodelist(), 2) != 1) {
                        LogUtil.error("MainLineCheckPoint,id[" + value.getId() + "] nodeList or order cfg error");
                        return false;
                    }
                    break;
                //歧路
                case 3:
                    if (!MainLineUtil.checkNodeListType(value.getNodelist(), new int[]{1, 2})
                            || MainLineUtil.getNodeTypeCount(value.getNodelist(), 2) != 1) {
                        LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] lineUp error, " +
                                "node type is not  1 or 2, or node type 2 is not only one");
                        return false;
                    }
                    break;
                //传送
                case 4:
                    if (!MainLineUtil.checkNodeListType(value.getNodelist(), new int[]{0, 1, 2, 4})
                            || !MainLineUtil.endWithType(value.getNodelist(), new int[]{1, 2})) {
                        LogUtil.error("MainLineCheckPoint,id[" + value.getId() + "] nodeList cfg error, ");
                        return false;
                    }

                    for (int i : nodeList) {
                        MainLineNodeObject nodeCfg_4 = MainLineNode.getById(i);
                        if (nodeCfg_4 == null) {
                            LogUtil.error("MainLineNodeCfg is null, nodeId = " + i);
                            return false;
                        }

                        if (nodeCfg_4.getNodetype() == 4) {
                            int transferTarget = nodeCfg_4.getParam();
                            if (!ArrayUtil.intArrayContain(nodeList, transferTarget)) {
                                LogUtil.error("MainLineCheckPoint， transferNode target is out of range");
                                return false;
                            }

                            MainLineNodeObject targetNodeCfg = MainLineNode.getById(transferTarget);
                            if (targetNodeCfg == null || targetNodeCfg.getNodetype() == 4) {
                                LogUtil.error("Unsupported multi transfer ");
                                return false;
                            }
                        }
                    }
                    break;
                //秘境
                case 5:
                    if (!MainLineUtil.checkNodeListType(value.getNodelist(), new int[]{0, 1, 2})
                            || MainLineUtil.getNodeTypeCount(value.getNodelist(), 2) != 1) {
                        LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] nodeList cfg error");
                        return false;
                    }
                    break;
                default:
                    LogUtil.error("MainLineCheckPoint,id[ " + value.getId() + "] unSupport type");
                    return false;
            }
        }
        return true;
    }

    public int findWorldMapMainLineNode() {
        AbstractRanking ranking = RankingManager.getInstance().getRanking(Activity.EnumRankingType.ERT_MainLine, RankingUtils.getRankingTypeDefaultName(Activity.EnumRankingType.ERT_MainLine));
        if (ranking == null) {
            return 0;
        }
        List<RankingQuerySingleResult> rankingTotalInfoList =
                ranking.getRankingTotalInfoList();
        if (CollectionUtils.isEmpty(rankingTotalInfoList)) {
            return 0;
        }
        int worldLvRank = GameConfig.getById(GameConst.CONFIG_ID).getWorldlvrank();
        int sum = 0;
        int num = 0;
        for (RankingQuerySingleResult item : rankingTotalInfoList) {
            if (item.getRanking() > worldLvRank) {
                continue;
            }
            int playerCurNode = item.getIntPrimaryScore();
            LogUtil.debug("findWorldMapMainLineNode playerIdx:{}, rank:{} ,score:{}", item.getPrimaryKey(), item.getRanking(), playerCurNode);
            if (playerCurNode <= 0) {
                continue;
            }
            sum += playerCurNode;
            num++;
        }
        LogUtil.debug("MainLineManager findWorldMapMainLineNode claimRankSize:{},totalPassNode:{},totalNum:{}", rankingTotalInfoList.size(), sum, num);
        if (num > 0) {
            return sum / num;
        }
        return 0;
    }
}
