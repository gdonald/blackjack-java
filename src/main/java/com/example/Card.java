package com.example;

public record Card(int value, int suit) implements Cloneable {
  public static final String[][] FACES = {
      {"A♠", "A♥", "A♣", "A♦"},
      {"2♠", "2♥", "2♣", "2♦"},
      {"3♠", "3♥", "3♣", "3♦"},
      {"4♠", "4♥", "4♣", "4♦"},
      {"5♠", "5♥", "5♣", "5♦"},
      {"6♠", "6♥", "6♣", "6♦"},
      {"7♠", "7♥", "7♣", "7♦"},
      {"8♠", "8♥", "8♣", "8♦"},
      {"9♠", "9♥", "9♣", "9♦"},
      {"T♠", "T♥", "T♣", "T♦"},
      {"J♠", "J♥", "J♣", "J♦"},
      {"Q♠", "Q♥", "Q♣", "Q♦"},
      {"K♠", "K♥", "K♣", "K♦"},
      {"??", "", "", ""}
  };
  public static final String[][] FACES2 = {
      {"🂡", "🂱", "🃁", "🃑"},
      {"🂢", "🂲", "🃂", "🃒"},
      {"🂣", "🂳", "🃃", "🃓"},
      {"🂤", "🂴", "🃄", "🃔"},
      {"🂥", "🂵", "🃅", "🃕"},
      {"🂦", "🂶", "🃆", "🃖"},
      {"🂧", "🂷", "🃇", "🃗"},
      {"🂨", "🂸", "🃈", "🃘"},
      {"🂩", "🂹", "🃉", "🃙"},
      {"🂪", "🂺", "🃊", "🃚"},
      {"🂫", "🂻", "🃋", "🃛"},
      {"🂭", "🂽", "🃍", "🃝"},
      {"🂮", "🂾", "🃎", "🃞"},
      {"🂠", "", "", ""}
  };

  Object superClone() throws CloneNotSupportedException {
    return super.clone();
  }
  
  @Override
  public Card clone() {
    try {
      return (Card) superClone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  public boolean isAce() {
    return this.value == 0;
  }

  public boolean isTen() {
    return this.value > 8;
  }
}
