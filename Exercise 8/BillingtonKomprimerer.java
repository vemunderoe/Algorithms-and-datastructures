import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class BillingtonKomprimerer {
    private static BillingtonTegnsett billingtonTegnsett;
    public static void main(String[] args) {
        String filnavn = (args.length > 0) ? args[0] : "diverse.lyx";
        String utfilNavn = (args.length > 1) ? args[1] : "diverse-komprimert.billington";

        System.out.println("Started: " + new Date());

        HashSet<Character> unikeTegn = new HashSet<>();
        StringBuilder tekst = new StringBuilder();

        try (BufferedReader innfil = new BufferedReader(
                new InputStreamReader(new FileInputStream(filnavn), StandardCharsets.UTF_8))) {
            int indeksTilTegn;
            while ((indeksTilTegn = innfil.read()) != -1) {
                char tegn = (char) indeksTilTegn;
                tekst.append(tegn);
                unikeTegn.add(tegn);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        System.out.println(unikeTegn.size());

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

        billingtonTegnsett = new BillingtonTegnsett(antallBytesPerTegn, unikeTegn.size());

        for (char tegn : unikeTegn) {
            billingtonTegnsett.leggTilNyttTegn(tegn);
        }
        // Komprimer med lzw til en liste med tall
        List<Integer> lzwKomprimerteTall = lzw(billingtonTegnsett, tekst.toString());

        try {
            skrivLZWTilFil(lzwKomprimerteTall, utfilNavn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done: " + new Date());
    }

    public static List<Integer> lzw(BillingtonTegnsett billingtonTegnsett, String tekst) {
        HashMap<String, Integer> ordliste = new HashMap<>();

        // Initialiser ordlisten med tegn fra BillingtonTegnsett
        for (int i = 0; i < billingtonTegnsett.tegn.length; i++) {
            ordliste.put("" + billingtonTegnsett.tegn[i], i);
        }

        List<Integer> resultat = new ArrayList<>();
        String nåværendeSekvens = "";
        for (char tegn : tekst.toCharArray()) {
            String nySekvens = nåværendeSekvens + tegn;
            if (ordliste.containsKey(nySekvens)) {
                nåværendeSekvens = nySekvens;
            } else {
                resultat.add(ordliste.get(nåværendeSekvens));
                ordliste.put(nySekvens, ordliste.size());
                nåværendeSekvens = "" + tegn;
            }
        }

        // Håndter siste sekvens
        if (!nåværendeSekvens.isEmpty()) {
            resultat.add(ordliste.get(nåværendeSekvens));
        }

        return resultat;
    }

    public static void skrivLZWTilFil(List<Integer> lzwKomprimerteTall, String utfilNavn) throws IOException {
        try (DataOutputStream utfil = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(utfilNavn))))  {
            // Skriv Billington tegnsett til fil med antall bytes per tegn og hvor mange unike tegn fulgt av
            // Selve tegnet med UTF-8, billington tegnkoden
            utfil.write(billingtonTegnsett.antallBytePerTegn);
            utfil.write(billingtonTegnsett.tegn.length);

            for (char tegn : billingtonTegnsett.tegn) {
                utfil.writeUTF("" + tegn);  // Skriv tegn
                byte[] tegnKode = billingtonTegnsett.tegnTilTegnKode(tegn);
                utfil.write(tegnKode);  // Skriv tegnkode
            }

            int størrelseAvBillingtonTegnsettet = utfil.size();
            System.out.println("Filstørrelse av billington tegnsettet: " + størrelseAvBillingtonTegnsettet);

            int størsteTall = Collections.max(lzwKomprimerteTall);
            int antallBytes = (int) Math.ceil(Math.log(størsteTall + 1) / Math.log(256));

            // Skriv ut antall bits brukt per LZW tall for dekoderen.
            utfil.write(antallBytes);

            List<Byte> outputBuffer = new ArrayList<>();
            // Skriv LZW-komprimerte tall til filen.
            for (int indeks : lzwKomprimerteTall) {
                ByteBuffer buffer = ByteBuffer.allocate(antallBytes);
                for (int i = 0; i < antallBytes; i++) {
                    int shift = (antallBytes - 1 - i) * 8;
                    byte b = (byte) ((indeks >> shift) & 0xFF);
                    outputBuffer.add(b);
                }
            }

            for (byte b : outputBuffer) {
                utfil.write(b);
            }
        }
    }
}
