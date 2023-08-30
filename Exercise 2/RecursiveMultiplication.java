import java.util.Date;
import java.util.Random;

public class RecursiveMultiplication {
  public static Random random = new Random();
  public static void main(String[] args) {
    System.out.println();
    System.out.println("### Time table ###");
    System.out.printf("%-5s %-5s %-20s %-15s %-20s %-15s\n", "n", "x", "method 1 result", "method 1 (ms)", "method 2 result", "method 2 (ms)");
    for (int i = 10; i < 100000; i *= 10) {   
      double randomX = random.nextDouble() * 10;      
      timerMethod1(i, randomX);
      timerMethod2(i, randomX);
    }
  }

  public static double method1(int n, double x) {
    //System.out.println("Recursive call method 1");
    if (n == 1) {
      return x;
    }
    // x + (n - 1) * x
    return x + method1(n - 1, x);
  }

  public static double method2(int n, double x) {
    //System.out.println("Recursive call method 2");
    if (n == 1) {
      return x;
    }
    // Partall
    if (n % 2 == 0) {
      // (n/2) * (x + x)
      return method2(n/2, x + x);
    }
    // Oddetall
    // x + (n-1) / 2 * (x + x)
    return x + method2((n-1)/2, x + x);
  }

  public static void timerMethod1(int n, double x) {
    Date startTime = new Date();
    Date endTime;
    int rounds = 0;
    double result = 0;
    do {
      result = method1(n, x);
      endTime = new Date();
      rounds++;
    } while (endTime.getTime() - startTime.getTime() < 1000);

    double time = (double) (endTime.getTime() - startTime.getTime()) / rounds;
    System.out.printf("%-5d %-5.2f %-20.6f %-15.6f ", n, x, result, time);
  }

  public static void timerMethod2(int n, double x) {
    Date startTime = new Date();
    Date endTime;
    int rounds = 0;
    double result = 0;
    do {
      result = method2(n, x);
      endTime = new Date();
      rounds++;
    } while (endTime.getTime() - startTime.getTime() < 1000);

    double time = (double) (endTime.getTime() - startTime.getTime()) / rounds;
    System.out.printf("%-20.6f %-15.6f\n", result, time);
  }
}
