import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DirectoryClientConnector extends DirectoryServer {
    //Constructor for the class. It should store the name of the server and the port number
    // to be stored by the server, as well as a "screen name" for the client.
    // The constructor should not actually connect to the server.
    // The port number specified here will NOT be used for a connection in this assignment.
    // Do NOT use it to connect to the DirectoryServer. v
    private int myid;

    HashMap<Integer, String> errorList = new HashMap<>();
    private final String servername;
    private final int Port;
    private final String Name;
    private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(10);

    public DirectoryClientConnector(String serverName, int port, String name) {
        this.servername = serverName;
        this.Port = port;
        this.Name = name;
    }

    //Connect to the server and issue a LOGON command as described by the DirectoryServer.

     //timer2.schedule(task2, 2000, 7000);
    public void logon() throws Exception {
        try (Socket socket = new Socket(this.servername, this.Port)){
            //should be wrapped in a try
            try{
                BufferedReader input = new BufferedReader((new InputStreamReader(socket.getInputStream())));
                PrintWriter output = new PrintWriter(socket.getOutputStream());

                output.println("LOGON " + Port + " "+ Name);
                output.flush();

                String serverResponse = input.readLine(); //get client command

                String[] s = serverResponse.split(":"); //split the response
                System.out.println(serverResponse);

                myid = Integer.parseInt(s[1]); //client id will be the 2nd place in command
                if (s[0].equals("ADDED")) {//if added is sent back then ping
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                              //why not map errors?

                                if(!errorList.containsKey(myid)) //if its there
                                    //beware
                                    //stop pinging
                                {
                                    ping();

                                }

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };


                     //run every 8 seconds because ttl is 10 seconds initially


                    timer.schedule(task, 0, 8000);
                    //timer2.schedule(task2, 2000, 7000);

                }

            } catch (Exception e) {
                System.out.println("Error: Failed To LOGON");
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid Host Name");
        }


    }

    public ArrayList<String> getList() throws Exception {
        ArrayList<String> clients = new ArrayList<>();
        try(Socket socket = new Socket(servername, Port);) {

            try {
                BufferedReader input = new BufferedReader((new InputStreamReader(socket.getInputStream())));
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                // String serverResponse = input.readLine(); //get client command
                // System.out.println(serverResponse);
                output.println("LIST " + myid); //sends the command to the server
                output.flush();

                String serverResponse; //get client command
                while((serverResponse=input.readLine())!=null){
                            clients.add(serverResponse);
                }
                socket.close(); //close
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {

        }
        //use the ping command as described

        return clients;
    }

    public void logoff() throws Exception {

        try (Socket socket = new Socket(this.servername, this.Port)) {
            try{
                BufferedReader input = new BufferedReader((new InputStreamReader(socket.getInputStream())));
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                output.println("LOGOFF " + myid);
                output.flush();
                String serverResponse = input.readLine(); //get client command
                System.out.println("\n" + serverResponse);
                socket.close();

            } catch (Exception e) {
                System.out.println("Error: Failed To LOGOFF");
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid Host Name");
        }
    }

    private void ping() throws Exception {
        try(Socket socket = new Socket(servername, Port);) {

            try {
                BufferedReader input = new BufferedReader((new InputStreamReader(socket.getInputStream())));
                PrintWriter output = new PrintWriter(socket.getOutputStream());

                output.println("PING " + myid); //sends the command to the server
                output.flush();

                String serverResponse = input.readLine(); //get client command
                System.out.print(serverResponse + "\n");

                String[] split = serverResponse.split(" ");
                if(split[0].equals("ERROR:")){
                    errorList.put(myid, split[0]); //what this does
                    // is that it saves the error produced by ping if
                    //a valid client id doesn't exist
                } else{
                    errorList.remove(myid);
                }
                socket.close(); //close
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
                e.printStackTrace();
        }
        //use the ping command as described
    }

}
