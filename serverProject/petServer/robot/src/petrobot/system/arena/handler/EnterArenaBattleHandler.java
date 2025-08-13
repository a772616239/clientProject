//package petrobot.system.arena.handler;
//
//import hyzNet.GameServerTcpChannel;
//import hyzNet.message.AbstractHandler;
//import hyzNet.message.MsgId;
//import petrobot.robot.Robot;
//import petrobot.robot.RobotManager;
//import petrobot.robotConst.DealResultConst;
//import petrobot.util.SyncExecuteFunction;
//import protocol.Arena.SC_EnterArenaBattle;
//import protocol.MessageId.MsgIdEnum;
//
///**
// * @author huhan
// * @date 2020/05/28
// */
//@MsgId(msgId = MsgIdEnum.SC_EnterArenaBattle_VALUE)
//public class EnterArenaBattleHandler extends AbstractHandler<SC_EnterArenaBattle> {
//    @Override
//    protected SC_EnterArenaBattle parse(byte[] bytes) throws Exception {
//        return SC_EnterArenaBattle.parseFrom(bytes);
//    }
//
//    @Override
//    protected void execute(GameServerTcpChannel gsChn, SC_EnterArenaBattle req, int i) {
//        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
//        if (robotByChannel == null) {
//            return;
//        }
//
//        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
//            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
//        });
//    }
//}
