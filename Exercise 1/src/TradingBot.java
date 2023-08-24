import java.util.Date;
import java.util.Random;

public class TradingBot {
  public static Random random = new Random();
  public static void main(String[] args) {
    int[] priceChanges = {-1, 3, -9, 2, 2, -1, 2, -1, -5};

    // Test ON with priceChanges
    int[] bestBuySellDay; // {profit, buyDay, sellDay}
    bestBuySellDay = ON(priceChanges);
    System.out.println("### Test of O(n) with priceChanges from text-book ###");
    System.out.printf("Best day to buy is day %d and best day to sell is day %d\n", bestBuySellDay[1], bestBuySellDay[2]);

    System.out.println();
    System.out.println("### Time Complexity O(n) ###");
    System.out.println("n-days - milliseconds");
    testONWithNDays(100000);
    testONWithNDays(1000000);
    testONWithNDays(10000000);

    System.out.println();
    System.out.println("### Time Complexity O(n^2) ###");
    System.out.println("n-days - milliseconds");
    testON2WithNDays(1000);
    testON2WithNDays(10000);
    testON2WithNDays(100000);
  }

  /**
   * Method to test the time complexity of O(n) with n-days
   * @param nDays n-days
   */
  public static void testONWithNDays(int nDays) {
    int[] bestBuySellDay; // {profit, buyDay, sellDay}
    int[] priceChangesNDays = randomPriceChanges(nDays);
    Date startTime = new Date();
    Date endTime;
    int rounds = 0;
    do {
      bestBuySellDay = ON(priceChangesNDays);
      endTime = new Date();
      rounds++;
    } while (endTime.getTime() - startTime.getTime() < 1000);

    double time = (double) (endTime.getTime() - startTime.getTime()) / rounds;
    System.out.printf("%d - %.2f\n", nDays, time);
  }

  /**
   * Method to test the time complexity of O(n^2) with n-days
   * @param nDays n-days
   */
  public static void testON2WithNDays(int nDays) {
    int[] bestBuySellDay; // {profit, buyDay, sellDay}
    int[] priceChangesNDays = randomPriceChanges(nDays);
    Date startTime = new Date();
    Date endTime;
    int rounds = 0;
    do {
      bestBuySellDay = ON2(priceChangesNDays);
      endTime = new Date();
      rounds++;
    } while (endTime.getTime() - startTime.getTime() < 1000);

    double time = (double) (endTime.getTime() - startTime.getTime()) / rounds;
    System.out.printf("%d - %.2f\n", nDays, time);
  }

  /**
   * Method to generate an array of random price changes with n-days
   * @param length n-days
   * @return array of random price changes
   */
  public static int[] randomPriceChanges(int length) {
    int[] priceChanges = new int[length];
    for (int i = 0; i < length; i++) {
      priceChanges[i] = random.nextInt(-10,10);
    }
    return priceChanges;
  }

  /**
   * Method to find the best day to buy and sell stock with O(n^2) time complexity
   * @param priceChanges array of price changes
   * @return array of best buy-day and sell-day
   */
  public static int[] ON2(int[] priceChanges) {
    int[] bestBuySellDay = {0, 0, 0}; // {profit, buyDay, sellDay}

    for (int i = 0; i < priceChanges.length; i++) {
      int priceChange = 0;
      for (int j = i + 1; j < priceChanges.length; j++) {
        priceChange += priceChanges[j];
        if (priceChange > bestBuySellDay[0]) {
          bestBuySellDay[0] = priceChange;
          bestBuySellDay[1] = i + 1;
          bestBuySellDay[2] = j + 1;
        }
      }
    }

    return bestBuySellDay;
  }

  /**
   * Method to find the best day to buy and sell stock with O(n) time complexity
   * @param priceChanges array of price changes
   * @return array of best buy-day and sell-day
   */
  public static int[] ON(int[] priceChanges) {
    // create array of stock value from the price changes
    int[] stockValues = new int[priceChanges.length + 1];
    stockValues[0] = 0; // stock value on day 0 is 0
    for (int i = 0; i < priceChanges.length; i++) {
      stockValues[i + 1] = stockValues[i] + priceChanges[i];
    }

    int[] bestStockPurchase = {0, 0, 0}; // {lowest value, buyDay, sellDay}

    // Loop through stock values and find the largest difference between
    for (int i = 0; i < priceChanges.length; i++) {
      if (stockValues[i] < stockValues[bestStockPurchase[0]]) {
        bestStockPurchase[0] = i;
      }

      // Check if current change value and current lowest value is greater than the current best profit
      if (stockValues[i] - stockValues[bestStockPurchase[0]] > stockValues[bestStockPurchase[2]] - stockValues[bestStockPurchase[1]]) {
        bestStockPurchase[1] = bestStockPurchase[0];
        bestStockPurchase[2] = i;
      }
    }

    return bestStockPurchase;
  }
}

