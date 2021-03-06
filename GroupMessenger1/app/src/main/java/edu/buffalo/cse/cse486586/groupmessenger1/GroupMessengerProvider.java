package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import static android.content.ContentValues.TAG;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */

public class GroupMessengerProvider extends ContentProvider {
    private static  int count;
    private static  String mc_values;


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         *
         */
        //values.valueSet();
        Log.i(TAG, "value set"+values.valueSet().toString());
        String key=values.getAsString("key");
        String V_Value=values.getAsString("value");

        String filename;
        filename=key;
        FileOutputStream outputStream;
        try {
                outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(V_Value.getBytes());
                outputStream.close();


                Log.i(TAG, "insert: successfull");
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }









        Log.v("insert", values.toString());
        return uri;
    }



    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         *
         *
         */
        //FileInputStream fileInput;

       /* try {
            Log.i(TAG, "selection:"+selection);
            fileInput=getContext().openFileInput(selection);

            StringBuilder mess=new StringBuilder();
            BufferedReader br= new BufferedReader(fileInput);
            String message;
            while((message=br.readLine())!= null){

                mess.append(message);
                mess.append("\n");
            }*/
        String text;
        String value ;
        try {
            Log.i(TAG, "file: " + selection);
            InputStream fileInput = getContext().openFileInput(selection);
            BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
            StringBuilder str = new StringBuilder();


            while ((text = bf.readLine()) != null) {
                str.append(text);
            }

            Log.i(TAG, "text" + str.toString());
            mc_values=str.toString();
            bf.close();



        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Object value1 = mc_values;  //converting and string to object
        MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
        mc.newRow().add("key", selection).add("value", value1);
        return mc;

        //Log.v("query", selection);
        //return ;
        //return

    }
}
