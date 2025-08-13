package model.activity.entity;

import cfg.PetBaseProperties;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import protocol.Activity.LotteryResult;
import util.GameUtil;
import util.LogUtil;

/**
 * eg:  length = 3,width = 3
 * /----0-----1-----2   x
 * 0--*(0)--*(1)--*(2)
 * 1--*(3)--*(4)--*(5)
 * 2--*(6)--*(7)--*(8)
 * /
 * y
 */

@Getter
@Setter
public class Lottery {
    /**
     * 水平向左
     */
    private static final int DIRECTION_HORIZONTAL_LEFT = 1;
    /**
     * 水平向右
     **/
    private static final int DIRECTION_HORIZONTAL_RIGHT = 2;
    /**
     * 垂直向上
     */
    private static final int DIRECTION_VERTICAL_UP = 3;
    /**
     * 垂直方向
     **/
    private static final int DIRECTION_VERTICAL_DOWN = 4;
    /**
     * 斜下
     **/
    private static final int DIRECTION_SLANT_DOWN = 5;
    /**
     * 斜上
     **/
    private static final int DIRECTION_SLANT_UP = 6;


    private int length;
    private int width;
    private int[][] context;

    /**
     * 所有查找的方向,包括斜向
     */
    private static final List<Integer> TOTAL_DIRECTION_CONTAIN_SLANT;

    static {
        List<Integer> temporary = new ArrayList<>();
        temporary.add(DIRECTION_HORIZONTAL_RIGHT);
        temporary.add(DIRECTION_VERTICAL_DOWN);
        temporary.add(DIRECTION_SLANT_DOWN);
        temporary.add(DIRECTION_SLANT_UP);
        TOTAL_DIRECTION_CONTAIN_SLANT = Collections.unmodifiableList(temporary);
    }

    /**
     * 所有查找的方向,不包括斜向
     */
    private static final List<Integer> TOTAL_DIRECTION_NOT_CONTAIN_SLANT;

    static {
        List<Integer> temporary = new ArrayList<>();
        temporary.add(DIRECTION_HORIZONTAL_RIGHT);
        temporary.add(DIRECTION_VERTICAL_DOWN);
        TOTAL_DIRECTION_NOT_CONTAIN_SLANT = Collections.unmodifiableList(temporary);
    }

    /**
     * 判断相邻方向是否一致的方向集合
     */
    private static final List<Integer> JUDGMENT_LINK_DIRECTION;

    static {
        List<Integer> temporary = new ArrayList<>();
        temporary.add(DIRECTION_HORIZONTAL_LEFT);
        temporary.add(DIRECTION_HORIZONTAL_RIGHT);
        temporary.add(DIRECTION_VERTICAL_UP);
        temporary.add(DIRECTION_VERTICAL_DOWN);
        temporary.add(DIRECTION_SLANT_DOWN);
        temporary.add(DIRECTION_SLANT_UP);
        JUDGMENT_LINK_DIRECTION = Collections.unmodifiableList(temporary);
    }

    public Lottery(int length, int width) {
        this.length = length;
        this.width = width;
        context = new int[length][width];
    }

    public Lottery(int[][] context) {
        this.context = context;
        this.length = context.length;
        this.width = context[0].length;
    }

    private int getMaxIndex() {
        return this.length * this.width - 1;
    }


    public static Lottery valueOf(List<LotteryResult> lottyList) {
        if (lottyList.isEmpty()) {
            return null;
        }

        int length = 0;
        int width = 0;

        for (LotteryResult lotteryResult : lottyList) {
            if (lotteryResult.getX() > length) {
                length = lotteryResult.getX();
            }
            if (lotteryResult.getY() > width) {
                width = lotteryResult.getY();
            }
        }

        if (length == 0 || width == 0) {
            return null;
        }

        //坐标是从0开始，长度应该+1
        int[][] context = new int[++length][++width];
        for (LotteryResult result : lottyList) {
            context[result.getX()][result.getY()] = result.getPetBookId();
        }

        Lottery lottery = new Lottery(length, width);
        lottery.setContext(context);
        return lottery;
    }


    /**
     * 随机添加奖票内容, 作为一个整体添加
     *
     * @param value        添加的值
     * @param count        添加个数
     * @param containSlant 是否包含斜向
     */
    public boolean addContext(int value, int count, boolean containSlant) {
        if (count <= 0) {
            return false;
        }

        List<Integer> position = randomGetSatisfyPosition(count, containSlant);
        if (position == null) {
            return false;
        }

        for (Integer index : position) {
            setValue(index, value);
        }

        return true;
    }

    /**
     * 添加内容且不允许与上下左右相同,
     * 单个填充
     */
    public void addContextNotLink(int content) {
        int aUnLinkPosition = findAUnLinkPosition(content);
        if (-1 == aUnLinkPosition) {
            return;
        }
        setValue(aUnLinkPosition, content);
    }

    /**
     * 查找一个上下左右与该内容都不相邻的位置
     *
     * @param content
     * @return -1未找到
     */
    public int findAUnLinkPosition(int content) {
        int findIndex = -1;
        List<List<Integer>> allSatisfyPosition = findAllSatisfyPosition(1, false);
        if (GameUtil.collectionIsEmpty(allSatisfyPosition)) {
            LogUtil.error("lottery is full, can not add content");
            return findIndex;
        }

        List<Integer> resultList = new ArrayList<>();
        for (List<Integer> integers : allSatisfyPosition) {
            for (Integer index : integers) {
                if (!isLinkALine(index, content)) {
                    resultList.add(index);
                }
            }
        }

        Random random = new Random();
        if (resultList.isEmpty()) {
            //找不到相邻位置不相同就随机返回一个位置
            LogUtil.debug("Lottery.findAUnLinkPosition, random return a satisfy position");
            return allSatisfyPosition.get(random.nextInt(allSatisfyPosition.size())).get(0);
        }

        return resultList.get(random.nextInt(resultList.size()));
    }

    /**
     * 检查当前序号是否与上下左右相邻
     *
     * @param index   检查序号
     * @param content
     * @return
     */
    public boolean isLink(int index, int content) {
        for (Integer direction : JUDGMENT_LINK_DIRECTION) {
            int nextLinkIndex = findNextLinkIndex(index, direction);
            if (-1 == nextLinkIndex) {
                continue;
            }
            int value = getIndexValue(nextLinkIndex);
            if (value != 0 && value == content) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定值的index值
     *
     * @param value
     * @return
     */
    private List<Integer> getValueIndexList(int value) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < getMaxIndex(); i++) {
            if (getIndexValue(i) == value) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * 判断当前已经存在的内容下标是否和当前的curIndex连接成一条线
     *
     * @param curIndex
     * @param content
     * @return
     */
    private boolean isLinkALine(int curIndex, int content) {
        List<Integer> valueIndexList = getValueIndexList(content);
        if (CollectionUtils.isEmpty(valueIndexList)) {
            return false;
        }

        for (Integer direct : TOTAL_DIRECTION_CONTAIN_SLANT) {
            if (isAline(valueIndexList, curIndex, direct)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是一条线
     *
     * @param curIndex
     * @param ownIndexList 已经存在的index
     * @param direct
     * @return
     */
    private boolean isAline(List<Integer> ownIndexList, int curIndex, int direct) {
        if (CollectionUtils.isEmpty(ownIndexList)) {
            return false;
        }

        if (direct == DIRECTION_HORIZONTAL_LEFT
                || direct == DIRECTION_HORIZONTAL_RIGHT) {
            int curY = getIndex_Y(curIndex);
            for (Integer ownIndex : ownIndexList) {
                if (getIndex_Y(ownIndex) != curY) {
                    return false;
                }
            }
        } else if (direct == DIRECTION_VERTICAL_DOWN
                || direct == DIRECTION_VERTICAL_UP) {
            int curX = getIndex_X(curIndex);
            for (Integer ownIndex : ownIndexList) {
                if (getIndex_X(ownIndex) != curX) {
                    return false;
                }
            }
        } else if (direct == DIRECTION_SLANT_UP) {
            ArrayList<Integer> newList = new ArrayList<>(ownIndexList);
            newList.add(curIndex);
            for (Integer index : newList) {
                if (getIndex_X(index) != getIndex_Y(index)) {
                    return false;
                }
            }
        } else {
            ArrayList<Integer> newList = new ArrayList<>(ownIndexList);
            newList.add(curIndex);
            for (Integer index : newList) {
                if (getIndex_X(index) + getIndex_Y(index) != getMinLineCount() - 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取连接成一条线所需的最小数量
     *
     * @return
     */
    private int getMinLineCount() {
        return Math.min(this.length, this.width);
    }

    /**
     * @param needSize
     * @param containSlant 是否包含斜向
     * @return
     */
    public List<Integer> randomGetSatisfyPosition(int needSize, boolean containSlant) {
        List<List<Integer>> allSatisfyPosition = findAllSatisfyPosition(needSize, containSlant);
        if (GameUtil.collectionIsEmpty(allSatisfyPosition)) {
            return null;
        }
        if (allSatisfyPosition.size() == 1) {
            return allSatisfyPosition.get(0);
        }
        return allSatisfyPosition.get(new Random().nextInt(allSatisfyPosition.size()));
    }

    public void setValue(int index, int value) {
        this.context[getIndex_X(index)][getIndex_Y(index)] = value;
    }

    /**
     * @param needSize     需要连续的位置数量
     * @param containSlant 是否包含斜向
     * @return 返回满足条件的所有组合
     */
    private List<List<Integer>> findAllSatisfyPosition(int needSize, boolean containSlant) {
        if (needSize > this.length || needSize > this.width || needSize <= 0) {
            LogUtil.error("can not find specify length link position, lottery size is too small, or needCount <= 0");
            return null;
        }

        List<Integer> emptyIndexList = getEmptyIndexList();
        if (emptyIndexList.isEmpty()) {
            LogUtil.error("this lottery is full");
            return null;
        }

        List<List<Integer>> result = new ArrayList<>();
        for (Integer startIndex : emptyIndexList) {
            if (needSize == 1) {
                result.add(Arrays.asList(startIndex));
            } else {
                List<Integer> findDirectionList;
                if (containSlant) {
                    findDirectionList = TOTAL_DIRECTION_CONTAIN_SLANT;
                } else {
                    findDirectionList = TOTAL_DIRECTION_NOT_CONTAIN_SLANT;
                }

                for (Integer direction : findDirectionList) {
                    List<Integer> position = findLinkPosition(startIndex, direction, needSize);
                    if (position != null) {
                        result.add(position);
                    }
                }
            }
        }
        return result;
    }

    public List<Integer> getEmptyIndexList() {
        List<Integer> emptyIndex = new ArrayList<>();
        for (int i = 0; i < this.length; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.context[i][j] == 0) {
                    emptyIndex.add(getIndex(i, j));
                }
            }
        }
        return emptyIndex;
    }

    private List<Integer> findLinkPosition(int startIndex, int direction, int needSize) {
        if (!indexIsEmpty(startIndex)) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        result.add(startIndex);

        int curIndex = startIndex;
        for (int i = 0; i < needSize - 1; i++) {
            int nextLinkIndex = findNextLinkIndex(curIndex, direction);
            if (nextLinkIndex == -1 || !indexIsEmpty(nextLinkIndex)) {
                return null;
            }

            curIndex = nextLinkIndex;
            result.add(nextLinkIndex);
        }

        return result;
    }

    /**
     * 返回当前位置的下一个位置相连位置
     *
     * @param index
     * @param direction
     * @return -1未找到指定方向相连位置
     */
    private int findNextLinkIndex(int index, int direction) {
        if (!hasNextIndex(index, direction)) {
            return -1;
        }
        int nextIndex = -1;
        if (DIRECTION_HORIZONTAL_LEFT == direction) {
            nextIndex = index - 1;
        } else if (DIRECTION_HORIZONTAL_RIGHT == direction) {
            nextIndex = index + 1;
        } else if (DIRECTION_VERTICAL_UP == direction) {
            nextIndex = index - this.length;
        } else if (DIRECTION_VERTICAL_DOWN == direction) {
            nextIndex = index + this.length;
        } else if (DIRECTION_SLANT_DOWN == direction) {
            nextIndex = index + 1 + this.length;
        } else if (DIRECTION_SLANT_UP == direction) {
            nextIndex = index + 1 - this.length;
        }
        return nextIndex;
    }

    private boolean hasNextIndex(int index, int direction) {
        if (DIRECTION_HORIZONTAL_LEFT == direction) {
            return getIndex_X(index) > 0;
        } else if (DIRECTION_HORIZONTAL_RIGHT == direction) {
            return getIndex_X(index) < getMaxX();
        } else if (DIRECTION_VERTICAL_UP == direction) {
            return getIndex_Y(index) > 0;
        } else if (DIRECTION_VERTICAL_DOWN == direction) {
            return getIndex_Y(index) < getMaxY();
        } else if (DIRECTION_SLANT_DOWN == direction) {
            return getIndex_X(index) < getMaxX() && getIndex_Y(index) < getMaxY();
        } else if (DIRECTION_SLANT_UP == direction) {
            return getIndex_X(index) < getMaxX() && getIndex_Y(index) > 0;
        }
        return true;
    }

    private int getIndex_Y(int index) {
        return index / this.length;
    }

    private int getIndex_X(int index) {
        return index % this.length;
    }

    /**
     * 判断某个序号是否超出范围
     *
     * @return
     */
    private boolean indexIsOutOfScope(int index) {
        return index >= getMaxIndex() || index < 0;
    }

    private boolean indexIsEmpty(int index) {
        return getIndexValue(index) == 0;
    }

    private int getIndexValue(int index) {
        return context[getIndex_X(index)][getIndex_Y(index)];
    }

    private int getIndex(int x, int y) {
        return x + y * this.length;
    }

    private int getMaxX() {
        return this.length - 1;
    }

    private int getMaxY() {
        return this.width - 1;
    }

    private int getMaxLinkCount() {
        return Math.max(this.length, this.width);
    }

    /**
     * 返回当前奖票的最大坑数
     *
     * @return
     */
    public int getSlotCount() {
        return this.length * this.width;
    }

    /**
     * ====================================结算方法=============================
     */

    /**
     * 寻找相连的数字 四个方向
     *
     * @return <品质,对应的相连的个数>
     */
    public int findLinkNumCount(int curIndex, int direction) {
        int startValue = getIndexValue(curIndex);
        int count = 1;
        int startIndex = curIndex;
        for (int i = 0; i < getMaxLinkCount(); i++) {
            int nextLinkIndex = findNextLinkIndex(startIndex, direction);
            if (nextLinkIndex != -1 && Objects.equals(startValue, getIndexValue(nextLinkIndex))) {
                startIndex = nextLinkIndex;
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 结算彩票
     *
     * @return 返回对应宠物头像相连的个数, 只保留最大连接个数
     */
    public Map<Integer, Integer> settleLottery() {
        Map<Integer, Integer> result = new HashMap<>();
        for (int i = 0; i <= getMaxIndex(); i++) {
            for (Integer direction : TOTAL_DIRECTION_CONTAIN_SLANT) {
                int indexValue = getIndexValue(i);
                int count = findLinkNumCount(i, direction);
                if (result.containsKey(indexValue)) {
                    result.put(indexValue, Math.max(result.get(indexValue), count));
                } else {
                    result.put(indexValue, count);
                }
            }
        }
        return result;
    }

    public boolean checkContent() {
        for (int[] ints : context) {
            for (int anInt : ints) {
                if (PetBaseProperties.getByPetid(anInt) == null) {
                    return false;
                }
            }
        }
        return true;
    }
}
