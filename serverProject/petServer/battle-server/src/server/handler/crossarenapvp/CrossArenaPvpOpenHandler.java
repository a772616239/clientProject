package server.handler.crossarenapvp;

import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst.RedisKey;
import common.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.Battle.PlayerBaseInfo;
import protocol.CrossArenaPvp.CrossArenaPvpPlayer;
import protocol.CrossArenaPvp.CrossArenaPvpRoom;
import protocol.CrossArenaPvp.CrossArenaPvpRoom.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaOpen;
import protocol.ServerTransfer.GS_BS_CrossArenaOpen;
import util.JedisUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaOpen_VALUE)
public class CrossArenaPvpOpenHandler extends AbstractHandler<GS_BS_CrossArenaOpen> {
	@Override
	protected GS_BS_CrossArenaOpen parse(byte[] bytes) throws Exception {
		return GS_BS_CrossArenaOpen.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaOpen req, int i) {
		LogUtil.debug("recv CS apply pvp battle msg");

		BS_GS_CrossArenaOpen.Builder b = BS_GS_CrossArenaOpen.newBuilder();

		CrossArenaPvpRoom oldRoom = null;

		byte[] hget = JedisUtil.jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), req.getCreateRoomId().getBytes());
		if (hget == null) {
			return;
		}
		try {
			oldRoom = CrossArenaPvpRoom.parseFrom(hget);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		// 有房只能重设
		if (oldRoom != null && !req.getCreateRoomId().equals(req.getId())) {
			b.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_HAVEROOM));
			gsChn.send(MsgIdEnum.BS_GS_CrossArenaOpen_VALUE, b);
			return;
		}

		CrossArenaPvpRoom.Builder room = CrossArenaPvpRoom.newBuilder();
		room.setId(req.getCreateRoomId());
		room.addAllBlackpet(req.getBlackpetList());
		room.setCreteTime(GlobalTick.getInstance().getCurrentTime());
		room.setCostIndex(req.getCostIndex());
		room.setMaxLv(req.getMaxLv());
		room.setMinLv(req.getMinLv());

		CrossArenaPvpPlayer.Builder pb = CrossArenaPvpPlayer.newBuilder();
		pb.setPlayerBaseInfo(req.getPlayerBaseInfo());
		pb.setSvrIndex(req.getSvrIndex());
		pb.setReady(0);
		pb.setTeamInfo(req.getTeamInfo());
		room.setOwner(pb);
		// 房主更新房间
		if (oldRoom != null) {
			// 房间更新

			if (!JedisUtil.lockRedisKey(req.getCreateRoomId(), 2000)) {
				b.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE));
				gsChn.send(MsgIdEnum.BS_GS_CrossArenaOpen_VALUE, b);
				return;
			}
			if (!oldRoom.getAtter().getPlayerBaseInfo().getPlayerId().equals("")) {
				PlayerBaseInfo atter = oldRoom.getAtter().getPlayerBaseInfo();
				boolean kick = false;
				if (atter.getLevel() < room.getMinLv() || atter.getLevel() > room.getMaxLv()) {
					kick = true;
				} else {
					for (Integer id : oldRoom.getAtter().getBookIdsList()) {
						if (room.getBlackpetList().contains(id)) {
							kick = true;
							break;
						}
					}
				}
				Builder newRoom = oldRoom.toBuilder();
				if (kick) {// 踢掉
					int atterFromSvrIndex = oldRoom.getAtter().getSvrIndex();
					newRoom.clearAtter();
					GameServerTcpChannel channel = WarpServerManager.getInstance().getGameServerChannel(atterFromSvrIndex);
					if (channel != null) {
						// TODO 提示踢人和被踢

					}
				} else {
					room.setAtter(oldRoom.getAtter());
				}
			}
			JedisUtil.jedis.hset(RedisKey.CROSSARENAPVP_ROOM.getBytes(), oldRoom.getId().getBytes(), oldRoom.toByteArray());
			JedisUtil.unlockRedisKey(req.getCreateRoomId());
		} else {

			if (!JedisUtil.lockRedisKey(req.getCreateRoomId(), 2000)) {
				b.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE));
				gsChn.send(MsgIdEnum.BS_GS_CrossArenaOpen_VALUE, b);
				return;
			}
			JedisUtil.jedis.hset(RedisKey.CROSSARENAPVP_ROOM.getBytes(), room.getId().getBytes(), room.build().toByteArray());
			JedisUtil.unlockRedisKey(req.getCreateRoomId());
		}
		b.setRoom(room);
		// TODO 房间更新消息
		b.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		gsChn.send(MsgIdEnum.BS_GS_CrossArenaOpen_VALUE, b);
	}
}
