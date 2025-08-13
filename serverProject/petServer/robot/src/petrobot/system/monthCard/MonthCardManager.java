package petrobot.system.monthCard;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import protocol.GM;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard;
import protocol.MonthCard.CS_ClaimMonthCard;

@Controller
public class MonthCardManager {

    private static final String baseMonthCardGm = "purchase|g06.com.oceanvista.yueka.g06.30";

    private static final String advancedMonthCardGm = "purchase|hmgas9.99";


    private static final List<String> monthCardList = Arrays.asList(baseMonthCardGm, advancedMonthCardGm);


    @Index(value = IndexConst.MonthCard_Info)
    public void claimCardInfo(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_ClaimMonthCard_VALUE, CS_ClaimMonthCard.newBuilder());
    }

    @Index(value = IndexConst.BUY_MONTH_CARD)
    public void buyCard(Robot robot) {
        List<MonthCard.MonthCardInfo> monthCardInfo = robot.getData().getMonthCardInfo();

        GM.CS_GM.Builder msg = GM.CS_GM.newBuilder();
        if (CollectionUtils.isEmpty(monthCardInfo)) {
            String monthCardGm = monthCardList.get(RandomUtils.nextInt(monthCardList.size()));
            robot.getClient().send(MsgIdEnum.CS_GM_VALUE, msg.setStr(monthCardGm));
            return;
        }
        Optional<MonthCard.MonthCardInfo> activeCard = monthCardInfo.stream().filter(card -> card.getRemainDays() <= 0).findFirst();
        if (!activeCard.isPresent()) {
            String monthCardGm = monthCardList.get(monthCardInfo.get(0).getMonthCardId() - 1);
            robot.getClient().send(MsgIdEnum.CS_GM_VALUE, msg.setStr(monthCardGm));
            return;
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }


}
