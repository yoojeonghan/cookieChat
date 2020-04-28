
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;
 
public class ChatServer2 
{
	public static final int cs_port = 5002;
	public static final int cs_maxclient = 50;
	
	public static void main(String args[])
	{
		try
		{
			@SuppressWarnings("resource")
			ServerSocket ss_socket = new ServerSocket(cs_port);
			System.out.println("[서버] 쿠키챗 서버소켓이 실행되었습니다.");
			
			while(true)
			{
				Socket sock = null;
				ServerThread client = null;
				
				try
				{
					// 접속요청을 기다린다.
					sock = ss_socket.accept();
					// 하트비트 적용
					sock.setSoTimeout(5000);
					// 스레드를 실행시킨다.
					client = new ServerThread(sock);
					client.start();
				}
				catch(IOException e)
				{
					System.out.println(e);
					try
					{
						if(sock != null)
						{
							sock.close();
						}
					}
					catch(IOException e1)
					{
						System.out.println(e1);
					}
					finally
					{
						sock = null;
					}
				}
			}
		}
		catch(IOException e)
		{
			
		}
	}
}

class ServerThread extends Thread
{
	private Socket st_sock;
	private DataInputStream st_in;
	private DataOutputStream st_out;
	private StringBuffer st_buffer;
	
	public static MainRoom st_mainroom = new MainRoom(1);
	public static MainRoom st_mainroom2 = new MainRoom(2);
	public static MainRoom st_mainroom3  = new MainRoom(3);
	public static MainRoom st_mainroom4  = new MainRoom(4);
	public static MainRoom st_mainroom5  = new MainRoom(5);
	
	public static ArrayList<Player> WorldList = new ArrayList<Player>();
	public static Hashtable<String, ServerThread> WorldHash  = new Hashtable<String, ServerThread>(1000);
	public static ArrayList<String> DuplicList = new ArrayList<String>();	
		
	public String st_ID;
	public int st_roomNumber;
	public int st_WearItem;
	
	private static final String SEPARATOR = "|"; // 구분자	
	private static final int HEART_BEAT = 1000; // 하트비트

	private static final int REQ_LOGON = 1001; // 로그인 요청
	private static final int REQ_LOGOUT = 1041; // 로그아웃 요청
	private static final int REQ_SENDWORDS = 1051; // 일반채팅
	private static final int REQ_CHARMOVE = 1061; // 캐릭터 움직임
    private static final int REQ_WORLDCHAT = 1071; // 전체채팅
	
	private static final int YES_LOGON = 2001;  // 로그인 동기화
	private static final int YES_LOGOUT = 2041; // 로그아웃 동기화
	private static final int YES_SENDWORD = 2051; // 일반채팅 동기화
	
	private static final int REQ_WISPERSEND = 1022; // 귓속말 요청
    private static final int ANS_NOTICE = 1100; // 서버로부터의 알림
    
	private static final int ANS_WISPERSEND_1 = 1122; // 귓속말 발신 동기화
	private static final int ANS_WISPERSEND_2 = 1123; // 귓속말 수신 동기화
	private static final int ANS_DUPLICATION = 1200; // 중복로그인 알림
	
    private static final int ANS_WORLDCHAT = 1171;
    
    private static final int REQ_ITEMDEL = 1501;

	public ServerThread(Socket sock)
	{
		try
		{
			st_sock = sock;
			
			st_in = new DataInputStream(sock.getInputStream());
			st_out = new DataOutputStream(sock.getOutputStream());
			
			st_buffer = new StringBuffer(2048);			
			st_roomNumber = 1;
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
	
	private void send(String sendData) throws IOException
	{
		synchronized(st_out)
		{
			System.out.println("[서버] "+sendData);
			st_out.writeUTF("0"+sendData);
			st_out.flush();
		}
	}
	
	private synchronized void broadcast(String sendData, int roomNumber) throws IOException
	{
		ServerThread client;
		Hashtable clients = null;
		Enumeration enu = null;
		
		switch(roomNumber)
		{
		case 1:
			 clients = st_mainroom.getClients();
			break;
		case 2:
			 clients = st_mainroom2.getClients();
			break;
		case 3:
			clients = st_mainroom3.getClients();
			break;
		case 4:
			clients = st_mainroom4.getClients();
			break;
		case 5:
			clients = st_mainroom5.getClients();
			break;
		}
		
		if(clients != null)
		{
			enu = clients.keys();
			
			while(enu.hasMoreElements())
			{
				client = (ServerThread) clients.get(enu.nextElement());
				client.send(sendData);
			}
		}
		

	}
	
	public void run()
	{	
		try
		{	
			while(true)
			{
				String recvData = st_in.readUTF();
								
				StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
				int command;
				
				try
				{
					command = Integer.parseInt(st.nextToken());
				}
				catch(NumberFormatException e)
				{
					command = HEART_BEAT;
				}
				
				if(command != HEART_BEAT)
				{
					System.out.println("[클라] "+recvData);
				}
															
				switch(command)
				{
					case REQ_LOGON : // "1001", 아이디를 수신한 경우
					{
						st_ID = st.nextToken();
						st_WearItem = Integer.parseInt(st.nextToken());

						for(int i = 0; i < WorldList.size(); i++)
						{
							if(WorldList.get(i).ID.equals(st_ID))
							{
								DuplicList.add(st_ID);
								
								System.out.println("[서버] " + st_ID + "(이)가 동일계정으로 접속을 시도하였습니다. 기존의 세션을 로그오프시킵니다.");
								
								ServerThread sthread = (ServerThread)WorldHash.get(st_ID);
								
								st_buffer.setLength(0);
								st_buffer.append(ANS_DUPLICATION);
								sthread.send(st_buffer.toString());
								
								System.out.println("[서버] " +st_ID + " 중복로그인으로 인한 접속해제 시도");
								
								try
								{
									sthread.release();
									System.out.println("[서버] " +st_ID + " 중복로그인으로 인한 접속해제 완료");
								}
								catch(SocketException e)
								{
									System.out.println("SocketException :: " + e);
								}
								
							}
						}
						/*--------------------------------------------------------------------------------------*/
						
						System.out.println("[서버] " +st_ID + " 클라이언트의 접속을 시도합니다.");

						Player player = new Player(st_ID);
						player.WearItem = st_WearItem;
						
						st_mainroom.addUser(player, this);
						
						WorldList.add(player);
						WorldHash.put(player.ID, this);
						
						st_buffer.setLength(0);
						st_buffer.append(YES_LOGON);
						st_buffer.append(SEPARATOR);
						st_buffer.append(st_ID);
						
						broadcast(st_buffer.toString(), 1);
						broadcast(st_buffer.toString(), 2);
						broadcast(st_buffer.toString(), 3);
						broadcast(st_buffer.toString(), 4);
						broadcast(st_buffer.toString(), 5);

						broadcast(st_mainroom.getUsers(), 1);
						broadcast(st_mainroom2.getUsers(), 2);
						broadcast(st_mainroom3.getUsers(), 3);
						broadcast(st_mainroom4.getUsers(), 4);
						broadcast(st_mainroom5.getUsers(), 5);
						
						broadcast(st_mainroom.getItems(), 1);
						broadcast(st_mainroom2.getItems(), 2);
						broadcast(st_mainroom3.getItems(), 3);
						broadcast(st_mainroom4.getItems(), 4);
						broadcast(st_mainroom5.getItems(), 5);
	
						System.out.println("[서버] "+st_ID +"의 클라이언트가 접속하였습니다.");
						
						break;
					}
					
					case REQ_SENDWORDS :
					{
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						//st_roomNumber = roomNumber;
						
						st_buffer.setLength(0);
						st_buffer.append(YES_SENDWORD);
						st_buffer.append(SEPARATOR);
						st_buffer.append(id);
						st_buffer.append(SEPARATOR);
						st_buffer.append(roomNumber);
						st_buffer.append(SEPARATOR);
						
						try
						{
							String data = st.nextToken();
							st_buffer.append(data);
						}
						catch(NoSuchElementException e)
						{
							
						}
						
						broadcast(st_buffer.toString(), roomNumber);
						break;
					}
					
					case REQ_WORLDCHAT :
					{
						String id = st.nextToken();
						//st_roomNumber = roomNumber;
						
						st_buffer.setLength(0);
						st_buffer.append(ANS_WORLDCHAT);
						st_buffer.append(SEPARATOR);
						st_buffer.append(id);
						st_buffer.append(SEPARATOR);
						
						try
						{
							String data = st.nextToken();
							st_buffer.append(data);
						}
						catch(NoSuchElementException e)
						{
							
						}
						
						broadcast(st_buffer.toString(), 1);
						broadcast(st_buffer.toString(), 2);
						broadcast(st_buffer.toString(), 3);
						broadcast(st_buffer.toString(), 4);
						broadcast(st_buffer.toString(), 5);

						break;
					}
					
					
					case REQ_LOGOUT :
					{
						System.out.println ("[서버] "+st_ID+"(이)가 로그아웃을 요청합니다.");
						release();
						
						broadcast(st_mainroom.getUsers(), 1);
						broadcast(st_mainroom2.getUsers(), 2);
						broadcast(st_mainroom3.getUsers(), 3);
						broadcast(st_mainroom4.getUsers(), 4);
						broadcast(st_mainroom5.getUsers(), 5);
						
						broadcast(st_mainroom.getItems(), 1);
						broadcast(st_mainroom2.getItems(), 2);
						broadcast(st_mainroom3.getItems(), 3);
						broadcast(st_mainroom4.getItems(), 4);
						broadcast(st_mainroom5.getItems(), 5);
						
						break;
					}
					
					case REQ_CHARMOVE :
					{
						String id = null;
						float PosX = 0;
						float PosY = 0;
						boolean isleft = false;
						int mapNum = 1;
						int WearItem = 0;
						
						try
						{
							 id = st.nextToken();
							 PosX = Float.parseFloat(st.nextToken());
							 PosY = Float.parseFloat(st.nextToken());
							 isleft = Boolean.parseBoolean(st.nextToken());
							 mapNum = Integer.parseInt(st.nextToken());
							 WearItem = Integer.parseInt(st.nextToken());
						}
						catch(NumberFormatException e)
						{
							System.out.println("NumberFromatException :: " + e);
						}
									
						switch(st_roomNumber)
						{
						case 1:
							for(int i = 0; i < st_mainroom.userList.size(); i++)
							{
								if( st_mainroom.userList.get(i).ID.equals(id))
								{
									st_mainroom.userList.get(i).PosX = PosX;
									st_mainroom.userList.get(i).PosY = PosY;
									st_mainroom.userList.get(i).IsLeft = isleft;
									st_mainroom.userList.get(i).WearItem = WearItem;
																	
									if(st_roomNumber != mapNum)
									{
										System.out.println("[서버] " + id +" 가 "+ st_roomNumber +"번 방에서 "+ mapNum +"번 방으로 맵을 이동하였습니다.");
										st_roomNumber = mapNum;
										//st_mainroom.userList.get(i).MapNum = 2;
										switch(mapNum)
										{
										case 1:
											break;
										case 2:
											st_mainroom2.addUser(st_mainroom.userList.get(i),st_mainroom.userHash.get(id));
											st_mainroom.delUser(id);
											break;
										case 3:
											st_mainroom3.addUser(st_mainroom.userList.get(i),st_mainroom.userHash.get(id));
											st_mainroom.delUser(id);
											break;
										case 4:
											st_mainroom4.addUser(st_mainroom.userList.get(i),st_mainroom.userHash.get(id));
											st_mainroom.delUser(id);
											break;
										case 5:
											st_mainroom5.addUser(st_mainroom.userList.get(i),st_mainroom.userHash.get(id));
											st_mainroom.delUser(id);
											break;
										}

									}
									
								}
							}
							break;
						case 2:
							for(int i = 0; i < st_mainroom2.userList.size(); i++)
							{
								if( st_mainroom2.userList.get(i).ID.equals(id))
								{
									st_mainroom2.userList.get(i).PosX = PosX;
									st_mainroom2.userList.get(i).PosY = PosY;
									st_mainroom2.userList.get(i).IsLeft = isleft;
									st_mainroom2.userList.get(i).WearItem = WearItem;
																	
									if(st_roomNumber != mapNum)
									{
										System.out.println("[서버] " + id +" 가 "+ st_roomNumber +"번 방에서 "+ mapNum +"번 방으로 맵을 이동하였습니다.");
										st_roomNumber = mapNum;
										//st_mainroom.userList.get(i).MapNum = 2;
										switch(mapNum)
										{
										case 1:
											st_mainroom.addUser(st_mainroom2.userList.get(i),st_mainroom2.userHash.get(id));
											st_mainroom2.delUser(id);
											break;
										case 2:
											break;
										case 3:
											st_mainroom3.addUser(st_mainroom2.userList.get(i),st_mainroom2.userHash.get(id));
											st_mainroom2.delUser(id);
											break;
										case 4:
											st_mainroom4.addUser(st_mainroom2.userList.get(i),st_mainroom2.userHash.get(id));
											st_mainroom2.delUser(id);
											break;
										case 5:
											st_mainroom5.addUser(st_mainroom2.userList.get(i),st_mainroom2.userHash.get(id));
											st_mainroom2.delUser(id);
											break;
										}
									}
								}
							}
									
							break;
						case 3:
							for(int i = 0; i < st_mainroom3.userList.size(); i++)
							{
								if( st_mainroom3.userList.get(i).ID.equals(id))
								{
									st_mainroom3.userList.get(i).PosX = PosX;
									st_mainroom3.userList.get(i).PosY = PosY;
									st_mainroom3.userList.get(i).IsLeft = isleft;
									st_mainroom3.userList.get(i).WearItem = WearItem;
																	
									if(st_roomNumber != mapNum)
									{
										System.out.println("[서버] " + id +" 가 "+ st_roomNumber +"번 방에서 "+ mapNum +"번 방으로 맵을 이동하였습니다.");
										st_roomNumber = mapNum;
										//st_mainroom.userList.get(i).MapNum = 2;
										switch(mapNum)
										{
										case 1:
											st_mainroom.addUser(st_mainroom3.userList.get(i),st_mainroom3.userHash.get(id));
											st_mainroom3.delUser(id);
											break;
										case 2:
											st_mainroom2.addUser(st_mainroom3.userList.get(i),st_mainroom3.userHash.get(id));
											st_mainroom3.delUser(id);
											break;
										case 3:
											break;
										case 4:
											st_mainroom4.addUser(st_mainroom3.userList.get(i),st_mainroom3.userHash.get(id));
											st_mainroom3.delUser(id);
											break;
										case 5:
											st_mainroom5.addUser(st_mainroom3.userList.get(i),st_mainroom3.userHash.get(id));
											st_mainroom3.delUser(id);
											break;
										}
									}
								}
							}
							break;
						case 4:
							for(int i = 0; i < st_mainroom4.userList.size(); i++)
							{
								if( st_mainroom4.userList.get(i).ID.equals(id))
								{
									st_mainroom4.userList.get(i).PosX = PosX;
									st_mainroom4.userList.get(i).PosY = PosY;
									st_mainroom4.userList.get(i).IsLeft = isleft;
									st_mainroom4.userList.get(i).WearItem = WearItem;
																	
									if(st_roomNumber != mapNum)
									{
										System.out.println("[서버] " + id +" 가 "+ st_roomNumber +"번 방에서 "+ mapNum +"번 방으로 맵을 이동하였습니다.");
										st_roomNumber = mapNum;
										//st_mainroom.userList.get(i).MapNum = 2;
										switch(mapNum)
										{
										case 1:
											st_mainroom.addUser(st_mainroom4.userList.get(i),st_mainroom4.userHash.get(id));
											st_mainroom4.delUser(id);
											break;
										case 2:
											st_mainroom2.addUser(st_mainroom4.userList.get(i),st_mainroom4.userHash.get(id));
											st_mainroom4.delUser(id);
											break;
										case 3:
											st_mainroom3.addUser(st_mainroom4.userList.get(i),st_mainroom4.userHash.get(id));
											st_mainroom4.delUser(id);
											break;
										case 4:
											break;
										case 5:
											st_mainroom5.addUser(st_mainroom4.userList.get(i),st_mainroom4.userHash.get(id));
											st_mainroom4.delUser(id);
											break;
										}
									}
								}
							}
							break;
						case 5:
							for(int i = 0; i < st_mainroom5.userList.size(); i++)
							{
								if(st_mainroom5.userList.get(i).ID.equals(id))
								{
									st_mainroom5.userList.get(i).PosX = PosX;
									st_mainroom5.userList.get(i).PosY = PosY;
									st_mainroom5.userList.get(i).IsLeft = isleft;
									st_mainroom5.userList.get(i).WearItem = WearItem;
																	
									if(st_roomNumber != mapNum)
									{
										System.out.println("[서버] " + id +" 가 "+ st_roomNumber +"번 방에서 "+ mapNum +"번 방으로 맵을 이동하였습니다.");
										st_roomNumber = mapNum;
										switch(mapNum)
										{
										case 1:
											st_mainroom.addUser(st_mainroom5.userList.get(i),st_mainroom5.userHash.get(id));
											st_mainroom5.delUser(id);
											break;
										case 2:
											st_mainroom2.addUser(st_mainroom5.userList.get(i),st_mainroom5.userHash.get(id));
											st_mainroom5.delUser(id);
											break;
										case 3:
											st_mainroom3.addUser(st_mainroom5.userList.get(i),st_mainroom5.userHash.get(id));
											st_mainroom5.delUser(id);
											break;
										case 4:
											st_mainroom4.addUser(st_mainroom5.userList.get(i),st_mainroom5.userHash.get(id));
											st_mainroom5.delUser(id);
											break;
										case 5:
											break;
										}
									}
								}
							}
							break;
						}
						
						broadcast(st_mainroom.getUsers(), 1);
						broadcast(st_mainroom2.getUsers(), 2);
						broadcast(st_mainroom3.getUsers(), 3);
						broadcast(st_mainroom4.getUsers(), 4);
						broadcast(st_mainroom5.getUsers(), 5);
						
						broadcast(st_mainroom.getItems(), 1);
						broadcast(st_mainroom2.getItems(), 2);
						broadcast(st_mainroom3.getItems(), 3);
						broadcast(st_mainroom4.getItems(), 4);
						broadcast(st_mainroom5.getItems(), 5);

						break;
					}
					
					case REQ_WISPERSEND :
					{
						String id = st.nextToken();
						String WUserNickname = st.nextToken();
						String message = null;
						boolean IsLive = false;
						
						if(st.hasMoreTokens())
						{
							message = st.nextToken();
						}
						
						for(int i = 0; i <  WorldList.size(); i++)
						{
							if(WorldList.get(i).ID.equals(WUserNickname))
							{
								IsLive = true;
							}
						}
						
						if(IsLive)
						{
							ServerThread SThread = (ServerThread)WorldHash.get(id);
							
							// 귓속말 발신 . ANS_WISPERSEND_1 | 받는사람 | 메시지
							
							st_buffer.setLength(0);
							st_buffer.append(ANS_WISPERSEND_1);
							st_buffer.append(SEPARATOR);
							st_buffer.append(WUserNickname);
							st_buffer.append(SEPARATOR);
							st_buffer.append(message);
							
							SThread.send(st_buffer.toString());
							
							// 귓속말 수신. ANS_WISPERSEND_2 | 보낸사람 | 메시지
							
							SThread = null;
							SThread = (ServerThread)WorldHash.get(WUserNickname);
														
							st_buffer.setLength(0);
							st_buffer.append(ANS_WISPERSEND_2);
							st_buffer.append(SEPARATOR);
							st_buffer.append(id);
							st_buffer.append(SEPARATOR);
							st_buffer.append(message);
							
							SThread.send(st_buffer.toString());
						}
						else
						{
							ServerThread SThread = (ServerThread)WorldHash.get(id);
							
							st_buffer.setLength(0);
							st_buffer.append(ANS_NOTICE);
							st_buffer.append(SEPARATOR);
							st_buffer.append("접속하지 않은 사용자입니다.");
							
							SThread.send(st_buffer.toString());
						}
						break;
					}
					
					case REQ_ITEMDEL :
					{
						int MapNum = Integer.parseInt(st.nextToken());
						
						switch(MapNum)
						{
						case 1:
							st_mainroom.ItemList.clear();
							break;
						case 2:
							st_mainroom2.ItemList.clear();
							break;
						case 3:
							st_mainroom3.ItemList.clear();
							break;
						case 4:
							st_mainroom4.ItemList.clear();
							break;
						case 5:
							st_mainroom5.ItemList.clear();
							break;
						}
						
						broadcast(st_mainroom.getItems(), 1);
						broadcast(st_mainroom2.getItems(), 2);
						broadcast(st_mainroom3.getItems(), 3);
						broadcast(st_mainroom4.getItems(), 4);
						broadcast(st_mainroom5.getItems(), 5);
						
						break;
					}
					
					case HEART_BEAT :
					{
						break;
					}
				}
				
				Thread.sleep(100);

			}
		}
		catch (NullPointerException e)
		{
			switch(st_roomNumber)
			{
			case 1:
				st_mainroom.delUser(st_ID);
				break;
			case 2:
				st_mainroom2.delUser(st_ID);
				break;
			case 3:
				st_mainroom3.delUser(st_ID);
				break;
			case 4:
				st_mainroom4.delUser(st_ID);
				break;
			case 5:
				st_mainroom5.delUser(st_ID);
				break;
			}
		}
		catch (InterruptedIOException e)
		{
			System.out.println ("[서버] "+st_ID+"(이)가 더 이상 하트비트를 보내오지 않습니다.");
		
			switch(st_roomNumber)
			{
			case 1:
				st_mainroom.delUser(st_ID);
				break;
			case 2:
				st_mainroom2.delUser(st_ID);
				break;
			case 3:
				st_mainroom3.delUser(st_ID);
				break;
			case 4:
				st_mainroom4.delUser(st_ID);
				break;
			case 5:
				st_mainroom5.delUser(st_ID);
				break;
			}
		}
		catch(IOException e)
		{
			System.out.println(e);
			
			switch(st_roomNumber)
			{
			case 1:
				st_mainroom.delUser(st_ID);
				break;
			case 2:
				st_mainroom2.delUser(st_ID);
				break;
			case 3:
				st_mainroom3.delUser(st_ID);
				break;
			case 4:
				st_mainroom4.delUser(st_ID);
				break;
			case 5:
				st_mainroom5.delUser(st_ID);
				break;
			}
		} 
		catch (InterruptedException e) 
		{
			System.out.println(e);
			
			switch(st_roomNumber)
			{
			case 1:
				st_mainroom.delUser(st_ID);
				break;
			case 2:
				st_mainroom2.delUser(st_ID);
				break;
			case 3:
				st_mainroom3.delUser(st_ID);
				break;
			case 4:
				st_mainroom4.delUser(st_ID);
				break;
			case 5:
				st_mainroom5.delUser(st_ID);
				break;
			}
		} 
		
		try 
		{
			if(st_ID != null)
			{
				release();
			}
		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
}

public void release() throws IOException
{
	if(st_ID != null)
	{
		st_buffer.setLength(0);
		st_buffer.append(YES_LOGOUT);
		st_buffer.append(SEPARATOR);
		st_buffer.append(st_ID);
		
		broadcast(st_buffer.toString(), 1);
		broadcast(st_buffer.toString(), 2);
		broadcast(st_buffer.toString(), 3);
		broadcast(st_buffer.toString(), 4);
		broadcast(st_buffer.toString(), 5);
		
		for(int i = 0; i < WorldList.size(); i++)
		{
			if(WorldList.get(i).ID.equals(st_ID))
			{
				WorldList.remove(i);
				WorldHash.remove(st_ID);
			}
		}

		switch(st_roomNumber)
		{
		case 1:
			try 
			{
				broadcast(st_mainroom.getUsers(), st_roomNumber);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			st_mainroom.delUser(st_ID);
			break;
		case 2:
			try 
			{
				broadcast(st_mainroom2.getUsers(), st_roomNumber);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			st_mainroom2.delUser(st_ID);
			break;
		case 3:
			try 
			{
				broadcast(st_mainroom3.getUsers(), st_roomNumber);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			st_mainroom3.delUser(st_ID);
			break;
		case 4:
			try 
			{
				broadcast(st_mainroom4.getUsers(), st_roomNumber);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			st_mainroom4.delUser(st_ID);
			break;
		case 5:
			try 
			{
				broadcast(st_mainroom5.getUsers(), st_roomNumber);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			st_mainroom5.delUser(st_ID);
			break;
		}

		
		System.out.println("[서버] "+st_ID + "와의 연결을 종료합니다.");
		st_ID = null;
	}
	try
	{
		if(st_in != null)
		{
			st_in.close();
		}
	}
	catch(IOException e1)
	{
		
	}
	finally
	{
		st_in = null;
	}
	
	try
	{
		if(st_out != null)
		{
			st_out.close();
		}
	}
	catch(IOException e1)
	{
		
	}
	finally
	{
		st_out = null;
	}
	
	try
	{
		if(st_sock != null)
		{
			st_sock.close();
		}
	}
	catch(IOException e1)
	{
		
	}
	finally
	{
		st_sock = null;
	}
	

	}
}

class MainRoom
{
	private static final int MAX_ROOM = 4;
	private static final int MAX_USER = 100;
	private static final int ANS_CURRENTUSER = 1300; // 현재 접속한 유저 알림
	private static final String SEPARATOR = "|"; // 구분자	
    private static final int ITEM_GEN = 1500;

	public ArrayList<Player> userList = new ArrayList<Player>();
	public Hashtable<String, ServerThread> userHash  = new Hashtable<String, ServerThread>(MAX_USER);

	public ArrayList<Item> ItemList = new ArrayList<Item>();
	
	public Hashtable<Integer, MainRoom> roomHash = new Hashtable<Integer, MainRoom>(MAX_ROOM);
	
	private int roomCount;
	Random random = new Random();
	
	ItemGenThread itemGenThread;

	public MainRoom(int roomnumber)
	{
		roomCount = roomnumber;
		itemGenThread = new ItemGenThread();
		itemGenThread.start();
	}
	
	public void addUser(Player player, ServerThread client)
	{		
		userList.add(player);
		userHash.put(player.ID, client);
	}
	
	public void delUser(String ID)
	{
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).ID.equals(ID))
			{
				userList.remove(i);
				userHash.remove(ID);
			}
		}
	}
	
	public synchronized String getUsers()
	{			
		StringBuffer player = new StringBuffer();
		String players;
		
		player.setLength(0);
		player.append(ANS_CURRENTUSER);
		player.append(SEPARATOR);
		
		for(int i = 0; i < userList.size(); i++)
		{
			player.append(userList.get(i).ID);
			player.append(SEPARATOR);
			player.append(userList.get(i).PosX);
			player.append(SEPARATOR);
			player.append(userList.get(i).PosY);
			player.append(SEPARATOR);
			player.append(userList.get(i).IsLeft);
			player.append(SEPARATOR);
			player.append(userList.get(i).WearItem);
			
			if(i+1 != userList.size())
			{
				player.append(SEPARATOR);
			}
		}
		
		try
		{
			players = new String(player);
			players = players.substring(0, players.length());
		}
		catch(StringIndexOutOfBoundsException e)
		{
			return "";
		}
		return players;
	}
	
	public synchronized String getItems()
	{
		StringBuffer item = new StringBuffer();
		String Items;
		
		item.setLength(0);
		item.append(ITEM_GEN);
		item.append(SEPARATOR);
		
		for(int i = 0; i < ItemList.size(); i++)
		{
			item.append(ItemList.get(i).ItemPos.x);
			item.append(SEPARATOR);
			item.append(ItemList.get(i).ItemPos.y);
			
			if(i+1 != ItemList.size())
			{
				item.append(SEPARATOR);
			}
		}
		
		try
		{
			Items = new String(item);
			Items = Items.substring(0, Items.length());
		}
		catch(StringIndexOutOfBoundsException e)
		{
			return "";
		}
		return Items;
	}

	public synchronized Hashtable<String, ServerThread> getClients()
	{		
		if(!userHash.isEmpty())
		{
			return userHash;
		}
		else
		{
			return null;
		}
	}
	
	public class ItemGenThread extends Thread
	{
		public void run()
		{
			while(true)
			{
				try
				{
					sleep(30000);
					if(ItemList.size() < 1)
					{
						int ItemPosX =  random.nextInt(300)-300;
						int ItemPosY =  random.nextInt(70)-70;
						
						Item item = new Item(ItemPosX, ItemPosY);
						ItemList.add(item);
					}
				}
				catch(InterruptedException e)
				{	
					System.out.println(e);
				}
			}
		}
	}
	
}

class Player
{
	public String ID;
	public float PosX;
	public float PosY;
	public boolean IsLeft;
	public int MapNum;
	public int WearItem;
	
	public Player(String id)
	{
		ID = id;
		PosX = 0.0f;
		PosY = 0.0f;
		IsLeft = false;
		WearItem = 0;
	}
}

class Item
{
	public Point ItemPos = new Point();
	
	public Item(int posX, int posY)
	{
		ItemPos.x = posX;
		ItemPos.y = posY;
	}
}
