
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DirectoryTest extends DirectoryClientConnector{
    public DirectoryTest(String serverName, int port, String name) {
        super(serverName, port, name);
    }
    static DirectoryClientConnector client1 = new DirectoryClientConnector("127.0.0.1", 54321, "s2s22s"); //used to test logoff
    static DirectoryClientConnector client2 = new DirectoryClientConnector("127.0.0.1", 54321, "s22s2s"); //logon test
    static DirectoryClientConnector client3 = new DirectoryClientConnector("127.0.0.1", 54321, "s2s52s"); //logoff test
    static DirectoryClientConnector client4 = new DirectoryClientConnector("127.0.0.1", 54321, "s2s42s"); //logon test
    static DirectoryClientConnector client5 = new DirectoryClientConnector("127.0.0.1", 54321, "s2s62s");  //logon test
    static DirectoryClientConnector client6 = new DirectoryClientConnector("127.0.0.1", 54321, "s2s72s");
    //duplicate logons with the same alias

    static ArrayList<DirectoryClientConnector> holder = new ArrayList<>();
    public static void logonTest() throws Exception {
        holder.add(client1);
        holder.add(client2);
        holder.add(client3);
        holder.add(client4);
        holder.add(client5);
        holder.add(client6);

        for(int i = 0; i < holder.size(); i++){
            holder.get(i).logon();
        }

    }

    public static void logofftest() throws Exception {
        for(int i = 0; i < 2; i++){
            holder.get(i).logoff();
        }
    }
    public static void main(String args[]){


        try{
            logonTest();
            logofftest();

            System.out.println(client4.getList()); //we can test it this way
            System.out.println(client1.getList()); //testing non existant client ids
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}