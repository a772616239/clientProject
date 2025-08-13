package server.http.entity;

/**
 * @author huhan
 * @date 2020/07/08
 */
public class IllegalIpRequestException extends Exception{

    public IllegalIpRequestException(String info) {
        super(info);
    }
}
