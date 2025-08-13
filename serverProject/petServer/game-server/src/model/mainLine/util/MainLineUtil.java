package model.mainLine.util;

import cfg.*;
import common.GameConst;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.reward.RewardUtil;
import protocol.PetMessage.PetProperty;
import util.ArrayUtil;
import util.LogUtil;
import util.TimeUtil;

public class MainLineUtil {

    /**
     * 获取计算兑换活动道具掉落的间隔
     * @return
     */
    public static long getCalculateDropInterval() {
       return GameConfig.getById(GameConst.CONFIG_ID).getDropmainlineinterval() * TimeUtil.MS_IN_A_S;
    }

    public static long getOnHookCalculateInterval() {
        return GameConfig.getById(GameConst.CONFIG_ID).getMainlineonhookrefreash() * TimeUtil.MS_IN_A_S;
    }


    /**
     * 检查当前关卡是否已经完成所有的进度
     *
     * @param nodeList
     * @param progressList
     * @return
     */
    public static boolean isFinished(int[] nodeList, List<Integer> progressList) {
        if (nodeList == null || progressList == null) {
            return false;
        }

        for (int i = 0; i < nodeList.length; i++) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(nodeList[i]);
            if (nodeCfg == null || nodeCfg.getNodetype() == 0) {
                continue;
            }

            if (!progressList.contains(nodeList[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * 歧路型关卡节点是否可达
     *
     * @param progressList
     * @param nodeId
     * @return
     */
    public static boolean nodeCanReach(List<Integer> progressList, int[] nodeList, int nodeId) {
        if (progressList == null) {
            return false;
        }

        if (progressList.isEmpty()) {
            return isFirstNode(nodeList, nodeId);
        }

        for (Integer integer : progressList) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(integer);
            if (nodeCfg == null) {
                LogUtil.error("MainLineNodeCfg is null, nodeId = " + integer);
                continue;
            }

            if (ArrayUtil.intArrayContain(nodeCfg.getAfternodeid(), nodeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为当前关卡的第一个节点
     *
     * @param nodeList
     * @param nodeId
     * @return
     */
    private static boolean isFirstNode(int[] nodeList, int nodeId) {
        return ArrayUtil.getMinInt(nodeList, 0) == nodeId;
    }

    /**
     * 迷阵型关卡用于判断是否能够到达目标节点，只判断当前位置的可达节点
     *
     * @param targetNode
     * @param containTransfer 是否包含传送节点传送后的位置
     * @return
     */
    public static boolean nodeCanReach(List<Integer> unlockNodeList, int targetNode, boolean containTransfer) {

        if (unlockNodeList == null || unlockNodeList.isEmpty()) {
            return false;
        }

        for (int anInt : unlockNodeList) {
            if (anInt == targetNode) {
                return true;
            }

            if (containTransfer) {
                MainLineNodeObject cfg = MainLineNode.getById(anInt);
                if (cfg == null) {
                    continue;
                }

                //不支持连续传送
                if (cfg.getNodetype() == 4 && cfg.getParam() == targetNode) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查checkPoint中nodeList的配置是否正确
     *
     * @param nodeList
     * @return
     */
    public static boolean checkNodeList(int[] nodeList) {
        if (nodeList == null) {
            return false;
        }

        for (int i : nodeList) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(i);
            if (nodeCfg == null) {
                LogUtil.error("mainline node cfg is null, id = " + i);
                return false;
            }

            if (!checkLinkNodeList(nodeCfg.getPrevnodeid()) || !checkLinkNodeList(nodeCfg.getAfternodeid())) {
                LogUtil.error("mainline node link node cfg is error, id = " + i);
                return false;
            }

            if (nodeCfg.getNodetype() == 4) {
                if (MainLineNode.getById(nodeCfg.getParam()) == null) {
                    LogUtil.error("mainline node param node cfg is error, id = " + i + ", type = " + nodeCfg.getNodetype());
                    return false;
                }
            }

            if (nodeCfg.getNodetype() == 1 || nodeCfg.getNodetype() == 2) {
                if (FightMake.getById(nodeCfg.getFightmakeid()) == null) {
                    LogUtil.error("fightMakeId[" + nodeCfg.getFightmakeid() + "] not exist");
                    return false;
                }
            }

            //检查属性配置
            if (!checkPropertyLegality(nodeCfg.getEnhance()) || !checkPropertyLegality(nodeCfg.getWeaken())) {
                LogUtil.error("mainline node Property cfg is error, id = " + i);
                return false;
            }

            if (nodeCfg.getOnhookable()) {
                if (!RewardUtil.checkRewardByIntArr(nodeCfg.getOnhookresourceoutput())
                        || !RewardUtil.checkRandomReward(nodeCfg.getOnhookrandompool())) {
                    LogUtil.error("MainLineCheckPoint,id[" + nodeCfg.getId() + "]  onHookOutPut cfg error,");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查node的link node是否存在，0 特殊处理
     *
     * @param linkNodeList
     * @return
     */
    public static boolean checkLinkNodeList(int[] linkNodeList) {
        if (linkNodeList == null) {
            return false;
        }

        for (int i : linkNodeList) {
            if (i == 0) {
                continue;
            }
            if (MainLineNode.getById(i) == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查node配置的属性增强或者减弱是否有误
     *
     * @param propertyArr
     * @return
     */
    public static boolean checkPropertyLegality(int[][] propertyArr) {
        if (propertyArr == null) {
            return true;
        }

        for (int[] intArr : propertyArr) {
            if (intArr.length != 2) {
                LogUtil.error("propertyArr eachLength error");
                return false;
            }

            PetProperty petProperty = PetProperty.forNumber(intArr[0]);
            if (petProperty == null || petProperty == PetProperty.NULL) {
                LogUtil.error("PetProperty, unsupported property enum, num = " + intArr[0]);
                return false;
            }
        }

        return true;
    }

    /**
     * 根据指定的checkPoint,nodeList计算给定nodeType的个数
     *
     * @param nodeList
     * @param nodeType
     * @return
     */
    public static int getNodeTypeCount(int[] nodeList, int nodeType) {
        if (nodeList == null) {
            return 0;
        }

        int typeCount = 0;
        for (int i : nodeList) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(i);
            if (nodeCfg == null) {
                LogUtil.error("MainLineNodeCfg is null, nodeId = " + i);
                continue;
            }

            if (nodeCfg.getNodetype() == nodeType) {
                typeCount++;
            }
        }

        return typeCount;
    }

    public static int getNodeTypeCount(int[] nodeList, int[] nodeType) {
        int sum = 0;
        if (nodeList == null || nodeType == null) {
            return 0;
        }

        for (int type : nodeType) {
            sum += getNodeTypeCount(nodeList, type);
        }

        return sum;
    }

    /**
     * @param nodeList
     * @param typeList
     * @return
     */
    public static boolean checkNodeListType(int[] nodeList, int[] typeList) {
        if (nodeList == null || typeList == null) {
            return false;
        }

        for (int i : nodeList) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(i);
            if (nodeCfg == null) {
                LogUtil.error("MainLineNodeCfg is null, nodeId = " + i);
                return false;
            }

            if (!ArrayUtil.intArrayContain(typeList, nodeCfg.getNodetype())) {
                LogUtil.error("MainLineNodeCfg type is error , node id= " + i);
                return false;
            }

        }
        return true;
    }

    public static boolean CheckPointIsUnlock(int level, int checkPoint) {
        MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(checkPoint);
        if (checkPointCfg == null) {
            return false;
        }

        return checkPointCfg.getUnlocklv() <= level;
    }

    /**
     * 检查密码是否输入完毕（只适用于进入战斗中）
     *
     * @param progressList
     * @param nodeList
     * @return
     */
    public static boolean isInputPswFinished(List<Integer> progressList, int[] nodeList) {
        if (progressList == null || nodeList == null) {
            return false;
        }

        List<Integer> pswOrder = new ArrayList<>();
        for (int i : nodeList) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(i);
            if (nodeCfg == null || nodeCfg.getNodetype() != 3) {
                continue;
            }

            pswOrder.add(i);
        }

        return progressList.containsAll(pswOrder);
    }


    public static boolean intContainAll(List<Integer> proList, int[] intArr) {
        if (proList == null || intArr == null) {
            return false;
        }

        for (int i : intArr) {
            if (!proList.contains(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean pswIsRight(int pointId, List<Integer> progress) {
        MainLineCheckPointObject cfg = MainLineCheckPoint.getById(pointId);
        if (cfg == null || cfg.getType() != 1|| progress == null || progress.isEmpty()) {
            return false;
        }

        int[] correctOrderList = cfg.getCorrectorder();
        if (correctOrderList == null || correctOrderList.length <= 0) {
            return false;
        }

        int length = Math.min(progress.size(), correctOrderList.length);

        for (int i = 0; i < length; i++) {
            MainLineNodeObject byId = MainLineNode.getById(correctOrderList[i]);
            if (byId != null && byId.getNodetype() == 3) {
                if (correctOrderList[i] != progress.get(i)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 返回加强系数,破阵关卡适用
     *
     * @param curCheckPoint
     * @param progressList
     * @return 0-100
     */
    public static int calculateEnhanceFactor(int curCheckPoint, List<Integer> progressList) {
        MainLineCheckPointObject byId = MainLineCheckPoint.getById(curCheckPoint);
        if (byId == null || byId.getType() != 2 || progressList == null) {
            return 100;
        }

        int containCount = 0;
        for (int nodeId : byId.getNodelist()) {
            if (progressList.contains(nodeId)) {
                containCount ++;
            }
        }

        if (containCount == 0) {
            return 100;
        }

        return 100 - (100 * containCount) / getNodeTypeCount(byId.getNodelist(), 1) ;
    }

    public static int[] removeAlreadyPassed(int[] afterNodeId, List<Integer> progressList) {
        if (afterNodeId == null) {
            return new int[0];
        }

        if (progressList == null) {
            return afterNodeId;
        }

        List<Integer> resultList = new ArrayList<>();
        for (int i : afterNodeId) {
            if (!progressList.contains(i)) {
                resultList.add(i);
            }
        }

        return parseIntListToIntArr(resultList);
    }

    public static int[] parseIntListToIntArr(List<Integer> intList) {
        if (intList == null) {
            return null;
        }

        int[] result = new int[intList.size()];
        for (int i = 0; i < intList.size(); i++) {
            result[i] = intList.get(i);
        }
        return result;
    }

    /**
     * 是否是按照顺畅击杀小怪,相对顺序，对应序号的node相等则不增强，适用于破阵型关卡
     *
     * 新顺序=正确顺序移除已经通过的节点
     * 判断当前节点是否是新顺序的第一个节点
     * @param nodeId         当前节点
     * @param progressList   已经通过的节点
     * @param correctOrder   正确顺序
     * @return
     */
    public static boolean killMonsterByOrder(int nodeId, List<Integer> progressList, int[] correctOrder) {
        if (correctOrder == null || progressList == null) {
            return false;
        }

        for (int order : correctOrder) {
            if (!progressList.contains(order)) {
                return nodeId == order;
            }
        }

        return false;
    }

    /**
     * 指定关卡的节点是否是以指定类型节点结束
     * @param nodeList
     * @param ints
     * @return
     */
    public static boolean endWithType(int[] nodeList, int[] ints) {
        if (nodeList == null || nodeList.length <= 0 || ints == null || ints.length <= 0) {
            return false;
        }

        // 任何一个节点的next node不存在于当前关卡节点内为最后一个节点
        int lastNode = -1;
        for (int i : nodeList) {
            MainLineNodeObject byId = MainLineNode.getById(i);
            if (byId == null) {
                LogUtil.info("main line node id = " + i + ", is not exist");
                return false;
            }

            if (byId.getAfternodeid().length <= 0) {
                continue;
            }

            if (!ArrayUtil.haveDuplicateElement(nodeList, byId.getAfternodeid())) {
                lastNode = i;
                break;
            }
        }

        if (lastNode == -1) {
            return false;
        }
        MainLineNodeObject byId = MainLineNode.getById(lastNode);
        return ArrayUtil.intArrayContain(ints, byId.getNodetype());
    }

    /**
     * 过滤掉指定类型的节点
     * @param nodes   节点列表
     * @param type   节点类型
     * @return
     */
    public Set<Integer> filterSpecialType(int[] nodes, int type) {
        Set<Integer> result = new HashSet<>();
        if (nodes == null) {
            return result;
        }

        for (int node : nodes) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(node);
            if (nodeCfg == null || nodeCfg.getNodetype() == type) {
                result.add(node);
            }
        }

        return result;
    }

    public static int getNodeType(int nodeId) {
        MainLineNodeObject cfg = MainLineNode.getById(nodeId);
        return cfg == null ? 0 : cfg.getNodetype();
    }
}
