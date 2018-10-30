package id.ac.umn.twofysh;

/**
 * Created by SUNDERI on 10/2/2017.
 */

public class AuthenticationData {
    static String action = "authentication";
    String appId;
    String challenge;
    String authenticate_portal;
    String keyHandle;
    String username;

    AuthenticationData(
            String username,
            String appId,
            String challenge,
            String authenticate_portal,
            String keyHandle
    ){
        this.username=username;
        this.appId=appId;
        this.challenge=challenge;
        this.authenticate_portal=authenticate_portal;
        this.keyHandle=keyHandle;
    }

}
