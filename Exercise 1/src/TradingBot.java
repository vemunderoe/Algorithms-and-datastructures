import java.util.Date;
import java.util.Random;

public class TradingBot {
  public static Random random = new Random();
  public static void main(String[] args) {
    int[] priceChanges = {-1, 3, -9, 2, 2, -1, 2, -1, -5};
    int[] priceChanges2 = {-1, -1, -1, -1, -1, -1};
    int days = 100000;
    int[] priceChanges3 = randomPriceChanges(days);

    Date startTime = new Date();
    int rounds = 0;
    int[] bestBuySellDay; // {profit, buyDay, sellDay}
    Date endTime;

    bestBuySellDay = ON(priceChanges);
    System.out.printf("Best day to buy is day %d and best day to sell is day %d\n", bestBuySellDay[1], bestBuySellDay[2]);

    // For O(n)
    do {
      bestBuySellDay = ON(priceChanges3);
      endTime = new Date();
      rounds++;
    } while (endTime.getTime() - startTime.getTime() < 1000);

    double time = (double) (endTime.getTime() - startTime.getTime()) / rounds;
    System.out.println("Milliseconds per round: " + time);
    System.out.printf("Best day to buy is day %d and best day to sell is day %d\n", bestBuySellDay[1], bestBuySellDay[2]);

    // For O(n^2)
    do {
      bestBuySellDay = ON2(priceChanges3);
      endTime = new Date();
      rounds++;
    } while (endTime.getTime() - startTime.getTime() < 1000);

    time = (double) (endTime.getTime() - startTime.getTime()) / rounds;
    System.out.println("Milliseconds per round: " + time);
    System.out.printf("Best day to buy is day %d and best day to sell is day %d\n", bestBuySellDay[1], bestBuySellDay[2]);
  }

  public static int[] randomPriceChanges(int length) {
    int[] priceChanges = new int[length];
    for (int i = 0; i < length; i++) {
      priceChanges[i] = random.nextInt(-10,10);
    }
    return priceChanges;
  }

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

