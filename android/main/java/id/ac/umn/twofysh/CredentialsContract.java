package id.ac.umn.twofysh;

import android.provider.BaseColumns;

/**
 * Created by SUNDERI on 9/26/2017.
 */

public class CredentialsContract {
    private CredentialsContract(){}

    public static class CredentialsEntry implements BaseColumns{
        public static final String TABLE_NAME="credentials";
        public static final String COLUMN_NAME_keyhandle="keyhandle";
        public static final String COLUMN_NAME_username="username";
        public static final String COLUMN_NAME_appid="appid";
        public static final String COLUMN_NAME_authenticate_portal="authenticate_portal";
        public static final String COLUMN_NAME_counter="counter";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CredentialsEntry.TABLE_NAME + " (" +
                    CredentialsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CredentialsEntry.COLUMN_NAME_keyhandle + " TEXT," +
                    CredentialsEntry.COLUMN_NAME_username + " TEXT," +
                    CredentialsEntry.COLUMN_NAME_appid + " TEXT," +
                    CredentialsEntry.COLUMN_NAME_authenticate_portal + " TEXT," +
                    CredentialsEntry.COLUMN_NAME_counter + " INTEGER)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CredentialsEntry.TABLE_NAME;


}
