import java.util.ArrayList;
import java.util.List;

public class Hand {
    protected final Game game;
    protected List<Card> cards;
    protected boolean stood;
    protected boolean played;

    public Hand(Game game) {
        this.game = game;
        this.cards = new ArrayList<>();
        this.stood = false;
        this.played = false;
    }

    public void dealCard() {
        Shoe shoe = game.getShoe();
        cards.add(shoe.getNextCard());
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
