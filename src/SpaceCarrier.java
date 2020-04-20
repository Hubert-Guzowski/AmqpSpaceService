import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpaceCarrier {
    private static List<String> serviceTypes;

    public static void main(String[] args) throws Exception{

        System.out.println("Space carrier service started!");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);

        System.out.println("Which services should we provide? Enter two separated with \",\"");
        System.out.println("- 0 - people \n- 1 - load \n- 2 - satellite...");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            serviceTypes = Stream.of(br.readLine().
                    split(",")).limit(2).
                    map(num -> Services.getName(Integer.valueOf(num))).
                    collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (serviceTypes.size() != 2) {
            System.err.println("Incorrect number of services given");
            System.exit(1);
        }

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String replyName = message.split(" ")[0];
                System.out.println("Received: " + message);
                channel.queueDeclare(replyName, false, false, false, null);
                channel.basicPublish("", replyName, null, "Request handled".getBytes());
                channel.basicPublish("", "AdministrationMonitor", null, "Request handled".getBytes());
                channel.basicAck(envelope.getDeliveryTag(), false); // send ack
            }
        };

        Consumer noReplyConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false); // send ack
            }
        };

        // declare queues
        serviceTypes.forEach(queueName -> {
            try {
                System.out.println(queueName);
                channel.queueDeclare(queueName, false, false, false, null);
                channel.basicConsume(queueName, false, consumer);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        String queueName = channel.queueDeclare().getQueue();
        channel.exchangeDeclare("AdministrationBroadcast", BuiltinExchangeType.TOPIC);
        channel.queueBind(queueName, "AdministrationBroadcast", "*.carrier");
        channel.basicConsume(queueName, false, noReplyConsumer);

        channel.queueDeclare("AdministrationMonitor", false, false, false, null);

        System.out.println("Waiting for jobs...");

    }
}
