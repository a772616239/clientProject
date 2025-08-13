package server.handler.drawCard;

import java.util.ArrayList;
import java.util.List;

import cfg.DrawCard;
import cfg.DrawCardObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.drawCard.DrawCardManager;
import model.drawCard.DrawCardUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import platform.logs.entity.PetCallLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.DrawCard.CS_FriendShipDrawCard;
import protocol.DrawCard.SC_FriendShipDrawCard;
import protocol.DrawCard.SC_FriendShipDrawCard.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/03/18
 */
@MsgId(msgId = MsgIdEnum.CS_FriendShipDrawCard_VALUE)
public class FriendShipDrawCardHandler extends AbstractBaseHandler<CS_FriendShipDrawCard> {
	@Override
	protected CS_FriendShipDrawCard parse(byte[] bytes) throws Exception {
		return CS_FriendShipDrawCard.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FriendShipDrawCard req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		playerEntity player = playerCache.getByIdx(playerIdx);
		DrawCardObject drawCardCfg = DrawCard.getById(GameConst.CONFIG_ID);

		Builder resultBuilder = SC_FriendShipDrawCard.newBuilder();
		if (player == null || drawCardCfg == null || PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.DrawCard)) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
			gsChn.send(MsgIdEnum.SC_FriendShipDrawCard_VALUE, resultBuilder);
			return;
		}

		// 是否达到上限
		if (!DrawCardManager.getInstance().canDraw(playerIdx, req.getDrawCount())) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_BanShu_OutOfLimit));
			gsChn.send(MsgIdEnum.SC_FriendShipDrawCard_VALUE, resultBuilder);
			return;
		}

		List<Reward> rewards = DrawCardManager.getInstance().drawFriendShipCard(playerIdx, req.getDrawCount());
		Consume consume = ConsumeUtil.parseAndMulti(drawCardCfg.getFrienddrawcardconsume(), req.getDrawCount());
		if (rewards == null || consume == null) {
			LogUtil.error("friend ship draw card consume cfg error");
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
			gsChn.send(MsgIdEnum.SC_FriendShipDrawCard_VALUE, resultBuilder);
			return;
		}

		boolean consumeSuccess = ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_FriensShip));

		if (!consumeSuccess) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
			gsChn.send(MsgIdEnum.SC_FriendShipDrawCard_VALUE, resultBuilder);
			return;
		}

		SyncExecuteFunction.executeConsumer(player, p -> {
			player.addDrawCardExp(drawCardCfg.getEachfriendexp() * req.getDrawCount());
		});

		List<Reward> showList = new ArrayList<>(rewards);
		rewards = DrawCardUtil.changeFrag2Pet(rewards);
		RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_FriensShip), false);
		resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		resultBuilder.addAllRewars(showList);
		gsChn.send(MsgIdEnum.SC_FriendShipDrawCard_VALUE, resultBuilder);

		// 设置计数
		DrawCardManager.getInstance().addDrawCount(playerIdx, req.getDrawCount());

		// 目标：累积x次普通抽卡
		EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuCommonDrawCard, req.getDrawCount(), 0);
		LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.DrawCard));
		LogService.getInstance().submit(new PetCallLog(playerIdx, rewards, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_FriensShip)));
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_Friend;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FriendShipDrawCard_VALUE, SC_FriendShipDrawCard.newBuilder().setRetCode(retCode));
	}
}
