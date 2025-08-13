package model.patrol.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.HashCodeBuilder;
import protocol.Patrol.PatrolPoint;
import util.PatrolUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡逻队地图
 *
 * @author xiao_FL
 * @date 2019/7/29
 */
public final class PatrolTree {
    /**
     * 起始点/默认点
     */
    public static final int EVENT_BEGIN_DEFAULT = 0;
    /**
     * 路径点
     */
    public static final int EVENT_PATH = 1;
    /**
     * 宝箱点
     */
    public static final int EVENT_TREASURE = 2;
    /**
     * 探索点
     */
    public static final int EVENT_EXPLORE = 3;
    /**
     * 小怪点
     */
    public static final int EVENT_BASTARD = 4;
    /**
     * boss点
     */
    public static final int EVENT_BOSS = 5;
    /**
     * 密室点
     */
    public static final int EVENT_CHAMBER = 6;

    /**
     * Boss钥匙
     */
    public static final int BOSS_KEY = 7;
    /**
     * 旅行商人
     */
    public static final int TRAVELING_SALESMAN = 8;
    /**
     * 空格
     */
    public static final int EVENT_EMPTY = 9;

    /**
     * 标记
     */
    private int id;

    /**
     * 父节点
     */
    private PatrolTree parentPoint;

    /**
     * 子节点
     */
    private List<PatrolTree> childList;

    /**
     * 横坐标
     */
    private int x;

    /**
     * 纵坐标
     */
    private int y;

    /**
     * 事件点类型
     */
    private int pointType;

    /**
     * 如果是探索点，预存3个探索事件的buff id
     */
    private int[] exploreStatus;

    /**
     * 0未被探索过；1探索过
     */
    private int explored;

    /**
     * 0分支线路；1主线路
     */
    private int main;

    /**
     * 分支数
     */
    private int degree;

    /**
     * 如果是战斗点，预存makeId
     */
    private int fightMakeId;

    /**
     * 如果是密室点，预存pet信息
     */
    private List<PatrolPet> petList;

    @Getter
    @Setter
    private int treasureGreedConfig;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PatrolTree getParentPoint() {
        return parentPoint;
    }

    public void setParentPoint(PatrolTree parentPoint) {
        this.parentPoint = parentPoint;
    }

    public List<PatrolTree> getChildList() {
        return childList;
    }

    public void setChildList(List<PatrolTree> childList) {
        this.childList = childList;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPointType() {
        return pointType;
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }

    public int[] getExploreStatus() {
        return exploreStatus;
    }

    public void setExploreStatus(int[] exploreStatus) {
        this.exploreStatus = exploreStatus;
    }

    public int getExplored() {
        return explored;
    }

    public void setExplored(int explored) {
        this.explored = explored;
    }

    public int getMain() {
        return main;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getFightMakeId() {
        return fightMakeId;
    }

    public void setFightMakeId(int fightMakeId) {
        this.fightMakeId = fightMakeId;
    }

    public List<PatrolPet> getPetList() {
        return petList;
    }

    public void setPetList(List<PatrolPet> petList) {
        this.petList = petList;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(x).append(y).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PatrolTree) {
            return ((PatrolTree) obj).getX() == x
                    && ((PatrolTree) obj).getY() == y;
        }
        return false;
    }

    public PatrolTree() {
    }

    /**
     * 生成待寻找点，用于遍历
     *
     * @param x 点x坐标
     * @param y 点x坐标
     */
    public PatrolTree(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 本点在主线还是支线
     *
     * @return 是否在主线
     */
    public boolean ifMainBranch() {
        return main == 1;
    }

    /**
     * 本点是否被探索过
     *
     * @return 是否探索过
     */
    public boolean ifExplored() {
        return explored == 1;
    }

    /**
     * 本节点是否可达：本节点/父节点/子节点被探索过（排除路径点）
     *
     * @return 是否可达
     */
    public boolean ifReachable() {
        if (explored == 1) {
            return true;
        } else if (parentPoint != null && parentPoint.ifExplored()) {
            return true;
        } else {
            if (childList != null) {
                for (PatrolTree patrolTree : childList) {
                    if (patrolTree.getChildList() != null) {
                        for (PatrolTree tree : patrolTree.getChildList()) {
                            if (tree.ifExplored()) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    public boolean ifBattlePoint() {
        return pointType == EVENT_BASTARD || pointType == EVENT_BOSS;
    }

    /**
     * 获得主分支上的分支节点和分支
     *
     * @param tree 分支起始点
     * @return 分支信息
     */
    public static void getMainChildTree(PatrolTree tree, PatrolChildTree patrolChildTree) {
        patrolChildTree.getBranch().add(PatrolUtil.getPointByTree(tree));
        // 有分支，放入分支节点
        if (tree.getChildList() != null) {
            if (tree.getChildList().size() > 1) {
                patrolChildTree.getDegreeMap().put(tree, tree.getChildList().size() - 1);
            }
            tree = tree.getChildList().get(0);
            getMainChildTree(tree, patrolChildTree);
        }
    }

    /**
     * 获得支线分支
     *
     * @param tree   起始点
     * @param number 子节点个数
     * @return 分支
     */
    public static List<PatrolPoint> getMainChildTree(PatrolTree tree, int number) {
        List<PatrolPoint> branch = new ArrayList<>();
        branch.add(PatrolUtil.getPointByTree(tree));
        branch.add(PatrolUtil.getPointByTree(tree.getChildList().get(number)));
        tree = tree.getChildList().get(number);
        getChild(tree, branch);
        return branch;
    }

    private static void getChild(PatrolTree tree, List<PatrolPoint> branch) {
        if (tree.getChildList() != null) {
            // 地图点类型转换成协议需要类型
            branch.add(PatrolUtil.getPointByTree(tree.getChildList().get(0)));
            tree = tree.getChildList().get(0);
            getChild(tree, branch);
        }
    }
}
