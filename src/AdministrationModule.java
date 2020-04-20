import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdministrationModule {

    public static void main(String[] args) throws Exception {

        System.out.println("Administration module initialized!");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false); // send ack
            }
        };

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String messageType = null;

        channel.queueDeclare("AdministrationMonitor", false, false, false, null);
        channel.exchangeDeclare("AdministrationBroadcast", BuiltinExchangeType.TOPIC);

        System.out.println("Who do You want to message?");
        System.out.println("- 0 - agencies \\n- 1 - carriers \\n- 2 - both...");
        System.out.println("Type e to exit");
        while (true){

            try {
                messageType = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if("e".equals(messageType)){
                return;
            }

            if("0".equals(messageType)){
                channel.basicPublish("AdministrationBroadcast", "agency.filler", null, "Message from admin".getBytes());
            }
            if("1".equals(messageType)){
                channel.basicPublish("AdministrationBroadcast", "filler.carrier", null, "Message from admin".getBytes());
            }
            if("2".equals(messageType)){
                channel.basicPublish("AdministrationBroadcast", "agency.carrier", null, "Message from admin".getBytes());
            }

            channel.basicConsume("AdministrationMonitor", false, consumer);

        }

    }
}
