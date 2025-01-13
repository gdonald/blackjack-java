package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class DealerHandTest {
  private Game mockGame;
  private DealerHand dealerHand;

  @BeforeEach
  void setUp() {
    mockGame = Mockito.mock(Game.class);
    dealerHand = new DealerHand(mockGame);
  }

  @Test
  @DisplayName("isBusted should correctly identify bust with hard count > 21")
  void testIsBusted() {
    dealerHand.setHideDownCard(false);

    dealerHand.cards.add(new Card(6, 0));
    dealerHand.cards.add(new Card(8, 0));
    assertFalse(dealerHand.isBusted());

    dealerHand.cards.add(new Card(0, 0));
    assertFalse(dealerHand.isBusted());

    dealerHand.cards.add(new Card(9, 0));
    assertTrue(dealerHand.isBusted());
  }

  @Test
  @DisplayName("getValue should respect hideDownCard setting")
  void testGetValue() {
    dealerHand.setHideDownCard(true);

    dealerHand.cards.add(new Card(9, 0));
    dealerHand.cards.add(new Card(0, 0));
    dealerHand.cards.add(new Card(4, 0));

    assertEquals(15, dealerHand.getValue(CountMethod.SOFT));

    dealerHand.setHideDownCard(false);
    assertEquals(16, dealerHand.getValue(CountMethod.SOFT));
  }

  @Test
  @DisplayName("toString should properly format hand with hidden card")
  void testToStringWithHiddenCard() {
    Mockito.when(mockGame.cardFace(13, 0)).thenReturn("??");
    Mockito.when(mockGame.cardFace(9, 0)).thenReturn("10♠");
    Mockito.when(mockGame.cardFace(0, 0)).thenReturn("A♠");

    dealerHand.cards.add(new Card(9, 0));
    dealerHand.cards.add(new Card(0, 0));

    String expected = " 10♠ ??  ⇒  10\n";
    assertEquals(expected, dealerHand.toString());
  }

  @Test
  @DisplayName("toString should properly format hand with revealed cards")
  void testToStringWithRevealedCards() {
    Mockito.when(mockGame.cardFace(9, 0)).thenReturn("10♠");
    Mockito.when(mockGame.cardFace(0, 0)).thenReturn("A♠");

    dealerHand.cards.add(new Card(9, 0));
    dealerHand.cards.add(new Card(0, 0));
    dealerHand.setHideDownCard(false);

    String expected = " 10♠ A♠  ⇒  21\n";
    assertEquals(expected, dealerHand.toString());
  }

  @Test
  @DisplayName("upcardIsAce should correctly identify ace as first card")
  void testUpcardIsAce() {
    dealerHand.cards.add(new Card(0, 0));
    assertTrue(dealerHand.upcardIsAce());

    dealerHand = new DealerHand(mockGame);
    dealerHand.cards.add(new Card(9, 0));
    assertFalse(dealerHand.upcardIsAce());
  }

  @Test
  @DisplayName("getValue should handle both hard and soft counts")
  void testGetValueCountMethods() {
    dealerHand.cards.add(new Card(0, 0));
    dealerHand.cards.add(new Card(4, 0));
    dealerHand.setHideDownCard(false);

    assertEquals(16, dealerHand.getValue(CountMethod.SOFT));
    assertEquals(6, dealerHand.getValue(CountMethod.HARD));
  }
}