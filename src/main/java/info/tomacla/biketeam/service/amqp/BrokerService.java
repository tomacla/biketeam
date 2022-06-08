package info.tomacla.biketeam.service.amqp;

import info.tomacla.biketeam.common.json.Json;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrokerService {

    @Autowired
    private RabbitTemplate rabbitMqPublisher;

    public void sendToBroker(String exchange, String routingKey) {
        this.sendToBroker(exchange, routingKey, "{}".getBytes());
    }

    public void sendToBroker(String exchange, String routingKey, Object message) {
        this.sendToBroker(exchange, routingKey, Json.serialize(message).getBytes());
    }

    private synchronized void sendToBroker(String exchange, String routingKey, byte[] message) {

        Message jsonMessage = MessageBuilder.withBody(message)
                .andProperties(MessagePropertiesBuilder.newInstance().setContentType("application/json")
                        .build()).build();

        rabbitMqPublisher.send(exchange, routingKey, jsonMessage);

    }

}
