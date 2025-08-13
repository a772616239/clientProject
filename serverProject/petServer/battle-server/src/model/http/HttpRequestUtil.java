package model.http;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import datatool.StringHelper;
import protocol.Battle.BattleCheckParam;
import protocol.Battle.CS_BattleResult;
import util.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpRequestUtil {

    private static String doPost(String httpUrl, String param) {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：5000毫秒
            connection.setConnectTimeout(5000);
            // 设置读取主机服务器返回数据超时时间：5000毫秒
            connection.setReadTimeout(5000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/json");
            // 通过连接对象获取一个输出流
            outputStream = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            outputStream.write(param.getBytes());
            inputStreamReader = new InputStreamReader(connection.getInputStream(), UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            String result = bufferedReader.readLine();
            LogUtil.info("HttpRequestUtil,Http Response:" + result + "\n");
            return result;
        } catch (Exception e) {
            LogUtil.error("error in HttpRequestUtil,method doPost(),connection exception");
            return null;
        } finally {
            // 关闭资源
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }

    public static CS_BattleResult checkBattle(BattleCheckParam battleCheckParam) {
        try {
            String url = ServerConfig.getInstance().getBattleCheckUrl();
            if (StringHelper.isNull(url)) {
                return null;
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("BattleCheckParam", battleCheckParam.toByteArray());
            String jsonStr = jsonObj.toJSONString();
            LogUtil.debug("check battle jsonStr=" + jsonStr);
            String result = doPost(url, jsonStr);
            if (result == null) {
                return null;
            }
            JSONObject retJson = JSONObject.parseObject(result);
            int errCode = retJson.getInteger("errCode");
            if (errCode != 200) {
                String errMsg = retJson.getString("errMsg");
                if (errMsg == null) {
                    errMsg = "";
                }
                LogUtil.error("check battle error,errCode=" + errCode + ",errMsg=" + errMsg);
                return null;
            }
            byte[] data = retJson.getBytes("data");
            if (data == null) {
                LogUtil.error("check battle failed,data is null");
                return null;
            }
            CS_BattleResult battleResult = CS_BattleResult.parseFrom(data);
            return battleResult;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public static String doPostConnectTest(String httpUrl, String param) {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：5000毫秒
            connection.setConnectTimeout(5000);
            // 设置读取主机服务器返回数据超时时间：5000毫秒
            connection.setReadTimeout(5000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/json");
            // 通过连接对象获取一个输出流
            outputStream = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            outputStream.write(param.getBytes());
            inputStreamReader = new InputStreamReader(connection.getInputStream(), UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.readLine();
        } catch (Exception ex) {
            //400状态码参数不对,但是url是通的
            if (ex.getMessage().contains("response code: 400")) {
                return null;
            } else {
                LogUtil.printStackTrace(ex);
                if(!ServerConfig.getInstance().isDebug()) {
                    throw new RuntimeException("http请求错误,请检查服务状态或请求地址:" + httpUrl);
                }else {
                    LogUtil.error("http请求错误,请检查服务状态或请求地址:" + httpUrl);
                }
            }
        } finally {
            // 关闭资源
            releaseResource(connection, outputStream, inputStreamReader, bufferedReader);
        }
        return null;
    }

    private static void releaseResource(HttpURLConnection connection, OutputStream outputStream, InputStreamReader inputStreamReader, BufferedReader bufferedReader) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
            LogUtil.warn("exception in HttpRequestUtil,method doPost(),return resource exception");
        }
    }
}