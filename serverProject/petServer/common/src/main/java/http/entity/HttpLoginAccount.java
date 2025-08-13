package http.entity;

/**
 * 请求业务参数：登陆账号
 *
 * @author xiao_FL
 * @date 2019/7/3
 */
public class HttpLoginAccount {
    private String userId;
    private String userToken;

    public HttpLoginAccount(String userId, String userToken) {
        setUserId(userId);
        setUserToken(userToken);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    @Override
    public String toString() {
        return "{\"userId\"" + ":" + "\"" + userId + "\","
                + "\"userToken\"" + ":" + "\"" + userToken + "\"}";
    }
}