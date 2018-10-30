package id.ac.umn.twofysh;

/**
 * Created by SUNDERI on 9/25/2017.
 */

public class RegistrationData {
    static String action = "registration";
    String username;
    String appId;
    String challenge;
    String register_portal;
    String authenticate_portal;
    String keyHandle;

    RegistrationData(
            String username,
            String appId,
            String challenge,
            String register_portal,
            String authenticate_portal
    ){
        this.username=username;
        this.appId=appId;
        this.challenge=challenge;
        this.register_portal=register_portal;
        this.authenticate_portal=authenticate_portal;

    }
}
