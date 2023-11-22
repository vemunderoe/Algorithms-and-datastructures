import java.io.*;
import java.nio.*;
import java.util.*;

public class BillingtonDekomprimerer {
    public static void main(String[] args) {
        String filnavn = (args.length > 0) ? args[0] : "diverse-komprimert.billington";
        String utfilNavn = (args.length > 1) ? args[1] : "diverse-utpakket.lyx";

        try {
            String dekomprimertTekst = dekomprimerTilTall(filnavn);

            skrivTilFil(dekomprimertTekst, utfilNavn);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static String dekomprimerTilTall(String filnavn) throws IOException, EOFException {
        try (DataInputStream innfil = new DataInputStream(new BufferedInputStream(new FileInputStream(filnavn)))) {
            int antallBytesPerUnikeTegn = innfil.read();  // Les antall bytes per tegn
            int antallUnikeTegn = innfil.read();  // Les antall unike tegn

            // Les tegn og tegnkoder fra filen
            BillingtonTegnsett billingtonTegnsett = new BillingtonTegnsett(antallBytesPerUnikeTegn, antallUnikeTegn);
            for (int i = 0; i < antallUnikeTegn; i++) {
                char tegn = innfil.readUTF().charAt(0);
                byte[] tegnKode = innfil.readNBytes(antallBytesPerUnikeTegn);
                billingtonTegnsett.lastInnTegn(tegn, tegnKode);
            }

            int antallBytesPerTegn = innfil.read();

            if (antallBytesPerTegn == -1) {
                throw new IllegalArgumentException("File wrongly formatted");
            }

            // Dekomprimer dataen
            List<String> ordliste = new ArrayList<>();
            for (char tegn : billingtonTegnsett.tegn) {
                ordliste.add(String.valueOf(tegn));
            }

            List<Integer> tekstSomIntegere = new ArrayList<>();
            byte[] bytes = new byte[antallBytesPerTegn];
            while (innfil.read(bytes) != -1) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                int indeks = 0;
                for (int i = 0; i < antallBytesPerTegn; i++) {
                    indeks = (indeks << 8) | (bytes[i] & 0xFF);
                }
                tekstSomIntegere.add(indeks);
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
        System.out.println(tekst);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(utFilnavn))) {
            bw.write(tekst);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}