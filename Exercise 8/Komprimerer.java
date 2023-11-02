import java.util.List;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class Komprimerer {
    public static void main(String[] args) {
        String filnavn = args[0];
        String komprimertFilnavn = args[1];

        // Initiere frekvensTabell
        FrekvensTabell frekvensTabell = new FrekvensTabell();

        // Lese inn fil
        try (BufferedReader innfil = new BufferedReader(
                new InputStreamReader(new FileInputStream(filnavn), StandardCharsets.UTF_8))) {
            int indeksTilTegn;
            while ((indeksTilTegn = innfil.read()) != -1) {
                char tegn = (char) indeksTilTegn;
                if (!frekvensTabell.harAlleredeTegn(tegn)) {
                    frekvensTabell.settInnNyttTegn(tegn);
                } else {
                    frekvensTabell.oppdaterFrekvensTilTegn(tegn);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        // Legge til i kø
        KøHåndterer køHåndterer = new KøHåndterer();
        for (char tegn : frekvensTabell.tegnListe) {
            køHåndterer.settInn(new Node(tegn, frekvensTabell.hentFrekvens(tegn)));
        }

        HuffmanTre huffmanTre = new HuffmanTre();

        while (!køHåndterer.erTom()) {
            Node nyRotNode = new Node(køHåndterer.hentNeste(), køHåndterer.hentNeste());
            huffmanTre.rotNode = nyRotNode;
            if (!køHåndterer.erTom()) {
                køHåndterer.settInn(nyRotNode);
            }
        }

        // Lage bit liste for tegnene
        huffmanTre.settOppListeMedBitStrenger();

        try (DataOutputStream utfil = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(komprimertFilnavn)))) {
            // Skriv frekvens til fil

            // Først skrive antall unike tegn, for å indikere hvor mange tegn og dens
            // frekvens man skal lese inn
            utfil.writeInt(frekvensTabell.tegnListe.size());

            // Skrive tegn og frekvens til fil
            for (char tegn : frekvensTabell.tegnListe) {
                utfil.writeChar(tegn); // Skrive tegnet
                utfil.writeInt(frekvensTabell.hentFrekvens(tegn)); // Skrive frekvensen
            }

            // Skrive til fil, sette sammen forskjellige binary strenger til bytes
            byte nesteByte = 0;
            int antallFyltePlasserIByte = 0;
            try (BufferedReader innfil = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filnavn), StandardCharsets.UTF_8))) {
                int indeksTilTegn;
                while ((indeksTilTegn = innfil.read()) != -1) {
                    char tegn = (char) indeksTilTegn;
                    String bitStreng = huffmanTre.hentUtBitStreng(tegn);
                    int lengdePåBitStreng = bitStreng.length();
                    long bits = Long.parseLong(bitStreng, 2);

                    while (lengdePåBitStreng > 0) {
                        if (lengdePåBitStreng <= 8 - antallFyltePlasserIByte) {
                            nesteByte |= (byte) (bits << (8 - antallFyltePlasserIByte - lengdePåBitStreng));
                            antallFyltePlasserIByte += lengdePåBitStreng;
                            lengdePåBitStreng = 0;
                        } else {
                            int ledigePlasser = 8 - antallFyltePlasserIByte;
                            nesteByte |= (byte) (bits >> (lengdePåBitStreng - ledigePlasser));
                            bits &= (1L << (lengdePåBitStreng - ledigePlasser)) - 1;
                            lengdePåBitStreng -= ledigePlasser;
                            antallFyltePlasserIByte = 8;
                        }

                        // En byte er fylt opp, skriv til fil
                        if (antallFyltePlasserIByte == 8) {
                            utfil.write(nesteByte);
                            nesteByte = 0;
                            antallFyltePlasserIByte = 0;
                        }
                    }

                }
            }

            // Skriv de resterende bitsene til fil
            if (antallFyltePlasserIByte > 0) {
                utfil.write(nesteByte);
            }

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}

class FrekvensTabell {
    List<Character> tegnListe = new ArrayList();
    List<Integer> frekvens = new ArrayList();

    public void settInnNyttTegn(char nyttTegn) {
        tegnListe.add(nyttTegn);
        frekvens.add(1);
    }

    public void settInnNyttTegn(char nyttTegn, int frekvens) {
        tegnListe.add(nyttTegn);
        this.frekvens.add(frekvens);
    }

    public void oppdaterFrekvensTilTegn(char tegn) {
        int indeksTilTegn = tegnListe.indexOf(tegn);
        frekvens.set(indeksTilTegn, frekvens.get(indeksTilTegn) + 1);
    }

    public int hentFrekvens(char tegn) {
        int indeksTilTegn = tegnListe.indexOf(tegn);
        return frekvens.get(indeksTilTegn);
    }

    public boolean harAlleredeTegn(char tegn) {
        return tegnListe.contains(tegn);
    }
}

class KøHåndterer {
    List<Node> kø = new ArrayList<Node>();

    public void settInn(Node node) {
        kø.add(node);
    }

    /**
     * Henter neste node med lavest frekvens
     * 
     * @return Node med lavest frekvens
     */
    public Node hentNeste() {
        Node nåværendeNodeMedLavestFrekvens = kø.get(0);
        int lavesteFrekvens = nåværendeNodeMedLavestFrekvens.frekvens;
        for (Node node : kø) {
            if (node.frekvens < lavesteFrekvens) {
                nåværendeNodeMedLavestFrekvens = node;
                lavesteFrekvens = nåværendeNodeMedLavestFrekvens.frekvens;
            }
        }
        kø.remove(nåværendeNodeMedLavestFrekvens);
        return nåværendeNodeMedLavestFrekvens;
    }

    public boolean erTom() {
        return kø.size() <= 0;
    }
}

class HuffmanTre {
    Node rotNode = null;
    List<BitStreng> bitStrenger = new ArrayList();

    // Liste med tegn som bits

    public void settOppListeMedBitStrenger() {
        traverserHuffmanTre(rotNode, "");
    }

    private void traverserHuffmanTre(Node nåværendeNode, String nåværendeBitStreng) {
        if (nåværendeNode.venstreNode != null) {
            traverserHuffmanTre(nåværendeNode.venstreNode, String.format("%s%d", nåværendeBitStreng, 0));
        }
        if (nåværendeNode.høyreNode != null) {
            traverserHuffmanTre(nåværendeNode.høyreNode, String.format("%s%d", nåværendeBitStreng, 1));
        }
        if (nåværendeNode.venstreNode == null && nåværendeNode.høyreNode == null) {
            bitStrenger.add(new BitStreng(nåværendeBitStreng, nåværendeNode.tegn));
        }
    }

    public String hentUtBitStreng(char tegn) {
        for (BitStreng bitStreng : bitStrenger) {
            if (bitStreng.tegn == tegn) {
                return bitStreng.bitStreng;
            }
        }

        return "";
    }
}

class BitStreng {
    String bitStreng;
    char tegn;

    public BitStreng(String bitStreng, char tegn) {
        this.bitStreng = bitStreng;
        this.tegn = tegn;
    }
}

class Node {
    int frekvens = 0;
    char tegn;
    Node venstreNode = null;
    Node høyreNode = null;

    public Node(Node venstreNode, Node høyreNode) {
        this.venstreNode = venstreNode;
        this.høyreNode = høyreNode;
        this.frekvens = venstreNode.frekvens + høyreNode.frekvens;
    }

    public Node(char tegn, int frekvens) {
        this.tegn = tegn;
        this.frekvens = frekvens;
    }
}