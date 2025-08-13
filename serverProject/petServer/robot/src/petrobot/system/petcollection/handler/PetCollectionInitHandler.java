package petrobot.system.petcollection.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetCollection;

/**
 * @author xiao_FL
 * @date 2019/12/18
 */
@MsgId(msgId = MsgIdEnum.SC_PetCollection_VALUE)
public class PetCollectionInitHandler extends AbstractHandler<SC_PetCollection> {
    @Override
    protected SC_PetCollection parse(byte[] bytes) throws Exception {
        return SC_PetCollection.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetCollection scPetCollection, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, rob -> {
            rob.getData().getPetCollectionIdList().clear();
            rob.getData().getPetCollectionIdList().addAll(scPetCollection.getCfgIdList());
            rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });

    }
}
