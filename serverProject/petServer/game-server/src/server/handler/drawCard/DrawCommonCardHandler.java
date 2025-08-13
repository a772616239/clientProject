package server.handler.drawCard;

import cfg.DrawCard;
import cfg.DrawCardObject;
import cfg.GameConfig;
import cfg.Item;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.drawCard.DrawCardManager;
import model.drawCard.DrawCardUtil;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import platform.logs.entity.PetCallLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.DrawCard.CS_DrawCommonCard;
import protocol.DrawCard.SC_DrawCommonCard;
import protocol.DrawCard.SC_RefreshCommonMustDrawTimes;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_DrawCardData;
import protocol.PlayerDB.DB_DrawCardData.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * 高级抽卡上行消息
 */
@MsgId(msgId = MsgIdEnum.CS_DrawCommonCard_VALUE)
public class DrawCommonCardHandler extends AbstractBaseHandler<CS_DrawCommonCard> {
	@Override
	protected CS_DrawCommonCard parse(byte[] bytes) throws Exception {
		return CS_DrawCommonCard.parseFrom(bytes);
	}


	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_DrawCommonCard req, int i) {
		int drawCount = req.getDrawCount();

		SC_DrawCommonCard.Builder resultBuilder = SC_DrawCommonCard.newBuilder();

		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		playerEntity player = playerCache.getByIdx(playerIdx);
		if (req.getDrawCount() <= 0 || player == null || PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.DrawCard)) {

			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionNotUnLock));
			gsChn.send(MsgIdEnum.SC_DrawCommonCard_VALUE, resultBuilder);
			return;
		}

		// 是否达到上限
		if (!DrawCardManager.getInstance().canDraw(playerIdx, req.getDrawCount())) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_BanShu_OutOfLimit));
			gsChn.send(MsgIdEnum.SC_DrawCommonCard_VALUE, resultBuilder);
			return;
		}

		// 奖励
		List<Reward> rewards;
		if (10 == req.getDrawCount() && !player.getDb_data().getNewBeeInfo().getNewBeeDrawCard()) {
			rewards = RewardUtil.getRewardsByRewardId(GameConfig.getById(GameConst.CONFIG_ID).getNewbeedrawcard());

			// 设置完成新手引导
			SyncExecuteFunction.executeConsumer(player, p -> {
				player.getDb_data().getNewBeeInfoBuilder().setNewBeeDrawCard(true);

				// 抽卡计数
				DB_DrawCardData.Builder drawCardBuilder = player.getDb_data().getDrawCardBuilder();
				drawCardBuilder.setCommonMustDrawCount(drawCardBuilder.getCommonMustDrawCount() + 10);
			});
		} else {
			// 先随机
			rewards = DrawCardManager.getInstance().drawCommonCard(playerIdx, drawCount);
		}

		if (GameUtil.collectionIsEmpty(rewards) || rewards.size() != req.getDrawCount()) {
			LogUtil.error("draw common card failed, needSize= " + req.getDrawCount() + ", random Size = " + (rewards == null ? 0 : rewards.size()));
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
			gsChn.send(MsgIdEnum.SC_DrawCommonCard_VALUE, resultBuilder);
			return;
		}
		List<Consume> consume = new ArrayList<>();
		boolean result = consume(playerIdx, consume, req.getDrawCount(), req.getUseItemFirst());
		if (!result) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
			gsChn.send(MsgIdEnum.SC_DrawCommonCard_VALUE, resultBuilder);
			return;
		}

		SyncExecuteFunction.executeConsumer(player, p -> {
			// 添加经验值
			p.addDrawCardExp(DrawCard.getById(GameConst.CONFIG_ID).getEachcommonexp() * req.getDrawCount());
			Builder drawCardBuilder = p.getDb_data().getDrawCardBuilder();
			drawCardBuilder.setUseItemFirst(req.getUseItemFirst());
			// 更新消费金额
			if (!GameUtil.collectionIsEmpty(consume)) {
				int consumeCount = ConsumeUtil.getConsumeCount(consume, RewardTypeEnum.RTE_Diamond, 0);
				if (consumeCount >= 0) {
					drawCardBuilder.setDrawCardConsume(drawCardBuilder.getDrawCardConsume() + consumeCount);
				}
			}
		});

		List<Reward> showList = new ArrayList<>(rewards);
		rewards = DrawCardUtil.changeFrag2Pet(rewards);
		RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_Common), false);
		resultBuilder.addAllResult(showList);
		resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		gsChn.send(MsgIdEnum.SC_DrawCommonCard_VALUE, resultBuilder);

		// 刷新新的保底次数
		gsChn.send(MsgIdEnum.SC_RefreshCommonMustDrawTimes_VALUE, SC_RefreshCommonMustDrawTimes.newBuilder().setNewTimes(player.getDb_data().getDrawCard().getCommonMustDrawCount()));

		player.sendDrawCardInfo();
		// 设置计数
		DrawCardManager.getInstance().addDrawCount(playerIdx, req.getDrawCount());

		// 目标：累积x次高级抽卡
		EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuHighDrwaCard, req.getDrawCount(), 0);
		LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.DrawCard, req.getDrawCount(), 0));
		if (!GameUtil.collectionIsEmpty(consume)) {
			LogService.getInstance().submit(new PetCallLog(playerIdx, rewards, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_Common)));
		}
		triggerGift(req.getDrawCount(),playerIdx);
	}

	private void triggerGift(int drawCount, String playerIdx) {
		if (drawCount!=1){
			return;
		}
	//	EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_PopMission_MaterialNotEnough,1,1);
	}

	/**
	 * 消耗材料且返回消耗的列表
	 *
	 * @param playerIdx
	 * @param times
	 * @param firstUseItem
	 * @return
	 */
	private boolean consume(String playerIdx, List<Consume> consumes, int times, boolean firstUseItem) {
		DrawCardObject drawCardCfg = DrawCard.getById(GameConst.CONFIG_ID);
		if (drawCardCfg == null) {
			return false;
		}

		itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
		if (Item.getById(drawCardCfg.getCommomdrawcardconsumeitem()) == null || drawCardCfg.getCommondrawcarddiamond() <= 0 || itemBag == null) {
			return false;
		}

		playerEntity player = playerCache.getByIdx(playerIdx);
		if (player == null) {
			return false;
		}
		// 单抽检查免费
		if (times == 1 && player.getDb_data().getDrawCard().getNextCommonCardFreeTime() < GlobalTick.getInstance().getCurrentTime()) {
			SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().getDrawCardBuilder().setNextCommonCardFreeTime(
					GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_DAY));
			return true;
		}

		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_Common);
		int remainTimes = times;
		if (firstUseItem) {
			remainTimes = SyncExecuteFunction.executeFunction(itemBag, bag -> {
				long itemCount = bag.getItemCount(drawCardCfg.getCommomdrawcardconsumeitem());
				int removeCount = (int) (itemCount <= times ? itemCount : times);
				if (removeCount > 0) {
					bag.removeItem(drawCardCfg.getCommomdrawcardconsumeitem(), removeCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_Common), true);
					Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE, drawCardCfg.getCommomdrawcardconsumeitem(), removeCount);
					if (consume != null) {
						consumes.add(consume);
					}
				}
				return times - removeCount;
			});
		}

		// 超出消耗
		int consumeDiamondCount = remainTimes * drawCardCfg.getCommondrawcarddiamond();
		if (times >= 10) {
			consumeDiamondCount = (consumeDiamondCount * drawCardCfg.getDrawcommondiamonddiscount()) / 100;
		}

		Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Diamond_VALUE, 0, consumeDiamondCount);
		if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
			Reward reward = RewardUtil.parseReward(RewardTypeEnum.RTE_Item, drawCardCfg.getCommomdrawcardconsumeitem(), times - remainTimes);
			RewardManager.getInstance().doReward(playerIdx, reward, reason, false);
			return false;
		}
		consumes.add(consume);
		return true;
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_High;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_DrawCommonCard_VALUE, SC_DrawCommonCard.newBuilder().setRetCode(retCode));
	}
}
