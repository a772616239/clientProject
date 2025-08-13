package server.http.entity.report;

import cfg.BossTowerConfig;
import cfg.EndlessSpireConfig;
import cfg.MainLineNode;
import cfg.PetBaseProperties;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Comment.CommentTypeEnum;

/**
 * @author huhan
 * @date 2020/07/21
 */
@Getter
@Setter
@ToString
public class UnreportedCommentQuery {

    /**
     * 与举报查询保持一致
     */
    private int querySource;

    /**
     * 查询范围区间,单一区间(起始区间==结束区间)
     * 宠物(宠物稀有度)
     * 主线(节点)
     * 无尽尖塔(塔层)
     * 勇气试炼(关卡)
     * boss塔(塔层)
     */
    private int startRange;
    private int endRange;

    /**
     * 模糊查询名字, 平台未填时为“”
     */
    private String name;

    private int page;

    /**
     * 最大查询区间
     */
    public static final int MAX_QUERY_RANGE_SIZE = 20;

    public boolean checkParams() {
        if (page <= 0) {
            return false;
        }

        if (querySource == CommentTypeEnum.CTE_Pet_VALUE) {
            if (startRange != endRange
                    || CollectionUtils.size(PetBaseProperties.getRarityTotalPet(startRange)) <= 0) {
                return false;
            }
        } else if (querySource == CommentTypeEnum.CTE_EndlessSpire_VALUE) {
            if ((endRange - startRange) > MAX_QUERY_RANGE_SIZE || startRange > EndlessSpireConfig._ix_spirelv.size()) {
                return false;
            }
        } else if (querySource == CommentTypeEnum.CTE_BossTower_VALUE) {
            if ((endRange - startRange) > MAX_QUERY_RANGE_SIZE || startRange > BossTowerConfig._ix_id.size()) {
                return false;
            }
        } else if (querySource == CommentTypeEnum.CTE_MainLine_VALUE) {
            if ((endRange - startRange) > MAX_QUERY_RANGE_SIZE || startRange > MainLineNode._ix_id.size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查当是模糊查询的时候是否存在对应的目标玩家
     *
     * @return
     */
    public boolean checkName() {
        if (StringUtils.isNotBlank(name)) {
            List<playerEntity> nameLike = playerCache.getInstance().getPlayerByNameLike(name);
            return !CollectionUtils.isEmpty(nameLike);
        }
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnreportedCommentQuery that = (UnreportedCommentQuery) o;
        return getQuerySource() == that.getQuerySource()
                && getStartRange() == that.getStartRange()
                && getEndRange() == that.getEndRange()
                && getPage() == that.getPage()
                && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuerySource(), getStartRange(), getEndRange(), getName(), getPage());
    }
}
