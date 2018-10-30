package id.ac.umn.twofysh;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by SUNDERI on 9/26/2017.
 */

public class MyCredentialAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Credential> data;
    private static LayoutInflater inflater=null;

    public MyCredentialAdapter(Activity a, ArrayList<Credential> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.credential_row, null);

        ImageView img =(ImageView)vi.findViewById(R.id.icon);
        TextView txtAppId = (TextView)vi.findViewById(R.id.txtAppId);
        TextView txtUsername = (TextView)vi.findViewById(R.id.txtUsername);
        Button btnDelete = (Button) vi.findViewById(R.id.btnDelete);

        final Credential credential = data.get(position);

        // Setting all values in listview
        txtAppId.setText(credential.appId);
        txtUsername.setText(credential.username);
//        Log.v("USERNAME", credential.username);
//        Log.v("ID", String.valueOf(credential._ID));
//        Log.v("position", String.valueOf(position));
        //
        // TODO: SET IMAGE BITMAP BASED ON APP ID FAVICON
        // img.setImageBitmap();

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null) {
                    callback.deletePressed(credential._ID, credential.keyHandle);
                }
            }
        });

        return vi;
    }
    private MyCredentialAdapterCallback callback;
    public void setCallback(MyCredentialAdapterCallback callback){

        this.callback = callback;
    }
    public interface MyCredentialAdapterCallback {

        void deletePressed(int position, String keyHandle);
    }

}
