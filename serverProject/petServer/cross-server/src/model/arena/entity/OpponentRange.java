package model.arena.entity;

import cfg.ArenaDan;
import common.GameConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/12
 * <p>
 * //{类型，参数1，参数2，推荐个数，是否标记为直升玩家}
 * //参数请按照从大到小填入
 * //
 * //类型：1按照排名推荐，2按照积分差推荐
 * //参数1，参数2：当类型为1时， 参数为推荐的排名区间,
 * //      当类型为2时,参数为推荐额积分差区间（百分比）
 * //
 * //推荐个数：
 * //是否标记为直升玩家：1是 0 不是
 */

@Getter
@Setter
@ToString
public class OpponentRange {
    private int rangeType;
    private int paramStart;
    private int paramEnd;
    private int needCount;
    private boolean directUp;

    private OpponentRange() {
    }


    public static OpponentRange createEntity(int[] ints) {
        if (ints.length < 5) {
            LogUtil.error("ArenaPartitionConfigObject, cfg error, opponentRange length < 5");
            return null;
        }

        //检查推荐类型
        if (ints[0] == 1) {
            if (ints[1] > ints[2]
                    || GameUtil.outOfScope(0, ArenaDan.getById(GameConst.ConfigId).getRoommaxsize(), ints[1])
                    || GameUtil.outOfScope(0, ArenaDan.getById(GameConst.ConfigId).getRoommaxsize(), ints[2])) {
                LogUtil.error("ArenaPartitionConfigObject, cfg error, ranking params is error, startRanking =" + ints[1] +
                        "endRanking:" + ints[2]);
                return null;
            }
        } else if (ints[0] == 2) {
            if (GameUtil.outOfScope(100, -100, ints[1])
                    || GameUtil.outOfScope(100, -100, ints[2])) {
                LogUtil.error("ArenaDanObject, cfg error, score range params is error, rangeStart =" + ints[1] +
                        "rangeEnd:" + ints[2]);
                return null;
            }
        } else {
            LogUtil.error("ArenaPartitionConfigObject, cfg error, unsupported target type, type =" + ints[0]);
            return null;
        }

        //检查需要个数
        if (ints[3] <= 0) {
            LogUtil.error("ArenaPartitionConfigObject, cfg error, need count cfg error, count:" + ints[3]);
            return null;
        }

        //检查标记
        if (ints[4] != 0 && ints[4] != 1) {
            LogUtil.error("ArenaPartitionConfigObject, cfg error, mark params error, params:" + ints[4]);
            return null;
        }

        OpponentRange range = new OpponentRange();
        range.setRangeType(ints[0]);
        range.setParamStart(ints[1]);
        range.setParamEnd(ints[2]);
        range.setNeedCount(ints[3]);
        range.setDirectUp(ints[4] == 1);
        return range;
    }

}

