package cz.example;


import org.zeromq.ZMQ;
import org.zeromq.ZMQQueue;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {

		//  Prepare our context and sockets
		ZMQ.Context context = ZMQ.context(1);

		//  Socket facing clients
		ZMQ.Socket publisher = context.socket(ZMQ.PUB);
		publisher.bind("tcp://*:5556");

		System.out.println("Press Enter when the workers are ready: ");
		System.in.read();

		int task_nbr;

		for (task_nbr = 0; task_nbr < 100; task_nbr++) {
			String string = "i[" + task_nbr + "]: hello!";
			publisher.send(string.getBytes(), 0);
			System.out.println("Sending >>" + string);
		}

		//  We never get here but clean up anyhow
		publisher.close();
		context.term();
	}
}
