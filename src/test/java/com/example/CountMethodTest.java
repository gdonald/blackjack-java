package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class CountMethodTest {

  @Test
  @DisplayName("Enum should have exactly two values")
  void testEnumSize() {
    assertEquals(2, CountMethod.values().length);
  }

  @Test
  @DisplayName("SOFT value should exist")
  void testSoftExists() {
    assertNotNull(CountMethod.SOFT);
    assertEquals("SOFT", CountMethod.SOFT.name());
  }

  @Test
  @DisplayName("HARD value should exist")
  void testHardExists() {
    assertNotNull(CountMethod.HARD);
    assertEquals("HARD", CountMethod.HARD.name());
  }

  @Test
  @DisplayName("valueOf should return correct enum constants")
  void testValueOf() {
    assertEquals(CountMethod.SOFT, CountMethod.valueOf("SOFT"));
    assertEquals(CountMethod.HARD, CountMethod.valueOf("HARD"));
  }

  @Test
  @DisplayName("valueOf should throw IllegalArgumentException for invalid values")
  void testValueOfInvalid() {
    assertThrows(IllegalArgumentException.class, () -> CountMethod.valueOf("INVALID"));
  }

  @Test
  @DisplayName("Enum constants should maintain order")
  void testEnumOrder() {
    CountMethod[] values = CountMethod.values();
    assertEquals(CountMethod.SOFT, values[0]);
    assertEquals(CountMethod.HARD, values[1]);
  }
}
