package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static android.content.ContentValues.TAG;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    private Uri providerUri;
    static  final int SERVER_PORT=10000;
    static final String REMOTE_PORT1="11108";
    static final String REMOTE_PORT2="11112";
    static final String REMOTE_PORT3="11116";
    static  final String REMOTE_PORT4="11120";
    static  final String REMOTE_PORT5="11124";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());


        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try{


            ServerSocket serverSocket= new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "cannot create server ");
        }

        final EditText editText= (EditText) findViewById(R.id.editText1);
        Button button=(Button)findViewById(R.id.button4);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=editText.getText().toString();
                editText.setText("");
                TextView textView= (TextView)findViewById(R.id.textView1);
                // textView.append(msg);
                //textView.append("\n");

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msg,myPort);

            }
        });
    }

    int count;
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... serverSockets) {

            ServerSocket serverSocket = serverSockets[0];


            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    DataInputStream ir = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    String msg="";


                    msg = ir.readUTF();
                    publishProgress(msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }




            return null;
        }

        protected void onProgressUpdate(String...strings) {
            String strReceived = strings[0].trim();

            Log.i(TAG, "message count "+count);
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived +"\t\n");

            providerUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
            ContentValues keyValueToInsert = new ContentValues();

            // inserting <”key-to-insert”, “value-to-insert”>
            keyValueToInsert.put("key",Integer.toString(count));
            keyValueToInsert.put("value",strReceived);

            Uri newUri = getContentResolver().insert(
                    providerUri,    // assume we already created a Uri object with our provider URI
                    keyValueToInsert
            );

            Log.i(TAG, "inserted"+newUri.toString());

            count=count+1;


            Log.i(TAG, "onProgressUpdate: string recieved"+strReceived);

        }


        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
    }

    private  class ClientTask extends  AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {

            //String remotePort= REMOTE_PORT1;
            //  if (strings[1].equals(REMOTE_PORT1)){

            try {
                String msgToSend=strings[0];


                //Log.i(TAG, "doInBackground: "+remotePort);
                Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT2));
                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT1));

                Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT3));
                Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT4));
                Socket socket5 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT5));
                //String msgToSend=strings[0];
                DataOutputStream ds1=new DataOutputStream(socket1.getOutputStream());
                ds1.writeUTF(msgToSend);
                ds1.flush();
                DataOutputStream ds2=new DataOutputStream(socket2.getOutputStream());
                ds2.writeUTF(msgToSend);
                ds2.flush();
                DataOutputStream ds3=new DataOutputStream(socket3.getOutputStream());
                ds3.writeUTF(msgToSend);
                ds3.flush();
                DataOutputStream ds4=new DataOutputStream(socket4.getOutputStream());
                ds4.writeUTF(msgToSend);
                ds4.flush();
                DataOutputStream ds5=new DataOutputStream(socket5.getOutputStream());
                ds5.writeUTF(msgToSend);
                ds5.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }


            // }

            return null;
        }




    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



}

