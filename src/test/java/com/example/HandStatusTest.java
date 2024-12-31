package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class HandStatusTest {

  @Test
  @DisplayName("Enum should have exactly four values")
  void testEnumSize() {
    assertEquals(4, HandStatus.values().length);
  }

  @Test
  @DisplayName("UNKNOWN value should exist")
  void testUnknownExists() {
    assertNotNull(HandStatus.UNKNOWN);
    assertEquals("UNKNOWN", HandStatus.UNKNOWN.name());
  }

  @Test
  @DisplayName("WON value should exist")
  void testWonExists() {
    assertNotNull(HandStatus.WON);
    assertEquals("WON", HandStatus.WON.name());
  }

  @Test
  @DisplayName("LOST value should exist")
  void testLostExists() {
    assertNotNull(HandStatus.LOST);
    assertEquals("LOST", HandStatus.LOST.name());
  }

  @Test
  @DisplayName("PUSH value should exist")
  void testPushExists() {
    assertNotNull(HandStatus.PUSH);
    assertEquals("PUSH", HandStatus.PUSH.name());
  }

  @Test
  @DisplayName("valueOf should return correct enum constants")
  void testValueOf() {
    assertEquals(HandStatus.UNKNOWN, HandStatus.valueOf("UNKNOWN"));
    assertEquals(HandStatus.WON, HandStatus.valueOf("WON"));
    assertEquals(HandStatus.LOST, HandStatus.valueOf("LOST"));
    assertEquals(HandStatus.PUSH, HandStatus.valueOf("PUSH"));
  }

  @Test
  @DisplayName("valueOf should throw IllegalArgumentException for invalid values")
  void testValueOfInvalid() {
    assertThrows(IllegalArgumentException.class, () -> HandStatus.valueOf("INVALID"));
  }

  @Test
  @DisplayName("Enum constants should maintain order")
  void testEnumOrder() {
    HandStatus[] values = HandStatus.values();
    assertEquals(HandStatus.UNKNOWN, values[0]);
    assertEquals(HandStatus.WON, values[1]);
    assertEquals(HandStatus.LOST, values[2]);
    assertEquals(HandStatus.PUSH, values[3]);
  }
}
