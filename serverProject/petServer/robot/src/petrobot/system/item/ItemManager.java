package petrobot.system.item;

import org.apache.commons.lang.math.RandomUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Bag.CS_SellItem;
import protocol.Bag.CS_SellItem.Builder;
import protocol.Bag.CS_UseItem;
import protocol.MessageId.MsgIdEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

@Controller
public class ItemManager {

    private static class LazyHolder {
        private static final ItemManager INSTANCE = new ItemManager();
    }

    public static ItemManager getIns() {
        return LazyHolder.INSTANCE;
    }


    @Index(value = IndexConst.SELL_ITEM)
    public void sellItem(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, t -> {
            Map<Integer, Long> itemMap = robot.getData().getItemMap();
            for (Entry<Integer, Long> entry : itemMap.entrySet()) {
                Builder builder = CS_SellItem.newBuilder();
                builder.setItemCfgId(entry.getKey());
                builder.setSellCount(new Random().nextInt(Integer.MAX_VALUE));
                robot.getClient().send(MsgIdEnum.CS_SellItem_VALUE, builder);
                break;
            }
        });
    }

    @Index(value = IndexConst.USE_ITEM)
    public void useItem(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, t -> {
            Map<Integer, Long> itemMap = robot.getData().getItemMap();
            List<Integer> itemIds = new ArrayList<>(itemMap.keySet());
            int useId = RandomUtils.nextInt(itemIds.size());
            CS_UseItem.Builder builder = CS_UseItem.newBuilder();
            builder.setItemCfgId(useId);
            builder.setUseCount(new Random().nextInt(5));
            robot.getClient().send(MsgIdEnum.CS_UseItem_VALUE, builder);
        });
    }
}
