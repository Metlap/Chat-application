package com.cns.project3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Chat {

	int serverPort; // The server will be listening on this port number
	ServerSocket serverSocket; // serversocket used to lisen on port number 8000
	Socket connection = null; // socket for the connection with the client
	String userName;

	String message; // message received from the client
	String MESSAGE; // uppercase message send to the client
	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket
	DataInputStream dis;
	DataOutputStream dos;

	private static String FILE_PATH = "./resources/";

	public Chat() {
		// creating a new thread that implements write
	}

	public Chat(int serverPort, String userName) {
		// creating a new thread that implements write

		this.userName = userName;
		this.serverPort = serverPort;
		// create a serversocket
		try {
			serverSocket = new ServerSocket(serverPort, 10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Wait for connection
		System.out.println("Waiting for connection");
		// accept a connection from the client
		
		new Handler(userName).start();
		this.run();
		
	}

	public static class Handler extends Thread {
		 int serverPortNumber;
		Socket requestSocket;
		String userName;

		// initialize inputStream and outputStream

		String message; // message received from the client
		String MESSAGE; // uppercase message send to the client
		ObjectOutputStream writeOut; // stream write to the socket
		ObjectInputStream writeIn; // stream read from the socket
		DataInputStream writeDis;
		DataOutputStream writeDos;

//		public Handler() {
//			
//		}

		public Handler(String userName) {
			try {
				this.userName = userName;
				System.out.println("Enter a port number to connect to and send message to: ");
				Scanner scanner = new Scanner(System.in);

				String input = scanner.nextLine();

				this.serverPortNumber = Integer.parseInt(input);
				this.requestSocket = new Socket("localhost", serverPortNumber);

				// initialize inputStream and outputStream
				writeOut = new ObjectOutputStream(requestSocket.getOutputStream());
				writeOut.flush();


			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			System.out.println("Waiting for user input messages");

			try (// Reading commands from user input
					Scanner scanner = new Scanner(System.in)) {
				while (true) {

					String input = scanner.nextLine();
					
					String[] splitStr = input.trim().split("\\s+");
					
					if (splitStr.length == 2 && (splitStr[0]).equals("transfer") && splitStr[1].length() > 0) {

						// Send this input to server
						sendMessage(this.userName + ": " + input);
						
						// Appending new to requested file name
						sendFile(splitStr[1]);

					}

					else if (splitStr.length > 0) {

						// Send this input to server
						sendMessage(this.userName + ": " +input);
					}

					else if (splitStr.length == 2 && (splitStr[0]).equals("exit") && (splitStr[1]).equals("ftpclient")) {

						// Exiting and closing by quitting the while loop
						break;

					}

				}
			} finally {
				// Close connections
				try {
					writeOut.close();
					if (writeDos != null)
						writeDos.close();
					requestSocket.close();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
		
		// send a message to the output stream -> to let the server know of the command
		// input by user in client
		void sendMessage(String msg) {
			try {
				// stream write the message
				writeOut.writeObject(msg);
				writeOut.flush();
//				System.out.println("Send message: " + msg);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		void sendFile(String fileName) {

			String filePath = FILE_PATH + fileName;

			//System.out.println("Sending file to the server");

			// Make file chunks of size 1kb and send to the server
			int totalBytesTransferred = 0;
			File file = new File(filePath);
			try {
				
				writeDos = new DataOutputStream(requestSocket.getOutputStream());
				
				if (file.exists()) {
					FileInputStream fileInputStream = new FileInputStream(file);
					
					// Getting the size of the file and sending it to server
					writeDos.writeLong(file.length());
					
					writeDos.flush();
					
					//System.out.println("Length is ::" + file.length());

					// Read data into buffer array
					byte[] buffer = new byte[1024];
					// -1 -> EOF
					while ((totalBytesTransferred = fileInputStream.read(buffer)) != -1) {

						// Writing sub arrays to include case of last chunk where size could be less
						// than 1kb
						writeDos.write(buffer, 0, totalBytesTransferred);
						writeDos.flush();
					}
					fileInputStream.close();

					//System.out.println("======File Send success======");

				} else {
					writeDos.writeLong(0L);
					writeDos.flush();
//					writeDos.close();
					System.out.println("File not found at the given directory");
					return;
					
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("File not found in the specified path");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Other than File not found");
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}

	}


	void run() {
		try {
			
			try {
				connection = serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());

			// initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());

			while (true) {

				message = (String) in.readObject();

				String[] splitStr = message.trim().split("\\s+");

				if (splitStr.length == 3 && (splitStr[1]).equals("transfer") && splitStr[1].length() > 0) {

					writeMessageToConsole(message);
					// Appending new to requested file name
					receiveFile("new" + splitStr[2]);

				}

				else if (splitStr.length > 1) {

					writeMessageToConsole(message);
				}

				else if (splitStr.length == 3 && (splitStr[1]).equals("exit") && (splitStr[2]).equals("ftpclient")) {

					// Exiting and closing by quitting the while loop
					break;

				}

			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Data received in unknown format");
			e.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// Close connections
			try {
				in.close();
				out.close();
				if (dos != null)
					dos.close();
				if (dis != null)
					dis.close();
				connection.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	private void writeMessageToConsole(String message) {

		System.out.println(message);

	}

	void receiveFile(String fileName) {

		try {

			String filePath = FILE_PATH + fileName;
			File file = new File(filePath);
			dis = new DataInputStream(connection.getInputStream());

			long size = dis.readLong();
			if (size == 0L) {
				System.out.println("File not found at the given directory");
				return;
			}

			FileOutputStream fileOutputStream = new FileOutputStream(file);

			int bytesRead = 0;
			byte[] buffer = new byte[1024];

			while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				fileOutputStream.write(buffer, 0, bytesRead);
				size -= bytesRead; // read upto file size
				fileOutputStream.flush();
			}
//				fileOutputStream.close();

			System.out.println("======File Receive success======");

		}

		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exception other than File not found");
			e.printStackTrace();
		}

	}

	// In main thread, just read the messages from BOb and print them in logs

	// In the new thread, take a port number as input and write messages on to the
	// port number
	// transfer filename -> transfers the file not just the message

	// Main thread creates a server socket and listens on it for incoming requests
	// If received message is transfer filename -> then read the file and store it
	// locally

	public static void main(String args[]) {
		String userName;
		int serverPort;

		System.out.println("Enter the userName: ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		userName = input;
		System.out.println("Enter the port: ");
		// String[] splitStr = input.trim().split("\\s+");
		input = scanner.nextLine();
		serverPort = Integer.parseInt(input);

		Chat chat = new Chat(serverPort, userName);
		
	}

}
