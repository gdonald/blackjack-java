package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

class PlayerHandTest {
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  private Shoe shoe;
  private Game game;
  private PlayerHand playerHand;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outputStream));

    game = spy(new Game());
    shoe = spy(new Shoe(game));
    when(game.getCurrentBet()).thenReturn(500);
    when(game.getShoe()).thenReturn(shoe);

    playerHand = spy(new PlayerHand(game));
  }

  @AfterEach
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("New playerHand should initialize with correct default values")
  void testNewHandInitialization() {
    assertEquals(500, playerHand.getBet());
    assertFalse(playerHand.isPaid());
    assertTrue(playerHand.getCards().isEmpty());
  }

  @Test
  @DisplayName("Clone should create deep copy with shared game reference")
  void testClone() {
    playerHand.dealCard();
    playerHand.dealCard();
    PlayerHand cloned = playerHand.clone();

    assertEquals(2, cloned.getCards().size());
    assertEquals(playerHand.getCards(), cloned.getCards());
    assertEquals(playerHand.getBet(), cloned.getBet());
    assertEquals(playerHand.isPaid(), cloned.isPaid());
  }

  @Test
  @DisplayName("isBusted should return false for hands under 21")
  void testIsBustedFalse() {
    when(playerHand.getValue(CountMethod.SOFT)).thenReturn(21);
    assertFalse(playerHand.isBusted());
  }

  @Test
  @DisplayName("isBusted should return true for hands over 21")
  void testIsBustedTrue() {
    when(playerHand.getValue(CountMethod.SOFT)).thenReturn(22);
    assertTrue(playerHand.isBusted());
  }

  @Test
  @DisplayName("toString should format playerHand correctly for normal  playerHand")
  void testToStringNormalHand() {
    ArrayList<PlayerHand> playerHands = new ArrayList<>();
    playerHands.add(playerHand);
    when(game.getPlayerHands()).thenReturn(playerHands);
    when(game.getCurrentHand()).thenReturn(0);

    when(shoe.getNextCard()).thenReturn(
        new Card(9, 0),
        new Card(10, 3));

    playerHand.dealCard();
    playerHand.dealCard();

    when(game.cardFace(any(Integer.class), any(Integer.class))).thenCallRealMethod();

    String result = playerHand.toString();
    assertTrue(result.contains("T♠"));
    assertTrue(result.contains("J♦"));
    assertTrue(result.contains("$5.00"));
    assertTrue(result.contains("⇐"));
  }

  @Test
  @DisplayName("toString should show correct status messages")
  void testToStringStatusMessages() {
    ArrayList<PlayerHand> playerHands = new ArrayList<>();
    playerHands.add(playerHand);
    when(game.getPlayerHands()).thenReturn(playerHands);
    when(game.getCurrentHand()).thenReturn(0);

    when(shoe.getNextCard()).thenReturn(
        new Card(8, 0),
        new Card(8, 1),
        new Card(8, 2));

    playerHand.dealCard();
    playerHand.dealCard();
    playerHand.setStatus(HandStatus.WON);

    String result = playerHand.toString();
    assertTrue(result.contains("+$5.00"));
    assertTrue(result.contains("Win!"));

    playerHand.dealCard();
    playerHand.setStatus(HandStatus.LOST);

    result = playerHand.toString();
    assertTrue(result.contains("-$5.00"));
    assertTrue(result.contains("Busted!"));

    playerHand.setStatus(HandStatus.PUSH);

    result = playerHand.toString();
    assertTrue(result.contains("Push!"));
  }

  @Test
  @DisplayName("setPaid should correctly update paid status")
  void testSetPaid() {
    PlayerHand hand = new PlayerHand(game);

    assertFalse(hand.isPaid());

    hand.setPaid(true);
    assertTrue(hand.isPaid());

    hand.setPaid(false);
    assertFalse(hand.isPaid());
  }

  @Test
  @DisplayName("Menu should show all options when all are available")
  public void testMenuShowsAllOptionsWhenAllAvailable() {
    when(playerHand.canHit()).thenReturn(true);
    when(playerHand.canStand()).thenReturn(true);
    when(playerHand.canSplit()).thenReturn(true);
    when(playerHand.canDbl()).thenReturn(true);

    doNothing().when(playerHand).hit();
    when(game.getChar()).thenReturn('h');

    playerHand.getAction();

    assertEquals(" (H) Hit  (S) Stand  (P) Split  (D) Double\n", outputStream.toString());
  }

  @Test
  @DisplayName("setBet should set the bet value")
  void testSetBet() {
    playerHand.setBet(100);
    assertEquals(100, playerHand.getBet());
  }

  @Nested
  @DisplayName("dbl Tests")
  class DoubleTests {
    @Test
    @DisplayName("dbl should deal card, double bet, and mark as played")
    void testDbl() {
      doNothing().when(playerHand).dealCard();
      doNothing().when(playerHand).process();
      playerHand.setBet(1000);
      playerHand.dbl();

      assertEquals(2000, playerHand.getBet());
      assertTrue(playerHand.played);
    }

    @Test
    @DisplayName("double should process hand if done")
    void testDblProcessesWhenDone() {
      doNothing().when(playerHand).dealCard();
      doReturn(true).when(playerHand).isDone();
      doNothing().when(playerHand).process();
      playerHand.dbl();

      verify(playerHand).process();
    }

    @Test
    @DisplayName("double should not process hand if not done")
    void testDblDoesNotProcessWhenNotDone() {
      doNothing().when(playerHand).dealCard();
      doReturn(false).when(playerHand).isDone();
      playerHand.dbl();

      verify(playerHand, never()).process();
    }
  }

  @Nested
  @DisplayName("Hit Tests")
  class HitTests {
    @Test
    @DisplayName("hit should process and not continue when done")
    void testHitProcessesWhenDone() {
      doNothing().when(playerHand).dealCard();
      doReturn(true).when(playerHand).isDone();
      doNothing().when(playerHand).process();

      playerHand.hit();

      verify(game, never()).drawHands();
      verify(game, never()).getPlayerHands();
    }

    @Test
    @DisplayName("hit should continue to next action when not done")
    void testHitContinuesWhenNotDone() {
      ArrayList<PlayerHand> playerHands = new ArrayList<>();
      playerHands.add(playerHand);
      when(game.getPlayerHands()).thenReturn(playerHands);
      when(game.getCurrentHand()).thenReturn(0);
      doNothing().when(playerHand).dealCard();
      doReturn(false).when(playerHand).isDone();
      doNothing().when(playerHand).getAction();

      playerHand.hit();

      verify(game).drawHands();
      verify(playerHand).getAction();
    }
  }

  @Nested
  @DisplayName("Stand Tests")
  class StandTests {
    @AfterEach
    void tearDown() {
      assertTrue(playerHand.stood);
      assertTrue(playerHand.played);
    }

    @Test
    @DisplayName("stand should mark hand as stood and played")
    void testStandMarksHandStatus() {
      doNothing().when(game).playMoreHands();
      when(game.moreHandsToPlay()).thenReturn(true);

      playerHand.stand();

      verify(game).playMoreHands();
      verify(game, never()).playDealerHand();
      verify(game, never()).drawHands();
      verify(game, never()).betOptions();
    }

    @Test
    @DisplayName("stand and do not play more hands unless required")
    void testStandPlaysMoreHandsWhenAvailable() {
      doNothing().when(game).playDealerHand();
      doNothing().when(game).drawHands();
      doNothing().when(game).betOptions();
      when(game.moreHandsToPlay()).thenReturn(false);

      playerHand.stand();

      verify(game, never()).playMoreHands();
      verify(game).playDealerHand();
      verify(game).drawHands();
      verify(game).betOptions();
    }
  }
}
