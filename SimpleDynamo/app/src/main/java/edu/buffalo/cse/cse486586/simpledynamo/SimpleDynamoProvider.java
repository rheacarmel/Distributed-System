package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import javax.security.auth.login.LoginException;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class SimpleDynamoProvider extends ContentProvider {

	List<String> nodeList= Arrays.asList("5562","5556","5554","5558","5560");
	static  final int SERVER_PORT=10000;
	ReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    Node node=new Node();
	public List<String>globalmc=new ArrayList<String>();
	String Failurenode = "";
	int flag;
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		if(selection.equals("@"))
		{ try {
			for (String file : getContext().fileList()) {
				getContext().deleteFile(file);
				Log.i(TAG, "File is deleted");
			}
		 }
		 catch (Exception e)
		 {
			Log.i(TAG, "delete: ");
		 }
		}

		else if(selection.equals("*"))
		{

				if(flag ==0)
				{
					flag = 1;
					for (String file : getContext().fileList())
					{
						getContext().deleteFile(file);
						//Log.i(TAG, "File is deleted");
					}

				//	Log.i(TAG, "delete succssesor");
					String Del_message="Del_*";
					Socket socket1= null;
					try
					{
						socket1 = new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(node.getSuccessor1())*2);
						PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
						ds1.println(Del_message);
						ds1.flush();
						Thread.sleep(1000);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}


				}

				return 0;


		}
		else if(!selection.equals("*")||!selection.equals("@"))
		{


				String port = "";
                 Socket socketd1=null;
				for (int i = 0; i < nodeList.size(); i++)
				{

					try {
						port = nodeList.get(i);



						String Del_message = selection + ":" + "DELETE_NODE";

						socketd1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port) * 2);
						PrintWriter ds1 = new PrintWriter(socketd1.getOutputStream());
						ds1.println(Del_message);
						ds1.flush();


					}
			       catch (Exception e)
			       {
				     Log.i(TAG, "delete: is failing in single delete"+socketd1.getPort());
			       }

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
    public Uri  insert(Uri uri, ContentValues values) {
		String destnode="";
		String succdestnode="";
		String succdestnode1="";



		int nodeIndex = 0;
		// TODO Auto-generated method stub
		String Key = values.getAsString("key");
		String Value = values.getAsString("value");
		//Log.i(TAG, "sending the message from port"+node.getCurrent());


			try {
				for (int i = 0; i < 5; i++) {
					//Log.i(TAG, "inside the loop");


					if (genHash(Key).compareTo(genHash(nodeList.get(i))) > 0 && i == nodeList.size() - 1) {
						destnode = nodeList.get(0);
						nodeIndex = 0;
						break;
					} else if ((genHash(Key).compareTo(genHash(nodeList.get(i)))) <= 0) {
						destnode = nodeList.get(i);
						//Log.i(TAG, "destnode" + destnode);
						nodeIndex = i;
						break;
					}


				}


				String insertmess = Key + ":" + Value + ":" + "insert_message";





					Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(destnode) * 2);
					PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
					ds1.println(insertmess);
					ds1.flush();
				   BufferedReader ir = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
				   String ack = ir.readLine();
				try {
					if (ack.contains("ack_ok")) {
						//Log.i(TAG, "no failure");
					}
				} catch (NullPointerException e) {
					Log.i(TAG, "there is a failure");
					Failurenode = Integer.toString(socket1.getPort() / 2);
					Log.i(TAG, "node failed" + Failurenode);


				}


				succdestnode = nodeList.get((nodeIndex + 1) % 5);  //1st successor node
				String destsuccmess = Key + ":" + Value + ":" + "Succ_mess";
				Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(succdestnode) * 2);
				PrintWriter ds2 = new PrintWriter(socket2.getOutputStream());
				ds2.println(destsuccmess);
				ds2.flush();

				try {
					BufferedReader ir1 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
					String ack1 = ir1.readLine();
					if (ack1.contains("ack_ok")) {
						//Log.i(TAG, "no failure");
					}
				} catch (NullPointerException e) {
					Failurenode = Integer.toString(socket2.getPort() / 2);
					Log.i(TAG, "there is a failure");

				}


				succdestnode1 = nodeList.get((nodeIndex + 2) % 5);   //2nd successor node
				String destsuccmess1 = Key + ":" + Value + ":" + "Succ_mess";
				Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(succdestnode1) * 2);
				PrintWriter ds3 = new PrintWriter(socket3.getOutputStream());
				ds3.println(destsuccmess1);
				ds3.flush();


				BufferedReader ir2 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
				String ack2 = ir2.readLine();
				try {
					if (ack2.contains("ack_ok")) {
						//Log.i(TAG, "no failure");
					}
				} catch (NullPointerException e) {
					Log.i(TAG, "there is a failure");
					Failurenode = Integer.toString(socket3.getPort() / 2);
				}


			} catch (NoSuchAlgorithmException ex) {
				ex.printStackTrace();
			} catch (UnknownHostException ex) {
				ex.printStackTrace();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}









		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub

		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		int index=nodeList.indexOf(portStr);

        int predindex=(5+index-1)%5;
		int succindex1=(index+1)%5;
		int succindex2=(index+2)%5;

		node.setCurrent(portStr);

		Log.i(TAG, "node"+node.getCurrent());
		node.setSuccessor1(nodeList.get(succindex1));
		node.setSuccessor2(nodeList.get(succindex2));
        node.setPredessor(nodeList.get(predindex));



		ServerSocket serverSocket = null;

		Log.i(TAG, "onCreate: server started");
		//ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

		} catch (IOException e) {
			e.printStackTrace();
		}



			if (getContext().fileList().length!=0)    //getContext().fileList().length!=0
			{
				Log.i(TAG, "onCreate: resume");
				for(String file:getContext().fileList())
				{
					getContext().deleteFile(file);
				}
				getContext().deleteFile("resume");
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"hurray now resume",myPort);

			}
			else{
				FileOutputStream file;
				try {
					file=getContext().openFileOutput("resume",Context.MODE_PRIVATE);
					String resume="resume";
					file.write(resume.getBytes());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}



		return false;
	}



	@Override
	 public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		 String mc_values;

		// TODO Auto-generated method stub

		if (selection.equals("@"))
		{

			//Log.i(TAG, "inside the @");

			MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
			//Log.i(TAG, "file is"+getContext().fileList());
			readWriteLock.readLock().lock();
			for (String file : getContext().fileList()) {

				if(file.compareTo("resume")==0)
				{
					getContext().deleteFile("resume");
				}
				else {
					//Log.i(TAG, "inside the for @");
					try {
						InputStream fileInput = getContext().openFileInput(file);

						BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));

						mc_values = bf.readLine();
						// this is the print statement

						//Log.i(TAG, "text" + mc_values.toString());
						//mc_values = str.toString();
						bf.close();
						mc.newRow().add("key", file).add("value", mc_values);


					} catch (FileNotFoundException e) {

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			readWriteLock.readLock().unlock();

			return  mc;



		}
		else if (selection.equals("*")) {
			try {
				MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
				globalmc.clear();
				String port = "";
				String dataset="";
				//Log.i(TAG, "query:  ");
				//Log.i(TAG, "node list" + nodeList);

				for(int i=0;i<nodeList.size();i++) {

						port = nodeList.get(i);
						//	Log.i(TAG, "query:going to node succ" + nodeList.get(i));
						//String key = selection;
						String Query_message = node.getCurrent() + ":"+nodeList.get(i)+":" + "*_QUERY";
						//Log.i(TAG, "* query  message" + Insert_message);
						Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port) * 2);
						PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
						ds1.println(Query_message);
						ds1.flush();

						String querymess="";
						try {

							BufferedReader ir = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

							querymess = ir.readLine();

							if(querymess.equals(""))
							{
								continue;
							}
							else {
								dataset = dataset + querymess + "-";
							}



						}catch (Exception e)
						{

						}




				}

                sleep(1000);
				//Log.i(TAG, "total dataset "+dataset);
				globalmc= Arrays.asList(dataset.split("-"));
				Collections.sort(globalmc,new List_comparator());
				for(String data:globalmc){
					String[] datakey=data.split("~");

				//	Log.i(TAG, " the ke and value "+datakey[0]+datakey[1]+"\n");

					mc.newRow().add("key",datakey[0]).add("value", datakey[1]);

				}
				Log.i(TAG, "query: size "+mc.getCount());





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
		else
		{
			try {
				MatrixCursor mc = new MatrixCursor(new String[]{"key", "value"});
				String port = "";
				//Log.i(TAG, "query:  ");



				for(int i=0;i<nodeList.size();i++) {

					port = nodeList.get(i);
					//Log.i(TAG, "query:going to node succ" + nodeList.get(i));
					String key = selection;
					String Query_message = selection + ":" + node.getCurrent() + ":"+nodeList.get(i)+":" + "REQUEST_QUERY";
					//Log.i(TAG, "insert message" + Insert_message);
					Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port) * 2);
					PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
					ds1.println(Query_message);
					ds1.flush();


					try {

						BufferedReader ir = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
						String querymess = ir.readLine();

						String[] newquerymess = querymess.split(":");
						if (!newquerymess[1].equals("null")) {
							//Log.i(TAG, "the message from server is" + querymess);
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





		//check the @

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
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

					Socket clientSocket = serverSocket.accept();
					//Dataset.clear();

					BufferedReader ir = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintWriter ds = new PrintWriter(clientSocket.getOutputStream());
					String message = ir.readLine();

					if(message.contains("insert_message"))
					{
                        try {
							String[] insert_mess = message.split(":");
							String filename1;


								readWriteLock.writeLock().lock();
								//Log.i(TAG, "port is locked in insert");
								filename1 = insert_mess[0];

								String value = insert_mess[1];

								FileOutputStream outputStream1;
								outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
								outputStream1.write(value.getBytes());
								outputStream1.close();

								readWriteLock.writeLock().unlock();


								String ack = "ack_ok";
								Log.i(TAG, "ack send ");
								PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
								ds1.println(ack);
								ds1.flush();

						}catch (Exception e)
						{

						}



					}
					else if(message.contains("Succ_mess"))
					{
                        try {
							String[] succ_mess = message.split(":");
							String filename1;

								//Log.i(TAG, "succesor insert locked");
								readWriteLock.writeLock().lock();
								filename1 = succ_mess[0];
								String value = succ_mess[1];

								FileOutputStream outputStream1;
								outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
								outputStream1.write(value.getBytes());
								outputStream1.close();
								readWriteLock.writeLock().unlock();


								String ack = "ack_ok";
								PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());

								ds1.println(ack);
								ds1.flush();

						}catch (Exception e)
						{

						}

					}

					else  if(message.contains("REQUEST_QUERY"))
					{ String mc_values="";
						try {
							String[] queryreqMess = message.split(":");
							//Log.i(TAG, "im inside the query node request" + message+ "to the node"+queryreqMess[2]);

							String sendmessage="";



							for (String file : getContext().fileList()) {
								// Log.i(TAG, "inside the file in request query");

								if (queryreqMess[0].compareTo(file) == 0) {
									readWriteLock.readLock().lock();
									InputStream fileInput = getContext().openFileInput(file);
									BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
									mc_values = bf.readLine();
									readWriteLock.readLock().unlock();
									//sendmessage=file+":"+mc_values;
									break;

								} else {
									//Log.i(TAG, "not in the port");
									mc_values = "null";

								}

							}
							try {

								sendmessage=queryreqMess[0]+":"+mc_values;
							//	Log.i(TAG, "the message found in"+queryreqMess[2]+"send message"+sendmessage);
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
					{    String mc_values="";
						try {

							List <String> Dataset=new ArrayList<String>();
							String[] queryreqMess = message.split(":");
							//Log.i(TAG, "im inside the query node request" + message+ "to the node"+queryreqMess[2]);

							String sendmessage="";


                            if(getContext().fileList().length!=0) {
								readWriteLock.readLock().lock();
								for (String file : getContext().fileList()) {
									// Log.i(TAG, "inside the file in request query");

									InputStream fileInput = getContext().openFileInput(file);
									BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
									mc_values = bf.readLine();
									//sendmessage=file+":"+mc_values;
									String datakey = file + "~" + mc_values;
									Dataset.add(datakey);



								}
								readWriteLock.readLock().unlock();

								String datastr = "";
								try {

									for (int i = 0; i < Dataset.size() - 1; i++) {
										datastr = datastr + Dataset.get(i) + "-";


									}
									datastr = datastr + Dataset.get(Dataset.size() - 1);

									sendmessage = datastr;
									//Log.i(TAG, "the message found in" + queryreqMess[2] + "send message" + sendmessage);
									PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
									ds1.println(sendmessage);
									ds1.flush();
								} catch (IOException e) {
									e.printStackTrace();
								}


							}
                            else
							{
								String datastr = "";
								sendmessage = datastr;
								//Log.i(TAG, "the message found in" + queryreqMess[2] + "send message" + sendmessage);
								PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
								ds1.println(sendmessage);
								ds1.flush();

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
							//	Log.i(TAG, "File is deleted");
							}

							//Log.i(TAG, "delete in server node succssesor");
							String Del_message="Del_*";
							Socket socket1= null;
							try
							{
								socket1 = new Socket(InetAddress.getByAddress(new byte[]{10,0,2,2}),Integer.parseInt(node.getSuccessor1())*2);
								PrintWriter ds1=new PrintWriter(socket1.getOutputStream());
								ds1.println(Del_message);
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
						readWriteLock.writeLock().lock();
						try {

							  if(getContext().fileList().length==0)
							  {

							  }
							  else {
								  if (getContext().getFileStreamPath("resume").equals(true)) {
									  getContext().deleteFile("resume");
								  }
								  String[] deleteMess = message.split(":");
								  File file = getContext().getFileStreamPath(deleteMess[0]);
								  if (file != null) {
									  //String delete_ack = "delete_ok";
									  getContext().deleteFile(deleteMess[0]);
								  }
							  }

						}
						catch (NullPointerException e)
						{
							Log.e(TAG, "null pointer exception in delete node" );
						}
						catch (Exception e)
						{
							Log.i(TAG, "delete error in ");
						}
						readWriteLock.writeLock().unlock();


					}

					else if(message.contains("Co-recovery"))    //if failed node is a coordinator
					{   try {
						List<String> globalmc2;
						//send the message to the   successor  and recieve the list from the successor


						Log.i(TAG, "inside co-recovery" + "the succor node is " + node.getSuccessor1());
						String Co_message = node.getCurrent() + ":" + node.getPredessor() + ":" + "List";
						Socket socket1;
						String dataset = "";

						socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.getSuccessor1()) * 2);
						PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
						ds1.println(Co_message);
						ds1.flush();



						String Comess = "";
						while (true) {

							BufferedReader ir1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
							Comess = ir1.readLine();
							if (!Comess.equals("")) {
								break;
							}
						}


						if (Comess.equals("nothing")) {
						}

						else{
						dataset = Comess;

						//}


						Log.i(TAG, "total dataset " + dataset);
						globalmc2 = Arrays.asList(dataset.split("-"));

						readWriteLock.writeLock().lock();
						for (String data : globalmc2) {
							String[] datakey = data.split("~");


							String filename1 = datakey[0];
							String value = datakey[1];
							FileOutputStream outputStream1;
							outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
							outputStream1.write(value.getBytes());
							outputStream1.close();


						}
						readWriteLock.writeLock().unlock();

						Log.i(TAG, "port " + socket1.getPort() / 2 + "new recovered data inserted");
					}
					}catch (Exception e)
					{

					}
                       /*PrintWriter ackClient=new PrintWriter(clientSocket.getOutputStream());
                       ackClient.write("ack_client");
                       ackClient.flush();*/



					}
					else if(message.contains("Suc-recovery"))   //if failed node is successor1
					{
						try {
							Log.i(TAG, "inside suc recovery");

							List<String> globalmc1;
							int index_recmess = nodeList.indexOf(node.getCurrent());
							String coordinator_node = nodeList.get((5 + index_recmess - 1) % 5);
							String predessornode = nodeList.get((5 + index_recmess - 2) % 5);
							Log.i(TAG, "the predessor node in suc recovery is " + predessornode);
							String su_message = coordinator_node + ":" + predessornode + ":" + "List";


							Socket socket1;
							String dataset = "";
							socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(coordinator_node) * 2);
							PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
							ds1.println(su_message);
							ds1.flush();
							String Comess = "";
							//sleep(100);
							while (true) {
								BufferedReader ir1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
								Comess = ir1.readLine();
								if (!Comess.equals("")) {

									break;
								}

							}

                            if(Comess.equals("nothing"))
							{

							}
                            else {
								dataset = Comess;

								Log.i(TAG, "total dataset " + dataset);
								globalmc1 = Arrays.asList(dataset.split("-"));

								readWriteLock.writeLock().lock();
								for (String data : globalmc1) {
									String[] datakey = data.split("~");


									String filename1 = datakey[0];
									String value = datakey[1];
									FileOutputStream outputStream1;
									outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
									outputStream1.write(value.getBytes());
									outputStream1.close();


								}
								readWriteLock.writeLock().unlock();

								Log.i(TAG, "new suc recovery data inserted");
							}
						}catch (Exception e)
						{

						}



					}
					else if(message.contains("2nd-recovery"))   //if the failed node is a 2nd successor node
					{
                        try {
							List<String> globalmc1;
							int index_recmess = nodeList.indexOf(node.getCurrent());
							String coordinator_node = nodeList.get((5 + index_recmess - 2) % 5);
							String predessornode = nodeList.get((5 + index_recmess - 3) % 5);
							Log.i(TAG, "the predessor node  in 2nd recoveru is " + predessornode);
							String suc_message = coordinator_node + ":" + predessornode + ":" + "List";

							Log.i(TAG, "inside the 1_suc-recovery " + coordinator_node);
							Log.i(TAG, "inside the 1_suc-recovery " + "and the list will go to" + coordinator_node);

							Socket socket1;
							String dataset = "";
							socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(coordinator_node) * 2);
							PrintWriter ds1 = new PrintWriter(socket1.getOutputStream());
							ds1.println(suc_message);
							ds1.flush();

							//sleep(100);

							String Comess = "";
							while (true) {
								BufferedReader ir1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
								Comess = ir1.readLine();
								if (!Comess.equals("")) {
									break;
								}
							}
							if(Comess.equals("nothing"))
							{

							}
							else {
								dataset = Comess;


								Log.i(TAG, "total dataset " + dataset);
								globalmc1 = Arrays.asList(dataset.split("-"));

								readWriteLock.writeLock().lock();
								for (String data : globalmc1) {
									String[] datakey = data.split("~");


									String filename1 = datakey[0];
									String value = datakey[1];
									FileOutputStream outputStream1;
									outputStream1 = getContext().openFileOutput(filename1, Context.MODE_PRIVATE);
									outputStream1.write(value.getBytes());
									outputStream1.close();


								}
								readWriteLock.writeLock().unlock();

								Log.i(TAG, "new suc recovery1 data inserted");
							}


						}catch (Exception e)
						{

						}


					}

					else if(message.contains("List")) {
						String[] recMess = message.split(":");
						String mc_values = "";

						List<String> Dataset = new ArrayList<String>();
						//String nodevalue= Integer.toString(Integer.parseInt(recMess[0]));
						int index_recmess = nodeList.indexOf(recMess[0]);

						Log.i(TAG, "the preessor is " + recMess[1]);
						if(getContext().fileList().length==0)
						{
                            String sendmessage="nothing";
							PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
							ds1.println(sendmessage);
							ds1.flush();
						}
						else{
							if (index_recmess == 0)    // when the node is a boundary node get data  that is greater than the last node .
							{
								int lastnodeindex = 4;

								String lastnode = nodeList.get(lastnodeindex);
								Log.i(TAG, "inside the first node" + recMess[0] + "the 4th node is" + lastnode);


								readWriteLock.readLock().lock();
								for (String file : getContext().fileList()) {
									if ((genHash(file).compareTo(genHash(recMess[0])) <= 0) || (genHash(file).compareTo(genHash(lastnode)) > 0))  //recMess[0]
									{
										InputStream fileInput = getContext().openFileInput(file);
										BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
										mc_values = bf.readLine();
										//sendmessage=file+":"+mc_values;
										String datakey = file + "~" + mc_values;
										Dataset.add(datakey);

									}
								}
								readWriteLock.readLock().unlock();


								String datastr = "";
								try {


									for (int i = 0; i < (Dataset.size() - 1); i++) {
										datastr = datastr + Dataset.get(i) + "-";


									}
									datastr = datastr + Dataset.get(Dataset.size() - 1);
									Dataset.clear();

									String sendmessage = datastr;
									Log.i(TAG, "data to be send to avd 4" + sendmessage);

									PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
									ds1.println(sendmessage);
									ds1.flush();

								} catch (IOException e) {
									e.printStackTrace();
								}

							}
							else{
								Log.i(TAG, "node in list " + recMess[0] + "predessor in list");

								readWriteLock.readLock().lock();
								for (String file : getContext().fileList()) {
									if ((genHash(file).compareTo(genHash(recMess[0])) <= 0))//&&(file.compareTo(genHash(recMess[1]))>0))
									{
										if (genHash(recMess[1]).compareTo(genHash(file)) < 0) {
											InputStream fileInput = getContext().openFileInput(file);
											BufferedReader bf = new BufferedReader(new InputStreamReader(fileInput));
											mc_values = bf.readLine();

											String datakey = file + "~" + mc_values;
											Dataset.add(datakey);
										}


									}
								}
								readWriteLock.readLock().unlock();


								String datastr = "";
								try {
									if (Dataset.size() == 0) {

									} else {
										for (int i = 0; i < (Dataset.size() - 1); i++) {
											datastr = datastr + Dataset.get(i) + "-";


										}
										datastr = datastr + Dataset.get(Dataset.size() - 1);
										Dataset.clear();
										String sendmessage = datastr;
										PrintWriter ds1 = new PrintWriter(clientSocket.getOutputStream());
										ds1.println(sendmessage);
										ds1.flush();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}


							}
						}



						}

					}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			catch (Exception e)
			{

			}

			return null;
		}
	}


	private class ClientTask extends AsyncTask<String,Void,String>{
		@Override
		protected String doInBackground(String... strings) {
			Log.i(TAG, "doInBackground: of client"+strings[0]);

			try {
				String mess=node.getCurrent()+":"+"Co-recovery";
				String mess1 =node.getCurrent()+":"+"Suc-recovery";
				String mess2 =node.getCurrent()+":"+"2nd-recovery";
				PrintWriter ds1 = null;


				Socket socketf1= new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(strings[1]));
				ds1 = new PrintWriter(socketf1.getOutputStream());
				ds1.println(mess);
				ds1.flush();

                 sleep(1000);


				Socket socketf2= new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(strings[1]));
				PrintWriter ds2 = new PrintWriter(socketf2.getOutputStream());
				ds2.println(mess1);
				ds2.flush();

				sleep(1000);



				Socket socketf3= new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(strings[1]));
				PrintWriter ds3 = new PrintWriter(socketf3.getOutputStream());
				ds3.println(mess2);
				ds3.flush();


				sleep(1000);


			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}catch (Exception e)
			{

			}

			return null;
		}
	}
}


class Node
{
	String predessor;
	String Successor1;
	String Successor2;
	String Current;
	String PredessorHash;
	String SuccessorHash;
	String CurrentHash;

	public String getCurrent() {
		return Current;
	}

	public String getCurrentHash() {
		return CurrentHash;
	}

	public String getPredessor() {
		return predessor;
	}

	public String getPredessorHash() {
		return PredessorHash;
	}

	public String getSuccessor1() {
		return Successor1;
	}

	public String getSuccessor2() {
		return Successor2;
	}

	public String getSuccessorHash() {
		return SuccessorHash;
	}

	public void setCurrent(String current) {
		Current = current;
	}

	public void setCurrentHash(String currentHash) {
		CurrentHash = currentHash;
	}

	public void setPredessor(String predessor) {
		this.predessor = predessor;
	}

	public void setPredessorHash(String predessorHash) {
		PredessorHash = predessorHash;
	}

	public void setSuccessor1(String successor1) {
		Successor1 = successor1;

	}

	public void setSuccessor2(String successor2) {
		Successor2 = successor2;
	}

	public void setSuccessorHash(String successorHash) {
		SuccessorHash = successorHash;
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
