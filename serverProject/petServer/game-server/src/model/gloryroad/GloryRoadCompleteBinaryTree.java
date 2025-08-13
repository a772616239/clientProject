package model.gloryroad;

import common.GlobalData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.recentpassed.RecentPassedUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.GameplayDB.GloryRoadTreeContent;
import protocol.GloryRoad.NodeMessage;
import protocol.GloryRoad.NodePlayerInfo;
import protocol.GloryRoad.SC_RefreshBattleResult;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamTypeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * 完全二叉树保存晋级路线
 * 根节点从0开始
 *
 * @author huhan
 * @date 2021/3/11
 */
public class GloryRoadCompleteBinaryTree {

    /**
     * 按照序号保存 从0开始
     */
    private final GloryRoadTreeContent.Builder[] treeNodes;

    /**
     * 总层数  从1开始
     */
    private final int maxLevel;

    /**
     * 当前总nodes数量
     */
    private int size;

    /**
     * @param bottomNodeSize 最底层的节点个数
     */
    public GloryRoadCompleteBinaryTree(int bottomNodeSize) {
        this.maxLevel = (int) Math.ceil(Math.log(bottomNodeSize) / Math.log(2)) + 1;
        int maxNodeSize = IntStream.range(0, this.maxLevel)
                .map(e -> (int) Math.pow(2, e))
                .reduce(Integer::sum)
                .getAsInt();
        this.treeNodes = new GloryRoadTreeContent.Builder[maxNodeSize];
    }

    /**
     * @param level 第几层  从1开始  从父节点开始
     * @param place 第几个位置 从1开始
     * @return
     */
    public boolean setPlayer(int level, int place, String playerIdx) {
        return setPlayer(getIndex(level, place), playerIdx);
    }

    private boolean setPlayer(int index, String playerIdx) {
        if (!assertIndex(index) || StringUtils.isEmpty(playerIdx)) {
            LogUtil.error("GloryRoadCompleteBinaryTree.setPlayer, error params, index:" + index + ", playerIdx:" + playerIdx);
            return false;
        }
        return setContent(createContent(index, playerIdx));
    }

    private synchronized boolean setContent(GloryRoadTreeContent.Builder content) {
        if (!assertContent(content)) {
            return false;
        }
        int index = content.getIndex();

        GloryRoadTreeContent.Builder oldVal = this.treeNodes[index];
        this.treeNodes[index] = content;
        
        if (oldVal == null) {
            size++;
        }
        return true;
    }

    public GloryRoadTreeContent.Builder getContent(int level, int place) {
        return getContent(getIndex(level, place));
    }

    public GloryRoadTreeContent.Builder getContent(int index) {
        if (!assertIndex(index)) {
            return null;
        }
        return this.treeNodes[index];
    }

    /**
     * @param level 从1开始  > 0
     * @param place 从1开始 > 0
     * @return
     */
    public int getIndex(int level, int place) {
        if (level <= 0 || place <= 0 || getLevelEndIndex(level) < place) {
            return -1;
        }
        return getLevelStartIndex(level) + place - 1;
    }

    public int getLevelStartIndex(int level) {
        return (int) Math.pow(2, level - 1) - 1;
    }

    public int getLevelEndIndex(int level) {
        return (int) Math.pow(2, level) - 2;
    }

    public synchronized int getContentIndex(GloryRoadTreeContent content) {
        if (content == null) {
            return -1;
        }
        for (int i = 0; i < this.treeNodes.length; i++) {
            if (this.treeNodes[i] == null) {
                continue;
            }
            if (Objects.equals(content.getPlayerIdx(), this.treeNodes[i].getPlayerIdx())) {
                return i;
            }
        }
        return -1;
    }

    public boolean assertIndex(int index) {
        int totalLength = this.treeNodes.length;
        if (index >= totalLength) {
            LogUtil.error("GloryRoadCompleteBinaryTree.assertSize, " +
                    "index is max than tree max size, index:" + index + ", max size:" + totalLength);
            return false;
        }
        return true;
    }

    public boolean assertContent(GloryRoadTreeContent.Builder content) {
        if (content == null) {
            LogUtil.error("GloryRoadCompleteBinaryTree.assertSize, content is error:" + content);
            return false;
        }
        return true;
    }

    public boolean assertLevel(int level) {
        if (GameUtil.outOfScope(1, this.maxLevel, level)) {
            LogUtil.error("GloryRoadCompleteBinaryTree.assertLevel, error param, param:" + level + ", maxLevel:" + this.maxLevel);
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return this.size <= 0;
    }

    public boolean setBottomPlayer(int place, String playerIdx) {
        return setPlayer(this.maxLevel, place, playerIdx);
    }

    public synchronized void clear() {
        Arrays.fill(this.treeNodes, null);
        this.size = 0;
    }

    public List<GloryRoadTreeContent.Builder> getPlayerInfoList(int level) {
        if (!assertLevel(level)) {
            return Collections.emptyList();
        }

        List<GloryRoadTreeContent.Builder> result = new ArrayList<>();
        for (int i = getLevelStartIndex(level); i <= getLevelEndIndex(level); i++) {
            if (this.treeNodes[i] != null) {
                result.add(this.treeNodes[i]);
            }
        }
        return result;
    }

    public List<String> getPlayerIdxList(int level) {
        List<GloryRoadTreeContent.Builder> infoList = getPlayerInfoList(level);
        if (CollectionUtils.isEmpty(infoList)) {
            return Collections.emptyList();
        }
        return infoList.stream().map(GloryRoadTreeContent.Builder::getPlayerIdx).collect(Collectors.toList());
    }

    /**
     * 更具层数获得战斗对手的信息, 第一层没有战斗
     *
     * @param level
     * @return
     */
    public List<GloryRoadOpponent> getLevelOpponent(int level) {
        if (!assertLevel(level) || level <= 1) {
            LogUtil.error("GloryRoadCompleteBinaryTree.getLevelOpponent, error params, level:" + level);
            return Collections.emptyList();
        }

        List<GloryRoadOpponent> result = new ArrayList<>();
        for (int i = getLevelStartIndex(level); i <= getLevelEndIndex(level); i += 2) {
            String playerIdx1 = getContent(i) == null ? null : getContent(i).getPlayerIdx();
            String playerIdx2 = getContent(i + 1) == null ? null : getContent(i + 1).getPlayerIdx();
            if (playerIdx1 == null && playerIdx2 == null) {
                continue;
            }
            result.add(new GloryRoadOpponent(playerIdx1, playerIdx2, getIndexParentIndex(i)));
        }
        return result;
    }

    public int getIndexParentIndex(int index) {
        return index % 2 == 0 ? (index / 2) - 1 : index / 2;
    }

    public List<NodeMessage> buildNodeMessage() {
        List<NodeMessage> result = new ArrayList<>();
        for (int i = 0; i < treeNodes.length; i++) {
            GloryRoadTreeContent.Builder node = treeNodes[i];
            if (node == null) {
                continue;
            }

            NodeMessage message = buildNodeMsg(i, node);
            if (message != null) {
                result.add(message);
            }
        }
        return result;
    }

    public NodeMessage buildNodeMsg(int index, GloryRoadTreeContent.Builder content) {
        if (content == null) {
            return null;
        }
        NodeMessage.Builder builder = NodeMessage.newBuilder();
        builder.setNodeIndex(index);
        builder.setPlayerIdx(content.getPlayerIdx());
        builder.setWin(content.getWin());
        if (content.getLinkBattleRecordId() != null) {
            builder.setLinkBattleRecordId(content.getLinkBattleRecordId());
        }
        return builder.build();
    }

    public synchronized boolean setWinPlayer(int parentIndex, String playerIdx, String linkBattleRecordId) {
        if (!assertIndex(parentIndex) || StringUtils.isEmpty(playerIdx)) {
            LogUtil.error("GloryRoadCompleteBinaryTree.setIndexWinPlayer, error param, playerIdx:" + playerIdx);
            return false;
        }
        if (this.treeNodes[parentIndex] != null) {
            LogUtil.error("GloryRoadCompleteBinaryTree.setIndexWinPlayer, index is not null, index:" + parentIndex
                    + ", curContent:" + this.treeNodes[parentIndex]);
            return false;
        }

        GloryRoadTreeContent.Builder leftSubContent = getContent(getLeftSubIndex(parentIndex));
        GloryRoadTreeContent.Builder rightSubContent = getContent(getRightSubIndex(parentIndex));
        if (leftSubContent == null && rightSubContent == null) {
            LogUtil.error("GloryRoadCompleteBinaryTree.setIndexWinPlayer, playerIdx:" + playerIdx
                    + ", both sub content is null");
            return false;
        }

        GloryRoadTreeContent.Builder winContent;
        GloryRoadTreeContent.Builder failedContent;
        if (leftSubContent != null && Objects.equals(leftSubContent.getPlayerIdx(), playerIdx)) {
            winContent = leftSubContent;
            failedContent = rightSubContent;
        } else if (rightSubContent != null && Objects.equals(rightSubContent.getPlayerIdx(), playerIdx)) {
            winContent = rightSubContent;
            failedContent = leftSubContent;
        } else {
            LogUtil.error("GloryRoadCompleteBinaryTree.setIndexWinPlayer, playerIdx:" + playerIdx
                    + ", both sub content equals, left:" + leftSubContent + ", right:" + rightSubContent);
            return false;
        }

        setPlayer(parentIndex, playerIdx);
        if (winContent != null) {
            winContent.setWin(1);
            if (StringUtils.isNotEmpty(linkBattleRecordId)) {
                winContent.setLinkBattleRecordId(linkBattleRecordId);
            }
            winContent.setRecent(RecentPassedUtil.buildRecentPassedInfo(winContent.getPlayerIdx(), TeamTypeEnum.TTE_GloryRoad));
        }

        if (failedContent != null) {
            failedContent.setWin(2);
            if (StringUtils.isNotEmpty(linkBattleRecordId)) {
                failedContent.setLinkBattleRecordId(linkBattleRecordId);
            }
            failedContent.setRecent(RecentPassedUtil.buildRecentPassedInfo(failedContent.getPlayerIdx(), TeamTypeEnum.TTE_GloryRoad));
        }

        sendRefreshNodeMsg(parentIndex);

        LogUtil.info("GloryRoadCompleteBinaryTree.setWinPlayer, set win playerIdx finished, parentsIdx:"
                + parentIndex + ", winPlayer:" + playerIdx);
        return true;
    }

    private int getLeftSubIndex(int parentIndex) {
        return (parentIndex + 1) * 2 - 1;
    }

    private int getRightSubIndex(int parentIndex) {
        return (parentIndex + 1) * 2;
    }

    public void sendRefreshNodeMsg(int parentIndex) {
        SC_RefreshBattleResult.Builder builder = SC_RefreshBattleResult.newBuilder();
        GloryRoadTreeContent.Builder parentContent = getContent(parentIndex);
        NodeMessage parentMsg = buildNodeMsg(parentIndex, parentContent);
        if (parentMsg != null) {
            builder.addNodes(parentMsg);
        }

        addBattleResultMsg(builder, getLeftSubIndex(parentIndex));
        addBattleResultMsg(builder, getRightSubIndex(parentIndex));

        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_RefreshBattleResult, builder, GloryRoadUtil.LV_CONDITION);
    }

    private void addBattleResultMsg(SC_RefreshBattleResult.Builder builder, int index) {
        GloryRoadTreeContent.Builder content = getContent(index);
        if (content != null) {
            NodePlayerInfo playerInfo = GloryRoadUtil.buildNodePlayerInfo(content.getPlayerIdx());
            if (playerInfo != null) {
                builder.addPlayerInfo(playerInfo);
            }

            NodeMessage subLeftMsg = buildNodeMsg(index, content);
            if (subLeftMsg != null) {
                builder.addNodes(subLeftMsg);
            }
        }
    }

    public synchronized List<GloryRoadTreeContent> getContentDbList() {
        return Arrays.stream(this.treeNodes)
                .filter(Objects::nonNull)
                .map(GloryRoadTreeContent.Builder::build)
                .collect(Collectors.toList());
    }

    public void revertContent(List<GloryRoadTreeContent> contentList) {
        if (CollectionUtils.isEmpty(contentList)) {
            return;
        }

        for (GloryRoadTreeContent gloryRoadTreeContent : contentList) {
            if (!assertIndex(gloryRoadTreeContent.getIndex())) {
                return;
            }

            setContent(gloryRoadTreeContent.toBuilder());
        }
    }

    private GloryRoadTreeContent.Builder createContent(int index, String playerIdx) {
        return GloryRoadTreeContent.newBuilder().setIndex(index).setPlayerIdx(playerIdx);
    }

    public int getParentIndex(int subIndex) {
        int result;
        if (subIndex % 2 == 0) {
            result = subIndex / 2 - 1;
        } else {
            result = subIndex / 2;
        }
        LogUtil.info("GloryRoadCompleteBinaryTree.getParentIndex, sub index:" + subIndex + ",result:" + result);
        return result;
    }

    public List<GloryRoadOpponent> getLevelUnSettleOpponent(int level) {
        if (!assertLevel(level) || level <= 1) {
            LogUtil.error("GloryRoadCompleteBinaryTree.getLevelOpponent, error params, level:" + level);
            return Collections.emptyList();
        }

        List<GloryRoadOpponent> result = new ArrayList<>();
        for (int i = getLevelStartIndex(level); i <= getLevelEndIndex(level); i += 2) {
            String playerIdx1 = getContent(i) == null ? null : getContent(i).getPlayerIdx();
            String playerIdx2 = getContent(i + 1) == null ? null : getContent(i + 1).getPlayerIdx();
            if (playerIdx1 == null && playerIdx2 == null) {
                continue;
            }

            //父节点不为空
            if (getContent(getParentIndex(i)) != null) {
                continue;
            }

            result.add(new GloryRoadOpponent(playerIdx1, playerIdx2, getIndexParentIndex(i)));
        }

        return result;
    }
}
