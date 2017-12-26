import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Created by lirus on 25/10/17.
 */
public class Sender_2015104 {
    int window=12;
    int next_seq=0;
    int prev_seq=0;
    int count=0;
    static int counter = 0;

    Semaphore s;
    int timeoutVal=2;
    Timer t;
    public void setTimer(boolean val){
        if (t != null)
            t.cancel();
        if (val)
        {
            t = new Timer();
            t.schedule(new Timeout(), timeoutVal);
        }
    }
    public class Timeout extends TimerTask {
        public void run(){
            try{
                s.acquire();	/***** enter CS *****/
                System.out.println("Sender: Timeout!");
                next_seq = prev_seq+1;	// resets nextSeqNum
                s.release();	/***** leave CS *****/
            }
            catch(Exception E){E.printStackTrace();}
        }
    }
    public int getfrom(DatagramPacket packet)
    {
        String s = new String(packet.getData(), packet.getOffset(), packet.getLength());
        return Integer.parseInt(s);
    }
    public byte[] generate(int val)
    {
        byte[] buffer = Integer.toString(val).getBytes();
        return buffer;
    }

    public class SThread extends Thread{
        DatagramSocket sock_s;
        int recv_port;
        InetAddress address;
        int send_port;
        public SThread(DatagramSocket sock,int port1,int port4)
        {
            this.sock_s=sock;
            this.recv_port=port4;
            this.send_port=port1;
        }
        public void run()
        {
            try {
                address = InetAddress.getLocalHost();
                while (count < window) {
                    //System.out.println("Im the counter "+count+" "+ window);
                    /*if(next_seq!=0 && counter==0 && next_seq%3==0) {
                        next_seq++;
                        counter=1;
                        continue;
                    }*/
                    byte[] send_data = new byte[10];
                    s.acquire();
                    send_data = generate(next_seq);
                    sock_s.send(new DatagramPacket(send_data, send_data.length, address, send_port));
                    System.out.println("Sender: Sent seqNum " + next_seq);
                    setTimer(true);
                    next_seq++;
                    s.release();
                    count++;
                    sleep(2);
                }
            }
            catch(Exception E){E.printStackTrace();}
        }
    }

    public class RThread extends Thread{
        DatagramSocket sock_r;
        public RThread(DatagramSocket sock)
        {
            this.sock_r=sock;
        }
        public void run()
        {
            byte[] data = new byte[10];
            DatagramPacket packet=new DatagramPacket(data,data.length);
            try {
                while (true)//time until we should receive packets
                {
                    sock_r.receive(packet);
                    int ack=getfrom(packet);
                    System.out.println("The ack received is "+ack);
                    if(ack<next_seq-1)//duplicate ack
                    {
                        s.acquire();
                        //  System.out.println("im here "+prev_seq +" "+ next_seq);
                        prev_seq=ack;
                        next_seq=ack+1;
                        window/=2;
                        setTimer(false);		// off timer
                        s.release();
                    }
                    else if(ack==next_seq)
                    {
                        s.acquire();
                        next_seq ++;
                        prev_seq=ack;
                        window*=2;
                        setTimer(false);
                        s.release();
                    }

                }
            }
            catch(Exception E){E.printStackTrace();}
        }
    }

    public Sender_2015104(int port1,int port4)
    {
        DatagramSocket send_1,recv_2;
        s=new Semaphore(1);
        try{
            send_1=new DatagramSocket();
            recv_2=new DatagramSocket(port4);
            RThread recv=new RThread(recv_2);
            SThread send=new SThread(send_1,port1,port4);
            send.start();
            recv.start();
        }
        catch(Exception E){E.printStackTrace();}
    }
    public static void main(String[] args) {new Sender_2015104(8000,8888);//8000 pe send and 8888 pe recv
    }
}