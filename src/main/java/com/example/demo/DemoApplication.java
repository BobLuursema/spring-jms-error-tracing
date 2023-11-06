package com.example.demo;

import io.micrometer.observation.ObservationRegistry;
import jakarta.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@SpringBootApplication
@EnableJms
public class DemoApplication {

  Logger logger = LoggerFactory.getLogger(DemoApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @Bean
  public JmsListenerContainerFactory<DefaultMessageListenerContainer> listenerContainer(
      ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer,
      ObservationRegistry observation) {
    var factory = new DefaultJmsListenerContainerFactory();
    configurer.configure(factory, connectionFactory);
    factory.setObservationRegistry(observation);
    factory.setErrorHandler(throwable -> logger.error("Logging in ErrorHandler"));
    return factory;
  }

  @Bean
  public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
    var template = new JmsTemplate();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

}
