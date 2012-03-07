package cz.example;


import org.zeromq.ZMQ;
import org.zeromq.ZMQQueue;

public class Worker {
	public static void main(String[] args) throws InterruptedException {

		//  Prepare our context and sockets
		ZMQ.Context context = ZMQ.context(1);

		//  Socket facing clients
		ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
		subscriber.connect("tcp://localhost:5556");
		subscriber.subscribe("".getBytes());
		
		while (true) {
			String string = new String(subscriber.recv(0)).trim();
			System.out.println("Recieving >>" + string);

		}
	}
}
