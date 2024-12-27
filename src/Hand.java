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
        Shoe shoe = game.getShoe();
        this.cards.add(shoe.getNextCard());
    }

    public boolean isBlackjack() {
        if (cards.size() != 2) {
            return false;
        }
        return (cards.get(0).isAce() && cards.get(1).isTen()) ||
                (cards.get(1).isAce() && cards.get(0).isTen());
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }
}
