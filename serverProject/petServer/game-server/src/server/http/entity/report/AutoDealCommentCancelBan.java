package server.http.entity.report;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 自动处理解除封禁
 * @author huhan
 * @date 2020/07/20
 */
@Getter
@Setter
public class AutoDealCommentCancelBan {

    private List<AutoDealCommentCancelBanInstance> cancelList;

    public boolean checkParams() {
        return cancelList != null;
    }

    @Getter
    @Setter
    public static class AutoDealCommentCancelBanInstance {
        private String playerIdx;
        private int querySource;
    }
}
