# Chat Application
Chat application for message and file transfer using TCP sockets
								
Description:

• This project involves java-based implementation using TCP sockets.
• The zipped folder includes the source codes, and the input and transferred files in the resources folder.
• The implementation consists of 1 application that acts as both server and client. There would be two threads , one writing thread and the main reading thread. This is a 1:1 chat application when both chatclients run on same machine. This could be extended to work when both client and server are running on different hosts and also for multi clientchat application.

Steps to run:

- Open the src folder of CN-Project-3 folder, and start the chatclient first by using the below commands to complie and start respectively:

•javac Chat.java 
•java Chat

- Then start the chatclient by using the below imputs to start:

• portNumber
• userName
• portNumber to connect to

After this, once the chat applications accept the incoming client connection, we could use one of the below commands as user input to transfer files and messages:

transfer <filename> -> to transfer files
all other user inputs -> to transfer messages

The path for file download and search is the resources folder of the project.

Once a user input is done, a check is done if it is a valid command. If it is valid, we use the objectoutput stream to write the same command as message to the output stream of the client socket (sendMessage() method does this). This would be read from the input stream at the server socket's end to identify the user input. Then corresponding methods for sending and reading are performed at both send and receive end.

Once the chunked file transfer is complete, then the client waits for next user input for subsequent file transfers.

We also send a FILE NOT FOUND message if the specified file is not found in the given directory(resourses folder) here.

The file is sent and read as chunks using the DataOutput and DataInput Streams. Closing these would close the socket's input and output streams. And hence, these are closed only when the exit ftpclient command is entered. Additionally, the chunks of 1Kb (buffered outputbytes) are written to output stream as soon as they are received using the flush() command.
