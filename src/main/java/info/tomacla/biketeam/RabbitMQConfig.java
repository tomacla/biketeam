package info.tomacla.biketeam;

import info.tomacla.biketeam.common.amqp.DirectBindings;
import info.tomacla.biketeam.common.json.Json;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Autowired
    private Environment env;

    @Bean
    public SimpleRabbitListenerContainerFactory multipleRabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                                       @Qualifier("jsonMessageConverter") MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(Runtime.getRuntime().availableProcessors() * 2);
        factory.setMessageConverter(messageConverter);
        factory.setAutoStartup(env.getProperty("rabbitmq.autostartup", Boolean.class));
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               @Qualifier("jsonMessageConverter") MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setMessageConverter(messageConverter);
        factory.setAutoStartup(env.getProperty("rabbitmq.autostartup", Boolean.class));
        return factory;
    }

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(env.getProperty("rabbitmq.host"));
        factory.setUsername(env.getProperty("rabbitmq.username"));
        factory.setPassword(env.getProperty("rabbitmq.password"));
        factory.setVirtualHost(env.getProperty("rabbitmq.vhost"));
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitMqPublisher(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(Json.objectMapper());
    }

    @Bean
    public AmqpAdmin amqpAdmin() {

        AmqpAdmin admin = new RabbitAdmin(rabbitConnectionFactory());

        for (DirectBindings binding : DirectBindings.values()) {
            final DirectExchange exchange = getExchange(binding.getExchange());
            admin.declareExchange(exchange);

            final Queue queue = getQueue(binding.getQueue());
            admin.declareQueue(queue);

            admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(binding.getRoutingKey()));

        }

        return admin;
    }

    private DirectExchange getExchange(String name) {
        return new DirectExchange(name, true, false, new HashMap<>());
    }

    private FanoutExchange getFanoutExchange(String name) {
        return new FanoutExchange(name, true, false, new HashMap<>());
    }

    private Queue getQueue(String name) {
        return new Queue(name, true, false, false, new HashMap<>());
    }

}
