// Telle opp antall unike tegn

// Hvert tegn får en egen definert byte-kode

// Må lagre først informasjon om hvor mange bytes charsette skal lagre for hvert tegn. Mellom 1-4. Denne informasjonen kan skrives som 3 bits 111 = 4+2+1 = 7

// Må så lagre informasjon om hvor mange unike tegn som skal inn i charsettet

import java.util.Arrays;
import java.util.HashMap;

// Må lagre først tegnet som tegnet selv, så dens nye byte-kode
class BillingtonTegnsett {
    char[] tegn;
    byte[][] tegnKoder;
    int antallBytePerTegn;

    public BillingtonTegnsett(int antallBytePerTegn, int antallUnikeTegn) {
        tegn = new char[antallUnikeTegn];
        tegnKoder = new byte[antallUnikeTegn][antallBytePerTegn];  // Swapped dimensions
        this.antallBytePerTegn = antallBytePerTegn;
    }

    private byte nextByteValue = 0;

    public void leggTilNyttTegn(char nyttTegn) {
        for (int i = 0; i < tegn.length; i++) {
            if (tegn[i] == '\0') {
                tegn[i] = nyttTegn;
                for (int j = 0; j < antallBytePerTegn; j++) {
                    tegnKoder[i][j] = nextByteValue++;  // Increment nextByteValue for each byte
                }
                break;
            }
        }
    }

    public void lastInnTegn(char nyttTegn, byte[] kode) {
        for (int i = 0; i < tegn.length; i++) {
            if (tegn[i] == '\0') {
                tegn[i] = nyttTegn;
                for (int j = 0; j < kode.length && j < antallBytePerTegn; j++) {
                    tegnKoder[i][j] = kode[j];
                }
                break;
            }
        }
    }

    public char tegnKodeTilTegn(byte[] kode) {
        for (int i = 0; i < tegn.length; i++) {
            boolean match = true;
            for (int j = 0; j < kode.length && j < antallBytePerTegn; j++) {
                if (tegnKoder[i][j] != kode[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return tegn[i];
            }
        }
        return '\0';
    }

    public byte[] tegnTilTegnKode(char søkeTegn) {
        for (int i = 0; i < tegn.length; i++) {
            if (tegn[i] == søkeTegn) {
                // Returner en kopi av tegnkoden for det spesifikke tegnet
                return Arrays.copyOf(tegnKoder[i], antallBytePerTegn);
            }
        }
        return null;  // Returner null hvis tegnet ikke finnes i tegnsettet
    }

    public boolean tegnAlleredeLagtTil(char nyttTegn) {
        for (char nåværendeTegn : tegn) {
            if (nåværendeTegn == nyttTegn) {
                return true;
            }
        }
        return false;
    }
}