package server.handler.friend;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import org.apache.commons.lang.StringUtils;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_FindFriend;
import protocol.Friend.FriendBaseInfo.Builder;
import protocol.Friend.SC_FindFriend;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FindFriend_VALUE)
public class FindFriendHandler extends AbstractBaseHandler<CS_FindFriend> {
    @Override
    protected CS_FindFriend parse(byte[] bytes) throws Exception {
        return CS_FindFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FindFriend req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_FindFriend.Builder resultBuilder = SC_FindFriend.newBuilder();
        String findStr = StringUtils.deleteWhitespace(req.getFindStr());
        if (StringUtils.isEmpty(findStr)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_FindFriend_VALUE, resultBuilder);
            return;
        }

        List<String> findPlayerIdx = playerCache.getInstance().getPlayerIdxByNameLike(findStr);
        if (findPlayerIdx != null && !findPlayerIdx.isEmpty()) {
            for (String idx : findPlayerIdx) {
                if (!idx.equalsIgnoreCase(playerIdx)) {
                    Builder builder = FriendUtil.builderFriendBaseInfo(playerCache.getByIdx(idx), 0);
                    if (builder != null) {
                        resultBuilder.addFindResult(builder);
                    }
                }
            }
        }

        if (StringUtils.isNumeric(findStr)) {
            int shortId = Integer.parseInt(findStr);
            if (shortId <= 0) {
                return;
            }
            if (shortId != getPlayerShortId(playerIdx)) {
                playerEntity findPlayer = playerCache.getInstance().getPlayerByShortId(shortId);
                Builder builder = FriendUtil.builderFriendBaseInfo(findPlayer, 0);
                if (builder != null) {
                    resultBuilder.addFindResult(builder);
                }
            }
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_FindFriend_VALUE, resultBuilder);
    }

    private int getPlayerShortId(String playerIdx) {
        if (playerIdx == null) {
            return 0;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return 0;
        }

        return player.getShortid();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_FindFriend_VALUE, SC_FindFriend.newBuilder().setRetCode(retCode));
    }
}
