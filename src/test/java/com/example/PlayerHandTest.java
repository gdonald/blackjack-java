package com.example;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    playerHand.dealCards(2);
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
  @DisplayName("setBet should set the bet value")
  void testSetBet() {
    playerHand.setBet(100);
    assertEquals(100, playerHand.getBet());
  }

  @Nested
  @DisplayName("toString Tests")
  class ToStringTests {
    private final ArrayList<PlayerHand> playerHands = new ArrayList<>();

    @BeforeEach
    void setUp() {
      when(game.cardFace(any(Integer.class), any(Integer.class))).thenCallRealMethod();

      playerHands.add(playerHand);
      when(game.getPlayerHands()).thenReturn(playerHands);
      when(game.getCurrentHand()).thenReturn(0);
    }

    @Test
    @DisplayName("toString should format playerHand correctly for normal  playerHand")
    void testToStringNormalHand() {
      when(shoe.getNextCard()).thenReturn(
          new Card(9, 0),
          new Card(10, 3));

      playerHand.dealCards(2);

      String result = playerHand.toString();
      assertTrue(result.contains("T♠"));
      assertTrue(result.contains("J♦"));
      assertTrue(result.contains("$5.00"));
      assertTrue(result.contains("⇐"));
    }

    @Test
    @DisplayName("toString should show correct status messages")
    void testToStringStatusMessages() {
      when(shoe.getNextCard()).thenReturn(
          new Card(8, 0),
          new Card(8, 1),
          new Card(8, 2));

      playerHand.dealCards(2);
      playerHand.setStatus(HandStatus.WON);

      String result = playerHand.toString();
      assertTrue(result.contains("+$5.00"));
      assertTrue(result.contains("Win!"));

      playerHand.setStatus(HandStatus.LOST);

      result = playerHand.toString();
      assertTrue(result.contains("-$5.00"));
      assertTrue(result.contains("Lose!"));

      playerHand.dealCard();
      result = playerHand.toString();
      assertTrue(result.contains("Busted!"));

      playerHand.setStatus(HandStatus.PUSH);

      result = playerHand.toString();
      assertTrue(result.contains("Push!"));
    }

    @Test
    @DisplayName("toString should show correct Blackjack message")
    void testToStringBlackjackMessage() {
      when(shoe.getNextCard()).thenReturn(
          new Card(0, 0),
          new Card(9, 1));

      playerHand.dealCards(2);
      playerHand.setStatus(HandStatus.WON);

      String result = playerHand.toString();
      assertTrue(result.contains("Blackjack!"));
    }

    @Test
    @DisplayName("toString should not show current hand indicator for a played hand")
    void testToStringPlayedIsNotCurrentHand() {
      when(shoe.getNextCard()).thenReturn(
          new Card(9, 0),
          new Card(10, 3));

      playerHand.dealCards(2);

      PlayerHand otherHand = spy(new PlayerHand(game));
      playerHands.add(otherHand);
      when(game.getPlayerHands()).thenReturn(playerHands);
      when(game.getCurrentHand()).thenReturn(1);

      String result = playerHand.toString();
      assertFalse(result.contains("⇐"));
    }

    @Test
    @DisplayName("toString should not show current hand indicator for a non-current hand")
    void testToStringIsNotCurrentHand() {
      when(shoe.getNextCard()).thenReturn(
          new Card(9, 0),
          new Card(10, 3));

      playerHand.dealCards(2);

      playerHand.played = true;

      String result = playerHand.toString();
      assertFalse(result.contains("⇐"));
    }
  }

  @Nested
  @DisplayName("getAction Tests")
  class GetActionTests {
    @Test
    @DisplayName("show all options")
    public void testMenuShowsAllOptionsWhenAllAvailable() {
      when(playerHand.canSplit()).thenReturn(true);
      when(playerHand.canDbl()).thenReturn(true);

      doNothing().when(playerHand).hit();
      when(game.getChar()).thenReturn('h');

      playerHand.getAction();

      assertEquals(" (H) Hit  (S) Stand  (P) Split  (D) Double\n", outputStream.toString());
    }

    @Test
    @DisplayName("cannot dbl or split")
    public void testMenuShowsCannotDblOrSplit() {
      when(playerHand.canSplit()).thenReturn(false);
      when(playerHand.canDbl()).thenReturn(false);

      doNothing().when(playerHand).stand();
      when(game.getChar()).thenReturn('s');

      playerHand.getAction();
      assertEquals(" (H) Hit  (S) Stand  \n", outputStream.toString());

      verify(playerHand).stand();
    }

    @Test
    @DisplayName("hand can be split")
    public void testHandCanBeSplit() {
      when(playerHand.canSplit()).thenReturn(true);
      doNothing().when(game).splitCurrentHand();
      when(game.getChar()).thenReturn('p');

      playerHand.getAction();

      verify(game).splitCurrentHand();
    }

    @Test
    @DisplayName("try to split hand that cannot split")
    public void testTryToSplitHandThatCannotSplit() {
      doNothing().when(game).splitCurrentHand();
      doNothing().when(playerHand).stand();
      when(game.getChar()).thenReturn('p', 's');

      playerHand.getAction();

      verify(game, never()).splitCurrentHand();
    }

    @Test
    @DisplayName("hand can dbl")
    public void testHandCanDbl() {
      when(playerHand.canDbl()).thenReturn(true);
      doNothing().when(playerHand).dbl();
      when(game.getChar()).thenReturn('d');

      playerHand.getAction();

      verify(playerHand).dbl();
    }

    @Test
    @DisplayName("try to dbl hand that cannot dbl")
    public void testTryToSplitHandThatCannotDbl() {
      doNothing().when(playerHand).dbl();
      doNothing().when(playerHand).stand();
      when(game.getChar()).thenReturn('d', 's');

      playerHand.getAction();

      verify(playerHand, never()).dbl();
    }

    @Test
    @DisplayName("handle invalid input")
    public void testHandleInvalidInput() {
      doNothing().when(playerHand).stand();
      when(game.getChar()).thenReturn('x', 's');

      playerHand.getAction();

      verify(playerHand).stand();
    }
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

  @Nested
  @DisplayName("process Tests")
  class ProcessTests {
    @Test
    @DisplayName("when more hands to play plays more hands")
    void testPlaysMoreHands() {
      when(game.moreHandsToPlay()).thenReturn(true);
      doNothing().when(game).playMoreHands();
      playerHand.process();

      verify(game).playMoreHands();
    }

    @Test
    @DisplayName("when no more hands to play does not play more hands")
    void testNoMoreHandsToPlay() {
      doNothing().when(game).playDealerHand();
      doNothing().when(game).drawHands();
      doNothing().when(game).betOptions();
      when(game.moreHandsToPlay()).thenReturn(false);
      playerHand.process();

      verify(game, never()).playMoreHands();
    }
  }

  @Nested
  @DisplayName("canDbl Tests")
  class CanDblTests {
    @Test
    @DisplayName("canDbl returns false when player cannot cover bet")
    void testCanDblCannotCoverBet() {
      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(3, 0));
      playerHand.dealCards(2);

      game.setMoney(0);

      assertFalse(playerHand.canDbl());
    }

    @Test
    @DisplayName("canDbl should return true when all conditions are met")
    void testCanDblReturnsTrue() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(3, 0));
      playerHand.dealCards(2);

      assertTrue(playerHand.canDbl());
    }

    @Test
    @DisplayName("canDbl returns false when already stood")
    void testCanDblFalseWhenAlreadyStood() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(3, 0));
      playerHand.dealCards(2);

      when(game.moreHandsToPlay()).thenReturn(false);
      doNothing().when(game).playDealerHand();
      doNothing().when(game).drawHands();
      doNothing().when(game).betOptions();

      playerHand.stand();

      assertFalse(playerHand.canDbl());
    }

    @Test
    @DisplayName("canDbl returns false when hand is blackjack")
    void testCanDblReturnsFalseWhenBlackjack() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(0, 0),
          new Card(9, 0));
      playerHand.dealCards(2);

      assertFalse(playerHand.canDbl());
    }
  }

  @Nested
  @DisplayName("canSplit Tests")
  class CanSplitTests {
    @Test
    @DisplayName("canSplit returns false when player cannot cover bet")
    void testCanSplitCannotCoverBet() {
      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(2, 0));
      playerHand.dealCards(2);

      game.setMoney(0);

      assertFalse(playerHand.canSplit());
    }

    @Test
    @DisplayName("canSplit returns false when hand is not a pair")
    void testCanSplitReturnsFalse() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(3, 0));
      playerHand.dealCards(2);

      assertFalse(playerHand.canSplit());
    }

    @Test
    @DisplayName("canSplit returns false when already stood")
    void testCanSplitFalseWhenAlreadyStood() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(2, 1));
      playerHand.dealCards(2);

      when(game.moreHandsToPlay()).thenReturn(false);
      doNothing().when(game).playDealerHand();
      doNothing().when(game).drawHands();
      doNothing().when(game).betOptions();

      playerHand.stand();

      assertFalse(playerHand.canSplit());
    }

    @Test
    @DisplayName("canSplit returns true when all conditions are met")
    void testCanSplitReturnsTrue() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(2, 1));
      playerHand.dealCards(2);

      assertTrue(playerHand.canSplit());
    }

    @Test
    @DisplayName("canSplit returns false when too many hands")
    void testCanSplitReturnsFalseWithTooManyHands() {
      when(game.getMoney()).thenReturn(10000);
      when(game.allBets()).thenReturn(1000);
      playerHand.setBet(500);

      when(shoe.getNextCard()).thenReturn(
          new Card(2, 0),
          new Card(2, 1));
      playerHand.dealCards(2);

      for (int i = 0; i < Game.MAX_PLAYER_HANDS; i++) {
        game.getPlayerHands().add(new PlayerHand(game));
      }

      assertFalse(playerHand.canSplit());
    }
  }
}
