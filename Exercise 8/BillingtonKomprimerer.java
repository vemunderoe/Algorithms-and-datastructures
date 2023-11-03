import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class BillingtonKomprimerer {
  public static void main(String[] args) {
    String filnavn = (args.length > 0) ? args[0] : "diverse.txt";
    String utfilNavn = (args.length > 1) ? args[1] : "diverse-komprimert.billington";
    
    List<Character> unikeTegn = new ArrayList();
    String tekst = "";
    
    try (BufferedReader innfil = new BufferedReader(
      new InputStreamReader(new FileInputStream(filnavn), StandardCharsets.UTF_8))) {
        int indeksTilTegn;
        while ((indeksTilTegn = innfil.read()) != -1) {
          char tegn = (char) indeksTilTegn;
          tekst += tegn;
          if (!unikeTegn.contains(tegn)) {
            unikeTegn.add(tegn);
          }
        }
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }

    int antallBytesPerTegn;
    if (unikeTegn.size() < 256) {
      antallBytesPerTegn = 1;
    } else if (unikeTegn.size() < 65536) {
      antallBytesPerTegn = 2;
    } else if (unikeTegn.size() < 16777216) {
      antallBytesPerTegn = 3;
    } else {
      antallBytesPerTegn = 4;
    }

    BillingtonTegnsett billingtonTegnsett = new BillingtonTegnsett(antallBytesPerTegn, unikeTegn.size());
    
    for (char tegn : unikeTegn) {
      billingtonTegnsett.leggTilNyttTegn(tegn);
    }
    try {
      komprimer(billingtonTegnsett, tekst, utfilNavn);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void komprimer(BillingtonTegnsett billingtonTegnsett, String tekst, String filnavn) throws IOException {
    List<String> ordliste = new ArrayList<>();
    List<Byte> outputBuffer = new ArrayList<>();
    List<Integer> tekstSomIntegere = new ArrayList<>();

    // Initialiser ordlisten med tegn fra BillingtonTegnsett
    for (char tegn : billingtonTegnsett.tegn) {
        ordliste.add(String.valueOf(tegn));
    }

    String nåværendeSekvens = "";
    for (char tegn : tekst.toCharArray()) {
        String nySekvens = nåværendeSekvens + tegn;
        if (ordliste.contains(nySekvens)) {
            nåværendeSekvens = nySekvens;
        } else {
            int indeks = ordliste.indexOf(nåværendeSekvens);
            tekstSomIntegere.add(indeks);
            ordliste.add(nySekvens);
            nåværendeSekvens = String.valueOf(tegn);
        }
    }

    // Håndter siste sekvens
    if (!nåværendeSekvens.isEmpty()) {
        int indeks = ordliste.indexOf(nåværendeSekvens);
        tekstSomIntegere.add(indeks);
    }

    // Finn den største indeksen for å beregne antall bytes som er nødvendig
    int størsteIndeks = Collections.max(tekstSomIntegere);
    int antallBytes = (int) Math.ceil(Math.log(størsteIndeks + 1) / Math.log(256));    

    // Gjøre om tekstSomIntegere til bytes på byte størrelse som er nødvendig
    for (int indeks : tekstSomIntegere) {
        ByteBuffer buffer = ByteBuffer.allocate(antallBytes);
        for (int i = 0; i < antallBytes; i++) {
            int shift = (antallBytes - 1 - i) * 8;
            byte b = (byte) ((indeks >> shift) & 0xFF);
            outputBuffer.add(b);
        }
    }

    // Skriv til fil
    try (DataOutputStream utfil = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filnavn)))) {
      // Skriv metadata      
      utfil.write(billingtonTegnsett.antallBytePerTegn);  // Antall bytes per tegn
      utfil.write(billingtonTegnsett.tegn.length);  // Antall unike tegn
      for (char tegn : billingtonTegnsett.tegn) {
          utfil.writeUTF("" + tegn);  // Skriv tegn
          byte[] tegnKode = billingtonTegnsett.tegnTilTegnKode(tegn);
          utfil.write(tegnKode);  // Skriv tegnkode          
      }

      // Skriv antall bytes per tegnkode
      utfil.write(antallBytes);

      // Skriv komprimert data
      for (byte b : outputBuffer) {
        utfil.write(b);
      }
    }
  }
}

// Telle opp antall unike tegn

// Hvert tegn får en egen definert byte-kode

// Må lagre først informasjon om hvor mange bytes charsette skal lagre for hvert tegn. Mellom 1-4. Denne informasjonen kan skrives som 3 bits 111 = 4+2+1 = 7

// Må så lagre informasjon om hvor mange unike tegn som skal inn i charsettet

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
