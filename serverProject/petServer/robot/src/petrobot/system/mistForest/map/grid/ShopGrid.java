package petrobot.system.mistForest.map.grid;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopGrid extends Grid {
    private int shopId;

    public ShopGrid(int gridType) {
        super(gridType);
    }
}
