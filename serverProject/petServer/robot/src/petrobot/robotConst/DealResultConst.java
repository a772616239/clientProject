package petrobot.robotConst;

/**
 * 1为当步骤结束(自动执行下个配置表步骤)   0当前步骤执行失败（机器人流程有误，需要人工检查机器人失败原因） -1正在执行某一步骤
 */
public class DealResultConst {
    public static final int CUR_STEP_SUCCESS = 1;
    public static final int CUR_STEP_FAILED = 0;
    public static final int CUR_STEP_RUNNING = -1;
}
