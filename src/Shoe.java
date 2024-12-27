import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shoe {
    private final Game game;
    private static final int[] SHUFFLE_SPECS = { 80, 81, 82, 84, 86, 89, 92, 95 };
    private final ArrayList<Card> cards;
    private static final int CARDS_PER_DECK = 52;

    public Shoe(Game game) {
        this.game = game;
        this.cards = new ArrayList<>();
    }

    public boolean needToShuffle() {
        if (cards.isEmpty()) {
            return true;
        }

        int totalCards = getTotalCards();
        int cardsDealt = totalCards - cards.size();
        double used = (cardsDealt / (double) totalCards) * 100.0;

        return used > SHUFFLE_SPECS[game.getNumDecks() - 1];
    }

    public void shuffle() {
        for (int i = 0; i < 7; i++) {
            Collections.shuffle(cards);
        }
    }

    public Card getNextCard() {
        return cards.isEmpty() ? null : cards.removeFirst();
    }

    public void buildNewShoe(int deckType) {
        switch (deckType) {
            case 2:
                newAces();
                break;
            case 3:
                newJacks();
                break;
            case 4:
                newAcesJacks();
                break;
            case 5:
                newSevens();
                break;
            case 6:
                newEights();
                break;
            default:
                newRegular();
                break;
        }

        shuffle();
    }

    public int getTotalCards() {
        return game.getNumDecks() * CARDS_PER_DECK;
    }

    private void newShoe(List<Integer> values) {
        int totalCards = getTotalCards();
        cards.clear();

        while (cards.size() < totalCards) {
            for (int deck = 0; deck < game.getNumDecks(); deck++) {
                for (int suit = 0; suit < 4; suit++) {
                    if (cards.size() >= totalCards) {
                        break;
                    }

                    for (int value : values) {
                        if (cards.size() >= totalCards) {
                            break;
                        }

                        cards.add(new Card(value, suit));
                    }
                }
            }
        }
    }

    private void newRegular() {
        List<Integer> range = new ArrayList<>();

        for (int i = 0; i < 13; i++) {
            range.add(i);
        }

        newShoe(range);
    }

    private void newAces() {
        newShoe(List.of(0));
    }

    private void newJacks() {
        newShoe(List.of(10));
    }

    private void newAcesJacks() {
        newShoe(List.of(0, 10));
    }

    private void newSevens() {
        newShoe(List.of(6));
    }

    private void newEights() {
        newShoe(List.of(7));
    }
}
