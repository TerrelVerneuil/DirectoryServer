import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DirectoryServer {
    //A directory server keeps track of clients logged into a multiuser system. Its purpose is to allow clients to
    // look up other clients that are logged on so a direct connection can be made between the two clients.
    // The server does not make the connections between clients,
    // it just provides information about each client. This server will listen on port 54321.
    public static final int PORT = 54321; //server listens on port 54321
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
    static long timeout = 10000;
    static TimerTask TTL = null;
    static Timer timer = new Timer();
    //we should handle the commands as switch -  cases
    public static void main(String args[]) {


        Map<Integer, String> clientMap = new HashMap<>(); //mapping client aliases to ID's
        Map<Integer, Integer> clientPinfo = new HashMap<>(); //client port info
        Map<Integer, InetAddress> clientIPinfo = new HashMap<>(); //client port info
        Map<Integer, Long> TTLinfo = new HashMap<>();
        Map<Integer, TimerTask> tasks = new HashMap<>();
        ArrayList<Integer> usedIDs = new ArrayList<Integer>(); //stores the ID's of the "CONNECTED" clients

        try( ServerSocket server = new ServerSocket(PORT)) { //link server to the port

            while (true) {
                Socket clientSocket = server.accept(); //accept connections while server is not timeout
                try(PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream()); //communicate
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    int clientID = 0;
                    String clientInput = inFromClient.readLine(); //get client command
                    String[] ClientArgs = clientInput.trim().split("\\s+"); //split up the arguments from client command
                    int finalClientID2 = clientID;
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (clientMap) {
                                Long expirationTime = TTLinfo.get(finalClientID2);
                                if (expirationTime != null && System.currentTimeMillis() - expirationTime >= timeout) {
                                    usedIDs.remove(Integer.valueOf(finalClientID2));
                                    clientMap.remove(Integer.parseInt(ClientArgs[1]));
                                    clientPinfo.remove(Integer.parseInt(ClientArgs[1]));
                                    clientIPinfo.remove(Integer.parseInt(ClientArgs[1]));
                                }
                            }
                        }
                    };
                    try {
                        switch (ClientArgs[0].toUpperCase()) { //this first arg should be the command
                            case "LOGON": //if client arg is LOGON "IT REQUIRES 2 args for [1] and [2]
                                //store the tasks the timer uses
                                Random rand = new Random();
                                //parse the 1st argument
                                if (ClientArgs.length < 3) { //if there is no argument or excess arguments
                                    outToClient.println("ERROR: Invalid Arguments. - Correct usage LOGON-port-clientName");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                } else {
                                    try {
                                        int clientPort = Integer.parseInt(ClientArgs[1]); //if this cant be done then notify the users input is invalid
                                    } catch (NumberFormatException e) {
                                        outToClient.println("ERROR: PORT NEEDS TO BE AN INTEGER");
                                        outToClient.flush();
                                        clientSocket.close();
                                        break;
                                    }
                                    try {

                                        if (ClientArgs.length > 3) {
                                            outToClient.println("ERROR: TOO MANY ARGs");
                                            outToClient.flush();
                                            clientSocket.close();
                                            break;
                                        } else {
                                            String clientAlias = ClientArgs[2]; //store alias for user
                                            if (clientMap.containsValue(clientAlias)) { //client is already in the scope
                                                outToClient.println("ERROR: THIS ALIAS EXISTS, CHOOSE ANOTHER");
                                                outToClient.flush();
                                                clientSocket.close();
                                                break;
                                            } else {


                                                int ClientPORT = Integer.parseInt(ClientArgs[1]);
                                                InetAddress ClientIP = clientSocket.getInetAddress(); //IP ADDRESS OF THE CLIENT
                                                clientID = rand.nextInt(10000 - 1) + 1;
                                                usedIDs.add(clientID);
                                                clientMap.put(clientID, clientAlias);//client alias is the key
                                                clientPinfo.put(clientID, ClientPORT);//client alias is the key
                                                clientIPinfo.put(clientID, ClientIP);
                                                TTLinfo.put(clientID, timeout);
                                                //implement an expiration time for the client
                                                int finalClientID1 = clientID;

                                              timer.schedule(timerTask,timeout);

                                            }//schedule new timer task under a new timeout

                                            outToClient.println("ADDED:" + clientID + ":" + (timeout / 1000) + ":\n"); //return the ttl and client alias to user
                                            outToClient.flush();
                                            clientSocket.close();
                                            break;
                                        }
                                    } catch (Exception e) {
                                        outToClient.println("ERROR: " + e.getMessage());
                                        outToClient.flush();
                                    }
                                }
                                clientSocket.close();

                                //one prerequsite is that it increments the client number so
                                //that a new client ID is assigned everytime a new client is found
                            case "PING":
                                if (ClientArgs.length != 2) { //what it should not be so we know what it should do
                                    outToClient.println("ERROR: DOES NOT CONTAIN valid arguments. - Correct usage PING-id");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }
                                try {
                                    int pingClientID = Integer.parseInt(ClientArgs[1]); //if this cant be done then notify the users input is invalid
                                } catch (NumberFormatException e) {
                                    outToClient.println("ERROR A CLIENT ID IS EXCLUSIVELY AN INTEGER.");
                                    outToClient.flush();
                                    clientSocket.close();
                                }
                                if (!clientMap.containsKey(Integer.parseInt(ClientArgs[1]))) {
                                    outToClient.println("ERROR: CLIENT ID DOES NOT EXIST.");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                } else {
//
                                    int finalClientID = Integer.parseInt(ClientArgs[1]);
                                    int newTimeout = 20000;
                                    timer.cancel();
                                    timer = new Timer(); //reinitialize
                                    TimerTask newTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            synchronized (clientMap) {
                                                Long expirationTime = TTLinfo.get(finalClientID);
                                                if (expirationTime != null && System.currentTimeMillis() - expirationTime >= timeout) {
                                                    usedIDs.remove(Integer.valueOf(finalClientID));
                                                    clientMap.remove(Integer.parseInt(ClientArgs[1]));
                                                    clientPinfo.remove(Integer.parseInt(ClientArgs[1]));
                                                    clientIPinfo.remove(Integer.parseInt(ClientArgs[1]));
                                                }
                                            }
                                        }
                                    };

                                    timer.schedule(newTask, newTimeout);

                                        TTLinfo.put(clientID, System.currentTimeMillis());
                                        outToClient.println("PONG: " + newTimeout / 1000 + "/seconds");
                                        outToClient.flush();
                                        clientSocket.close();
                                        break;



                                }

                            case "LIST":
                                if (ClientArgs[1].equals(" ") || ClientArgs.length > 2) { //what it should not be so we know what it should do
                                    outToClient.println("ERROR: DOES NOT CONTAIN valid arguments. LIST-id");
                                    clientSocket.close();
                                }
                                try {
                                    int pingClientID = Integer.parseInt(ClientArgs[1]); //if this cant be done then notify the users input is invalid
                                } catch (NumberFormatException e) {
                                    outToClient.println("ERROR A CLIENT ID IS EXCLUSIVELY AN INTEGER.");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }
                                if (!clientMap.containsKey(Integer.parseInt(ClientArgs[1]))) {
                                    outToClient.println("ERROR: INVALID CLIENT ID");

                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }
                                if (clientMap.containsKey(Integer.parseInt(ClientArgs[1]))) {
                                    outToClient.println("LIST: " + clientMap.size());
                                    for (int i = 0; i < usedIDs.size(); i++) {
                                        outToClient.println(usedIDs.get(i) + " " + clientMap.get(usedIDs.get(i))
                                                + " " + clientIPinfo.get(usedIDs.get(i)) + " " + clientPinfo.get(usedIDs.get(i)));
                                        outToClient.flush();
                                    }

                                    outToClient.flush();
                                    break;
                                } else {
                                    outToClient.println("INVALID CLIENT ID");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }

                                //   outToClient.println("> "); //shows user when to input
                                //   outToClient.flush();
                            case "LOGOFF":
                                //if this doesn't work return an error message
                                try {

                                    int pingClientID = Integer.parseInt(ClientArgs[1]); //if this cant be done then notify the users input is invalid
                                } catch (NumberFormatException e) {
                                    outToClient.println("ERROR A CLIENT ID IS EXCLUSIVELY AN INTEGER.");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }
                                if (ClientArgs.length != 2 || !clientMap.containsKey(Integer.parseInt(ClientArgs[1]))) { //if logoff doesn't include the client ID then we cant really disconnect it so return a written error code
                                    outToClient.println("ERROR: NO ID BY SUCH SPECIFICATION");
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }
                                if (clientMap.containsKey(Integer.parseInt(ClientArgs[1]))) {
                                    //there is an ID
                                    usedIDs.remove(Integer.valueOf(ClientArgs[1]));
                                    clientPinfo.remove(Integer.parseInt(ClientArgs[1]));//client alias is the key
                                    clientIPinfo.remove(Integer.parseInt(ClientArgs[1]));
                                    clientMap.remove(Integer.parseInt(ClientArgs[1]));

                                    outToClient.println("DONE " + ClientArgs[1]);
                                    outToClient.flush();
                                    clientSocket.close();
                                    break;
                                }
                        }

                    } catch (Exception e) {
                        outToClient.println("NOT A VALID COMMAND");
                        clientSocket.close();
                    }
                }catch (Exception e){

                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        //prompt user for an alias and store it


    }
}
