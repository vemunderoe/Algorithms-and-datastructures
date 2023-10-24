import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        // Lese fil
        String filnavn = "flytgraf2.txt";
        Graf graf = lesGrafFraFil(filnavn);
        List<FlytVei> flytøkendeVeier = new ArrayList<>();

        Node node;
        while ((node = graf.breddeFørstSøk(0)) != null) {
            Node forrigeNode = node;
            String flytøkendeVei = "" + forrigeNode.nodeNummer;
            int lavesteKapasitet = 1000000;

            // Bestemme laveste kapasitet
            while (forrigeNode.forgjenger != null) {
                Node nåværendeNode = forrigeNode.forgjenger;
                for (Kant kant : nåværendeNode.kanter) {
                    if (kant.tilNode == forrigeNode && lavesteKapasitet > kant.restKapasitet()) {
                        lavesteKapasitet = kant.restKapasitet();
                    }
                }
                for (Kant motsattKant : nåværendeNode.motsatteKanter) {
                    if (motsattKant.tilNode == forrigeNode  && lavesteKapasitet > motsattKant.restKapasitet()) {
                        lavesteKapasitet = motsattKant.restKapasitet();
                    }
                }
                forrigeNode = nåværendeNode;
            }

            forrigeNode = node;

            // Oppdatere flyt i flytveien
            while (forrigeNode.forgjenger != null) {
                Node nåværendeNode = forrigeNode.forgjenger;
                flytøkendeVei = String.format("%d %s", nåværendeNode.nodeNummer, flytøkendeVei);
                for (Kant kant : nåværendeNode.kanter) {
                    if (kant.tilNode == forrigeNode) {
                        // Oppdatere flyt i kanten
                        kant.oppdaterFlyt(lavesteKapasitet);

                        // Oppdatere kapasitet i motsatt kant
                        for (Kant motsattKant : forrigeNode.motsatteKanter) {
                            if (motsattKant.tilNode == nåværendeNode) {
                                motsattKant.kapasitet += lavesteKapasitet;
                            }
                        }
                    }
                }
                for (Kant motsattKant : nåværendeNode.motsatteKanter) {
                    if (motsattKant.tilNode == forrigeNode) {
                        motsattKant.oppdaterFlyt(lavesteKapasitet);
                    }

                    for (Kant kant : nåværendeNode.kanter) {
                        if (kant.tilNode == nåværendeNode) {
                            kant.oppdaterFlyt(-lavesteKapasitet);
                        }
                    }
                }
                forrigeNode = nåværendeNode;
            }
            FlytVei  flytVei = new FlytVei(flytøkendeVei, node, lavesteKapasitet);
            flytøkendeVeier.add(flytVei);

            // Nullstille besøkt
            for (Node nodeÅTilbakestille : graf.naboListe) {
                nodeÅTilbakestille.besøkt = false;
            }
        }
        int maksflyt = 0;
        for (FlytVei flytVei : flytøkendeVeier) {
            maksflyt += flytVei.økning;
            System.out.println(flytVei.økning + " " + flytVei.flytøkendeVei);
        }

        System.out.println("Maks flyt: " + maksflyt);
        System.out.println("Sluk: " + flytøkendeVeier.get(0).sluk.nodeNummer);
    }

    private static Graf lesGrafFraFil(String filnavn) {
        try (BufferedReader filLeser = new BufferedReader(new FileReader(filnavn))) {
            String linje = filLeser.readLine();
            Graf graf = new Graf(Integer.parseInt(linje.split("\\s+")[0]));

            // Lese inn kanter
            while ((linje = filLeser.readLine()) != null) {
                int fraNode = Integer.parseInt(linje.trim().split("\\s+")[0]);
                int tilNode = Integer.parseInt(linje.trim().split("\\s+")[1]);
                int kapasitet = Integer.parseInt(linje.trim().split("\\s+")[2]);
                Kant kant = new Kant(graf.naboListe[tilNode], kapasitet);
                graf.naboListe[fraNode].kanter.add(kant);
            }

            // Lage motsatt kanter
            for (Node node : graf.naboListe) {
                for (Kant kant : node.kanter) {
                    // Kantens tilnode må få den motsatte kanten hvis den ikke har den veien fra før
                    Kant motsattKant = new Kant(node, 0);
                    if (!graf.naboListe[kant.tilNode.nodeNummer].kanter.contains(motsattKant)) {
                        graf.naboListe[kant.tilNode.nodeNummer].motsatteKanter.add(motsattKant);
                    }
                }
            }

            return graf;
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
            ioException.printStackTrace();
            return null;
        }
    }
}

class Graf {
    Node[] naboListe;
    KøHåndterer køHåndterer;

    public Graf(int antallNoder) {
        køHåndterer = new KøHåndterer();
        naboListe = new Node[antallNoder];
        for (int i = 0; i < antallNoder; i++) {
            naboListe[i] = new Node(i);
        }
    }

    public Node breddeFørstSøk(int startNodeNummer) {
        // Ta ut startnode
        Node startNode = naboListe[startNodeNummer];
        Node sluttNode = null;
        startNode.besøkt = true;

        for (Kant kant : startNode.kanter) {
            if (!kant.tilNode.besøkt && kant.restKapasitet() > 0) {
                kant.tilNode.forgjenger = startNode;
                kant.tilNode.besøkt = true;
                køHåndterer.settInnIKø(kant.tilNode);
            }
        }

        while (!køHåndterer.tomKø()) {
            Node nåværendeNode = køHåndterer.nesteIKø();
            if (nåværendeNode.kanter.isEmpty()) {
                sluttNode = nåværendeNode;
            }
            for (Kant kant : nåværendeNode.kanter) {
                if (!kant.tilNode.besøkt && kant.restKapasitet() > 0) {
                    kant.tilNode.forgjenger = nåværendeNode;
                    kant.tilNode.besøkt = true;
                    køHåndterer.settInnIKø(kant.tilNode);
                }
            }
            for (Kant motsattKant : nåværendeNode.motsatteKanter) {
                if (!motsattKant.tilNode.besøkt && motsattKant.restKapasitet() > 0) {
                    motsattKant.tilNode.forgjenger = nåværendeNode;
                    motsattKant.tilNode.besøkt = true;
                    køHåndterer.settInnIKø(motsattKant.tilNode);
                }
            }
            køHåndterer.taUtAvKø();
        }

        return sluttNode;
    }
}

class Node {
    int nodeNummer;
    List<Kant> kanter;
    List<Kant> motsatteKanter;
    Node forgjenger;
    boolean besøkt;

    public Node(int nodeNummer) {
        this.nodeNummer = nodeNummer;
        this.kanter = new ArrayList<>();
        this.motsatteKanter = new ArrayList<>();
        this.forgjenger = null;
        this.besøkt = false;
    }

    public String toString() {
        return "Node: " + nodeNummer;
    }
}

class Kant {
    Node tilNode;
    int kapasitet;
    int maksKapasitet;
    int flyt = 0;

    public Kant(Node tilNode, int kapasitet) {
        this.tilNode = tilNode;
        this.kapasitet = kapasitet;
        this.maksKapasitet = kapasitet;
    }

    public int restKapasitet() {
        return kapasitet - flyt;
    }

    public void oppdaterFlyt(int flytøkning) {
        if (flyt + flytøkning > maksKapasitet) {
            flyt = maksKapasitet;
        } else {
            flyt += flytøkning;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kant kant = (Kant) o;
        return Objects.equals(tilNode, kant.tilNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tilNode);
    }
}

class KøHåndterer {
    List<Node> kø = new ArrayList<>();

    public void settInnIKø(Node nyNode) {
        kø.add(nyNode);
    }

    public void taUtAvKø() {
        kø.remove(0);
    }

    public Node nesteIKø() {
        return kø.get(0);
    }

    public boolean tomKø() {
        return kø.isEmpty();
    }
}

class FlytVei {
    String flytøkendeVei;
    Node sluk;
    int økning;

    public FlytVei(String flytøkendeVei, Node sluk, int økning) {
        this.flytøkendeVei = flytøkendeVei;
        this.sluk = sluk;
        this.økning = økning;
    }
}
