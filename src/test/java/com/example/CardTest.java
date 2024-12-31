package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

  @Test
  @DisplayName("Card constructor and getters work correctly")
  void testCardCreation() {
    Card card = new Card(0, 0);
    assertEquals(0, card.value());
    assertEquals(0, card.suit());
  }

  @Test
  @DisplayName("Card can be cloned correctly")
  void testCardCloning() {
    Card original = new Card(12, 3);
    Card cloned = original.clone();

    assertEquals(original.value(), cloned.value());
    assertEquals(original.suit(), cloned.suit());
    assertNotSame(original, cloned);
  }

  @Test
  @DisplayName("isAce() correctly identifies aces")
  void testIsAce() {
    Card ace = new Card(0, 0);
    Card king = new Card(12, 0);

    assertTrue(ace.isAce());
    assertFalse(king.isAce());
  }

  @Test
  @DisplayName("isTen() correctly identifies ten-value cards")
  void testIsTen() {
    Card nine = new Card(8, 0);
    Card ten = new Card(9, 0);
    Card jack = new Card(10, 0);
    Card queen = new Card(11, 0);
    Card king = new Card(12, 0);

    assertFalse(nine.isTen());
    assertTrue(ten.isTen());
    assertTrue(jack.isTen());
    assertTrue(queen.isTen());
    assertTrue(king.isTen());
  }

  @Test
  @DisplayName("FACES array contains correct card representations")
  void testFacesArray() {
    assertEquals("A♠", Card.FACES[0][0]); // Ace of Spades
    assertEquals("K♥", Card.FACES[12][1]); // King of Hearts
    assertEquals("T♣", Card.FACES[9][2]); // Ten of Clubs
    assertEquals("5♦", Card.FACES[4][3]); // Five of Diamonds
  }

  @Test
  @DisplayName("FACES2 array contains correct Unicode card representations")
  void testFaces2Array() {
    assertEquals("🂡", Card.FACES2[0][0]); // Ace of Spades
    assertEquals("🂾", Card.FACES2[12][1]); // King of Hearts
    assertEquals("🃊", Card.FACES2[9][2]); // Ten of Clubs
    assertEquals("🃕", Card.FACES2[4][3]); // Five of Diamonds
  }
}
