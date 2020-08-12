package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class SimpleDhtProvider extends ContentProvider {
    private static  String mc_values;
    private  String Start_node="11108";
    static  final int SERVER_PORT=10000;
    static final int MAIN_PORT=11108;



    public  static  int flag=0;


    public ArrayList<String>nodelist=new ArrayList<String>();
    public List<String>newnodelist=new ArrayList<String>();

    public ArrayList<Node1> Pmessage=new ArrayList<Node1>();
    Node1 Mainnode=new Node1();
    public List<String>globalmc=new ArrayList<String>();


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub

        if(selection.equals("@"))
        {
            for (String file : getContext().fileList()) {
                getContext().deleteFile(file);
                Log.i(TAG, "File is deleted");
            }
        }

        else if(selection.equals("*"))
        {
            if(Mainnode.getCurrentId().equals(Mainnode.getPredessorId())&& (Mainnode.getCurrentId().equals(Mainnode.getSuccersorId())))
            {
                for (String file : getContext().fileList()) {
                    getContext().deleteFile(file);
                    Log.i(TAG, "File is deleted");
                }
            }
            else{
                if(flag ==0)
                {
                    flag = 1;
                    for (String file : getContext().fileList())
                    {
                        getContext().deleteFile(file);
                        Log.i(TAG, "File is deleted");
                    }

                    Log.i(TAG, "delete succssesor");
                    String Insert_message="Del_*";
                    Socket socket1= null;
                    try
                    {
                        socket1 = new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(Mainnode.getSuccport())*2);
                        PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
                        ds1.println(Insert_message);
                        ds1.flush();
                        Thread.sleep(5000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

                    return 0;

            }
        }
        else if(!selection.equals("*")||!selection.equals("@")) {
            try {
                   File file =getContext().getFileStreamPath(selection);
                   if(file!=null)
                   {
                       getContext().deleteFile(selection);
                       Log.i(TAG, "delete: the file");
                   }
                   else {
                       String Insert_message=selection+":"+"DELETE_NODE";
                       Socket socket1;
                       socket1 = new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(Mainnode.getSuccport())*2);
                       PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
                       ds1.println(Insert_message);
                       ds1.flush();
                   }


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub

        Log.i(TAG, "value set" + values.valueSet().toString());
        String key = values.getAsString("key");
        String V_Value = values.getAsString("value");
        String keyId ="";
             //for single avd
            if (Mainnode.getPreport() == null || Mainnode.getSuccport() == null) {

                Log.i(TAG, "current port" + Mainnode.getCurport());


                String filename1;
                filename1 = key;
                FileOutputStream outputStream1;
                try {
                    outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
                    outputStream1.write(V_Value.getBytes());
                    outputStream1.close();


                    Log.i(TAG, "insert: successfull");

                } catch (Exception e) {
                    Log.e(TAG, "File write failed");
                }

            }
            //for avd 0
            else if (Mainnode.getCurrentId().compareTo(Mainnode.getPredessorId()) == 0 && Mainnode.getCurrentId().compareTo(Mainnode.getSuccersorId()) == 0) {   //only one node
                Log.i(TAG, "insert inside the avd");

                String filename1;
                filename1 = key;
                FileOutputStream outputStream1;


                try {
                    outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
                    outputStream1.write(V_Value.getBytes());
                    outputStream1.close();


                    Log.i(TAG, "insert: successfull");
                } catch (Exception e) {
                    Log.e(TAG, "File write failed");
                }
            }
            else {
                     try {
                         keyId = genHash(key);

                           // node case were predessor is greater than current node
                           if (Mainnode.getPredessorId().compareTo(Mainnode.getCurrentId()) > 0) {

                             if (keyId.compareTo(Mainnode.getCurrentId())<=0 ||keyId.compareTo(Mainnode.getPredessorId())>0) {

                                 //Log.i(TAG, "in the boundary");

                                 Log.i(TAG, "the insert key is" + key + "gen hash is" + keyId);
                                 String filename1;
                                 filename1 = key;
                                 FileOutputStream outputStream1;

                                 try {
                                     outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
                                     outputStream1.write(V_Value.getBytes());
                                     outputStream1.close();


                                     Log.i(TAG, "insert: successfull");
                                 } catch (Exception e) {
                                     Log.e(TAG, "File write failed");
                                 }


                             } else if ((keyId.compareTo(Mainnode.getCurrentId())> 0) || ( keyId.compareTo(Mainnode.getPredessorId())< 0 && keyId.compareTo(Mainnode.getCurrentId())<0 ))
                             {

                                 Socket socket1;

                                 try {

                                     String Insert_message = key + ":" + V_Value + ":" + "INSERT_NODE";
                                     socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(Mainnode.getSuccport()) * 2);
                                     PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                                     ds1.println(Insert_message);
                                     ds1.flush();
                                 } catch (UnknownHostException e) {
                                     e.printStackTrace();
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }

                             }


                         }
                         else {


                             if (keyId.compareTo(Mainnode.getCurrentId()) <= 0 && keyId.compareTo(Mainnode.getPredessorId()) > 0) {

                                 String filename1;
                                 filename1 = key;
                                 FileOutputStream outputStream1;
                                 try {
                                     outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
                                     outputStream1.write(V_Value.getBytes());
                                     outputStream1.close();


                                     Log.i(TAG, "insert: successfull");
                                 } catch (Exception e) {
                                     Log.e(TAG, "File write failed");
                                 }
                             }


                             else if (keyId.compareTo(Mainnode.getPredessorId())<0 &&  keyId.compareTo(Mainnode.getCurrentId())< 0 ||keyId.compareTo(Mainnode.getCurrentId()) > 0) {


                                 try {
                                     String Insert_message = key+":"+V_Value+":"+"INSERT_NODE";
                                     Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(Mainnode.getSuccport()) * 2);
                                     PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                                     ds1.println(Insert_message);
                                     ds1.flush();
                                 } catch (UnknownHostException e) {
                                     e.printStackTrace();
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }

                             }


                         }
                     }
                     catch(NoSuchAlgorithmException e){
                             e.printStackTrace();
                         }




        }





        Log.i(TAG, "Print the predessor"+Mainnode.getPreport()+" the successor"+Mainnode.getSuccport());









        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try{
            ServerSocket serverSocket= new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "cannot create server ");
        }

        String msg = null;
        try {



                   msg = portStr+ ":" +"Main port"+ ":"+MAIN_PORT+"NODE_JOIN";

                   Log.i(TAG, "message in on create" + msg);


                   new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);



        }
        catch (Exception e){

        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // TODO Auto-generated method stub

        String text;




            // for 1 avd
            if (Mainnode.getPreport() == null || Mainnode.getSuccport() == null) {


               if (selection.equals("@")) {
                   Log.i(TAG, "inside the @");
                   MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                   for (String file : getContext().fileList()) {
                       Log.i(TAG, "inside the for @");
                       try {
                           InputStream fileInput = getContext().openFileInput(file);
                           BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                           mc_values = bf.readLine();
                           bf.close();
                           mc.newRow().add("key", file).add("value", mc_values);

                       }catch (FileNotFoundException e)
                       {

                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
                   return  mc;




               }

                else if (selection.equals("*")) {
                    Log.i(TAG, "inside the *");
                    MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                   Log.i(TAG, "file is"+getContext().fileList());
                    for (String file : getContext().fileList()) {
                        Log.i(TAG, "inside the for *");
                        try {
                            InputStream fileInput = getContext().openFileInput(file);
                            BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                            mc_values = bf.readLine();
                            Log.i(TAG, "text" + mc_values.toString());
                            bf.close();
                            mc.newRow().add("key", file).add("value", mc_values);

                        }catch (FileNotFoundException e)
                        {

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return  mc;





                }
               else if (!selection.equals("@") || !selection.equals("*")) {
                   try {
                   Log.i(TAG, "inside query");
                    Log.i(TAG, "selection key  " + selection);
                    InputStream fileInput = getContext().openFileInput(selection);
                    BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                    StringBuilder str = new StringBuilder();


                    while ((text = bf.readLine()) != null) {
                        str.append(text);
                    }

                    Log.i(TAG, "text" + str.toString());
                    mc_values = str.toString();

                       bf.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }


               }


            }
            // avd 0
            else if(Mainnode.getCurport().compareTo(Mainnode.getPreport())==0 && Mainnode.getCurport().compareTo(Mainnode.getSuccport())==0)
            {

                if (selection.equals("@")) {
                    Log.i(TAG, "inside the @");
                    MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                    for (String file : getContext().fileList()) {
                        Log.i(TAG, "inside the for @");
                        try {
                            InputStream fileInput = getContext().openFileInput(file);
                            BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                            mc_values = bf.readLine();
                            bf.close();
                            mc.newRow().add("key", file).add("value", mc_values);

                        }catch (FileNotFoundException e)
                        {

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return  mc;




                }

                else if (selection.equals("*")) {
                    Log.i(TAG, "inside the *");
                    MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                    Log.i(TAG, "file is"+getContext().fileList());
                    for (String file : getContext().fileList()) {
                        Log.i(TAG, "inside the for *");
                        try {
                            InputStream fileInput = getContext().openFileInput(file);
                            BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                            mc_values = bf.readLine();
                            bf.close();
                            mc.newRow().add("key", file).add("value", mc_values);

                        }catch (FileNotFoundException e)
                        {

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return  mc;





                }
                else if (!selection.equals("@") || !selection.equals("*")) {
                    Log.i(TAG, "inside query");
                    Log.i(TAG, "file: " + selection);
                    InputStream fileInput = null;
                    try {
                        fileInput = getContext().openFileInput(selection);

                    BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                    StringBuilder str = new StringBuilder();


                    while ((text = bf.readLine()) != null) {
                        str.append(text);
                    }

                    Log.i(TAG, "text" + str.toString());
                    mc_values = str.toString();
                    bf.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }




            }
            else{

                    if (selection.equals("@"))
                   {

                       Log.i(TAG, "inside the @");

                       MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                       Log.i(TAG, "file is"+getContext().fileList());
                       for (String file : getContext().fileList()) {
                           Log.i(TAG, "inside the for @");
                           try {
                               InputStream fileInput = getContext().openFileInput(file);
                               BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));

                               mc_values = bf.readLine();
                               // this is the print statement
                               Log.i(TAG, "key hash is "+file+"compared to "+Mainnode.getCurport()+"is"+genHash(file).compareTo(Mainnode.getCurrentId()));
                               Log.i(TAG, "key hash is "+file+"compared to "+Mainnode.getPreport()+"is"+genHash(file).compareTo(Mainnode.getPredessorId()));
                               Log.i(TAG, "key hash is "+file+"compared to "+Mainnode.getSuccport()+"is"+genHash(file).compareTo(Mainnode.getSuccersorId()));

                               bf.close();
                               mc.newRow().add("key", file).add("value", mc_values);


                           }catch (FileNotFoundException e)
                           {

                           } catch (NoSuchAlgorithmException e) {
                               e.printStackTrace();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                       return  mc;



                   }

                   else if (selection.equals("*")) {
                        try {
                            MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                            Log.i(TAG, "node list" + newnodelist);


                            if (flag == 0)
                            {
                                globalmc.clear();
                                List <String> Dataset=new ArrayList<String>();


                                if (getContext().fileList().length != 0 ) {
                                    for (String file : getContext().fileList())
                                    {
                                        String key = file;
                                        InputStream fileInput = getContext().openFileInput(file);
                                        BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                                        mc_values = bf.readLine();
                                        String key_value=key+"~"+mc_values;
                                        Dataset.add(key_value);

                                    }

                                    flag = 1;

                                    String datastr = "";
                                    for (int i = 0; i < Dataset.size() - 1; i++) {
                                        datastr = datastr + Dataset.get(i) + "-";


                                    }
                                    datastr = datastr + Dataset.get(Dataset.size() - 1);
                                    Dataset.clear();

                                    String Insert_message = datastr + ":" + Mainnode.getCurport() + ":" + "*_QUERY";
                                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(Mainnode.getSuccport()) * 2);
                                    PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                                    ds1.println(Insert_message);
                                    ds1.flush();



                                    sleep(5000);



                                }
                                else{

                                    flag = 1;
                                    Log.i(TAG, "the query is empty");

                                    String datastr = "";


                                    String Insert_message = datastr + ":" + Mainnode.getCurport() + ":" + "*_QUERY";
                                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(Mainnode.getSuccport()) * 2);
                                    PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                                    ds1.println(Insert_message);
                                    ds1.flush();

                                    sleep(5000);

                                }

                            }


                            Log.i(TAG, "i got my flag as 1");
                            Collections.sort(globalmc,new List_comparator());
                            Log.i(TAG, "sorted globalmc"+globalmc);
                            for(String data:globalmc){
                                String[] datakey=data.split("~");

                                Log.i(TAG, " the ke and value "+datakey[0]+datakey[1]+"\n");

                                mc.newRow().add("key",datakey[0]).add("value", datakey[1]);

                            }
                            Log.i(TAG, "query: size "+mc.getCount());

                            flag=0;

                            return  mc;

                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                else {



                    try {
                        MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
                        String port = "";
                                        //Log.i(TAG, "query:  ");
                        Log.i(TAG, "node list" + newnodelist);


                        for(int i=0;i<newnodelist.size();i++)
                        {

                            port = newnodelist.get(i);
                            //Log.i(TAG, "query:going to node succ" + newnodelist.get(i));
                            String key = selection;
                            String Insert_message = selection + ":" + Mainnode.getCurport() + ":"+newnodelist.get(i)+":" + "REQUEST_QUERY";
                          //  Log.i(TAG, "insert message" + Insert_message);
                            Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port) * 2);
                            PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                            ds1.println(Insert_message);
                            ds1.flush();


                            try {

                                BufferedReader ir = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                                String querymess = ir.readLine();

                                String[] newquerymess = querymess.split(":");
                                if (!newquerymess[1].equals("null"))
                                {
                                    Log.i(TAG, "the message from server is" + querymess);
                                    mc.newRow().add("key", newquerymess[0]).add("value", newquerymess[1]);

                                    break;
                                }
                                else{
                                    continue;
                                }
                            }catch (Exception e)
                            {

                            }

                        }

                        return mc;


                    }catch (Exception e)
                    {

                    }



                }

            }


       Object value1 = mc_values;  //converting and string to object
        MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
        mc.newRow().add("key",selection).add("value", value1);
       return  mc;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, MatrixCursor> {
        @Override
        protected MatrixCursor doInBackground(ServerSocket... serverSockets) {

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority("edu.buffalo.cse.cse486586.simpledht.provider");
            uriBuilder.scheme("content");
            Uri mUri=uriBuilder.build();

            ServerSocket serverSocket = serverSockets[0];
            try {

                Log.i(TAG, "inside the server");
                while (true) {
                    Node1 node=new Node1();
                    Socket clientSocket = serverSocket.accept();


                    BufferedReader ir = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter ds=new PrintWriter(clientSocket.getOutputStream());
                    String message = ir.readLine();
                    if (message.contains("NODE_JOIN")) {



                        Log.i(TAG, "node connected in server " + message);


                        String[] servmessage=message.split(":");
                        Log.i(TAG, "node in node connection set"+servmessage[0]);
                        node.setCurport(servmessage[0]);
                        node.setPreport(servmessage[0]);
                        node.setSuccport(servmessage[0]);
                        node.setCurrentId(genHash(Integer.toString(Integer.parseInt(servmessage[0]))));

                        node.setPredessorId(genHash(Integer.toString(Integer.parseInt(servmessage[0]))));
                        node.setSuccersorId(genHash(Integer.toString(Integer.parseInt(servmessage[0]))));
                        Pmessage.add(node);

                        Collections.sort(Pmessage, new OObject_comparator());


                        for (Node1 pnode:Pmessage)
                        {
                            Log.i(TAG, "message"+pnode.getCurport()+" "+pnode.getCurrentId());
                        }

                        //get successr predessor value

                            for (int i = 0; i < Pmessage.size(); i++) {
                                if (i == Pmessage.size() - 1) {
                                    Log.i(TAG, "inside this loop"+i);

                                    Pmessage.get(i).setSuccersorId(Pmessage.get(0).getCurrentId());
                                    Pmessage.get(0).setPredessorId(Pmessage.get(i).getCurrentId());
                                    Pmessage.get(i).setSuccport(Pmessage.get(0).getCurport());
                                    Pmessage.get(0).setPreport(Pmessage.get(i).getCurport());

                                } else {
                                    Log.i(TAG, "inside the else loop"+i);
                                    Pmessage.get(i).setSuccersorId(Pmessage.get(i+1).getCurrentId());
                                    Pmessage.get(i+1).setPredessorId(Pmessage.get(i).getCurrentId());
                                    Pmessage.get(i).setSuccport(Pmessage.get(i+1).getCurport());
                                    Pmessage.get(i+1).setPreport(Pmessage.get(i).getCurport());
                                }
                            }





                        for (int i=0;i<Pmessage.size();i++) {
                            Node1 Finalnode = Pmessage.get(i);
                            if (!nodelist.contains(Finalnode.getCurport())) {
                                nodelist.add(Finalnode.getCurport());
                            }

                           // Log.i(TAG, "the nodelist is"+nodelist);
                            Collections.sort(nodelist,new node_comparator());


                        }
                        String nodestr="";
                        for (int i=0;i<nodelist.size()-1;i++)
                        {
                            nodestr=nodestr+nodelist.get(i)+"-";


                        }
                           nodestr=nodestr+nodelist.get(nodelist.size()-1);


                        //send predessor and successor to the  avds  from avd 0
                       for (int i=0;i<Pmessage.size();i++)
                        {
                            Node1 Finalnode=Pmessage.get(i);
                            String Finalmessage=Finalnode.getPredessorId()+":"+Finalnode.getCurrentId()+":"+Finalnode.getSuccersorId()+":"+Finalnode.getPreport()+":"+Finalnode.getCurport()+":"+Finalnode.getSuccport()+":"+Integer.parseInt(Finalnode.getCurport())*2+":"+nodestr+":"+"NODE-UPDATE";
                            Log.i(TAG, "Final predessor port"+Finalnode.getPreport()+"Final current port"+Finalnode.getCurport()+"final succeror port"+Finalnode.getSuccport());

                            Socket socket1=new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(Finalnode.getCurport())*2);
                            PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
                            ds1.println(Finalmessage);
                            ds1.flush();



                        }



                    }
                    else if(message.contains("NODE-UPDATE"))
                    {
                        Log.i(TAG, "hurray ur in node upate");
                        Log.i(TAG, "message"+message);
                        String[] UPnode=message.split(":");
                        Mainnode.setPredessorId(UPnode[0]);
                        Mainnode.setCurrentId(UPnode[1]);
                        Mainnode.setSuccersorId(UPnode[2]);
                        Mainnode.setPreport(UPnode[3]);
                        Mainnode.setCurport(UPnode[4]);
                        Mainnode.setSuccport(UPnode[5]);

                        Log.i(TAG, UPnode[7]);

                        newnodelist= Arrays.asList(UPnode[7].split("-"));

                       // Log.i(TAG, "my own nodelist"+newnodelist.get(0));

                    }

                    else if(message.contains("INSERT_NODE"))
                    {   String[] insertMess=message.split(":");
                        Log.i(TAG, "inside server INSERT_NODE"+message);
                        Log.i(TAG, "insert node"+insertMess[1]);
                        ContentValues cv = new ContentValues();
                        cv.put("key",insertMess[0]);
                        cv.put("value",insertMess[1]);
                        insert(mUri,cv);

                    }

                    else if(message.contains("QUERY_NODE"))
                    {
                        String[] queryMess=message.split(":");
                        Log.i(TAG, "query message"+message);
                        Cursor resultCursor =query(mUri, null,
                                queryMess[0], null, null);
                        if (resultCursor == null) {
                            Log.e(TAG, "Result null");
                            //throw new Exception();
                        }

                        int keyIndex = resultCursor.getColumnIndex("key");
                        int valueIndex = resultCursor.getColumnIndex("value");
                        if (keyIndex == -1 || valueIndex == -1) {
                            Log.e(TAG, "Wrong columns");
                            resultCursor.close();
                            //throw new Exception();
                        }

                        resultCursor.moveToFirst();

                        if (!(resultCursor.isFirst() && resultCursor.isLast())) {
                            Log.e(TAG, "Wrong number of rows");
                            resultCursor.close();
                            //throw new Exception();
                        }


                        resultCursor.close();
                    }

                    else if(message.contains("REQUEST_QUERY"))
                    {
                        try {
                            String[] queryreqMess = message.split(":");
                            Log.i(TAG, "im inside the query node request" + message+ "to the node"+queryreqMess[2]);

                            String sendmessage="";



                                       for (String file : getContext().fileList()) {
                                           // Log.i(TAG, "inside the file in request query");
                                           if (queryreqMess[0].compareTo(file) == 0) {

                                               InputStream fileInput = getContext().openFileInput(file);
                                               BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                                               mc_values = bf.readLine();
                                               //sendmessage=file+":"+mc_values;
                                               break;

                                           } else {
                                               Log.i(TAG, "not in the port");
                                               mc_values = "null";

                                           }
                                       }
                            try {

                                sendmessage=queryreqMess[0]+":"+mc_values;
                                Log.i(TAG, "the message found in"+queryreqMess[2]+"send message"+sendmessage);
                                PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
                                ds1.println(sendmessage);
                                ds1.flush();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }







                            } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                    }

                    else if(message.contains("*_QUERY"))
                    {
                        try {



                            List <String> Dataset=new ArrayList<String>();
                            List<String> newDataset=new ArrayList<String>();

                            String[] queryreqMess = message.split(":");
                            if(queryreqMess[0].equals(""))
                            {
                                Dataset.clear();
                            }
                            else {
                                Dataset = Arrays.asList(queryreqMess[0].split("-"));
                            }


                            if(flag==0)
                            {

                                if(getContext().fileList().length!=0 ) {
                                    List<String>Finaldataset=new ArrayList<String>();


                                    for (String file : getContext().fileList())
                                    {

                                        InputStream fileInput = getContext().openFileInput(file);
                                        BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
                                        mc_values = bf.readLine();

                                        String datakey = file + "~" + mc_values;
                                        newDataset.add(datakey);


                                    }

                                    Finaldataset.addAll(Dataset);
                                    Finaldataset.addAll(newDataset);


                                    String datastr = "";
                                    for (int i = 0; i < Finaldataset.size() - 1; i++) {
                                        datastr = datastr + Finaldataset.get(i) + "-";


                                    }
                                    datastr = datastr +Finaldataset.get(Finaldataset.size() - 1);

                                    String Insert_message = datastr + ":" + Mainnode.getCurport() + ":" + "*_QUERY";
                                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(Mainnode.getSuccport()) * 2);
                                    PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                                    ds1.println(Insert_message);
                                    ds1.flush();

                                }

                                else
                                    {

                                    Log.i(TAG, "avd null");


                                    String datastr = "";
                                    for (int i = 0; i < Dataset.size() - 1; i++) {
                                        datastr = datastr + Dataset.get(i) + "-";


                                    }
                                    datastr = datastr + Dataset.get(Dataset.size() - 1);

                                    String Insert_message = datastr + ":" + Mainnode.getCurport() + ":" + "*_QUERY";
                                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(Mainnode.getSuccport()) * 2);
                                    PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
                                    ds1.println(Insert_message);
                                    ds1.flush();


                                  }


                            }

                            else if(flag==1)
                               {

                                Log.i(TAG, "node flag=1 sending the dataset back"+queryreqMess[0]);
                                globalmc= Arrays.asList(queryreqMess[0].split("-"));
                                flag=0;
                              }

                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                    }


                    else if(message.contains("Del_*"))
                    {
                        if(flag ==0)
                        {
                            for (String file : getContext().fileList())
                            {
                                getContext().deleteFile(file);
                                Log.i(TAG, "File is deleted");
                            }

                            Log.i(TAG, "delete in server node succssesor");
                            String Insert_message="Del_*";
                            Socket socket1= null;
                            try
                            {
                                socket1 = new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(Mainnode.getSuccport())*2);
                                PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
                                ds1.println(Insert_message);
                                ds1.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                        else if (flag==1)
                        {
                            flag=0;
                        }

                    }

                    else if(message.contains("DELETE_NODE"))
                    {
                        String[] deleteMess=message.split(":");
                        File file =getContext().getFileStreamPath(deleteMess[0]);
                        if(file!=null)
                        {
                            getContext().deleteFile(deleteMess[0]);
                            Log.i(TAG, "delete: the file");
                        }
                        else {
                            String Insert_message=deleteMess[0]+":"+"DELETE_NODE";
                            Socket socket1;
                            socket1 = new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(Mainnode.getSuccport())*2);
                            PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
                            ds1.println(Insert_message);
                            ds1.flush();
                        }
                    }



                }


            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String clientnode= strings[0];
            String [] msg=clientnode.split(" :");
            Log.i(TAG, "message"+msg[0]);
            String messagePort[]=msg[0].split(":");

            Log.i(TAG, "inside the client");
            Socket socket=null;

            try {
                Log.i(TAG, "flag inside client"+flag);
                       if(clientnode.contains("NODE_JOIN")){
                           socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), MAIN_PORT);
                           String message = msg[0];
                           Log.i(TAG, "sending message from client"+socket.getPort());

                           PrintWriter ds = new PrintWriter(socket.getOutputStream());
                           ds.println(message);
                           ds.flush();

                       }



            } catch (IOException e) {
                e.printStackTrace();

            }



            return null;
        }

    }
}

class Node1{
    String PredessorId;
    String succersorId;
    String currentId;
    String Preport;
    String Curport;
    String Succport;
    String type;

    public void setType(String type) {
        this.type = type;
    }

    public void setCurport(String curport) {
        Curport = curport;
    }
    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }
    public void setPredessorId(String predessorId) {
        PredessorId = predessorId;
    }
    public void setPreport(String  preport) {
        Preport = preport;
    }
    public void setSuccersorId(String  succersorId) {
        this.succersorId = succersorId;
    }
    public void setSuccport(String succport) {
        Succport = succport;
    }
    public String  getCurport() {
        return Curport;
    }
    public String getCurrentId() {
        return currentId;
    }

    public String getType() {
        return type;
    }

    public String getPredessorId() {
        return PredessorId;
    }
    public String getPreport() {
        return Preport;
    }
    public String getSuccersorId() {
        return succersorId;
    }
    public String getSuccport() {
        return Succport;
    }
}
class OObject_comparator implements Comparator<Node1>                               //geekforgeeks/priorityqueue-comparator-method-in-java
{
    @Override
    public int compare(Node1 lhs, Node1 rhs) {
        if(lhs.getCurrentId().compareTo(rhs.getCurrentId())>0)

        {
            return 1;
        }
        else if(lhs.getCurrentId().compareTo(rhs.getCurrentId())<0)
        {
            return -1;
        }
        return 0;
    }
}


class node_comparator implements Comparator<String>                               //geekforgeeks/priorityqueue-comparator-method-in-java
{
    @Override
    public int compare(String lhs, String rhs) {
        try {
            if(genHash(lhs).compareTo(genHash(rhs))>0)

            {
                return 1;
            }
            else if(genHash(lhs).compareTo(genHash(rhs))<0)
            {
                return -1;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }


    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}

class List_comparator implements Comparator<String>                               //geekforgeeks/priorityqueue-comparator-method-in-java
{
    @Override
    public int compare(String lhs, String rhs) {
        try {
            String[] messl=lhs.split("~");
            String[] messr=rhs.split("~");
            if(genHash(messl[0]).compareTo(genHash(messr[0]))>0)

            {
                return 1;
            }
            else if(genHash(messl[0]).compareTo(genHash(messr[0]))<0)
            {
                return -1;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }


    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}


