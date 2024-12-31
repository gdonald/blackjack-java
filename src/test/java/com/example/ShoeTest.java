package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

import java.util.ArrayList;

class ShoeTest {
  private Game mockGame;
  private Shoe shoe;

  @BeforeEach
  void setUp() {
    mockGame = Mockito.mock(Game.class);
    shoe = new Shoe(mockGame);
  }

  @Test
  @DisplayName("needToShuffle should return true for empty shoe")
  void testNeedToShuffleEmpty() {
    assertTrue(shoe.needToShuffle());
  }

  @Test
  @DisplayName("needToShuffle should calculate threshold correctly")
  void testNeedToShuffleThreshold() {

    Mockito.when(mockGame.getNumDecks()).thenReturn(1);
    shoe.buildNewShoe(1);

    int cardsToRemove = (int) (52 * 0.81);
    for (int i = 0; i < cardsToRemove; i++) {
      shoe.getNextCard();
    }

    assertTrue(shoe.needToShuffle());
  }

  @Test
  @DisplayName("shuffle should reorder cards")
  void testShuffle() {
    Mockito.when(mockGame.getNumDecks()).thenReturn(1);

    ArrayList<Card> originalOrder = new ArrayList<>();
    ArrayList<Card> newOrder = new ArrayList<>();
    Card card;

    shoe.buildNewShoe(1);

    card = shoe.getNextCard();
    while (card != null) {
      originalOrder.add(card);
      card = shoe.getNextCard();
    }

    shoe.buildNewShoe(1);

    card = shoe.getNextCard();
    while (card != null) {
      newOrder.add(card);
      card = shoe.getNextCard();
    }

    assertFalse(originalOrder.equals(newOrder));
  }

  @Test
  @DisplayName("getNextCard should return null for empty shoe")
  void testGetNextCardEmpty() {
    assertNull(shoe.getNextCard());
  }

  @Test
  @DisplayName("getTotalCards should return correct number of cards")
  void testGetTotalCards() {
    Mockito.when(mockGame.getNumDecks()).thenReturn(2);
    assertEquals(104, shoe.getTotalCards());

    Mockito.when(mockGame.getNumDecks()).thenReturn(6);
    assertEquals(312, shoe.getTotalCards());
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2, 3, 4, 5, 6 })
  @DisplayName("buildNewShoe should create correct deck types")
  void testBuildNewShoe(int deckType) {
    Mockito.when(mockGame.getNumDecks()).thenReturn(1);
    shoe.buildNewShoe(deckType);

    ArrayList<Card> cards = new ArrayList<>();
    for (Card card; (card = shoe.getNextCard()) != null;) {
      cards.add(card);
    }

    switch (deckType) {
      case 2 ->
        assertTrue(cards.stream().allMatch(Card::isAce));
      case 3 ->
        assertTrue(cards.stream().allMatch(c -> c.value() == 10));
      case 4 ->
        assertTrue(cards.stream().allMatch(c -> c.isAce() || c.value() == 10));
      case 5 ->
        assertTrue(cards.stream().allMatch(c -> c.value() == 6));
      case 6 ->
        assertTrue(cards.stream().allMatch(c -> c.value() == 7));
      default ->
        assertEquals(52, cards.stream().distinct().count());
    }
  }

  @Test
  @DisplayName("buildNewShoe should honor number of decks")
  void testBuildNewShoeMultipleDecks() {
    Mockito.when(mockGame.getNumDecks()).thenReturn(2);
    shoe.buildNewShoe(1);

    int cardCount = 0;
    Card card = shoe.getNextCard();
    while (card != null) {
      cardCount++;
      card = shoe.getNextCard();
    }

    assertEquals(104, cardCount);
  }
}