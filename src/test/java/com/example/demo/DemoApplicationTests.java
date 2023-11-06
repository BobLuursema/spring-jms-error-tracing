package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.example.demo.JmsTestConfig.Publisher;
import com.example.demo.JmsTestConfig.Receiver;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureObservability
@ExtendWith(OutputCaptureExtension.class)
@Import(JmsTestConfig.class)
class DemoApplicationTests {

  @Autowired
  private Publisher publisher;

  @Autowired
  private Receiver receiver;

  @Test
  void loggingContainsTraceId(CapturedOutput output) throws InterruptedException {
    publisher.publish();
    receiver.containsCall(5);
    var logging = output.getOut();
    var listenerLog = findLine("Received call, with traceId!", logging);
    assertTrue(hasTracingSet(listenerLog));
    var exceptionLog = findLine("Logging in ErrorHandler", logging);
    assertTrue(hasTracingSet(exceptionLog));
  }

  private boolean hasTracingSet(String logLine) {
    var pattern = Pattern.compile("\\[[\\w\\-]+\\] c\\.example");
    var matcher = pattern.matcher(logLine);
    return matcher.find();
  }

  private String findLine(String regex, String logging) {
    var pattern = Pattern.compile("\r?\n(.*" + regex + ".*)\r?\n");
    var matcher = pattern.matcher(logging);
    if (matcher.find()) {
      return matcher.group(0);
    }
    fail("Regex not found in logging");
    return null;
  }

}
