package com.example;

import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;

public class GameTest {
  private static final String SAVE_FILE = "blackjack.txt";
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  private Game game;
  private Shoe shoe;

  private void deleteSaveFile() {
    File file = new File(SAVE_FILE);
    if (file.exists()) {
      assertTrue(file.delete());
    }
  }

  @BeforeEach
  void setUp() {
    deleteSaveFile();
    System.setOut(new PrintStream(outputStream));

    game = spy(new Game());
    shoe = spy(new Shoe(game));
    when(game.getShoe()).thenReturn(shoe);
  }

  @AfterEach
  public void tearDown() {
    System.setOut(originalOut);
    deleteSaveFile();
  }

  @SuppressWarnings("SameParameterValue")
  private void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private <T> T getField(Object target, String fieldName, Class<T> fieldType) {
    try {
      Class<?> currentClass = target.getClass();
      while (currentClass != null) {
        try {
          Field field = currentClass.getDeclaredField(fieldName);
          field.setAccessible(true);
          return fieldType.cast(field.get(target));
        } catch (NoSuchFieldException ignored) {
          currentClass = currentClass.getSuperclass();
        }
      }
      throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy of " + target.getClass());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("clear tests")
  void testClear() {
    game.clear();
    assertEquals("\033[H\033[2J", outputStream.toString());
  }

  @Nested
  @DisplayName("getNewDeckType tests")
  class GetNewDeckTypeTest {
    @Test
    void testGetNewDeckType() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('1');

      game.getNewDeckType();
      verify(game).saveGame();
    }

    @Test
    void testGetNewDeckTypeDeckType2() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('2');

      game.getNewDeckType();
      verify(game).saveGame();
      assertEquals(8, game.getNumDecks());
    }

    @Test
    void testGetNewDeckTypeInvalidInputLow() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('#', '1');

      game.getNewDeckType();
      verify(game).saveGame();
      verify(game, times(2)).getNewDeckType();
    }

    @Test
    void testGetNewDeckTypeInvalidInputHigh() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('8', '1');

      game.getNewDeckType();
      verify(game).saveGame();
      verify(game, times(2)).getNewDeckType();
    }
  }

  @Nested
  @DisplayName("getNewFaceType tests")
  class GetNewFaceTypeTest {
    @Test
    void testGetNewFaceType() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('1');

      game.getNewFaceType();
      verify(game).saveGame();
    }

    @Test
    void testGetNewFaceTypeInvalidInput() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('x', '2');

      game.getNewFaceType();
      verify(game, times(2)).getNewFaceType();
      verify(game).saveGame();
    }
  }

  @Nested
  @DisplayName("insureHand tests")
  class InsureHandsTest {
    @Test
    void testInsureHands() {
      doNothing().when(game).drawHands();
      doNothing().when(game).betOptions();

      PlayerHand playerHand = spy(new PlayerHand(game));
      game.getPlayerHands().add(playerHand);

      game.insureHand();
      assertEquals(9750, game.getMoney());
    }
  }

  @Nested
  @DisplayName("gameOptions tests")
  class GameOptionsTests {
    @Test
    void testGameOptionsNewNumDecks() {
      doNothing().when(game).drawHands();
      doNothing().when(game).getNewNumDecks();
      when(game.getChar()).thenReturn('n');

      game.gameOptions();
      verify(game).getNewNumDecks();
    }

    @Test
    void testGameOptionsNewDeckType() {
      doNothing().when(game).drawHands();
      doNothing().when(game).getNewDeckType();
      when(game.getChar()).thenReturn('t');

      game.gameOptions();
      verify(game).getNewDeckType();
    }

    @Test
    void testGameOptionsNewFaceType() {
      doNothing().when(game).drawHands();
      doNothing().when(game).getNewFaceType();
      when(game.getChar()).thenReturn('f');

      game.gameOptions();
      verify(game).getNewFaceType();
    }

    @Test
    void testGameOptionsGoBack() {
      doNothing().when(game).drawHands();
      doNothing().when(game).betOptions();
      when(game.getChar()).thenReturn('b');

      game.gameOptions();
      verify(game).betOptions();
    }

    @Test
    void testGameOptionsInvalidInput() {
      doNothing().when(game).drawHands();
      doNothing().when(game).getNewNumDecks();
      when(game.getChar()).thenReturn('x', 'n');

      game.gameOptions();
      verify(game, times(2)).gameOptions();
    }
  }

  @Nested
  @DisplayName("playMoreHands tests")
  class PlayMoreHandsTests {
    private PlayerHand playerHand2;

    @BeforeEach
    void setUp() {
      shoe.buildNewShoe(1);

      PlayerHand playerHand = spy(new PlayerHand(game));
      playerHand2 = spy(new PlayerHand(game));

      playerHand.dealCards(2);
      game.getPlayerHands().add(playerHand);

      playerHand2.dealCards(1);
      game.getPlayerHands().add(playerHand2);
    }

    @Test
    void testPlayMoreHands() {
      when(playerHand2.isDone()).thenReturn(false);
      doNothing().when(playerHand2).getAction();

      game.playMoreHands();
      verify(game).drawHands();
      verify(playerHand2).getAction();
    }

    @Test
    void testPlayMoreHandsIsDone() {
      when(playerHand2.isDone()).thenReturn(true);
      doNothing().when(playerHand2).process();

      game.playMoreHands();
      verify(game, never()).drawHands();
      verify(playerHand2).process();
    }
  }

  @Nested
  @DisplayName("betOptions tests")
  class BetOptionsTests {
    @Test
    void testBetOptions() {
      when(game.getChar()).thenReturn('q');

      game.betOptions();
      verify(game).clear();
      verify(game, never()).drawHands();
    }

    @Test
    void testBetOptionsDealNewHand() {
      doNothing().when(game).dealNewHand();
      when(game.getChar()).thenReturn('d');

      game.betOptions();
      verify(game, never()).drawHands();
    }

    @Test
    void testBetOptionsGetNewBet() {
      doNothing().when(game).getNewBet();
      when(game.getChar()).thenReturn('b');

      game.betOptions();
      verify(game).getNewBet();
      verify(game, never()).drawHands();
    }

    @Test
    void testBetOptionsGameOptions() {
      doNothing().when(game).gameOptions();
      when(game.getChar()).thenReturn('o');

      game.betOptions();
      verify(game).gameOptions();
      verify(game, never()).drawHands();
    }

    @Test
    void testBetOptionsInvalidInput() {
      doNothing().when(game).drawHands();
      when(game.getChar()).thenReturn('x', 'q');

      game.betOptions();
      verify(game, times(2)).betOptions();
    }
  }

  @Nested
  @DisplayName("normalizeBet tests")
  class GetNewBetTests {
    @Test
    void getNewBet2() {
      when(game.getChar()).thenReturn('2', 'q');
      doNothing().when(game).dealNewHand();
      game.getNewBet();

      assertEquals(1000, game.getCurrentBet());
    }

    @Test
    void getNewBet3() {
      when(game.getChar()).thenReturn('3', 'q');
      doNothing().when(game).dealNewHand();
      game.getNewBet();

      assertEquals(2500, game.getCurrentBet());
    }

    @Test
    void getNewBet4() {
      when(game.getChar()).thenReturn('4', 'q');
      doNothing().when(game).dealNewHand();
      game.getNewBet();

      assertEquals(10000, game.getCurrentBet());
    }

    @Test
    void getNewBetInvalidInput() {
      when(game.getChar()).thenReturn('5', '4', 'q');
      doNothing().when(game).dealNewHand();
      game.getNewBet();

      assertEquals(10000, game.getCurrentBet());
    }
  }

  @Nested
  @DisplayName("normalizeBet tests")
  class NormalizeBetTests {
    @Test
    void testNormalizeBet() {
      setField(game, "money", 100);
      when(game.getChar()).thenReturn('1', 'q');
      doNothing().when(game).dealNewHand();
      game.getNewBet();

      assertEquals(100, game.getCurrentBet());
    }
  }

  @Nested
  @DisplayName("dealNewHand tests")
  class DealNewHandTests {
    @Test
    void testDealNewHand() {
      when(shoe.getNextCard()).thenReturn(
          new Card(8, 0),
          new Card(7, 0),
          new Card(8, 0),
          new Card(8, 0));

      when(game.getChar()).thenReturn('s', 'q');

      game.dealNewHand();
      verify(game, times(2)).saveGame();
    }

    @Test
    void testDealNewHandNoNeedToShuffle() {
      when(shoe.needToShuffle()).thenReturn(false);
      when(game.getChar()).thenReturn('s', 'q');

      when(shoe.getNextCard()).thenReturn(
          new Card(8, 0),
          new Card(7, 0),
          new Card(8, 0),
          new Card(8, 0));

      when(game.getChar()).thenReturn('s', 'q');

      game.dealNewHand();
      verify(shoe, never()).buildNewShoe(any(int.class));
    }

    @Test
    void testDealerUpcardIsAceAskInsurance() {
      when(shoe.getNextCard()).thenReturn(
          new Card(8, 0),
          new Card(0, 0),
          new Card(8, 0),
          new Card(7, 0));

      when(game.getChar()).thenReturn('n', 's', 'q');

      game.dealNewHand();
      verify(game).askInsurance();
    }

    @Test
    void testPlayerHandIsDone() {
      when(shoe.getNextCard()).thenReturn(
          new Card(0, 0),
          new Card(3, 0),
          new Card(9, 0),
          new Card(5, 0));

      when(game.getChar()).thenReturn('q');

      game.dealNewHand();
      verify(game).payHands();
    }
  }

  @Nested
  @DisplayName("drawHands tests")
  class DrawHandsTests {
    @Test
    void testDrawHands() {
      game.getPlayerHands().add(new PlayerHand(game));
      PlayerHand playerHand = spy(game.getPlayerHands().get(0));
      doNothing().when(playerHand).getAction();

      game.drawHands();

      String output = outputStream.toString();
      assertTrue(output.contains("Dealer:"));
      assertTrue(output.contains("Player $100.00:"));
    }
  }

  @Nested
  @DisplayName("cardFace tests")
  class CardFaceTests {
    @Test
    void testCardFaceRegularFaces() {
      when(game.getFaceType()).thenReturn(1);

      assertEquals("A♠", game.cardFace(0, 0));
      assertEquals("K♦", game.cardFace(12, 3));
      assertEquals("7♥", game.cardFace(6, 1));
      assertEquals("??", game.cardFace(13, 0));
    }

    @Test
    void testCardFaceFaces2() {
      when(game.getFaceType()).thenReturn(2);

      assertEquals("🂡", game.cardFace(0, 0));
      assertEquals("🃞", game.cardFace(12, 3));
      assertEquals("🂷", game.cardFace(6, 1));
      assertEquals("🂠", game.cardFace(13, 0));
    }
  }

  @Nested
  @DisplayName("game runner tests")
  class GameRunnerTests {
    @Test
    void testRunInvokesLoop() {
      try (MockedConstruction<Game> mocked = mockConstruction(Game.class)) {
        Game.run();
        verify(mocked.constructed().get(0)).loop();
      }
    }
  }

  @Nested
  @DisplayName("payHands tests")
  class PayHandsTests {
    private DealerHand dealerHand;
    private PlayerHand playerHand;

    @BeforeEach
    void setUp() {
      doNothing().when(game).saveGame();

      dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);

      playerHand = spy(new PlayerHand(game));
      game.getPlayerHands().add(playerHand);
    }

    @Test
    void testPayHands() {
      game.payHands();
      verify(game).saveGame();
    }

    @Test
    void testPayHandsPlayerHandAlreadyPaid() {
      when(playerHand.isPaid()).thenReturn(true);

      game.payHands();
      verify(playerHand, never()).setPaid(true);
      verify(game).saveGame();
    }

    @Test
    void testPayHandsWithABustedDealerHand() {
      when(dealerHand.isBusted()).thenReturn(true);

      game.payHands();
      assertEquals(HandStatus.WON, getField(playerHand, "status", HandStatus.class));
      verify(game).saveGame();
    }
  }

  @Nested
  @DisplayName("needToPlayDealerHand tests")
  class NeedToPlayDealerHandTests {
    private PlayerHand playerHand;

    @BeforeEach
    void setUp() {
      playerHand = spy(new PlayerHand(game));
      game.getPlayerHands().add(playerHand);
    }

    @Test
    void testNeedToPlayDealerHand() {
      when(playerHand.isBlackjack()).thenReturn(false);
      when(playerHand.isBusted()).thenReturn(false);

      assertTrue(game.needToPlayDealerHand());
    }

    @Test
    void testNeedToPlayDealerHandPlayerBusted() {
      when(playerHand.isBusted()).thenReturn(true);

      assertFalse(game.needToPlayDealerHand());
    }

    @Test
    void testNeedToPlayDealerHandPlayerHasBlackjack() {
      when(playerHand.isBlackjack()).thenReturn(true);

      assertFalse(game.needToPlayDealerHand());
    }
  }

  @Nested
  @DisplayName("noInsurance tests")
  class NoInsuranceTests {
    @Test
    void testNoInsurance() {
      PlayerHand playerHand = spy(new PlayerHand(game));
      game.getPlayerHands().add(playerHand);

      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);
      when(dealerHand.isBlackjack()).thenReturn(false);

      when(game.getChar()).thenReturn('s', 'q');
      doNothing().when(game).playDealerHand();

      game.noInsurance();
      verify(playerHand).getAction();
    }

    @Test
    void testNoInsuranceDealerHasBlackjack() {
      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);
      when(dealerHand.isBlackjack()).thenReturn(true);

      when(game.getChar()).thenReturn('q');

      game.noInsurance();
      verify(game).payHands();
    }

    @Test
    void testNoInsuranceDealerDoesNotHaveBlackjack() {
      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);
      when(dealerHand.isBlackjack()).thenReturn(false);
      doNothing().when(game).playDealerHand();

      PlayerHand playerHand = spy(new PlayerHand(game));
      game.getPlayerHands().add(playerHand);
      when(playerHand.isDone()).thenReturn(true);

      when(game.getChar()).thenReturn('q');

      game.noInsurance();
      verify(game).playDealerHand();
    }
  }

  @Nested
  @DisplayName("playDealerHand tests")
  class PlayDealerHandTests {
    @Test
    void testPlayDealerHand() {
      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);
      game.playDealerHand();

      assertTrue(getField(dealerHand, "played", Boolean.class));
      verify(game).payHands();
    }

    @Test
    void testPlayDealerHandDealerHasBlackjack() {
      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);
      when(dealerHand.isBlackjack()).thenReturn(true);
      game.playDealerHand();

      assertFalse(getField(dealerHand, "hideDownCard", Boolean.class));
      verify(game).payHands();
    }

    @Test
    void testPlayDealerHandWithCardsDealtSoftCount() {
      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);

      when(game.needToPlayDealerHand()).thenReturn(true);

      when(shoe.getNextCard()).thenReturn(
          new Card(4, 0),
          new Card(4, 0),
          new Card(4, 0),
          new Card(1, 0));

      game.playDealerHand();

      assertTrue(getField(dealerHand, "played", Boolean.class));
      verify(game).payHands();
    }

    @Test
    void testPlayDealerHandWithCardsDealtHardCount() {
      DealerHand dealerHand = spy(new DealerHand(game));
      setField(game, "dealerHand", dealerHand);

      when(game.needToPlayDealerHand()).thenReturn(true);

      when(shoe.getNextCard()).thenReturn(
          new Card(4, 0),
          new Card(4, 0),
          new Card(4, 0),
          new Card(2, 0));

      game.playDealerHand();

      assertTrue(getField(dealerHand, "played", Boolean.class));
      verify(game).payHands();
    }
  }

  @Nested
  @DisplayName("askInsurance Tests")
  class AskInsuranceTests {
    @Test
    @DisplayName("askInsurance should insure hand if the player wants insurance")
    void testAskInsuranceYes() {
      when(game.getChar()).thenReturn('y');
      doNothing().when(game).insureHand();

      game.askInsurance();
      verify(game).insureHand();
    }

    @Test
    @DisplayName("askInsurance should not insure hand if the player does not want insurance")
    void testAskInsuranceNo() {
      when(game.getChar()).thenReturn('n');
      doNothing().when(game).noInsurance();

      game.askInsurance();
      verify(game).noInsurance();
    }

    @Test
    @DisplayName("askInsurance should ask again if the player does not enter y or n")
    void testAskInsuranceInvalidResponse() {
      when(game.getChar()).thenReturn('x', 'n');
      doNothing().when(game).noInsurance();

      game.askInsurance();
      verify(game).noInsurance();
    }
  }

  @Nested
  @DisplayName("getChar Tests")
  class GetCharTests {
    @Test
    @DisplayName("getChar should return the first character of the input")
    void testGetChar() throws IOException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader reader = spy(bufferedReader);

      when(game.getReader()).thenReturn(reader);
      when(reader.read()).thenReturn(97);

      assertEquals('a', game.getChar());
    }

    @Test
    @DisplayName("getChar can throw an IOException")
    void testGetCharIOException() throws IOException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader reader = spy(bufferedReader);

      when(game.getReader()).thenReturn(reader);
      when(reader.read()).thenThrow(new IOException("Read failed"));

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> game.getChar());

      assertEquals("Error reading input: Read failed", exception.getMessage());
      assertInstanceOf(IOException.class, exception.getCause());
      verify(reader).read();
    }
  }

  @Nested
  @DisplayName("loop Tests")
  class LoopTests {
    @Test
    @DisplayName("loop should run when not quitting")
    void testLoop() {
      AtomicInteger count = new AtomicInteger(0);

      willAnswer(invocation -> {
        if (count.incrementAndGet() >= 2) {
          setField(game, "quitting", true);
        }

        return null;
      }).given(game).dealNewHand();

      game.loop();

      verify(game, times(2)).dealNewHand();
    }
  }

  @Nested
  @DisplayName("saveGame Tests")
  class SaveGameTests {
    private String readSaveFile() throws IOException {
      try {
        BufferedReader lineReader = new BufferedReader(new FileReader(SAVE_FILE));
        return lineReader.readLine();
      } catch (FileNotFoundException e) {
        throw new RuntimeException("Save file not found: " + e.getMessage());
      }
    }

    @Test
    @DisplayName("saveGame should save the game state to a file")
    public void testSaveGame() throws IOException {
      game.saveGame();

      String savedContent = readSaveFile();
      assertEquals("1|10000|500|1|1", savedContent);
    }

    @Test
    @DisplayName("saveGame should ignore exception if the save file cannot be written")
    public void testSaveGameCannotWriteFile() throws IOException {
      File mockFile = new File(SAVE_FILE);
      assertTrue(mockFile.createNewFile());
      assertTrue(mockFile.setReadOnly());

      game.saveGame();

      assertTrue(mockFile.setWritable(true));
    }
  }

  @Nested
  @DisplayName("moreHandsToPlay Tests")
  class MoreHandsToPlayTests {
    @Test
    @DisplayName("returns true when there are more split hands left to play")
    public void testMoreHandsToPlay() {
      game.getPlayerHands().add(new PlayerHand(game));
      assertFalse(game.moreHandsToPlay());

      game.getPlayerHands().add(new PlayerHand(game));
      assertTrue(game.moreHandsToPlay());
    }
  }

  @Nested
  @DisplayName("loadGame Tests")
  class LoadGameTests {
    private void createSaveFile(String content) throws IOException {
      try (FileWriter writer = new FileWriter(SAVE_FILE)) {
        writer.write(content);
      }
    }

    @Test
    @DisplayName("loadGame should load data if the save file exists")
    public void testLoadGameWithValidData() throws IOException {
      String saveData = "8|10000|500|1|2";
      createSaveFile(saveData);

      game.loadGame();

      assertEquals(8, game.getNumDecks());
      assertEquals(10000, game.getMoney());
      assertEquals(500, game.getCurrentBet());
      assertEquals(1, getField(game, "deckType", Integer.class));
      assertEquals(2, getField(game, "faceType", Integer.class));
    }

    @Test
    @DisplayName("loadGame should not throw an exception if the save file does not exist")
    public void testLoadGameWithNoSaveFile() {
      game.loadGame();

      assertEquals(1, game.getNumDecks());
      assertEquals(10000, game.getMoney());
      assertEquals(500, game.getCurrentBet());
      assertEquals(1, getField(game, "deckType", Integer.class));
      assertEquals(1, getField(game, "faceType", Integer.class));
    }

    @Test
    @DisplayName("loadGame should not load save file if it's malformed")
    public void testLoadGameWithMalformedSaveFile() throws IOException {
      String saveData = "8|";
      createSaveFile(saveData);

      game.loadGame();

      assertEquals(1, game.getNumDecks());
      assertEquals(10000, game.getMoney());
      assertEquals(500, game.getCurrentBet());
      assertEquals(1, getField(game, "deckType", Integer.class));
      assertEquals(1, getField(game, "faceType", Integer.class));
    }

    @Test
    @DisplayName("loadGame should give money to the poor")
    public void testLoadGameGivesMoneyToThePoor() throws IOException {
      String saveData = "8|0|500|1|2";
      createSaveFile(saveData);

      game.loadGame();

      assertEquals(8, game.getNumDecks());
      assertEquals(10000, game.getMoney());
      assertEquals(500, game.getCurrentBet());
      assertEquals(1, getField(game, "deckType", Integer.class));
      assertEquals(2, getField(game, "faceType", Integer.class));
    }
  }
}
