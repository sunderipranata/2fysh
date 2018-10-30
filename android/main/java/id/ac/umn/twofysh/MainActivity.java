package id.ac.umn.twofysh;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.security.auth.x500.X500Principal;

public class MainActivity extends AppCompatActivity implements MyCredentialAdapter.MyCredentialAdapterCallback {

    private DecoratedBarcodeView qrCodeView;
    private BeepManager beepManager;
    private String lastText;
    private MyCredentialAdapter myCredentialAdapter;
    private ListView lvCredentials;
    private ArrayList<Credential> credentialsData;

    CredentialsDbHelper dbHelper;
    SQLiteDatabase dbReadable;
    SQLiteDatabase dbWritable;

    private JSONObject authData = null;

    private static int NFC_ACTIVITY=1;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            qrCodeView.setStatusText(result.getText());
            beepManager.playBeepSoundAndVibrate();

            registerOrAuthenticate(result.getText());



        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    void registerOrAuthenticate(String txt){
        JSONObject data=null;
        try{
            data = new JSONObject(txt);
            if(data.getString("action").toString().equals("registration")){
                Toast.makeText(MainActivity.this, "Register", Toast.LENGTH_SHORT).show();
                register(data);
                //TODO: THIS CODE IS BELOW FUCKED UP BUT IT WORKS ANYWAYS :)
                refreshKeyList();
            }
            else if(data.getString("action").toString().equals("authentication")){
                Toast.makeText(MainActivity.this, "Authenticate", Toast.LENGTH_SHORT).show();
                authData=data;
                Intent i = new Intent(MainActivity.this, NFCActivity.class);
                startActivityForResult(i,NFC_ACTIVITY);
                Log.v("Activity", "Activity started");
            }
            else{
                Toast.makeText(MainActivity.this, "QR ERROR: Not 2fysh data format. (Wrong action data)", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "QR ERROR: Not 2fysh data format. (Not even JSON)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvCredentials = (ListView) findViewById(R.id.lvCredentials);
        dbHelper = new CredentialsDbHelper(getApplicationContext());
        dbReadable = dbHelper.getReadableDatabase();
        dbWritable = dbHelper.getWritableDatabase();

        printAllKeyAliases();
        checkCameraPermission();
        refreshKeyList();

        qrCodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        qrCodeView.setStatusText("Scan QR code to register or authenticate.");
        beepManager = new BeepManager(this);
        qrCodeView.decodeContinuous(callback);

        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            if (data.getQueryParameter("JSONdata") != null) {
                String txt = data.getQueryParameter("JSONdata");
                Log.v("isi dari link",txt);
                registerOrAuthenticate(txt);
            }
        }


    }
    @Override
    protected void onResume() {
        super.onResume();

        if(qrCodeView!=null)
            qrCodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(qrCodeView!=null)
            qrCodeView.pause();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return qrCodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("Activity", "masuk on Activity result");
        if (requestCode == NFC_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                Log.v("encrypted salt (main)",data.getStringExtra("result"));
                try{
                    String keyhandle=authData.getString("keyHandle").toString();
                    String salt=null;
                    if(keyhandle!=null)
                        salt = MyRSA.decryptSalt(keyhandle,data.getStringExtra("result"));
                    if(salt!=null)
                        if (authenticate(authData, salt)) finish();
                    else
                        Toast.makeText(MainActivity.this, "Wrong card", Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "QR ERROR: Not 2fysh authentication data format.", Toast.LENGTH_SHORT).show();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private boolean authenticate(JSONObject data, String salt){
        Log.v("SALT", salt);
        AuthenticationData authenticationData;
        try{
            authenticationData = new AuthenticationData(
                    data.getString("username").toString(),
                    data.getString("appId").toString(),
                    data.getString("challenge").toString(),
                    data.getString("authenticate_portal").toString(),
                    data.getString("keyHandle").toString()
            );
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "QR ERROR: Not 2fysh authentication data format.", Toast.LENGTH_SHORT).show();
            return false;
        }
        //TODO: read NFC salt & decrypt it
        //but for now lets just use any way.
        //saltnya si ayamgoreng
        //String salt = "805e0473bcaa2f4b8d69a77593968aa3";
        int counter = getCounter(authenticationData.keyHandle);


        boolean success = sendAuthenticationBackToServer(authenticationData, salt, counter);
        if(success){
            /////////////////////////////////STORE THINGS UP/////////////////////////////
            dbWritable = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CredentialsContract.CredentialsEntry.COLUMN_NAME_counter, counter+1);
            String selection = CredentialsContract.CredentialsEntry.COLUMN_NAME_keyhandle + " LIKE ?";
            String[] selectionArgs = { authenticationData.keyHandle };

            long count = dbWritable.update(
                    CredentialsContract.CredentialsEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            return true;
        }
        else return false;

    }


    private void register(JSONObject data){
        RegistrationData registrationData;
        try{
            registrationData = new RegistrationData(
                    data.getString("username").toString(),
                    data.getString("appId").toString(),
                    data.getString("challenge").toString(),
                    data.getString("register_portal").toString(),
                    data.getString("authenticate_portal").toString()
            );
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "QR ERROR: Not 2fysh registration data format.", Toast.LENGTH_SHORT).show();
            return;
        }

        registrationData.keyHandle=createKeyHandle();

        String publicKey = MyRSA.createKeyPair(registrationData.keyHandle, getApplicationContext());
        Log.v("RSA PUBLIC KEY", publicKey);


        //IF SUCCESS BARU STORE, KALO NGGAK GAK STORE + DELETE KEY PAIR YG BARU DIBUAT
        boolean success = sendCredentialsToServer(registrationData, publicKey);
        if(success){
            /////////////////////////////////STORE THINGS UP/////////////////////////////
            dbWritable = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CredentialsContract.CredentialsEntry.COLUMN_NAME_keyhandle, registrationData.keyHandle);
            values.put(CredentialsContract.CredentialsEntry.COLUMN_NAME_username, registrationData.username);
            values.put(CredentialsContract.CredentialsEntry.COLUMN_NAME_appid, registrationData.appId);
            values.put(CredentialsContract.CredentialsEntry.COLUMN_NAME_authenticate_portal, registrationData.authenticate_portal);
            values.put(CredentialsContract.CredentialsEntry.COLUMN_NAME_counter, 0);
            long newRowId = dbWritable.insert(CredentialsContract.CredentialsEntry.TABLE_NAME, null, values);
        }
        else{
            try{
                MyRSA.deleteEntry(registrationData.keyHandle);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        printAllKeyAliases();
    }

    private boolean sendAuthenticationBackToServer (final AuthenticationData authenticationData, String salt, int counter){
        JSONObject signedStuffJSON = new JSONObject();
        try{
            signedStuffJSON.put("received_challenge_salt", authenticationData.challenge+salt);
            signedStuffJSON.put("received_counter", counter);
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "JSON went wrong", Toast.LENGTH_SHORT).show();
        }
        String signedStuff = MyRSA.encryptString(authenticationData.keyHandle, signedStuffJSON.toString());

        final HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("username", authenticationData.username);
        hashMap.put("signedStuff", signedStuff);

        try{
            AsyncTask<Void,Void,JSONObject> asyncTask = new AsyncTask<Void, Void, JSONObject>() {
                ProgressDialog progressDialog;
                @Override
                protected void onPreExecute() {
                    progressDialog=new ProgressDialog(MainActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Authenticating...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected JSONObject doInBackground(Void... params) {
                    JSONObject result;
                    result = HttpPostReturnJSON.makeHttpRequest(authenticationData.authenticate_portal, hashMap);
                    if(result!=null)Log.v("SERVER", result.toString());
                    return result;
                }

                @Override
                protected void onPostExecute(JSONObject result) {
                    String message="Authentication failed.";
                    try{
                        message=result.getString("message");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG ).show();
                    progressDialog.dismiss();
                }
            };

            JSONObject result = asyncTask.execute().get();
            return result==null? false : result.getString("success").equals("0") ? false : true;
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Couldn't authenticate", Toast.LENGTH_SHORT).show();
        }
        return false;

    }


    private boolean sendCredentialsToServer(final RegistrationData registrationData, String publicKey){
        JSONObject signedStuffJSON = new JSONObject();
        try{
            signedStuffJSON.put("challenge", registrationData.challenge);
            signedStuffJSON.put("keyHandle", registrationData.keyHandle);
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "JSON went wrong", Toast.LENGTH_SHORT).show();
        }
        String signedStuff = MyRSA.encryptString(registrationData.keyHandle, signedStuffJSON.toString());


        final HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("publicKey", publicKey);
        hashMap.put("username", registrationData.username);
        hashMap.put("signedStuff", signedStuff);

        try{
            AsyncTask<Void,Void,JSONObject> asyncTask = new AsyncTask<Void, Void, JSONObject>() {
                ProgressDialog progressDialog;
                @Override
                protected void onPreExecute() {
                    progressDialog=new ProgressDialog(MainActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Registering...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected JSONObject doInBackground(Void... params) {
                    JSONObject result;
                    result = HttpPostReturnJSON.makeHttpRequest(registrationData.register_portal, hashMap);
                    if(result!=null)Log.v("SERVER", result.toString());
                    return result;
                }

                @Override
                protected void onPostExecute(JSONObject result) {
                    String message="Registration failed.";
                    try{
                        message=result.getString("message");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG ).show();
                    progressDialog.dismiss();
                }
            };

            JSONObject result = asyncTask.execute().get();
            return result==null? false : result.getString("success").equals("0") ? false : true;
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Couldn't register", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void deletePressed(final int _ID, final String keyHandle){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
// Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String selection = CredentialsContract.CredentialsEntry._ID + " = ?";
                String[] selectionArgs = { String.valueOf(_ID) };
                dbWritable.delete(CredentialsContract.CredentialsEntry.TABLE_NAME, selection, selectionArgs);
                try{
                    MyRSA.deleteEntry(keyHandle);
                    printAllKeyAliases();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                refreshKeyList();
                Toast.makeText(MainActivity.this,"Key deleted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setMessage("Delete this credential?");
        dialog.show();
    }


    public void refreshKeyList(){
        if(credentialsData!=null)
            credentialsData.clear();
        credentialsData = getKeyLists();
        myCredentialAdapter = new MyCredentialAdapter(MainActivity.this, credentialsData);
        myCredentialAdapter.setCallback(this);
        lvCredentials.setAdapter(myCredentialAdapter);
    }


    private int getCounter(String keyHandle){
        String[] projection = {
                CredentialsContract.CredentialsEntry.COLUMN_NAME_counter
        };
        String sortOrder = CredentialsContract.CredentialsEntry._ID + " ASC";
        String selection = CredentialsContract.CredentialsEntry.COLUMN_NAME_keyhandle + " = ?";
        String[] selectionArgs = { keyHandle };

        Cursor c = dbReadable.query(
                CredentialsContract.CredentialsEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if(c.moveToFirst()){
            return c.getInt(c.getColumnIndex(CredentialsContract.CredentialsEntry.COLUMN_NAME_counter));
        }
        return -1;
    }

    private ArrayList<Credential> getKeyLists(){

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                CredentialsContract.CredentialsEntry._ID,
                CredentialsContract.CredentialsEntry.COLUMN_NAME_keyhandle,
                CredentialsContract.CredentialsEntry.COLUMN_NAME_username,
                CredentialsContract.CredentialsEntry.COLUMN_NAME_appid,
                CredentialsContract.CredentialsEntry.COLUMN_NAME_authenticate_portal,
                CredentialsContract.CredentialsEntry.COLUMN_NAME_counter
        };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                CredentialsContract.CredentialsEntry._ID + " ASC";

        Cursor c = dbReadable.query(
                CredentialsContract.CredentialsEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Credential> temp = new ArrayList<Credential>();
        if(c.moveToFirst()){
            do{
                Credential reg = new Credential(
                        c.getInt(c.getColumnIndex(CredentialsContract.CredentialsEntry._ID)),
                        c.getString(c.getColumnIndex(CredentialsContract.CredentialsEntry.COLUMN_NAME_keyhandle)),
                        c.getString(c.getColumnIndex(CredentialsContract.CredentialsEntry.COLUMN_NAME_username)),
                        c.getString(c.getColumnIndex(CredentialsContract.CredentialsEntry.COLUMN_NAME_appid)),
                        c.getString(c.getColumnIndex(CredentialsContract.CredentialsEntry.COLUMN_NAME_authenticate_portal)),
                        c.getInt(c.getColumnIndex(CredentialsContract.CredentialsEntry.COLUMN_NAME_counter))
                );
                temp.add(reg);
            }
            while(c.moveToNext());
        }
        return temp;
    }


    private void checkCameraPermission(){
        int requestCode = 200; // 200 OK
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, requestCode);
    }

    private void printAllKeyAliases(){
        try{
            Log.v("KEYSTORE COUNT", MyRSA.getKeyCount());
            ArrayList<String> al = MyRSA.getAllAliasesInTheKeystore();
            for(String x : al){
                Log.v("KEYSTORE", x);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    private String createKeyHandle(){
        Random ran = new SecureRandom();
        byte [] temp = new byte[32];
        ran.nextBytes(temp);
        return bytesToHex(temp);
    }
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    protected void onDestroy() {
        dbReadable.close();
        dbWritable.close();
        super.onDestroy();
    }









}
