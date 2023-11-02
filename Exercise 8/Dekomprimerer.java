import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class Dekomprimerer {
    public static void main(String[] args) {
        String filnavn = args[0];
        String dekomprimertFilnavn = args[1];
        try (
                DataInputStream innfil = new DataInputStream(
                        new BufferedInputStream(new FileInputStream(filnavn)))) {
            // Lese de første 256 bytes for å lese inn frekvens tabell
            // int[] frekvensTabell = new int[256];
            FrekvensTabell frekvensTabell = new FrekvensTabell();

            // Lese hvor mange unike tegn som skal inn fra første int i filen
            int antallUnikeTegn = innfil.readInt();

            for (int i = 0; i < antallUnikeTegn; i++) {
                char tegn = innfil.readChar();
                int frekvens = innfil.readInt();
                frekvensTabell.settInnNyttTegn(tegn, frekvens);
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

            try (BufferedWriter utfil = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(dekomprimertFilnavn), StandardCharsets.UTF_8))) {
                // Gå gjennom bit for bit i huffmantre og dekode innhold
                int nesteByte;
                Node node = huffmanTre.rotNode;
                while ((nesteByte = innfil.read()) != -1) {
                    for (int i = 7; i >= 0; i--) {
                        int bit = (nesteByte >> i) & 1;
                        // Ta neste steg i huffman tre
                        if (node.venstreNode != null && bit == 0) {
                            node = node.venstreNode;
                        } else if (node.høyreNode != null && bit == 1) {
                            node = node.høyreNode;
                        }
                        if (node.venstreNode == null && node.høyreNode == null) {
                            utfil.write(node.tegn);
                            node = huffmanTre.rotNode;
                        }
                    }
                }
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