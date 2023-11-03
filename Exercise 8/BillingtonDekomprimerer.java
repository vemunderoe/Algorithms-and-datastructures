import java.io.*;
import java.nio.*;
import java.util.*;

public class BillingtonDekomprimerer {
  public static void main(String[] args) throws IOException {
    String filnavn = (args.length > 0) ? args[0] : "diverse-komprimert.billington";
    String utfilNavn = (args.length > 1) ? args[1] : "diverse-utpakket.txt";

    String dekomprimertTekst = dekomprimer(filnavn);

    skrivTilFil(dekomprimertTekst, utfilNavn);
  }
  public static String dekomprimer(String filnavn) throws IOException {
    try (DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filnavn)))) {
        int antallBytesPerUnikeTegn = fis.read();  // Les antall bytes per tegn
        int antallUnikeTegn = fis.read();  // Les antall unike tegn        

        // Les tegn og tegnkoder fra filen
        BillingtonTegnsett billingtonTegnsett = new BillingtonTegnsett(antallBytesPerUnikeTegn, antallUnikeTegn);
        for (int i = 0; i < antallUnikeTegn; i++) {           
            char tegn = fis.readUTF().charAt(0);
            byte[] tegnKode = new byte[antallBytesPerUnikeTegn];
            tegnKode = fis.readNBytes(antallBytesPerUnikeTegn);
            billingtonTegnsett.lastInnTegn(tegn, tegnKode);            
        }        

        int antallBytesPerTegn = fis.read();

        // Les den komprimerte dataen fra filen
        List<Integer> tekstSomIntegere = new ArrayList<>();
        byte[] bytes = new byte[antallBytesPerTegn];
        while (fis.read(bytes) != -1) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int indeks = 0;
            for (int i = 0; i < antallBytesPerTegn; i++) {
                indeks = (indeks << 8) | (bytes[i] & 0xFF);
            }
            tekstSomIntegere.add(indeks);
        }        

        // Dekomprimer dataen
        List<String> ordliste = new ArrayList<>();
        for (char tegn : billingtonTegnsett.tegn) {
            ordliste.add(String.valueOf(tegn));
        }

        StringBuilder dekomprimertTekst = new StringBuilder();
        String forrigeSekvens = "";
        for (int indeks : tekstSomIntegere) {
            String sekvens = indeks < ordliste.size() ? ordliste.get(indeks) : (forrigeSekvens + forrigeSekvens.charAt(0));
            dekomprimertTekst.append(sekvens);
            if (!forrigeSekvens.isEmpty()) {
                ordliste.add(forrigeSekvens + sekvens.charAt(0));
            }
            forrigeSekvens = sekvens;
        }

        return dekomprimertTekst.toString();
    }
  }

  public static void skrivTilFil(String tekst, String utFilnavn) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(utFilnavn))) {
        pw.print(tekst);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}

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