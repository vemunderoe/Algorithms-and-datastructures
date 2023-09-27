import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Hasher {
    public static void main(String[] args) {
        String filNavn = "navn.txt";
        String linje;
        HashTabell hashTabell = new HashTabell();
        try (BufferedReader filLeser = new BufferedReader(new FileReader(filNavn))) {
            while ((linje = filLeser.readLine()) != null) {
                hashTabell.leggInn(linje);
            }
            System.out.println("Oppslag: ");
            System.out.println(hashTabell.hentUt("Birthe Emilie Christiansen"));
            System.out.println(hashTabell.hentUt("Vemund Ellingsson Røe"));
            System.out.println(hashTabell.hentUt("Håkon Rene Billingstad"));
            System.out.println(hashTabell.hentUt("Ole Fredrik Høivang Heggum"));
            System.out.println(hashTabell.hentUt("Anders Emil Bergan"));
            System.out.println();
            System.out.println("Kollisjoner mellom: ");
            for (Hash hash : hashTabell.hashTabell) {
                if (hash != null && hash.neste != null) {
                    String kollisjonerMellom = hash.verdi;
                    while (hash.neste != null) {
                        kollisjonerMellom += " og " + hash.neste.verdi;
                        hash = hash.neste;
                    }
                    System.out.println(kollisjonerMellom);
                }
            }
            System.out.println();
            System.out.println("Lastfaktor: " + (double) hashTabell.antall / hashTabell.hashTabell.length);
            System.out.println("Antall kollisjoner: " + hashTabell.kollisjoner);
            System.out.println("Gjennomsnittlig antall kollisjoner per person: " + (double) hashTabell.kollisjoner / hashTabell.antall);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class HashTabell {
    Hash[] hashTabell = new Hash[149];
    int antall = 0;
    int kollisjoner = 0;

    public Hash hentUt(String string) {
        int hash = stringhash(string, hashTabell.length);
        while (hashTabell[hash] != null && !hashTabell[hash].verdi.equals(string)) {
            hashTabell[hash] = hashTabell[hash].neste;
        }
        return hashTabell[hash];
    }

    public void leggInn(String string) {
        int hash = stringhash(string, hashTabell.length);
        if (hashTabell[hash] != null) {
            Hash hashObjekt = hashTabell[hash];
            while (hashObjekt.neste != null) {
                hashObjekt = hashObjekt.neste;
            }
            hashObjekt.neste = new Hash(hash, string);
            kollisjoner++;
        } else {
            hashTabell[hash] = new Hash(hash, string);
        }
        antall++;
    }

    private int stringhash(String s, int n) {
        int hash = 0;
        for (int i = s.length(); i > 0; i--) {
            hash = (7 * hash + s.charAt(i - 1)) % n;
        }
        return hash ;
    }

    public String toString() {
        StringBuilder hashTabellString = new StringBuilder();
        for (Hash hash : hashTabell) {
            hashTabellString.append(hash).append("\n");
        }
        return hashTabellString.toString();
    }
}

class Hash {
    int nøkkel;
    String verdi;
    Hash neste = null;

    public Hash(int nøkkel, String verdi) {
        this.nøkkel = nøkkel;
        this.verdi = verdi;
    }

    public String toString() {
        return "Hash: " + nøkkel + ", Verdi: " + verdi + ", Neste: " + neste;
    }
}
