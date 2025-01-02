package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;

class PlayerHandTest {
  private Shoe mockShoe;
  private Game mockGame;
  private PlayerHand playerHand;

  @BeforeEach
  void setUp() {
    mockShoe = mock(Shoe.class);

    mockGame = mock(Game.class);
    when(mockGame.getShoe()).thenReturn(mockShoe);
    when(mockGame.getCurrentBet()).thenReturn(100);
    playerHand = new PlayerHand(mockGame);
  }

  @Test
  @DisplayName("New playerHand should initialize with correct default values")
  void testNewHandInitialization() {
    assertEquals(100, playerHand.getBet());
    assertFalse(playerHand.isPaid());
    assertTrue(playerHand.getCards().isEmpty());
  }

  @Test
  @DisplayName("Clone should create deep copy with shared game reference")
  void testClone() {
    playerHand.dealCard();
    playerHand.dealCard();

    PlayerHand cloned = playerHand.clone();

    assertEquals(playerHand.getCards().size(), cloned.getCards().size());
    assertEquals(playerHand.getCards(), cloned.getCards());
    assertEquals(playerHand.getBet(), cloned.getBet());
    assertEquals(playerHand.isPaid(), cloned.isPaid());
  }

  @Test
  @DisplayName("isBusted should correctly identify bust hands")
  void testIsBusted() {
    when(mockShoe.getNextCard()).thenReturn(
        new Card(7, 0),
        new Card(7, 1),
        new Card(7, 2));

    playerHand.dealCard();
    playerHand.dealCard();
    assertFalse(playerHand.isBusted());

    playerHand.dealCard();
    assertTrue(playerHand.isBusted());
  }

  @Test
  @DisplayName("toString should format playerHand correctly for normal playerHand")
  void testToStringNormalHand() {
    ArrayList<PlayerHand> playerHands = new ArrayList<>();
    playerHands.add(playerHand);
    when(mockGame.getPlayerHands()).thenReturn(playerHands);
    when(mockGame.getCurrentHand()).thenReturn(0);

    when(mockShoe.getNextCard()).thenReturn(
        new Card(9, 0),
        new Card(10, 3));

    playerHand.dealCard();
    playerHand.dealCard();

    when(mockGame.cardFace(any(Integer.class), any(Integer.class))).thenCallRealMethod();

    String result = playerHand.toString();
    System.out.println(result);
    assertTrue(result.contains("T♠"));
    assertTrue(result.contains("J♦"));
    assertTrue(result.contains("$1.00"));
    assertTrue(result.contains("⇐"));
  }

  @Test
  @DisplayName("toString should show correct status messages")
  void testToStringStatusMessages() {
    ArrayList<PlayerHand> playerHands = new ArrayList<>();
    playerHands.add(playerHand);
    when(mockGame.getPlayerHands()).thenReturn(playerHands);
    when(mockGame.getCurrentHand()).thenReturn(0);

    when(mockShoe.getNextCard()).thenReturn(
        new Card(8, 0),
        new Card(8, 1),
        new Card(8, 2));

    playerHand.dealCard();
    playerHand.dealCard();

    playerHand.setBet(500);
    playerHand.setStatus(HandStatus.WON);

    when(mockGame.cardFace(any(Integer.class), any(Integer.class))).thenCallRealMethod();

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
    PlayerHand hand = new PlayerHand(mockGame);

    assertFalse(hand.isPaid());

    hand.setPaid(true);
    assertTrue(hand.isPaid());

    hand.setPaid(false);
    assertFalse(hand.isPaid());
  }
}