import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SpaceAgency {

    private static String agencyName;
    private static int currentID = 0;

    public static void main(String[] args) throws Exception{

        System.out.println("Space agency starting!");

        System.out.println("Give me a name: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            agencyName = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false); // send ack
            }
        };

        channel.queueDeclare(agencyName, false, false, false, null);

        channel.exchangeDeclare("AdministrationBroadcast", BuiltinExchangeType.TOPIC);
        channel.queueBind(agencyName, "AdministrationBroadcast", "agency.*");

        channel.queueDeclare("AdministrationMonitor", false, false, false, null);

        System.out.println("What type of mission do You want to launch? \n- 0 - people \n- 1 - load \n- 2 - satellite...");
        System.out.println("Type e to exit");
        String messageType;

        while(true){
            messageType = br.readLine();
            if("e".equals(messageType))
                return;

            String queueName = Services.getName(Integer.parseInt(messageType));

            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicPublish("", queueName, null, (agencyName + " " + currentID).getBytes());
            channel.basicPublish("", "AdministrationMonitor", null, (agencyName + " " + currentID).getBytes());
            currentID += 1;

            channel.basicConsume(agencyName, false, consumer);

        }

    }
}
