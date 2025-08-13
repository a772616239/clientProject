package model.gloryroad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

/**
 * @author huhan
 * @date 2021/3/16
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class GloryRoadOpponent {
    private String playerIdx1;
    private String playerIdx2;
    private int parentIndex;

    public boolean isEmpty() {
        return StringUtils.isEmpty(playerIdx1) && StringUtils.isEmpty(playerIdx2);
    }
}
