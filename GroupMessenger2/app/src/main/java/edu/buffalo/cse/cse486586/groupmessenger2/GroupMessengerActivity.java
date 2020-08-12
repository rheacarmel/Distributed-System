package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.EOFException;
import java.io.OptionalDataException;
import java.io.Serializable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

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
    /* static final String REMOTE_PORT1="11108";
     static final String REMOTE_PORT2="11112";
     static final String REMOTE_PORT3="11116";
     static  final String REMOTE_PORT4="11120";
     static  final String REMOTE_PORT5="11124";*/
    static  double a[]=new double[5];
    public static double Fserver_no=-0.1;
    static int FailPort =0;
    int Sfailport=0;
    double counter=0.0;
    public static int msgId=0;
    int Dflag=0;
    int failindex=-1;
    int number_socket=5;
    static double proposed__counter=0.0;
    static double final_counter=0.0;
    static double Final_counter=0.0;
    static int numofMess=0;

    double fcounter=-0.0;


    PriorityQueue<App_message>Pqueue=new PriorityQueue<App_message>(25,new OObject_comparator());
    PriorityQueue<App_message>Pqueue1=new PriorityQueue<App_message>(25,new OObject_comparator());
    String [] REMOTE_PORT ={"11108","11112","11116","11120","11124"};

    ArrayList<Double>FinalGCounter=new ArrayList<Double>();
    ArrayList<Double>FinalServerNo=new ArrayList<Double>();


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
                msgId=msgId+1;

                editText.setText("");
                TextView textView= (TextView)findViewById(R.id.textView1);
                // textView.append(msg);
                //textView.append("\n");

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msg,myPort);

            }
        });
    }

    int count=0;
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... serverSockets) {
            int seq=0;
           // Log.i(TAG, "initial seq"+seq);
            ServerSocket serverSocket = serverSockets[0];



            try {


                while (true) {

                    Socket clientSocket = serverSocket.accept();
                    ObjectOutputStream ds = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream ir = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));


                    App_message server_message;
                    server_message = (App_message) ir.readObject();
                    Sfailport=server_message.getFailport();
                    Log.i(TAG, "Failport in server"+Sfailport);    // checks for failed ports

                    if (server_message.getFinal_counter() == 0.0)
                    {                                                   //To get the Proposed counter

                        server_message.setSeq(seq);
                         seq = seq + 1;

                        if (proposed__counter <= final_counter) {
                            proposed__counter = (int) final_counter + server_message.getServer_number() + 1;

                        } else {
                            proposed__counter = (int) proposed__counter + server_message.getServer_number() + 1;

                        }

                        server_message.setCounter(proposed__counter);

                        if (server_message.getPortnumber() != Sfailport)
                        {
                            Pqueue.add(server_message);
                            synchronized (Pqueue)
                            {
                                for (App_message p : Pqueue)
                                {
                                     Log.i(TAG, "Pqueue proposed terms added" +p.getCounter());
                                }
                             }
                             server_message.setAcknowledge("true");                            //server sends and acknowledgement  with the proposed counter
                             ds.writeObject(server_message);
                             ds.flush();


                         }

                        clientSocket.close();

                    }

                    else if (server_message.getFinal_counter() != 0.0)
                    {   //App_message Final_message;
                        final_counter = server_message.getFinal_counter();

                        synchronized (Pqueue)
                        {
                            for (App_message p : Pqueue)
                            {
                                Log.i(TAG, "Pqueue counterlist"+p.getCounter());                     // printing the Pqueue(Initial) counter
                            }
                        }

                        synchronized (Pqueue1)
                        {
                            for (App_message p : Pqueue1)
                            {
                                Log.i(TAG, "Pqueue1 counterlist"+p.getCounter());
                            }
                        }


                        synchronized (Pqueue)
                        {
                            for (App_message p : Pqueue)

                            {
                                if (p.getPortnumber() != Sfailport)                                    //elements in the initial queue that dont have the port number equal to the failport are added to the final queue along with its final counter
                                {
                                    if (p.getCounter() == server_message.getCounter())
                                    {
                                        Log.i(TAG, "inside remove add");
                                        Pqueue.remove(p);                                              //remove from initial queue and add to the final queue.
                                        p.setCounter(server_message.getFinal_counter());
                                        p.setDeliverable(true);
                                        Pqueue1.add(p);
                                    }
                                }



                            }
                        }





                            //Log.i(TAG, "final queue is"+Pqueue.peek().getCounter());
                        Log.i(TAG, "Pqueue1 size" + Pqueue1.size());
                        Log.i(TAG, "Pqueue size" + Pqueue.size());




                        synchronized (Pqueue1)
                        {
                            for (App_message p : Pqueue)
                            {
                                Log.i(TAG, "Pqeue final list " + p.getCounter()+"port number"+p.getPortnumber());
                            }
                        }




                        server_message.setAcknowledge("true");                                 //server sends acknowledgement the second time

                        ds.writeObject(server_message);
                        ds.flush();


                    }

                    // Printing the queue

                    synchronized (Pqueue)                                                       //messages from the failed avd are deleted from the initial queue.
                    {
                        for (App_message p : Pqueue)
                        {
                            if(p.getPortnumber()==Sfailport)
                            {
                                Pqueue.remove(p);
                                Log.i(TAG, "Pqueue removed"+"fail port "+Sfailport+"size after remove "+Pqueue.size());
                            }
                        }
                    }



                    // This is the printing logic.  if the Pqueue (initial queue) size==0  then it means all the messages have been send to the final queue and
                    // are hence delivered to the avd. If the intial queue is not zero than there may be messages that may still have not found its final counter or may be a message from its failure node which is yet to be deleted.
                    //This logic is used to provide the queue with a stabilization time as the messages pass from 1 queue to another.



                    synchronized (Pqueue1) {
                        App_message first = Pqueue1.peek();

                        while (first != null && first.getDeliverable() == true)
                        {
                            if (Pqueue.size() != 0) {

                                break;
                            } else {
                                first = Pqueue1.poll();
                                String msg = first.getMessage() + ":" + first.getSeq();
                                publishProgress(msg);
                                first = Pqueue1.peek();

                                 }

                        }
                    }


                    clientSocket.close();

                }

            } catch (OptionalDataException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();

            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }


            return null;
        }


        protected void onProgressUpdate(String...strings) {

            String [] message=strings[0].split(":");
             String strReceived = message[0].trim();

            Log.i(TAG, "message count "+message[1]);
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived +"\t\n");

            providerUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            ContentValues keyValueToInsert = new ContentValues();


            // inserting <”key-to-insert”, “value-to-insert”>
            keyValueToInsert.put("key",Integer.toString(count));
            //keyValueToInsert.put("key",message[1]);
            keyValueToInsert.put("value",strReceived);

            Uri newUri = getContentResolver().insert(
                    providerUri,    // assume we already created a Uri object with our provider URI using uribuilder
                    keyValueToInsert
            );
            count=count+1;
            Log.i(TAG, "inserted"+newUri.toString());

            //count=count+1;


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


            App_message app_message = new App_message();
            App_message Final_message = new App_message();



            try {

                     String msgToSend = strings[0];


                     ArrayList Pcounter=new ArrayList();


                    for (int i = 0; i < 5; i++)
                    {
                         Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT[i]));
                         socket.setSoTimeout(30000);
                         app_message.setMessage(msgToSend);
                         app_message.setDeliverable(false);
                         app_message.setMsgId(msgId);
                         app_message.setPortnumber(Integer.parseInt(strings[1]));


                         ObjectOutputStream ds = new ObjectOutputStream(socket.getOutputStream());
                         String serv_no = "0." + i;

                        app_message.setServer_number(Double.parseDouble(serv_no));
                        ds.writeObject(app_message);
                        ds.flush();

                    //read the proposed counter
                        try
                        {

                            ObjectInputStream ir = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            App_message new_message = (App_message) ir.readObject();
                            FinalGCounter.add(new_message.getCounter());
                            Final_message=new_message;
                            Pcounter.add(new_message.getCounter());
                        //Log.i(TAG, "doInBackground: "+new_message.getAcknowledge()+new_message.getServer_number());
                           if (new_message.getAcknowledge().equals("true"))
                             {
                                   socket.close();
                                   new_message.setAcknowledge("false");
                             }

                        }

                        catch (ClassNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch (OptionalDataException e)
                        {
                              e.printStackTrace();
                        }
                        catch (StreamCorruptedException e)
                        {
                              e.printStackTrace();
                        }
                        catch (IOException e)
                        {

                            FailPort=socket.getPort();
                            Log.i(TAG, "Failport in client"+FailPort);
                            socket.close();
                        }
                        catch (Exception e)
                        {
                             e.printStackTrace();
                            FailPort=socket.getPort();
                            Log.i(TAG, "Failport in client"+FailPort);
                            socket.close();
                        }

                    }









                    //get the maximum proposed counter
                    Final_counter= (Double)Collections.max(Pcounter);


                   for (int i = 0; i <5; i++)
                   {
                         Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT[i]));
                         if (socket.getPort() == FailPort)
                        {

                        FinalGCounter.add(i,0.0);
                        socket.close();
                        continue;
                        }


                        Final_message.setCounter(FinalGCounter.get(i));
                        Final_message.setMsgId(msgId);
                        Final_message.setFailport(FailPort);

                         ObjectOutputStream ds = new ObjectOutputStream(socket.getOutputStream());
                         Final_message.setFinal_counter(Final_counter);
                         ds.writeObject(Final_message);
                         ds.flush();

                    //recieve second acknowledgement
                        try {
                           ObjectInputStream ir = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                           Final_message = (App_message) ir.readObject();
                        //Log.i(TAG, "new_message" + "server" + Final_message.getServer_number() + "ack" + Final_message.getAcknowledge());

                           if (Final_message.getAcknowledge().equals("true"))
                               {
                      //      Log.i(TAG, "Final_message closed");
                               socket.close();

                               }
                          }
                        catch (ClassNotFoundException ex)
                         {
                        ex.printStackTrace();
                         }
                        catch (IOException e)
                        {
                          socket.close();

                         }
                        catch (NullPointerException e)
                        {
                           socket.close();

                        }

                   }
                   FinalGCounter.clear();
                   FinalServerNo.clear();
                   Pcounter.clear();

                     //app_message.setServer_number(0.0);
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (OptionalDataException e)
            {
                e.printStackTrace();
            }
            catch (StreamCorruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();

            }


            //send the final count



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


class App_message implements  Serializable{
    int seq;
    int msgId;
    double counter;
    double final_counter=0.0;
    String message;
    Boolean deliverable= Boolean.FALSE;
    double server_number;
    int portnumber;
    String acknowledge;
    int Failport=0;

    public void setFailport(int failport) {
        Failport = failport;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;

    }


    public void setPortnumber(int portnumber) {
        this.portnumber = portnumber;
    }

    public void setAcknowledge(String acknowledge) {
        this.acknowledge = acknowledge;
    }

    public void setServer_number(double server_number) {
        this.server_number = server_number;
    }

    public void setFinal_counter(double final_counter) {
        this.final_counter = final_counter;
    }

    public int getFailport() {
        return Failport;
    }

    public double getServer_number() {
        return server_number;
    }

    public String getAcknowledge() {
        return acknowledge;
    }

    public int getMsgId() {
        return msgId;
    }

    public Boolean getDeliverable() {
        return deliverable;
    }

    public int getPortnumber() {
        return portnumber;
    }

    public int getSeq() {
        return seq;
    }

    public void setCounter(double counter) {
        this.counter = counter;
    }

    public double getFinal_counter() {
        return final_counter;
    }

    public double getCounter() {
        return counter;
    }
    public void setMessage(String message){
        this.message=message;
    }

    public String getMessage() {
        //if(counter==this.counter)
            return message;
        //else{
          //  return null;

        //s}
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public void setDeliverable(Boolean deliverable) {
        this.deliverable = deliverable;
    }

   /* @Override
    public int compareTo(App_message another) {
        if (this.getCounter() > another.getCounter()) {
            return 1;
        } else if (this.getCounter() < another.getCounter()) {
            return -1;
        } else {
            return 0;
        }
    }*/
}

 class OObject_comparator implements Comparator<App_message>                               //geekforgeeks/priorityqueue-comparator-method-in-java
 {
     @Override
     public int compare(App_message lhs, App_message rhs) {
         if(lhs.getCounter()>rhs.getCounter())
         {
             return 1;
         }
         else if(lhs.getCounter()<rhs.getCounter())
         {
             return -1;
         }
         return 0;
     }
 }
