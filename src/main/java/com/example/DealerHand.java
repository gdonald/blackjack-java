package com.example;

public class DealerHand extends Hand {
  private boolean hideDownCard;

  public DealerHand(Game game) {
    super(game);
    this.hideDownCard = true;
  }

  public void setHideDownCard(boolean hideDownCard) {
    this.hideDownCard = hideDownCard;
  }

  public boolean isBusted() {
    return getValue(CountMethod.SOFT) > 21;
  }

  public int getValue(CountMethod countMethod) {
    return calculateValue(countMethod, hideDownCard);
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder(" ");

    for (int i = 0; i < cards.size(); i++) {
      if (i == 1 && hideDownCard) {
        out.append(game.cardFace(13, 0)).append(" ");
      } else {
        Card c = cards.get(i);
        out.append(game.cardFace(c.value(), c.suit())).append(" ");
      }
    }

    out.append(" ⇒  ").append(getValue(CountMethod.SOFT));
    out.append("\n");
    return out.toString();
  }

  public boolean upcardIsAce() {
    return cards.get(0).isAce();
  }
}
