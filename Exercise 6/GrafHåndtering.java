import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GrafHåndtering {
    public static void main(String[] args) {
        // Read file and create graph
        String filnavn = "ø6g2.txt";

        try (BufferedReader filLeser = new BufferedReader(new FileReader(filnavn))) {
            String linje = filLeser.readLine();
            Graf graf = new Graf(Integer.parseInt(linje.split(" ")[0]));
            while ((linje = filLeser.readLine()) != null) {
                String[] deler = linje.trim().split("\\s+");
                graf.lenkNode(Integer.parseInt(deler[0]), Integer.parseInt(deler[1]));
            }
            graf.breddeSøk(5);
            printBreddeFørstSøk(graf);
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    public static void printBreddeFørstSøk(Graf graf) {
        int maxNodeNummerBredde = Math.max(String.valueOf(graf.naboListe.length).length(), "Node".length());
        int maxForgjengerBredde = Math.max(maxNodeNummerBredde, "Forgjenger".length());
        int maxDistanseBredde = Math.max(String.valueOf(Node.uendelig).length(), "Distanse".length());

        System.out.printf("%-" + maxNodeNummerBredde + "s    %-"
                + maxForgjengerBredde + "s    %-"
                + maxDistanseBredde + "s%n", "Node", "Forgjenger", "Distanse");

        for (Node node : graf.naboListe) {
            String forgjengerStr = (node.forgjenger == null) ? "" : String.valueOf(node.forgjenger.nodeNummer);
            System.out.printf("%-" + maxNodeNummerBredde + "d    %-"
                    + maxForgjengerBredde + "s    %-"
                    + maxDistanseBredde + "d%n", node.nodeNummer, forgjengerStr, node.distanse);
        }
    }
}

class Graf {
    Node[] naboListe;
    KøHåndterer køHåndterer;

    public Graf(int antallNoder) {
        køHåndterer = new KøHåndterer(antallNoder);
        naboListe = new Node[antallNoder];
        for (int i = 0; i < antallNoder; i++) {
            naboListe[i] = new Node(i);
        }
    }

    public void lenkNode(int franode, int tilnode) {
        naboListe[franode].settInnKant(naboListe[tilnode]);
    }

    public void breddeSøk(int startnode) {
        Node startNode = naboListe[startnode];
        startNode.distanse = 0;
        for (Node node : startNode.kanter) {
            køHåndterer.settInn(node);
            node.forgjenger = startNode;
            node.distanse = startNode.distanse + 1;
        }

        while(!køHåndterer.tomKø()) {
            Node nesteNodeIKøen = køHåndterer.neste();
            for (Node node : nesteNodeIKøen.kanter) {
                if (node.distanse == Node.uendelig) {
                    køHåndterer.settInn(node);
                    node.forgjenger = nesteNodeIKøen;
                    node.distanse = nesteNodeIKøen.distanse + 1;
                }
            }
            køHåndterer.taUt(); // Ta ut den håndterte noden fra køen
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Node node : naboListe) {
            stringBuilder.append(node).append("\n");
        }
        return stringBuilder.toString();
    }
}

class Node {
    int nodeNummer;
    LinkedList<Node> kanter;
    int distanse;
    Node forgjenger;
    static int uendelig = 1000000000;

    public Node(int nodeNummer) {
        this.kanter = new LinkedList<>();
        this.nodeNummer = nodeNummer;
        this.distanse = uendelig;
    }

    public void settInnKant(Node tilnode) {
        kanter.add(tilnode);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append(nodeNummer);
        for (Node node : kanter) {
            stringBuilder.append(" - ").append(node.nodeNummer);
        }
        return stringBuilder.toString();
    }
}

class KøHåndterer {
    List<Node> kø;
    public KøHåndterer(int antallNoder) {
        kø = new ArrayList<>();
    }

    public void settInn(Node node) {
        kø.add(node);
    }

    public void taUt() {
        kø.remove(0);
    }

    public Node neste() {
        return kø.get(0);
    }

    public boolean tomKø() {
        return kø.isEmpty();
    }
}

