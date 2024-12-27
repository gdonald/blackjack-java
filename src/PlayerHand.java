import java.util.ArrayList;

public class PlayerHand extends Hand {
    private int bet;
    private HandStatus status;
    private boolean paid;

    public PlayerHand(Game game) {
        super(game);

        this.bet = game.getCurrentBet();
        this.status = HandStatus.UNKNOWN;
        this.paid = false;
    }

    @Override
    public PlayerHand clone() {
        PlayerHand cloned = (PlayerHand) super.clone();
        cloned.game = this.game;
        return cloned;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public void setStatus(HandStatus status) {
        this.status = status;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isBusted() {
        return getValue(CountMethod.SOFT) > 21;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(" ");
        for (Card c : cards) {
            out.append(game.cardFace(c.value(), c.suit())).append(" ");
        }

        out.append(" ⇒  ").append(getValue(CountMethod.SOFT)).append(" ");

        if (status == HandStatus.LOST) {
            out.append("-");
        } else if (status == HandStatus.WON) {
            out.append("+");
        }

        out.append("$").append(String.format("%.2f", bet / 100.0));

        if (!played && this.equals(game.getPlayerHands().get(game.getCurrentHand()))) {
            out.append(" ⇐");
        }

        out.append(" ");

        if (status == HandStatus.LOST) {
            out.append(isBusted() ? "Busted!" : "Lose!");
        } else if (status == HandStatus.WON) {
            out.append(isBlackjack() ? "Blackjack!" : "Win!");
        } else if (status == HandStatus.PUSH) {
            out.append("Push!");
        }

        out.append("\n");
        return out.toString();
    }

    public int getValue(CountMethod countMethod) {
        return calculateValue(countMethod, false);
    }

    public boolean isDone() {
        if (played || stood || isBlackjack() || isBusted() ||
                getValue(CountMethod.SOFT) == 21 || getValue(CountMethod.HARD) == 21) {

            played = true;

            if (!paid) {
                if (isBusted()) {
                    paid = true;
                    status = HandStatus.LOST;
                    game.setMoney(game.getMoney() - bet);
                }
            }

            return true;
        }

        return false;
    }

    public boolean canSplit() {
        if (stood || game.getPlayerHands().size() >= Game.MAX_PLAYER_HANDS) {
            return false;
        }

        if (game.getMoney() < game.allBets() + bet) {
            return false;
        }

        return cards.size() == 2 && cards.get(0).value() == cards.get(1).value();
    }

    public boolean canDbl() {
        if (game.getMoney() < game.allBets() + bet) {
            return false;
        }

        return !stood && cards.size() == 2 && !isBusted() && !isBlackjack();
    }

    public boolean canStand() {
        return !stood && !isBusted() && !isBlackjack();
    }

    public boolean canHit() {
        return !played && !stood && getValue(CountMethod.HARD) != 21 && !isBlackjack() && !isBusted();
    }

    public void hit() {
        dealCard();
        if (isDone()) {
            process();
            return;
        }
        game.drawHands();
        game.getPlayerHands().get(game.getCurrentHand()).getAction();
    }

    public void dbl() {
        dealCard();
        played = true;
        bet *= 2;
        if (isDone()) {
            process();
        }
    }

    public void stand() {
        stood = true;
        played = true;
        if (game.moreHandsToPlay()) {
            game.playMoreHands();
            return;
        }
        game.playDealerHand();
        game.drawHands();
        game.betOptions();
    }

    public void process() {
        if (game.moreHandsToPlay()) {
            game.playMoreHands();
            return;
        }
        game.playDealerHand();
        game.drawHands();
        game.betOptions();
    }

    public void getAction() {
        StringBuilder out = new StringBuilder(" ");
        if (canHit()) {
            out.append("(H) Hit  ");
        }
        if (canStand()) {
            out.append("(S) Stand  ");
        }
        if (canSplit()) {
            out.append("(P) Split  ");
        }
        if (canDbl()) {
            out.append("(D) Double  ");
        }
        System.out.println(out);

        boolean decisionMade = false;
        char c = game.getChar();

        switch (c) {
            case 'h':
                if (canHit()) {
                    decisionMade = true;
                    hit();
                    return;
                }
            case 's':
                if (canStand()) {
                    decisionMade = true;
                    stand();
                    return;
                }
            case 'p':
                if (canSplit()) {
                    decisionMade = true;
                    game.splitCurrentHand();
                    return;
                }
            case 'd':
                if (canDbl()) {
                    decisionMade = true;
                    dbl();
                    return;
                }
        }

        if (!decisionMade) {
            game.drawHands();
            getAction();
        }
    }
}
