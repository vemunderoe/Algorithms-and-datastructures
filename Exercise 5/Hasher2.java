import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Hasher2 {
    static Random random = new Random();
    public static void main(String[] args) {
        int tableSize = 10000019;
        int numberOfValues = 10000000;
        double[] fillRatios = new double[]{0.5, 0.8, 0.9, 0.99, 1};
        long[] values = generateRandomUniqueArray(numberOfValues);

        long testSearchValue = values[random.nextInt(tableSize - 1)];

        for (double fillRatio : fillRatios) {
            HashTable linearProbingHashTable = new LinearProbingHashTable(tableSize);
            testHashTable(linearProbingHashTable, values, fillRatio);

            HashTable doubleHashingHashTable = new DoubleHashingHashTable(tableSize);
            testHashTable(doubleHashingHashTable, values, fillRatio);

            int indexLinearProbingHash = linearProbingHashTable.search(testSearchValue);
            System.out.println("Index of test value (" + testSearchValue +  ") is: " + indexLinearProbingHash + " in linearProbingHashTable");

            int indexDoubleHashingHash = doubleHashingHashTable.search(testSearchValue);
            System.out.println("Index of test value (" + testSearchValue +  ") is: " + indexDoubleHashingHash + " in doubleHashingHashTable");
            System.out.println("------------------------------");
        }

    }

    public static void testHashTable(HashTable hashTable, long[] values, double fillRatio) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < values.length * fillRatio; i++) {
            hashTable.insert(values[i]);
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Hash Table Type: " + hashTable.getClass().getSimpleName());
        System.out.println("Fill Ratio: " + (fillRatio * 100) + "%");
        System.out.println("Collisions: " + hashTable.collisions);
        System.out.println("Time (ms): " + (endTime - startTime));
        System.out.println("------------------------------");
    }

    public static long[] generateRandomUniqueArray(int tableSize) {
        long[] array = new long[tableSize];
        array[0] = random.nextInt(1, 999);
        for (int i = 1; i < array.length; i++) {
            array[i] = array[i-1] + random.nextInt(1, 999);
        }

        // Shuffle array
        List<Long> list = LongStream.of(array).boxed().collect(Collectors.toList());
        Collections.shuffle(list);

        return list.stream().mapToLong(l -> l).toArray();
    }
}

abstract class HashTable {
    long[] table;
    int tableSize;
    long collisions = 0;

    public HashTable(int tableSize) {
        table = new long[tableSize];
        this.tableSize = tableSize;
    }

    public abstract void insert(long value);
    public abstract int search(long value);

    public int primaryHash(long value) {
        return (int) (value % tableSize);
    }

    public int secondaryHash(long value) {
        return (int) (value % (tableSize - 1) + 1);
    }

    public long[] getTable() {
        return table;
    }
}

class LinearProbingHashTable extends HashTable {

    public LinearProbingHashTable(int tableSize) {
        super(tableSize);
    }

    @Override
    public void insert(long value) {
        int hash = primaryHash(value);
        int initialHash = hash;
        while (table[hash] != 0) {
            collisions++;
            hash = primaryHash(hash + 1);
            if (hash == initialHash) {
                return;
            }
        }
        table[hash] = value;
    }

    @Override
    public int search(long value) {
        int hash = primaryHash(value);
        int initialHash = hash;
        while (table[hash] != value) {
            hash = primaryHash(hash + 1);
            if (hash == initialHash) {
                return -1;
            }
        }
        return hash;
    }
}

class DoubleHashingHashTable extends HashTable {

    public DoubleHashingHashTable(int tableSize) {
        super(tableSize);
    }

    @Override
    public void insert(long value) {
        int hash = primaryHash(value);
        if (table[hash] == 0) {
            table[hash] = value;
            return;
        }
        int hash2 = secondaryHash(value);
        for (;;) {
            collisions++;
            hash = primaryHash(hash + hash2);
            if (table[hash] == 0) {
                table[hash] = value;
                return;
            }
        }
    }

    @Override
    public int search(long value) {
        int hash = primaryHash(value);
        int initialHash = hash;
        if (table[hash] == value) {
            return hash;
        }
        int hash2 = secondaryHash(value);
        for (;;) {
            hash = primaryHash(hash + hash2);
            if (hash == initialHash) {
                return -1;
            }
            if (table[hash] == value) {
                return hash;
            }
        }
    }
}
