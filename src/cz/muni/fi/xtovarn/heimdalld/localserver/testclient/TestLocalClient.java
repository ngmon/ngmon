package cz.muni.fi.xtovarn.heimdalld.localserver.testclient;

import org.codehaus.jackson.map.util.ISO8601Utils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;

import java.io.IOException;
import java.util.Date;

public class TestLocalClient {

	public static void main(String[] args) {

		ChannelFactory factory = new DefaultLocalClientChannelFactory();

		ClientBootstrap bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new LocalClientPipelineFactory());

		Channel channel = bootstrap.connect(new LocalAddress("/dev/eventlog")).getChannel(); // fake address

		System.out.println("Press enter to continue...");
		try {System.in.read();} catch (IOException e){}
		System.out.println("Reading!");

		int i = 1000;
		while (i > 1) {
			i--;
			String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"proc_cron NAme\",\"processId\":\"id005\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":null,\"schemaVersion\":null,\"value\":4648,\"value2\":\"aax4x46aeEF\"}}}";
			channel.write(json);
		}
	}

}
