import java.util.Random;

/**
 * Class responsible for sorting arrays with the quicksort algorithm.
 * The class also uses help algorithms for sorting arrays (boblesort and median3sort).
 * The algorithms are written with the inspirtation of the book "Algoritmer og datastrukturer"
 * by Helge Hafting and Mildrid Ljosland, chapter 3 "Sortering".
 */
public class Sorting {
  private static Random random = new Random();
  private static int[] unsortedNumbers = new int[1000000];
  private static int threshold = 50;

  public static void main(String[] args) {
    fillArrayWithRandomNumbers(unsortedNumbers, false);
    testQuicksort();    
    for (int i = 2; i <= threshold; i++) {
      System.out.println();
      testQuickSortWithNThreshold(i);
    }    
  }

  public static void testQuicksort() {
    int[] copiedArray = unsortedNumbers.clone();    
    int sumArrayBefore = sumOfArray(copiedArray);   

    // Ta tiden
    long startTime = System.currentTimeMillis();
    quicksort(copiedArray, 0, copiedArray.length - 1);    
    long endTime = System.currentTimeMillis();

    System.out.printf("Brukte %d ms på vanlig quicksort \n", endTime - startTime);
    int sumArrayAfter = sumOfArray(copiedArray);    
    System.out.println("Test sortert riktig: " + test(copiedArray));
    System.out.println("Samme tall før som etter: " + (sumArrayBefore - sumArrayAfter == 0));
  }
  
  public static void testQuickSortWithNThreshold(int threshold) {
    int[] copiedArray = unsortedNumbers.clone();
    int sumArrayBefore = sumOfArray(copiedArray);        

    long startTime = System.currentTimeMillis();
    quicksortWithBubble(copiedArray, 0, copiedArray.length - 1, threshold);    
    long endTime = System.currentTimeMillis();

    System.out.printf("Brukte %d ms med threshold på %d\n", endTime - startTime, threshold);
    int sumArrayAfter = sumOfArray(copiedArray);    
    System.out.println("Test sortert riktig: " + test(copiedArray));
    System.out.println("Samme tall før som etter: " + (sumArrayBefore - sumArrayAfter == 0));

    // Test med sortert tabell
    sumArrayBefore = sumOfArray(copiedArray); 

    long startTime2 = System.currentTimeMillis();
    quicksortWithBubble(copiedArray, 0, copiedArray.length - 1, threshold);    
    long endTime2 = System.currentTimeMillis();

    System.out.printf("Brukte %d ms med threshold på %d på sortert tabell\n", endTime2 - startTime2, threshold);
    sumArrayAfter = sumOfArray(copiedArray);    
    System.out.println("Test sortert riktig: " + test(copiedArray));
    System.out.println("Samme tall før som etter: " + (sumArrayBefore - sumArrayAfter == 0));
  }

  /**
   * Method for sorting the arrays with the quicksort algorithm.
   *
   * @param t (int[]) array
   * @param v (int) start index
   * @param h (int) end index
   */
  public static void quicksort(int []t, int v, int h) {
    if (h - v > 2) {
      int delepos = split(t, v, h);
      quicksort(t, v, delepos - 1);
      quicksort(t, delepos + 1, h);
    } else {
      median3sort(t, v, h);
    }
  }

  /**
   * Method for sorting the arrays with the quicksort algorithm.
   *
   * @param t (int[]) array
   * @param v (int) start index
   * @param h (int) end index
   */
  public static void quicksortWithBubble(int []t, int v, int h, int trehsold) {
    if (h - v > trehsold) { //Bytte ut 2 ned noe, for å finne ut når det er hensiktsmessig å benytte en hjelpealgoritme
      int delepos = split(t, v, h);
      quicksortWithBubble(t, v, delepos - 1, trehsold);
      quicksortWithBubble(t, delepos + 1, h, trehsold);
    } else {
      //median3sort(t, v, h); //Bytte til enten innsetningssortering eller boblesortering
      boblesort(t, v, h);
    }
  }

  /**
   * Private help method for the boblesort algorithm.
   * Swaps the integers in the array if the integer with index j
   * is bigger than the integer with index j + 1.
   *
   * @param t (int[]) array
   */
  private static void boblesort(int [] t, int v, int h) { //Kjøretid: Theta(n^2)
    for (int i = h; i > v; --i) {
      for (int j = v; j < i; ++j) {
        if (t[j] > t[j + 1]) {
            swap(t, j, j+1);
        }
      }
    }
  }

  /**
   * Private help method for splitting the array into two part arrays,
   * and placing the integers with an index bigger or smaller than the index of the pivot.
   *
   * @param t (int[]) array
   * @param v (int) start index
   * @param h  (int) end index
   * @return iv (int) index of the pivot
   */
  private static int split(int[]t, int v, int h) {
    int iv;
    int ih;
    int m = median3sort(t, v, h);
    int dv = t[m];
    swap(t, m, h - 1);
    for (iv = v, ih = h - 1;;) {
      while (t[++iv] < dv);
      while (t[--ih] > dv);
      if (iv >= ih) {
        break;
      }
      swap(t, iv, ih);
    }
    swap(t, iv, h - 1);
    return iv;
  }

  /**
   * Private help method for finding the median of the array and sorting the three values.
   *
   * @param t (int[]) array
   * @param v (int) start index
   * @param h  (int) end index
   * @return m (int) median
   */
 private static int median3sort(int[] t, int v, int h) {
    int m = (v + h) / 2;
    if (t[v] > t[m]) {
      swap(t, v, m);
    }
    if (t[m] > t[h]) {
      swap(t, m, h);
      if (t[v] > t[m]) {
        swap(t, v, m);
      }
    }
    return m;
  }

  /**
   * Private help method for switching the values of the array.
   *
   * @param t (int[]) array
   * @param i (int) index of one of the integers
   * @param j (int) index of the other integer
   */
  private static void swap(int[] t, int i, int j) {
    int k = t[j];
    t[j] = t[i];
    t[i] = k;
  }

  /**
   * Private help method for filling an array with random numbers.
   * Differentiating between unsorted lists with and without duplicates.
   *
   * @param array (int[])
   */
  private static void fillArrayWithRandomNumbers(int[] array, boolean withDuplicates) {
    for (int i = 0; i < array.length; i++) {
      array[i] = random.nextInt(1000000); //Fyller listen med random tall mellom 0 og 1 mill

      if (withDuplicates && i % 2 == 0) { //Fyller listen med 42 på hver annen plass, resten er random tall
        array[i] = 42;
      } else {
        array[i] = random.nextInt(1000000);
      }
    }
  }

  /**
   * Private help method for calculating the sum of an array.
   *
   * @param array (int[])
   * @return sum (int) sum of the array
   */
  private static int sumOfArray(int[] array) {
    int sum = 0;
    for (int i = 0; i < array.length; i++) {
      sum += array[i];
    }
    return sum;
  }

  private static boolean test(int[] array) {
    for (int i = 1; i < array.length - 1; i++) {
      if (array[i] < array[i - 1]) {
        return false;
      }
    }
    return true;
  }
}


