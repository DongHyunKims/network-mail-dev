package com.mail.client;
import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;

//import org.apache.commons.codec.binary.Base64; 
public class SMTPClient
{
    
		private String hostname;
		private int port;
		private String senderEmail;
		private String senderPassword;
		private String recieverEmail;
		private String content;
		private String subject;
		private String serverLog;
		private String clientLog;
		private BufferedReader inFromServer;
		private SSLSocket sock;
		private DataOutputStream outToServer;
		
		

	public SMTPClient(String hostname, String senderEmail,
				String senderPassword, String recieverEmail, String content,
				String subject) {
			super();
			this.hostname = hostname;
			this.senderEmail = senderEmail;
			this.senderPassword = senderPassword;
			this.recieverEmail = recieverEmail;
			this.content = content;
			this.subject = subject;
			serverLog = "";
			clientLog = "";
		}


	public SMTPClient(String hostname, int port, String senderEmail,
				String senderPassword, String recieverEmail, String content,
				String subject) {
			super();
			this.hostname = hostname;
			this.port = port;
			this.senderEmail = senderEmail;
			this.senderPassword = senderPassword;
			this.recieverEmail = recieverEmail;
			this.content = content;
			this.subject = subject;
			serverLog = "";
			clientLog = "";
		}

	public SMTPClient(String hostname, int port, String senderEmail,
				String senderPassword, String recieverEmail, String content,
				String subject, String serverLog, String clientLog) {
			super();
			this.hostname = hostname;
			this.port = port;
			this.senderEmail = senderEmail;
			this.senderPassword = senderPassword;
			this.recieverEmail = recieverEmail;
			this.content = content;
			this.subject = subject;
			this.serverLog = serverLog;
			this.clientLog = clientLog;
		}




	public SMTPClient() {
		
		super();
		
		serverLog = "";
		clientLog = "";
	}

	@Override
	public String toString() {
		return "SMTPClient [hostname=" + hostname + ", port=" + port
				+ ", senderEmail=" + senderEmail + ", senderPassword="
				+ senderPassword + ", recieverEmail=" + recieverEmail
				+ ", content=" + content + ", subject=" + subject
				+ ", serverLog=" + serverLog + ", clientLog=" + clientLog + "]";
	}




	public String getSubject() {
		return subject;
	}



	public void setSubject(String subject) {
		this.subject = subject;
	}


	public String getServerLog() {
		return serverLog;
	}

	public void setServerLog(String serverLog) {
		this.serverLog = serverLog;
	}

	public String getClientLog() {
		return clientLog;
	}

	public void setClientLog(String clientLog) {
		this.clientLog = clientLog;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public String getSenderPassword() {
		return senderPassword;
	}

	public void setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
	}
	
	public String getRecieverEmail() {
		return recieverEmail;
	}

	public void setRecieverEmail(String reciever) {
		this.recieverEmail = reciever;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}


	public String connectMail(){
    	//딜레이 0.5  
		int delay =500;
			
		  //보안소켓을 생성하기 위한 객체 팩토리이다.
          SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
          //SSLSocket sock;
          
          
		try {
			selectPort(hostname);
			//createSokcket 메소드 호출을 통해 SSLSocket 인스턴스를 생성할 수 있다. 
			sock = (SSLSocket) sslsocketfactory.createSocket(hostname, port);
	         //서버로 부터 가져온다. 서버의 log를 남기기 위한통로.
	         inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	         
	         //서버의 로그를 남길 쓰레드를 생성한다.
	          Thread tr = new Thread(new Runnable()
	          {
	               public void run()
	               {
	                    try
	                    {
	                    	
	                    	String tempLine = "";     
	                    	//inFromServer 통로를 통해 서버의 응답을 받아온다. 
	                         while((tempLine = inFromServer.readLine()) != null){     	 
	                        	 //여러가지 예외처리가 필요하다. 
	                        	 if(tempLine.startsWith("535-5.7.8")){
	                        		 serverLog = "SEVER : Login Error"; 
	                        		 break;
	                        	 }                     	 
	                        	 System.out.println("SERVER: "+tempLine);
	                        	 //받아온 응답을 모아서 로그로 만든다.
	                        	 serverLog += "SEVER : " + tempLine + "\n";
	                         }
	                    }
	                    catch (IOException e)
	                    {
	                         e.printStackTrace();
	                    }
	               }
	          });
	          
	          tr.start();
	          
	          //메일 서버로 나가는 통로. 
	          outToServer = new DataOutputStream(sock.getOutputStream());
	          
	          send("EHLO "+ hostname+"\r\n",outToServer);
	          //스레드를 대기상태로 만들어준다.
	          Thread.sleep(delay);
	          
	          
	          // 로그인 하기위한 정보,Base64로 인코딩 후 정보를 보내준다. 
	          String encodedToken =  Base64.encodeBase64String(senderEmail.getBytes());
	          send("AUTH LOGIN " + encodedToken +"\r\n", outToServer);
	          Thread.sleep(delay);
	          String encodedPassword =  Base64.encodeBase64String(senderPassword.getBytes());
	          send(encodedPassword + "\r\n", outToServer);
	          Thread.sleep(delay);
	          if(serverLog.equals("Login Error")){
	        	  return serverLog;
	          }
	          //보내는 이메일 주소 
	          send("MAIL FROM: <"+senderEmail+">\r\n", outToServer);
	          Thread.sleep(delay);
	          //받는 이메일 주소 
	          send("RCPT TO: <"+recieverEmail+">\r\n", outToServer);
	          Thread.sleep(delay);
	          //데이터
	          send("DATA\r\n", outToServer);
	          Thread.sleep(delay);
	          //제목
	          send("Subject: "+subject+"\r\n", outToServer);
	          Thread.sleep(delay);
	          //내용
	          send(content, outToServer);
	          Thread.sleep(delay);
	          //메세지의 마지막을 나타냄 
	          send("\r\n.\r\n", outToServer);
	          Thread.sleep(delay);
	          //끝 
	          send("QUIT\r\n", outToServer);
	          
	          
	 		 //closeAll(sock,outToServer,inFromServer);
	          
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
         return serverLog;
     }
     //메세지를 보내는 메소드.
     private void send(String s, DataOutputStream ots) throws Exception
     {
    	  
    	 ots.writeBytes(s);
    	 //클라이언트 로그도 만들어 준다.
    	 clientLog +="Client : " + s + "\n";
         System.out.println("CLIENT: "+s);
        
     }
     
     //포트선택 메소드.
     private void selectPort(String hostName){
    	 
    	 if(hostName.equals("smtp.gmail.com") | hostName.equals("smtp.mail.yahoo.com") | hostName.equals("smtp.naver.com")){
    		 port = 465;
    	 }
    	 

     }
     //모든 통로 및 소켓을 닫는 메소드 
     public void closeAll(){
  
    	 try {
    	  	outToServer.close();
			inFromServer.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	 
     }
     
}
