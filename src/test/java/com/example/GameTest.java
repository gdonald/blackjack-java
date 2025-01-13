package com.example;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;

public class GameTest {
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  private Game game;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outputStream));
    game = spy(new Game());
  }

  @AfterEach
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("clear tests")
  void testClear() {
    game.clear();
    assertEquals("\033[H\033[2J", outputStream.toString());
  }

  @Nested
  @DisplayName("loop Tests")
  class LoopTests {
    @SuppressWarnings("SameParameterValue")
    private void setField(Object target, String fieldName, Object value) {
      try {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Test
    @DisplayName("loop should run when not quitting")
    void testLoop() {
      AtomicInteger count = new AtomicInteger(0);

      willAnswer(invocation -> {
        if (count.incrementAndGet() >= 2) { // Break after 3 calls
          setField(game, "quitting", true);
        }

        return null;
      }).given(game).dealNewHand();

      game.loop();

      verify(game, times(2)).dealNewHand();
    }
  }
}
