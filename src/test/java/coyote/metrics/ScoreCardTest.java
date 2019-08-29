package coyote.metrics;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScoreCardTest {

  @Test
   void getHostname(){
    String hostname = ScoreCard.getHostname();
    assertNotNull(hostname);
  }

  @Test
  void getId() {
    String id = ScoreCard.getId();
    assertNotNull(id);
  }

  @Test
  void setId() {
    ScoreCard.setId("123");
    assertEquals("123", ScoreCard.getId());
  }

  @Test
  void getUptimeString() {
    String uptime = ScoreCard.getUptimeString();
    assertNotNull(uptime);
  }

  @Test
  void getLocalQualifiedHostName() {
    String qhostname = ScoreCard.getQualifiedHostName(ScoreCard.getHostIpAddress());
    assertNotNull(qhostname);
  }


  @Test
  void getHostIpAddress() {
    InetAddress address = ScoreCard.getHostIpAddress();
    assertNotNull(address);
  }
}
