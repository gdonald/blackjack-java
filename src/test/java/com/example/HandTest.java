package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class HandTest {
  private Game mockGame;
  private Hand hand;

  @BeforeEach
  void setUp() {
    mockGame = Mockito.mock(Game.class);
    hand = new Hand(mockGame);
  }

  @Test
  @DisplayName("New hand should be initialized with correct default values")
  void testInitialization() {
    assertNotNull(hand.cards);
    assertTrue(hand.cards.isEmpty());
    assertFalse(hand.stood);
    assertFalse(hand.played);
    assertEquals(mockGame, hand.game);
  }

  @Test
  @DisplayName("clone() should create deep copy")
  void testClone() {
    Card card = new Card(0, 0);
    hand.cards.add(card);
    hand.stood = true;
    hand.played = true;

    Hand cloned = hand.clone();

    assertEquals(hand.cards.size(), cloned.cards.size());
    assertEquals(hand.cards.get(0).value(), cloned.cards.get(0).value());
    assertEquals(hand.cards.get(0).suit(), cloned.cards.get(0).suit());
    assertEquals(hand.stood, cloned.stood);
    assertEquals(hand.played, cloned.played);
    assertEquals(hand.game, cloned.game);

    assertNotSame(hand.cards, cloned.cards);
  }

  @Test
  @DisplayName("clone() should throw AssertionError if CloneNotSupportedException occurs")
  void testCloneError() {
    Game mockGame = Mockito.mock(Game.class);
    Hand hand = Mockito.spy(new Hand(mockGame));
    try {
      Mockito.when(hand.superClone()).thenThrow(new CloneNotSupportedException("Test exception"));
    } catch (CloneNotSupportedException e) {
      fail("Unexpected exception during test setup");
    }

    assertThrows(AssertionError.class, hand::clone);
  }

  @Test
  @DisplayName("calculateValue should handle hard count correctly")
  void testCalculateValueHard() {
    hand.cards.add(new Card(0, 0));
    hand.cards.add(new Card(9, 0));
    hand.cards.add(new Card(8, 0));

    assertEquals(20, hand.calculateValue(CountMethod.HARD, false));
  }

  @Test
  @DisplayName("calculateValue should handle soft count correctly")
  void testCalculateValueSoft() {
    hand.cards.add(new Card(0, 0));
    hand.cards.add(new Card(5, 0));

    assertEquals(17, hand.calculateValue(CountMethod.SOFT, false));
  }

  @Test
  @DisplayName("calculateValue should handle multiple aces correctly")
  void testCalculateValueMultipleAces() {
    hand.cards.add(new Card(0, 0));
    hand.cards.add(new Card(0, 1));
    hand.cards.add(new Card(3, 0));

    assertEquals(16, hand.calculateValue(CountMethod.SOFT, false));
  }

  @Test
  @DisplayName("calculateValue should handle skipHiddenCard correctly")
  void testCalculateValueSkipHidden() {
    hand.cards.add(new Card(9, 0));
    hand.cards.add(new Card(0, 0));
    hand.cards.add(new Card(5, 0));

    assertEquals(16, hand.calculateValue(CountMethod.SOFT, true));
  }

  @Test
  @DisplayName("calculateValue should switch to hard count when soft count exceeds 21")
  void testCalculateValueSoftToHard() {
    hand.cards.add(new Card(0, 0));
    hand.cards.add(new Card(9, 0));
    hand.cards.add(new Card(8, 0));

    assertEquals(20, hand.calculateValue(CountMethod.SOFT, false));
  }

  @Test
  @DisplayName("dealCard should add card from shoe")
  void testDealCard() {
    Shoe mockShoe = Mockito.mock(Shoe.class);
    Card card = new Card(0, 0);
    Mockito.when(mockGame.getShoe()).thenReturn(mockShoe);
    Mockito.when(mockShoe.getNextCard()).thenReturn(card);

    hand.dealCard();

    assertEquals(1, hand.cards.size());
    assertEquals(card, hand.cards.get(0));
  }

  @Test
  @DisplayName("isBlackjack should correctly identify blackjack hands")
  void testIsBlackjack() {

    hand.cards.add(new Card(0, 0));
    hand.cards.add(new Card(9, 0));
    assertTrue(hand.isBlackjack());

    hand = new Hand(mockGame);
    hand.cards.add(new Card(9, 0));
    hand.cards.add(new Card(9, 1));
    hand.cards.add(new Card(0, 0));
    assertFalse(hand.isBlackjack());

    hand = new Hand(mockGame);
    hand.cards.add(new Card(9, 0));
    hand.cards.add(new Card(8, 0));
    assertFalse(hand.isBlackjack());
  }

  @Test
  @DisplayName("setPlayed should update played status")
  void testSetPlayed() {
    assertFalse(hand.played);
    hand.setPlayed(true);
    assertTrue(hand.played);
    hand.setPlayed(false);
    assertFalse(hand.played);
  }
}