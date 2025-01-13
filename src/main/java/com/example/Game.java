package com.example;

import java.io.*;
import java.util.ArrayList;

public class Game {
  public static final int MAX_PLAYER_HANDS = 7;
  private static final String SAVE_FILE = "blackjack.txt";
  private static final int MIN_BET = 500;
  private static final int MAX_BET = 10000000;
  private final BufferedReader reader;
  private final Shoe shoe;
  private final ArrayList<PlayerHand> playerHands;
  private int numDecks;
  private int deckType;
  private int faceType;
  private int money;
  private int currentBet;
  private DealerHand dealerHand;
  private int currentHand;
  private boolean quitting;

  public Game() {
    this.reader = new BufferedReader(new InputStreamReader(System.in));
    this.shoe = new Shoe(this);
    this.numDecks = 1;
    this.deckType = 1;
    this.faceType = 1;
    this.money = 10000;
    this.currentBet = 500;
    this.playerHands = new ArrayList<>();
    this.quitting = false;
    loadGame();
  }

  public static void run() {
    (new Game()).loop();
  }

  public int getNumDecks() {
    return numDecks;
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

    PlayerHand playerHand = playerHands.get(currentHand);
    playerHand.dealCard();

    if (playerHand.isDone()) {
      playerHand.process();
      return;
    }

    drawHands();
    playerHand.getAction();
  }

  public ArrayList<PlayerHand> getPlayerHands() {
    return playerHands;
  }

  public void splitCurrentHand() {
    int handCount = playerHands.size();
    PlayerHand newHand = new PlayerHand(this);
    playerHands.add(newHand);

    while (handCount > currentHand) {
      PlayerHand playerHand = playerHands.get(handCount - 1).clone();
      playerHands.set(handCount, playerHand);
      handCount--;
    }

    PlayerHand currentPlayerHand = playerHands.get(currentHand);
    PlayerHand splitHand = playerHands.get(currentHand + 1);

    Card splitCard1 = currentPlayerHand.cards.get(1).clone();
    Card splitCard0 = currentPlayerHand.cards.get(0).clone();

    splitHand.cards = new ArrayList<>();
    splitHand.cards.add(splitCard1);
    currentPlayerHand.cards = new ArrayList<>();
    currentPlayerHand.cards.add(splitCard0);
    currentPlayerHand.dealCard();

    if (currentPlayerHand.isDone()) {
      currentPlayerHand.process();
      return;
    }

    drawHands();
    currentPlayerHand.getAction();
  }

  public int allBets() {
    return playerHands.stream()
        .mapToInt(PlayerHand::getBet)
        .sum();
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
    drawHands();
    System.out.print(" (1) $5  (2) $10  (3) $25  (4) $100");

    switch (getChar()) {
      case '1':
        currentBet = 500;
        return;
      case '2':
        currentBet = 1000;
        return;
      case '3':
        currentBet = 2500;
        return;
      case '4':
        currentBet = 10000;
        return;
    }

    normalizeBet();
    dealNewHand();
  }

  public void getNewNumDecks() {
    drawHands();
    System.out.printf(" Number of Decks: %d  Enter New Number of Decks (1-8): ", numDecks);

    int newNumDecks = getChar() - '0';

    if (newNumDecks < 1) {
      newNumDecks = 1;
    } else if (newNumDecks > 8) {
      newNumDecks = 8;
    }

    this.numDecks = newNumDecks;
    gameOptions();
  }

  public void getNewDeckType() {
    drawHands();
    System.out.println(" (1) Regular  (2) Aces  (3) Jacks  (4) Aces & Jacks  (5) Sevens  (6) Eights");

    int newDeckType = getChar() - '0';

    if (newDeckType > 0 && newDeckType < 7) {
      deckType = newDeckType;

      if (newDeckType > 1) {
        this.numDecks = 8;
      }

      shoe.buildNewShoe(deckType);

      saveGame();
      return;
    }

    getNewDeckType();
  }

  public void getNewFaceType() {
    drawHands();
    System.out.println(" (1) A♠  (2) 🂡");

    int newFaceType = getChar() - '0';

    if (newFaceType == 1 || newFaceType == 2) {
      faceType = newFaceType;
      saveGame();
      return;
    }

    drawHands();
    getNewFaceType();
  }

  public void gameOptions() {
    drawHands();
    System.out.println(" (N) Number of Decks  (T) Deck Type  (F) Face Type  (B) Back");

    switch (getChar()) {
      case 'n':
        getNewNumDecks();
        return;
      case 't':
        getNewDeckType();
        return;
      case 'f':
        getNewFaceType();
        return;
      case 'b':
        drawHands();
        betOptions();
        return;
    }

    drawHands();
    gameOptions();
  }

  public void betOptions() {
    System.out.println(" (D) Deal Hand  (B) Change Bet  (O) Options  (Q) Quit");

    switch (getChar()) {
      case 'd':
        return;
      case 'b':
        getNewBet();
        return;
      case 'o':
        gameOptions();
        return;
      case 'q':
        this.quitting = true;
        clear();
        return;
    }

    drawHands();
    betOptions();
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
    return playerHands.stream()
        .anyMatch(hand -> !(hand.isBusted() || hand.isBlackjack()));
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
    System.out.println(" Insurance?  (Y) Yes (N) No");

    switch (getChar()) {
      case 'y':
        insureHand();
        return;
      case 'n':
        noInsurance();
        return;
    }

    drawHands();
    askInsurance();
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
      playerHands.get(0).dealCard();
      dealerHand.dealCard();
    }

    if (dealerHand.upcardIsAce()) {
      drawHands();
      askInsurance();

      return;
    }

    if (playerHands.get(0).isDone()) {
      dealerHand.setHideDownCard(false);

      payHands();
      drawHands();
      betOptions();

      return;
    }

    drawHands();
    playerHands.get(0).getAction();

    saveGame();
  }

  public void drawHands() {
    clear();

    StringBuilder output = new StringBuilder();

    output.append("\n Dealer:\n").append(dealerHand);
    output.append(String.format("\n Player $%.2f:\n", money / 100.0));

    for (PlayerHand playerHand : playerHands) {
      output.append(playerHand);
    }

    System.out.print(output);
  }

  public void saveGame() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
      writer.write(String.format("%d|%d|%d|%d|%d", numDecks, money, currentBet, deckType, faceType));
    } catch (IOException ignored) {
    }
  }

  public void loadGame() {
    try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
      String line = reader.readLine();

      if (line != null) {
        String[] data = line.split("\\|");

        if (data.length == 5) {
          this.numDecks = Integer.parseInt(data[0]);
          this.money = Integer.parseInt(data[1]);
          this.currentBet = Integer.parseInt(data[2]);
          this.deckType = Integer.parseInt(data[3]);
          this.faceType = Integer.parseInt(data[4]);
        }
      }
    } catch (IOException ignored) {
    }

    if (money < MIN_BET) {
      money = 10000;
      currentBet = MIN_BET;
    }
  }

  public char getChar() {
    try {
      return (char) reader.read();
    } catch (IOException e) {
      System.out.println("Error reading input: " + e.getMessage());
      System.exit(1);
    }

    return 0;
  }

  public void clear() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

  public void loop() {
    while (!quitting) {
      dealNewHand();
    }
  }
}
