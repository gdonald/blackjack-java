package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class MainTest {
  @Test
  @DisplayName("Test main constructor")
  @SuppressWarnings("InstantiationOfUtilityClass")
  void testMainConstructor() {
    new Main();
  }

  @Test
  @DisplayName("Run game")
  void testRunGame() {
    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      Main.main(new String[]{});

      gameMock.verify(
          Game::run,
          times(1)
      );
    }
  }
}
