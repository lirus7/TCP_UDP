import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by lirus on 25/10/17.
 */
public class Receiver_2015104 {
    public byte[] generate(int val)
    {
        byte[] buffer = Integer.toString(val).getBytes();
        return buffer;
    }
    public int getfrom(DatagramPacket packet)
    {
        String s = new String(packet.getData(), packet.getOffset(), packet.getLength());
        return Integer.parseInt(s);
    }
    public Receiver_2015104(int port2,int port3)
    {
        int count=0;
        int window=10;
        DatagramSocket send_2,recv_1;
        int prev_seq=-1; //initializing
        int next_seq=0;
        byte[] recv_data = new byte[10];
        try{
            DatagramPacket packet = new DatagramPacket(recv_data,recv_data.length);	// incoming packet
            InetAddress address = InetAddress.getLocalHost();
            recv_1=new DatagramSocket(port2);
            send_2=new DatagramSocket();
            System.out.println("listening");
            while(true) {
                {
                    while (count < window)
                    {
                        recv_1.receive(packet);
                        int data = getfrom(packet);
                        System.out.println("The packet received with seq_num " + data);
                        if (data == next_seq) {
                            //generate ack
                            byte[] send_data = new byte[10];
                            send_data = generate(data);
                            send_2.send(new DatagramPacket(send_data, send_data.length, address, port3));
                            System.out.println("Receiver: Sent Ack " + next_seq);
                            next_seq++;            // update nextSeqNum
                            prev_seq = data;
                            count++;
                        } else//some mixed packet
                        {
                            byte[] send_data = new byte[10];
                            send_data = generate(prev_seq);
                            send_2.send(new DatagramPacket(send_data, send_data.length, address, port3));
                            System.out.println("There was a loss so this is the most recent one");
                            System.out.println("Receiver: Sent Ack " + prev_seq);
                        }
                    }
                    count=0;
                    recv_1.receive(packet);
                    int data = getfrom(packet);
                    System.out.println("The packet received with seq_num " + data);
                    byte[] send_data = new byte[10];
                    send_data = generate(prev_seq);
                    send_2.send(new DatagramPacket(send_data, send_data.length, address, port3));
                    System.out.println("There was a loss so this is the most recent one due to flow control");
                    System.out.println("Receiver: Sent Ack " + prev_seq);
                }
            }
        }
        catch(Exception E){E.printStackTrace();}
    }
    public static void main(String[]args) {
        new Receiver_2015104(8000,8888);//8000 pe recv and send at 8888
    }
}