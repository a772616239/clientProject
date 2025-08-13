//package server.handler.foreignInvasion;
//
//import com.google.protobuf.ProtocolStringList;
//import hyzNet.GameServerTcpChannel;
//import common.AbstractBaseHandler;
//import hyzNet.message.MsgId;
//import java.util.List;
//import java.util.Map;
//import model.foreigninvasion.oldVersion.ForeignInvasionManager;
//import model.player.util.PlayerUtil;
//import protocol.Gameplay.CS_RecreateMonster;
//import protocol.Gameplay.ForeignInvasionStatusEnum;
//import protocol.Gameplay.MonsterInfo;
//import protocol.Gameplay.SC_RecreateMonster;
//import protocol.MessageId.MsgIdEnum;
//import protocol.RetCodeId.RetCodeEnum;
//import util.GameUtil;
//import util.LogUtil;
//
//@MsgId(msgId = MsgIdEnum.CS_RecreateMonster_VALUE)
//public class RecreateMonsterHandler extends AbstractBaseHandler<CS_RecreateMonster> {
//    @Override
//    protected CS_RecreateMonster parse(byte[] bytes) throws Exception {
//        return CS_RecreateMonster.parseFrom(bytes);
//    }
//
//    @Override
//    protected void execute(GameServerTcpChannel gsChn, CS_RecreateMonster req, int i) {
//        String playerIdx = String.valueOf(gsChn.getPlayerId1());
//
//        LogUtil.info("==============recv recreate:" + req.getMonsterIdxList().toString());
//
//        SC_RecreateMonster.Builder resultBuilder = SC_RecreateMonster.newBuilder();
//        if (PlayerUtil.queryPlayerLv(playerIdx) < ForeignInvasionManager.getInstance().getOpenLvLimit()) {
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
//            gsChn.send(MsgIdEnum.SC_RecreateMonster_VALUE, resultBuilder);
//            return;
//        }
//
//        if(ForeignInvasionManager.getInstance().getStatus() != ForeignInvasionStatusEnum.FISE_FirstStage){
//            //特殊处理,状态不匹配,返回success
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
//            gsChn.send(MsgIdEnum.SC_ClaimTransitionReward_VALUE, resultBuilder);
//            return;
//        }
//
//        ProtocolStringList monsterIdxList = req.getMonsterIdxList();
//        if (monsterIdxList == null || monsterIdxList.isEmpty()) {
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
//            gsChn.send(MsgIdEnum.SC_RecreateMonster_VALUE, resultBuilder);
//            return;
//        }
//
//        List<String> removeSuccessList = ForeignInvasionManager.getInstance().removeMonster(playerIdx, monsterIdxList);
//        if (removeSuccessList == null || removeSuccessList.isEmpty()) {
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ForInv_RemoveMonsterFailed));
//            gsChn.send(MsgIdEnum.SC_RecreateMonster_VALUE, resultBuilder);
//            return;
//        }
//
//        Map<String, MonsterInfo> newMonsters = ForeignInvasionManager.getInstance().createMonster(playerIdx, removeSuccessList.size());
//        if (newMonsters == null || newMonsters.isEmpty()) {
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCR_ForInv_CreateMonsterFailed));
//            gsChn.send(MsgIdEnum.SC_RecreateMonster_VALUE, resultBuilder);
//            return;
//        }
//
//        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
//        resultBuilder.addAllRemoveMonsterIdx(removeSuccessList);
//        resultBuilder.addAllMonsterInfo(newMonsters.values());
//        gsChn.send(MsgIdEnum.SC_RecreateMonster_VALUE, resultBuilder);
//    }
//}
