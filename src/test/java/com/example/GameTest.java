package com.example;

import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
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
  @DisplayName("clear tests")
  void testClear() {
    game.clear();
    assertEquals("\033[H\033[2J", outputStream.toString());
  }

  @Nested
  @DisplayName("getChar Tests")
  class GetCharTests {
    @Test
    @DisplayName("getChar should return the first character of the input")
    void testGetChar() throws IOException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader reader = spy(bufferedReader);

      when(game.getReader()).thenReturn(reader);
      when(reader.read()).thenReturn(97);

      assertEquals('a', game.getChar());
    }

    @Test
    @DisplayName("getChar can throw an IOException")
    void testGetCharIOException() throws IOException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader reader = spy(bufferedReader);

      when(game.getReader()).thenReturn(reader);
      when(reader.read()).thenThrow(new IOException("Read failed"));

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> game.getChar());

      assertEquals("Error reading input: Read failed", exception.getMessage());
      assertInstanceOf(IOException.class, exception.getCause());
      verify(reader).read();
    }
  }

  @Nested
  @DisplayName("loop Tests")
  class LoopTests {
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
