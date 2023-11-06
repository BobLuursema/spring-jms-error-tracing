package com.example.demo;

import jakarta.jms.TextMessage;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

@TestConfiguration
public class JmsTestConfig {
  @Bean
  public Receiver receiver() {
    return new Receiver();
  }

  @Bean
  public Publisher publisher(JmsTemplate jmsTemplate) {
    return new Publisher(jmsTemplate);
  }

  static class Receiver {

    Logger logger = LoggerFactory.getLogger(Receiver.class);

    private boolean receivedCall = false;

    boolean containsCall(int timeoutSeconds) throws InterruptedException {
      var timeoutMilliseconds = timeoutSeconds * 1000;
      var startTime = System.currentTimeMillis();
      while (!receivedCall) {
        TimeUnit.MILLISECONDS.sleep(500);
        if (System.currentTimeMillis() > startTime + timeoutMilliseconds) {
          return false;
        }
      }
      return true;
    }

    @JmsListener(destination = "test-receiver", containerFactory = "listenerContainer")
    public void receive(TextMessage message) {
      logger.info("Received call, with traceId!");
      receivedCall = true;
      throw new RuntimeException("I crash!");
    }

  }

  static class Publisher {

    private JmsTemplate jmsTemplate;

    public Publisher(JmsTemplate jmsTemplate) {
      this.jmsTemplate = jmsTemplate;
    }

    public void publish() {
      jmsTemplate.convertAndSend("test-receiver", "Hello world!");
    }
  }
}
