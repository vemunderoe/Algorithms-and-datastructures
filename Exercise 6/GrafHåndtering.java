import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class GrafHåndtering {
    public static void main(String[] args) {
        // Read file and create graph
        String filnavn = "ø6g1.txt";

        try (BufferedReader filLeser = new BufferedReader(new FileReader(filnavn))) {
            String linje = filLeser.readLine();
            Graf graf = new Graf(Integer.parseInt(linje.split(" ")[0]));
            while ((linje = filLeser.readLine()) != null) {
                String[] deler = linje.split(" ");
                graf.lenkNode(Integer.parseInt(deler[0]), Integer.parseInt(deler[1]));
            }
            System.out.println(graf);
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
            ioException.printStackTrace();
        }
    }
}

class Graf {
    Node[] naboListe;

    public Graf(int antallNoder) {
        naboListe = new Node[antallNoder];
        for (int i = 0; i < antallNoder; i++) {
            naboListe[i] = new Node(i);
        }
    }

    public void lenkNode(int franode, int tilnode) {
        naboListe[franode].settInnKant(naboListe[tilnode]);
    }

    public void breddeSøk(int startnode) {
        // Gå inn i startnode 
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

    public Node(int nodeNummer) {
        this.kanter = new LinkedList<>();
        this.nodeNummer = nodeNummer;
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
