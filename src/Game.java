import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    private static final String SAVE_FILE = "blackjack.txt";
    private static final int MIN_BET = 500;
    private static final int MAX_BET = 10000000;
    public static final int MAX_PLAYER_HANDS = 7;

    private final Shoe shoe;
    private int numDecks;
    private int deckType;
    private int faceType;
    private int money;
    private int currentBet;
    private DealerHand dealerHand;
    private int currentHand;
    private final List<PlayerHand> playerHands;
    private boolean quitting;

    public Game() {
        this.shoe = new Shoe();
        this.numDecks = 1;
        this.deckType = 1;
        this.faceType = 1;
        this.money = 10000;
        this.currentBet = 500;
        this.playerHands = new ArrayList<>();
        this.quitting = false;
        loadGame();
    }

    public String cardFace(int value, int suit) {
        if (faceType == 2) {
            return Card.FACES2[value][suit];
        }
        return Card.FACES[value][suit];
    }

    public int getCurrentHand() {
        return currentHand;
    }

    public boolean moreHandsToPlay() {
        return currentHand < playerHands.size() - 1;
    }

    public void playMoreHands() {
        currentHand++;
        PlayerHand hand = playerHands.get(currentHand);
        hand.dealCard();
        if (hand.isDone()) {
            hand.process();
            return;
        }
        drawHands();
        hand.getAction();
    }

    public List<PlayerHand> getPlayerHands() {
        return playerHands;
    }

    public void splitCurrentHand() {
        int handCount = playerHands.size();
        PlayerHand newHand = new PlayerHand(this);
        playerHands.add(newHand);

        while (handCount > currentHand) {
            PlayerHand handToCopy = playerHands.get(handCount - 1);
            playerHands.set(handCount, handToCopy);
            handCount--;
        }

        PlayerHand playerHand = playerHands.get(currentHand);
        PlayerHand splitHand = playerHands.get(currentHand + 1);

        splitHand.cards = List.of(playerHand.getCards().get(1));
        playerHand.cards = List.of(playerHand.getCards().get(0));
        playerHand.dealCard();

        if (playerHand.isDone()) {
            playerHand.process();
            return;
        }

        drawHands();
        playerHand.getAction();
    }


    public int allBets() {
        int bets = 0;
        for (PlayerHand hand : playerHands) {
            bets += hand.getBet();
        }
        return bets;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public Shoe getShoe() {
        return shoe;
    }

    private void normalizeBet() {
        if (currentBet < MIN_BET) {
            currentBet = MIN_BET;
        } else if (currentBet > MAX_BET) {
            currentBet = MAX_BET;
        }
        if (currentBet > money) {
            currentBet = money;
        }
    }

    public void getNewBet() {
        clear();
        drawHands();
        System.out.printf("  Current Bet: $%.2f  Enter New Bet: $", currentBet / 100.0);
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        currentBet = Integer.parseInt(input) * 100;
        normalizeBet();
        dealNewHand();
    }

    public void getNewNumDecks() {
        clear();
        drawHands();
        System.out.printf("  Number of Decks: %d  Enter New Number of Decks (1-8): ", numDecks);
        Scanner scanner = new Scanner(System.in);
        int tmp = scanner.nextInt();

        if (tmp < 1) {
            tmp = 1;
        } else if (tmp > 8) {
            tmp = 8;
        }

        this.numDecks = tmp;
        gameOptions();
    }

    public void getNewDeckType() {
        clear();
        drawHands();
        System.out.println(" (1) Regular  (2) Aces  (3) Jacks  (4) Aces & Jacks  (5) Sevens  (6) Eights");
        boolean decisionMade = false;
        Scanner scanner = new Scanner(System.in);

        while (!decisionMade) {
            String input = scanner.nextLine().trim();
            int c;
            try {
                c = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 6.");
                continue;
            }

            if (c > 0 && c < 7) {
                decisionMade = true;
                deckType = c;
                if (c > 1) {
                    this.numDecks = 8;
                }
                shoe.buildNewShoe(deckType);
                saveGame();
            }

            if (decisionMade) {
                drawHands();
                betOptions();
            }
        }
    }

    public void getNewFaceType() {
        clear();
        drawHands();
        System.out.println("(1) A♠  (2) 🂡");
        boolean decisionMade = false;
        Scanner scanner = new Scanner(System.in);

        while (!decisionMade) {
            String input = scanner.nextLine().trim();

            if (input.equals("1") || input.equals("2")) {
                decisionMade = true;
                faceType = Integer.parseInt(input);
                saveGame();
            }

            if (decisionMade) {
                drawHands();
                betOptions();
            } else {
                System.out.println("Invalid input. Please enter 1 or 2.");
            }
        }
    }

    public void gameOptions() {
        clear();
        drawHands();
        System.out.println(" (N) Number of Decks  (T) Deck Type  (F) Face Type  (B) Back");
        boolean decisionMade = false;
        Scanner scanner = new Scanner(System.in);

        while (!decisionMade) {
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "n":
                    decisionMade = true;
                    getNewNumDecks();
                    break;
                case "t":
                    decisionMade = true;
                    getNewDeckType();
                    break;
                case "f":
                    decisionMade = true;
                    getNewFaceType();
                    break;
                case "b":
                    decisionMade = true;
                    clear();
                    drawHands();
                    betOptions();
                    break;
                default:
                    System.out.println("Invalid input. Please choose (N), (T), (F), or (B).");
            }
        }
    }

    public void betOptions() {
        System.out.println(" (D) Deal Hand  (B) Change Bet  (O) Options  (Q) Quit");
        boolean decisionMade = false;
        Scanner scanner = new Scanner(System.in);

        while (!decisionMade) {
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "d":
                    decisionMade = true;
                    break;
                case "b":
                    decisionMade = true;
                    getNewBet();
                    break;
                case "o":
                    decisionMade = true;
                    gameOptions();
                    break;
                case "q":
                    decisionMade = true;
                    this.quitting = true;
                    clear();
                    break;
                default:
                    System.out.println("Invalid input. Please choose (D), (B), (O), or (Q).");
            }
        }
    }

    public void insureHand() {
        PlayerHand playerHand = playerHands.get(currentHand);
        playerHand.setBet(playerHand.getBet() / 2);
        playerHand.setPlayed(true);
        playerHand.setPaid(true);
        playerHand.setStatus(HandStatus.LOST);
        money -= playerHand.getBet();

        drawHands();
        betOptions();
    }

    public void payHands() {
        int dealerHandValue = dealerHand.getValue(CountMethod.SOFT);
        boolean dealerHandBusted = dealerHand.isBusted();

        for (PlayerHand playerHand : playerHands) {
            if (playerHand.isPaid()) {
                continue;
            }

            playerHand.setPaid(true);
            int playerHandValue = playerHand.getValue(CountMethod.SOFT);

            if (dealerHandBusted || playerHandValue > dealerHandValue) {
                if (playerHand.isBlackjack()) {
                    playerHand.setBet((int) (playerHand.getBet() * 1.5));
                }
                money += playerHand.getBet();
                playerHand.setStatus(HandStatus.WON);
            } else if (playerHandValue < dealerHandValue) {
                money -= playerHand.getBet();
                playerHand.setStatus(HandStatus.LOST);
            } else {
                playerHand.setStatus(HandStatus.PUSH);
            }
        }

        normalizeBet();
        saveGame();
    }

    public boolean needToPlayDealerHand() {
        for (PlayerHand hand : playerHands) {
            if (!(hand.isBusted() || hand.isBlackjack())) {
                return true;
            }
        }
        return false;
    }

    public void playDealerHand() {
        if (dealerHand.isBlackjack()) {
            dealerHand.setHideDownCard(false);
        }
        if (!needToPlayDealerHand()) {
            dealerHand.setPlayed(true);
            payHands();
            return;
        }
        dealerHand.setHideDownCard(false);
        int softCount = dealerHand.getValue(CountMethod.SOFT);
        int hardCount = dealerHand.getValue(CountMethod.HARD);
        while (softCount < 18 && hardCount < 17) {
            dealerHand.dealCard();
            softCount = dealerHand.getValue(CountMethod.SOFT);
            hardCount = dealerHand.getValue(CountMethod.HARD);
        }
        dealerHand.setPlayed(true);
        payHands();
    }

    public void noInsurance() {
        if (dealerHand.isBlackjack()) {
            dealerHand.setHideDownCard(false);
            dealerHand.setPlayed(true);
            payHands();
            drawHands();
            betOptions();
            return;
        }

        PlayerHand playerHand = playerHands.get(currentHand);
        if (playerHand.isDone()) {
            playDealerHand();
            drawHands();
            betOptions();
            return;
        }

        drawHands();
        playerHand.getAction();
    }

    public void askInsurance() {
        System.out.println("Insurance? (Y) Yes (N) No");
        boolean decisionMade = false;
        Scanner scanner = new Scanner(System.in);

        while (!decisionMade) {
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "y":
                    decisionMade = true;
                    insureHand();
                    break;

                case "n":
                    decisionMade = true;
                    noInsurance();
                    break;

                default:
                    System.out.println("Invalid input. Please enter 'Y' for Yes or 'N' for No.");
            }
        }
    }

    public void dealNewHand() {
        if (shoe.needToShuffle()) {
            shoe.buildNewShoe(deckType);
        }
        playerHands.clear();
        playerHands.add(new PlayerHand(this));
        currentHand = 0;
        dealerHand = new DealerHand(this);

        for (int i = 0; i < 2; i++) {
            playerHands.getFirst().dealCard();
            dealerHand.dealCard();
        }

        if (dealerHand.upcardIsAce()) {
            drawHands();
            askInsurance();
            return;
        }

        if (playerHands.getFirst().isDone()) {
            dealerHand.setHideDownCard(false);
            payHands();
            drawHands();
            betOptions();
            return;
        }

        drawHands();
        playerHands.getFirst().getAction();
        saveGame();
    }

    public void drawHands() {
        clear();
        StringBuilder output = new StringBuilder("\n Dealer:\n" + dealerHand + "\n");
        output.append(String.format("\n Player $%.2f:\n", money / 100.0));
        for (PlayerHand playerHand : playerHands) {
            output.append(playerHand).append("\n");
        }
        System.out.println(output);
    }

    public void saveGame() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write(String.format("%d|%d|%d|%d|%d", numDecks, money, currentBet, deckType, faceType));
        } catch (IOException _) {
        }
    }

    public void loadGame() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                String[] data = line.split("\\|");
                this.numDecks = Integer.parseInt(data[0]);
                this.money = Integer.parseInt(data[1]);
                this.currentBet = Integer.parseInt(data[2]);
                this.deckType = Integer.parseInt(data[3]);
                this.faceType = Integer.parseInt(data[4]);
            }
        } catch (IOException _) {
        }

        if (money < MIN_BET) {
            money = 10000;
            currentBet = MIN_BET;
        }
    }

    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void run() {
        while (!quitting) {
            dealNewHand();
        }
    }
}
