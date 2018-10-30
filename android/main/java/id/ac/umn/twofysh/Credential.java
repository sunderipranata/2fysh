package id.ac.umn.twofysh;

/**
 * Created by SUNDERI on 9/26/2017.
 */

public class Credential {
    int _ID;
    String keyHandle;
    String username;
    String appId;
    String authenticate_portal;
    int counter;

    Credential(
            int _ID,
            String keyHandle,
            String username,
            String appId,
            String authenticate_portal,
            int counter
    ) {
        this._ID=_ID;
        this.keyHandle = keyHandle;
        this.username = username;
        this.appId = appId;
        this.authenticate_portal = authenticate_portal;
        this.counter = counter;
    }
}
