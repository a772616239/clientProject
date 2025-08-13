package model.itembag.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import protocol.Common.Consume;

/**
 * @author huhan
 * @date 2020/11/27
 */
@Getter
@Setter
public class CanUseItemResult {
    private int canUseCount;
    private List<Consume> openMaterial;

    public CanUseItemResult(int canUseCount) {
        this(canUseCount, null);
    }

    public CanUseItemResult(int canUseCount, List<Consume> openMaterial) {
        this.canUseCount = canUseCount;
        this.openMaterial = openMaterial;
    }
}
