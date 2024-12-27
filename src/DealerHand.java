public class DealerHand  extends Hand {
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
        int total = 0;

        for (int i = 0; i < cards.size(); i++) {
            if (i == 1 && hideDownCard) {
                continue;
            }

            int cardValue = cards.get(i).value() + 1;
            int v = cardValue > 9 ? 10 : cardValue;

            if (countMethod == CountMethod.SOFT && v == 1 && total < 11) {
                v = 11;
            }

            total += v;
        }

        if (countMethod == CountMethod.SOFT && total > 21) {
            return getValue(CountMethod.HARD);
        }

        return total;
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
        return out.toString();
    }

    public boolean upcardIsAce() {
        return cards.getFirst().isAce();
    }
}
