package coyote.metrics.prom;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

class PushGatewayClientTest {

  @Test
  void push() {
    Writer writer = new StringWriter();
//    try {
//      PushGatewayClient.write004(writer);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

    System.out.println(writer.toString());
  }
}