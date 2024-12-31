package com.example;

import java.util.ArrayList;

public class Hand implements Cloneable {
    protected Game game;
    protected ArrayList<Card> cards;
    protected boolean stood;
    protected boolean played;

    public Hand(Game game) {
        this.game = game;
        this.cards = new ArrayList<>();
        this.stood = false;
        this.played = false;
    }

    @Override
    public Hand clone() {
        try {
            Hand cloned = (Hand) super.clone();
            cloned.cards = new ArrayList<>(this.cards);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    protected int calculateValue(CountMethod countMethod, boolean skipHiddenCard) {
        int total = 0;

        for (int i = 0; i < cards.size(); i++) {
            if (skipHiddenCard && i == 1) {
                continue;
            }

            int cardValue = cards.get(i).value() + 1;
            int v = (cardValue > 9) ? 10 : cardValue;

            if (countMethod == CountMethod.SOFT && v == 1 && total < 11) {
                v = 11;
            }

            total += v;
        }

        if (countMethod == CountMethod.SOFT && total > 21) {
            return calculateValue(CountMethod.HARD, skipHiddenCard);
        }

        return total;
    }

    public void dealCard() {
        this.cards.add(game.getShoe().getNextCard());
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && calculateValue(CountMethod.SOFT, false) == 21;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }
}
