import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

//JMapViewer
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import org.openstreetmap.gui.jmapviewer.*;

/**
 * G15Maps - Gruppe-15-Maps
 */
public class G15Maps {
    Graf graf = null;
    Graf omvendtGraf = null;

    public static void main(String[] args) {
        G15Maps g15Maps = new G15Maps();
        // Ta tiden på dijkstras og telle noder besøkt
        g15Maps.lesInnNoderFraFil("nordenKartNoder.txt");
        g15Maps.lesInnKanterFraFil("nordenKartKanter.txt");
        g15Maps.lesInnInteressepunkterFraFil("interessepkt.txt");

        if (!Files.exists(Paths.get("noderTilLandemerker.preprosessert"))) {
            System.out.println("Preprosesserer");
            g15Maps.preprosesserForALT();
        }

        g15Maps.lesInnPreprosesserteFilerForALT();
        //g15Maps.testPreprosesseringsData();

        g15Maps.settOppGUI();
    }

    public void settOppGUI() {
        JFrame frame = new JFrame("G15Maps");

        frame.add(new Vindu(graf));

        frame.pack();
        frame.setVisible(true);
    }

    public void lesInnNoderFraFil(String filnavn) {
        try (BufferedReader filLeser = new BufferedReader(new FileReader(filnavn))) {
            // Lese inn antall noder
            String linje = filLeser.readLine();

            // Initialisere graf
            graf = new Graf(Integer.parseInt(linje.trim()));
            omvendtGraf = new Graf(Integer.parseInt(linje.trim()));

            // Lese inn noder
            while ((linje = filLeser.readLine()) != null) {
                hsplit(linje, 3);
                int nodenummer = Integer.parseInt(felt[0]);
                double breddeGrad = Double.parseDouble(felt[1]);
                double lengdeGrad = Double.parseDouble(felt[2]);
                graf.naboListe[nodenummer] = new Node(nodenummer, breddeGrad, lengdeGrad);
                omvendtGraf.naboListe[nodenummer] = new Node(nodenummer, breddeGrad, lengdeGrad);
            }

            // Lese inn kanter og sette inn for nodene
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Noe gikk galt under innlesing av ");
        }
    }

    public void lesInnKanterFraFil(String filnavn) {
        try (BufferedReader filLeser = new BufferedReader(new FileReader(filnavn))) {
            // Lese inn antall noder
            String linje = filLeser.readLine();

            int antallKanter = Integer.parseInt(linje.trim());
            // Lese inn noder
            while ((linje = filLeser.readLine()) != null) {
                hsplit(linje, 3);
                int fraNode = Integer.parseInt(felt[0]);
                int tilNode = Integer.parseInt(felt[1]);
                int kjøreTid = Integer.parseInt(felt[2]);

                Kant kant = new Kant(graf.naboListe[tilNode], kjøreTid);
                Kant omvendtKant = new Kant(omvendtGraf.naboListe[fraNode], kjøreTid);
                graf.naboListe[fraNode].kanter.add(kant);
                omvendtGraf.naboListe[tilNode].kanter.add(omvendtKant);
            }

            // Lese inn kanter og sette inn for nodene
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Noe gikk galt under innlesing av " + filnavn);
        }
    }

    public void lesInnInteressepunkterFraFil(String filnavn) {
        try (BufferedReader filLeser = new BufferedReader(new FileReader(filnavn))) {
            // Lese inn antall noder
            String linje = filLeser.readLine();

            int antallInteressepunkt = Integer.parseInt(linje.trim());
            // Lese inn noder
            while ((linje = filLeser.readLine()) != null) {
                hsplit(linje, 2);
                int nodenummer = Integer.parseInt(felt[0]);
                int kode = Integer.parseInt(felt[1]);
                String navn = linje.substring(linje.indexOf("\"") + 1, linje.lastIndexOf("\""));

                graf.naboListe[nodenummer].kode = kode;
                graf.naboListe[nodenummer].navn = navn;
            }

            // Lese inn kanter og sette inn for nodene
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Noe gikk galt under innlesing av " + filnavn);
        }
    }

    public void lesInnPreprosesserteFilerForALT() {
        int antallNoder = graf.naboListe.length;
        try (DataInputStream innfil = new DataInputStream(new BufferedInputStream(new FileInputStream("landemerkerTilNoder.preprosessert")))) {
            for (int i = 0; i < Graf.landemerker.length; i++) {
                for (int j = 0; j < antallNoder; j++) {
                    graf.avstandFraLandemerkeTilNoder[i][j] = innfil.readInt();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Noe gikk galt under innlesing av landemerker til noder");
        }

        try (DataInputStream innfil = new DataInputStream(new BufferedInputStream(new FileInputStream("noderTilLandemerker.preprosessert")))) {
            for (int i = 0; i < Graf.landemerker.length; i++) {
                for (int j = 0; j < antallNoder; j++) {
                    graf.avstandTilLandemerkeFraNoder[i][j] = innfil.readInt();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Noe gikk galt under innlesing av landemerker til noder");
        }
    }

    static String[] felt = new String[5];
    public void hsplit(String linje, int antall) {
        int j = 0;
        int lengde = linje.length();
        for (int i = 0; i < antall; i++) {
            while (linje.charAt(j) <= ' ') j++;
            int ordstart = j;
            while (j < lengde && linje.charAt(j) > ' ') j++;
            felt[i] = linje.substring(ordstart, j);
        }
    }

    public void preprosesserForALT() {
        int[][] avstandFraLandemerker = new int[Graf.landemerker.length][graf.naboListe.length];

        for (int i = 0; i < Graf.landemerker.length; i++) {
            System.out.println("Starter dijkstra fra: " + i);
            graf.dijkstra(Graf.landemerker[i]);

            for (int j = 0; j < graf.naboListe.length; j++) {
                avstandFraLandemerker[i][j] = graf.naboListe[j].distanse;
            }

            System.out.println("Ferdig med dijkstra fra: " + i);
        }

        skrivFraLandemerkerTilNoderTilFil(avstandFraLandemerker);

        System.out.println();

        // Fra alle noder til landemerkene
        int[][] avstandFraNoderTilLandemerker = new int[Graf.landemerker.length][graf.naboListe.length];
        for (int i = 0; i < Graf.landemerker.length; i++) {
            System.out.println("Starter dijkstra til: " + i);
            omvendtGraf.dijkstra(Graf.landemerker[i]);

            for (int j = 0; j < omvendtGraf.naboListe.length; j++) {
                avstandFraNoderTilLandemerker[i][j] = omvendtGraf.naboListe[j].distanse;
            }

            System.out.println("Ferdig med dijkstra til: " + i);
        }

        skrivFraNoderTilLandemerkerTilFil(avstandFraNoderTilLandemerker);
    }

    public void skrivFraLandemerkerTilNoderTilFil(int[][] avstandFraLandemerkerTilTabell) {
        try (DataOutputStream utfil = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("landemerkerTilNoder.preprosessert")))) {
            for (int i = 0; i < Graf.landemerker.length; i++) {
                for (int j = 0; j < graf.naboListe.length; j++) {
                    utfil.writeInt(avstandFraLandemerkerTilTabell[i][j]);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    public void skrivFraNoderTilLandemerkerTilFil(int[][] avstandFraNoderTilLandemerker) {
        try (DataOutputStream utfil = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("noderTilLandemerker.preprosessert")))) {
            for (int i = 0; i < Graf.landemerker.length; i++) {
                for (int j = 0; j < omvendtGraf.naboListe.length; j++) {
                    utfil.writeInt(avstandFraNoderTilLandemerker[i][j]);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void testPreprosesseringsData() {
        // Landemerke: 3198614 - Kristiansand
        // Landemerke: 2531818 - Nordkapp
        // Landemerke: 2763740 - Odense
        // Landemerke: 7161953 - Stockholm
        int trondheim = 7826348;

        for (int i = 0; i < Graf.landemerker.length; i++) {
            int landemerke = Graf.landemerker[i];
            // Sjekke avstand fra Landemerke til Trondheim
            graf.dijkstra(landemerke, trondheim);
            System.out.printf("Fra landemerke: %d til node: %d tok det %d tid med dijkstra\n", landemerke, trondheim, graf.naboListe[trondheim].kjøretid);
            System.out.printf("Fra landemerke: %d til node: %d tok det %d tid med preprosessert data\n", landemerke, trondheim, graf.avstandFraLandemerkeTilNoder[i][trondheim]);


            // Sjekke avstand fra Trondheim til Landemerke
            graf.dijkstra(trondheim, landemerke);
            System.out.printf("Til landemerke: %d fra node: %d tok det %d tid med dijkstra\n", landemerke, trondheim, graf.naboListe[landemerke].kjøretid);
            System.out.printf("Til landemerke: %d fra node: %d tok det %d tid med preprosessert data\n", landemerke, trondheim, graf.avstandTilLandemerkeFraNoder[i][trondheim]);

            System.out.println();
        }
    }
}

class Graf {
    Node[] naboListe;
    int prosesserteNode = 0;
    static int[] landemerker = new int[]{3198614, 2531818, 2763740, 7161953};
    int[][] avstandTilLandemerkeFraNoder; // 0: Kristiansand, 1: Nordkapp, 2: Bergen
    int[][] avstandFraLandemerkeTilNoder; // 0: Kristiansand, 1: Nordkapp, 2: Bergen

    public Graf(int antallNoder) {
        naboListe = new Node[antallNoder];
        avstandFraLandemerkeTilNoder = new int[landemerker.length][antallNoder];
        avstandTilLandemerkeFraNoder = new int[landemerker.length][antallNoder];
    }

    public void dijkstra(int indeksTilStartNode, int indeksTilSluttNode) {
        Node startNode = naboListe[indeksTilStartNode];
        Node sluttNode = naboListe[indeksTilSluttNode];

        for (Node node : naboListe) {
            node.distanse = Integer.MAX_VALUE;
            node.kjøretid = 0;
            node.forgjenger = null;
        }

        prosesserteNode = 0;
        // Gi start noden avstand 0
        startNode.distanse = 0;

        // Legg under-nodene i prioritetskø
        PrioritetsKø prioritetsKø = new PrioritetsKø();
        prioritetsKø.leggTil(startNode);

        while (!prioritetsKø.erTom()) {
            Node nodeMedLavestAvstand = prioritetsKø.hentOgFjernNodeMedHøyestPrioritet();
            if (nodeMedLavestAvstand == sluttNode) {
                break;
            }
            for (Kant kant : nodeMedLavestAvstand.kanter) {
                Node nabo = kant.tilNode;

                int nyDistanse = nodeMedLavestAvstand.distanse + kant.vekt;
                if (nyDistanse < nabo.distanse) {
                    nabo.distanse = nyDistanse;
                    nabo.kjøretid = nyDistanse;
                    nabo.forgjenger = nodeMedLavestAvstand;
                    if (prioritetsKø.inneholderNode(nabo)) {
                        prioritetsKø.oppdaterPrioritetPåNode(nabo);
                    } else {
                        prioritetsKø.leggTil(nabo);
                    }
                }
            }
            prosesserteNode++;
        }
    }

    public void dijkstra(int indeksTilStartNode) {
        Node startNode = naboListe[indeksTilStartNode];
        for (Node node : naboListe) {
            node.distanse = Integer.MAX_VALUE;
            node.kjøretid = 0;
            node.forgjenger = null;

            for (Kant kant : node.kanter) {
                kant.besøkt = false;
            }
        }

        // Nullstill prosesserte noder
        prosesserteNode = 0;
        // Gi start noden avstand 0
        startNode.distanse = 0;

        // Legg under-nodene i prioritetskø
        PrioritetsKø prioritetsKø = new PrioritetsKø();
        prioritetsKø.leggTil(startNode);

        while (!prioritetsKø.erTom()) {
            Node nodeMedLavestAvstand = prioritetsKø.hentOgFjernNodeMedHøyestPrioritet();
            for (Kant kant : nodeMedLavestAvstand.kanter) {
                Node nabo = kant.tilNode;

                int nyDistanse = nodeMedLavestAvstand.distanse + kant.vekt;
                if (nyDistanse < nabo.distanse) {
                    nabo.distanse = nyDistanse;
                    nabo.kjøretid = nyDistanse;
                    nabo.forgjenger = nodeMedLavestAvstand;
                    if (prioritetsKø.inneholderNode(nabo)) {
                        prioritetsKø.oppdaterPrioritetPåNode(nabo);
                    } else {
                        prioritetsKø.leggTil(nabo);
                    }
                }
            }
            prosesserteNode++;
        }
    }

    public List<Node> finnNærmesteInteressepunkt(int indeksTilStartNode, int kodeTilInteressepunkt) {
        List<Node> nærmesteInteressepunkt = new ArrayList<>();
        Node startNode = naboListe[indeksTilStartNode];
        for (Node node : naboListe) {
            node.distanse = Integer.MAX_VALUE;
            node.kjøretid = 0;
            node.forgjenger = null;

            for (Kant kant : node.kanter) {
                kant.besøkt = false;
            }
        }

        // Nullstill prosesserte noder
        prosesserteNode = 0;
        // Gi start noden avstand 0
        startNode.distanse = 0;

        // Legg under-nodene i prioritetskø
        PrioritetsKø prioritetsKø = new PrioritetsKø();
        prioritetsKø.leggTil(startNode);

        while (!prioritetsKø.erTom()) {
            Node nodeMedLavestAvstand = prioritetsKø.hentOgFjernNodeMedHøyestPrioritet();

            if ((nodeMedLavestAvstand.kode & kodeTilInteressepunkt) == kodeTilInteressepunkt) {
                nærmesteInteressepunkt.add(nodeMedLavestAvstand);
                System.out.println("Node: " + nodeMedLavestAvstand.nodenummer + ", navn: " + nodeMedLavestAvstand.navn);
                if (nærmesteInteressepunkt.size() == 5) {
                    return nærmesteInteressepunkt;
                }
            }

            for (Kant kant : nodeMedLavestAvstand.kanter) {
                Node nabo = kant.tilNode;

                int nyDistanse = nodeMedLavestAvstand.distanse + kant.vekt;
                if (nyDistanse < nabo.distanse) {
                    nabo.distanse = nyDistanse;
                    nabo.kjøretid = nyDistanse;
                    nabo.forgjenger = nodeMedLavestAvstand;
                    if (prioritetsKø.inneholderNode(nabo)) {
                        prioritetsKø.oppdaterPrioritetPåNode(nabo);
                    } else {
                        prioritetsKø.leggTil(nabo);
                    }
                }
            }
            prosesserteNode++;
        }

        return null;
    }


    public void alt(int indeksTilStartnode, int indeksTilMålnode) {
        Node startNode = naboListe[indeksTilStartnode];
        Node målnode = naboListe[indeksTilMålnode];
        for (Node node : naboListe) {
            node.distanse = Integer.MAX_VALUE;
            node.kjøretid = 0;
            node.forgjenger = null;
        }

        // Nullstill prosesserte noder
        prosesserteNode = 0;
        // Gi startnoden avstand 0
        startNode.distanse = 0;

        // Legg under-nodene i prioritetskø
        PrioritetsKø prioritetsKø = new PrioritetsKø();
        prioritetsKø.leggTil(startNode);

        while (!prioritetsKø.erTom()) {
            Node nodeMedLavestAvstand = prioritetsKø.hentOgFjernNodeMedHøyestPrioritet();

            if (nodeMedLavestAvstand == målnode) {
                break;
            }

            for (Kant kant : nodeMedLavestAvstand.kanter) {
                Node nabo = kant.tilNode;

                int nyDistanse = nodeMedLavestAvstand.kjøretid + kant.vekt + beregnHeuristikk(nabo, målnode);

                if (nyDistanse < nabo.distanse) {
                    nabo.distanse = nyDistanse;
                    nabo.kjøretid = nodeMedLavestAvstand.kjøretid + kant.vekt;
                    nabo.forgjenger = nodeMedLavestAvstand;
                    if (prioritetsKø.inneholderNode(nabo)) {
                        prioritetsKø.oppdaterPrioritetPåNode(nabo);
                    } else {
                        prioritetsKø.leggTil(nabo);
                    }
                }
            }
            prosesserteNode++;
        }
    }

    private int beregnHeuristikk(Node nåværendeNode, Node målNode) {
        int heuristic = 0;
        for (int i = 0; i < landemerker.length; i++) {
            // Slå opp avstand fra første landemerke til målet, og trekk fra avstanden fra første landemerke til n
            // Hvis negativt, bruk 0
            int distanseFraLandemerkeTilMålnode = avstandFraLandemerkeTilNoder[i][målNode.nodenummer];
            int distanseFraLandemerkeTilNåværendeNode = avstandFraLandemerkeTilNoder[i][nåværendeNode.nodenummer];

            int nyHeuristikk = distanseFraLandemerkeTilMålnode - distanseFraLandemerkeTilNåværendeNode;
            if (nyHeuristikk > heuristic) {
                heuristic = nyHeuristikk;
            }

            // Slå deretter opp avstand fra n til første landemerke, og trekk fra avstanden fra målet til landemerket
            int distanseFraNodeTilLandemerke = avstandTilLandemerkeFraNoder[i][nåværendeNode.nodenummer];
            int distanseFraMålnodeTilLandemerke = avstandTilLandemerkeFraNoder[i][målNode.nodenummer];

            nyHeuristikk = distanseFraNodeTilLandemerke - distanseFraMålnodeTilLandemerke;

            if (nyHeuristikk > heuristic) {
                heuristic = nyHeuristikk;
            }
        }

        return heuristic;
    }
}

class Node {
    static int uendelig = Integer.MAX_VALUE;
    int distanse = uendelig;
    int kjøretid = 0;
    int nodenummer;
    int kode;
    String navn = "";
    List<Kant> kanter;
    Node forgjenger;
    double breddeGrad;
    double lengdeGrad;

    public Node(int nodenummer, double breddeGrad, double lengdeGrad) {
        this.nodenummer = nodenummer;
        this.breddeGrad = breddeGrad;
        this.lengdeGrad = lengdeGrad;
        this.kanter = new ArrayList<>();
        this.forgjenger = null;
    }
}

class Kant {
    Node tilNode;
    int vekt;
    boolean besøkt;

    public Kant(Node tilNode, int vekt) {
        this.tilNode = tilNode;
        this.vekt = vekt;
        this.besøkt = false;
    }
}

class NodeComparator implements Comparator<Node> {
    // Sammenligner to noder basert på deres distanse
    public int compare(Node node1, Node node2) {
        return Integer.compare(node1.distanse, node2.distanse);
    }
}

class PrioritetsKø {
    private PriorityQueue<Node> heap;
    private Set<Node> elements;

    public PrioritetsKø() {
        heap = new PriorityQueue<>(new NodeComparator());
        elements = new HashSet<>();
    }

    public void leggTil(Node node) {
        heap.add(node);
        elements.add(node);
    }

    public boolean erTom() {
        return heap.isEmpty();
    }

    public int storrelse() {
        return heap.size();
    }

    public void oppdaterPrioritetPåNode(Node node) {
        if (elements.contains(node)) {
            heap.remove(node);
            heap.add(node);
        }
    }

    public Node hentOgFjernNodeMedHøyestPrioritet() {
        Node node = heap.poll();
        if (node != null) {
            elements.remove(node);
        }
        return node;
    }

    public boolean inneholderNode(Node node) {
        return elements.contains(node);
    }
}

class Vindu extends JPanel implements ActionListener, DocumentListener, JMapViewerEventListener {
    JButton btn_dijkstra = new JButton("Dijkstra");
    JButton btn_alt = new JButton("ALT");
    JButton btn_bensinstasjoner = new JButton("5 nærmeste bensinstasjoner");
    JButton btn_ladestasjoner = new JButton("5 nærmeste ladestasjoner");
    JButton btn_spisesteder = new JButton("5 nærmeste spisesteder");
    JButton btn_drikkesteder = new JButton("5 nærmeste drikkesteder");
    JButton btn_overnattingssteder = new JButton("5 nærmeste overnattingssteder");
    JButton btn_slutt = new JButton("Avslutt");
    JLabel lbl_fra = new JLabel();
    JLabel lbl_til = new JLabel();
    JTextField txt_fra = new JTextField(30);
    JTextField txt_til = new JTextField(30);
    JLabel lbl_tur = new JLabel("—");
    JLabel lbl_alg = new JLabel("—");
    String gml_fra = "";
    String gml_til = "";
    JPanel kart = new JPanel(new BorderLayout());
    //JMapViewer stuff
    private final JMapViewerTree treeMap;
    private final JLabel zoomLabel;
    private final JLabel zoomValue;

    private final JLabel mperpLabelName;
    private final JLabel mperpLabelValue;

    Graf graf;

    Layer rutelag, areallag;

    public Vindu(Graf parameterG) {
        super(new GridBagLayout());
        graf = parameterG;
        GridBagConstraints c = new GridBagConstraints();
        GridBagConstraints hc =  new GridBagConstraints(); //høyrejustert
        GridBagConstraints vc =  new GridBagConstraints(); //venstrejustert

        btn_dijkstra.setActionCommand("dijkstra");
        btn_dijkstra.setMnemonic(KeyEvent.VK_D);
        btn_alt.setActionCommand("alt");
        btn_bensinstasjoner.setActionCommand("bensinstasjoner");
        btn_ladestasjoner.setActionCommand("ladestasjoner");
        btn_spisesteder.setActionCommand("spisesteder");
        btn_drikkesteder.setActionCommand("drikkesteder");
        btn_overnattingssteder.setActionCommand("overnattingssteder");

        btn_dijkstra.addActionListener(this);
        btn_alt.addActionListener(this);
        btn_bensinstasjoner.addActionListener(this);
        btn_ladestasjoner.addActionListener(this);
        btn_spisesteder.addActionListener(this);
        btn_drikkesteder.addActionListener(this);
        btn_overnattingssteder.addActionListener(this);
        btn_slutt.addActionListener(this);

        txt_fra.getDocument().addDocumentListener(this);
        txt_til.getDocument().addDocumentListener(this);

        hc.gridx = 0; hc.gridy = 1;

        hc.anchor = GridBagConstraints.NORTHEAST;
        vc.anchor = GridBagConstraints.NORTHWEST;
        hc.fill = vc.fill = GridBagConstraints.NONE;

        add(new JLabel("Fra:"), hc);

        c.gridx = 1; c.gridy = 1;
        add(txt_fra, c);

        hc.gridx = 3;
        add(new JLabel("Til:"), hc);

        c.gridx = 4;
        add(txt_til, c);


        hc.gridx = 0; hc.gridy = 2;
        add(new JLabel("Node:"), hc);

        vc.gridx = 1; vc.gridy = 2;
        add(lbl_fra, vc);

        hc.gridx = 3;
        add(new JLabel("Node:"), hc);

        vc.gridx = 4;
        add(lbl_til, vc);


        c.gridx = 0; c.gridy = 3;
        add(btn_dijkstra, c);

        c.gridx = 1;
        add(btn_alt, c);

        c.gridy = 4;
        c.gridx = 0;
        add(btn_bensinstasjoner, c);

        c.gridx = 1;
        add(btn_ladestasjoner, c);

        c.gridx = 2;
        add(btn_spisesteder, c);

        c.gridx = 3;
        add(btn_drikkesteder, c);

        c.gridx = 4;
        add(btn_overnattingssteder, c);

        vc.gridx = 0; vc.gridy = 5;
        vc.gridwidth = 6;
        add(lbl_tur, vc);

        vc.gridy = 6;
        add(lbl_alg, vc);

        c.gridx = 5; c.gridy = 6;
        add(btn_slutt, c);

        c.gridx = 0; c.gridy = 7;
        c.gridwidth = 6;
        c.gridheight = 5;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(kart, c);

        treeMap = new JMapViewerTree("Lag");
        rutelag = treeMap.addLayer("kjørerute");
        areallag = treeMap.addLayer("undersøkt areal");
        // Listen to the map viewer for user operations so components will
        // receive events and update
        map().addJMVListener(this);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel helpPanel = new JPanel();

        mperpLabelName = new JLabel("meter/Pixel: ");
        mperpLabelValue = new JLabel(String.format("%s", map().getMeterPerPixel()));

        zoomLabel = new JLabel("Zoomnivå: ");
        zoomValue = new JLabel(String.format("%s", map().getZoom()));

        kart.add(panel, BorderLayout.NORTH);
        kart.add(helpPanel, BorderLayout.SOUTH);
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelBottom, BorderLayout.SOUTH);
        JLabel helpLabel = new JLabel("Flytt med høyre musknapp,\n "
                + "zoom med venstre eller dobbeltklikk.");
        helpPanel.add(helpLabel);
        JButton button = new JButton("setDisplayToFitMapMarkers");
        button.addActionListener(e -> map().setDisplayToFitMapMarkers());
        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
                new OsmTileSource.Mapnik(),
                new OsmTileSource.TransportMap(),
                new BingAerialTileSource(),
        });
        tileSourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileSource((TileSource) e.getItem());
            }
        });
        JComboBox<TileLoader> tileLoaderSelector;
        tileLoaderSelector = new JComboBox<>(new TileLoader[] {new OsmTileLoader(map())});
        tileLoaderSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileLoader((TileLoader) e.getItem());
            }
        });
        map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
        panelTop.add(tileSourceSelector);
        panelTop.add(tileLoaderSelector);
        final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
        showMapMarker.setSelected(map().getMapMarkersVisible());
        showMapMarker.addActionListener(e -> map().setMapMarkerVisible(showMapMarker.isSelected()));
        panelBottom.add(showMapMarker);
        ///
        final JCheckBox showTreeLayers = new JCheckBox("Tree Layers visible");
        showTreeLayers.addActionListener(e -> treeMap.setTreeVisible(showTreeLayers.isSelected()));
        panelBottom.add(showTreeLayers);
        ///
        final JCheckBox showToolTip = new JCheckBox("ToolTip visible");
        showToolTip.addActionListener(e -> map().setToolTipText(null));
        panelBottom.add(showToolTip);
        ///
        final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
        showTileGrid.setSelected(map().isTileGridVisible());
        showTileGrid.addActionListener(e -> map().setTileGridVisible(showTileGrid.isSelected()));
        panelBottom.add(showTileGrid);
        final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
        showZoomControls.setSelected(map().getZoomControlsVisible());
        showZoomControls.addActionListener(e -> map().setZoomControlsVisible(showZoomControls.isSelected()));
        panelBottom.add(showZoomControls);
        final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
        scrollWrapEnabled.addActionListener(e -> map().setScrollWrapEnabled(scrollWrapEnabled.isSelected()));
        panelBottom.add(scrollWrapEnabled);
        panelBottom.add(button);

        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(mperpLabelName);
        panelTop.add(mperpLabelValue);

        kart.add(treeMap, BorderLayout.CENTER);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    map().getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if (showToolTip.isSelected()) map().setToolTipText(map().getPosition(p).toString());
            }
        });

    } //konstruktør for vindu

    public double grad(double rad) {
        return rad / Math.PI * 180;
    }

    //Tegn ruta på kartet
    public void tegn_ruta(int indeksTilStartNode, int indeksTilSluttNode) {
        Node sluttNode = graf.naboListe[indeksTilSluttNode];
        System.out.println("Starter å tegne ruta");
        do {
            MapMarkerDot prikk;
            prikk = new MapMarkerDot(rutelag, sluttNode.breddeGrad, sluttNode.lengdeGrad);
            map().addMapMarker(prikk);
            sluttNode = sluttNode.forgjenger;
        } while (sluttNode != graf.naboListe[indeksTilStartNode]);
        System.out.println("Ferdig å tegne ruta");
    }

    public void tegn_interessepunkt(List<Node> nærmesteInteressepunkter) {
        for (Node node : nærmesteInteressepunkter) {
            MapMarkerDot prikk;
            prikk = new MapMarkerDot(rutelag, node.breddeGrad, node.lengdeGrad);
            map().addMapMarker(prikk);
        }
    }

    //Tegn det gjennomsøkte arealet, altså alle noder med forgjengere.
    public void tegn_areal() {
        /* ... */
    }

    //Knapper
    public void actionPerformed(ActionEvent e) {
        int noder = 0;
        Date tid1 = new Date();
        String tur = "Kjøretur " + txt_fra.getText() + " — " + txt_til.getText();
        String alg = "";
        int startNode = 0;
        int sluttNode = 0;
        List<Node> nærmesteInteressepunkter = new ArrayList<>();

        switch (e.getActionCommand()) {
            case "dijkstra":
                /* sett inn et kall for å kjøre Dijkstras algoritme her */
                alg = "Dijkstras algoritme ";
                startNode = Integer.parseInt(txt_fra.getText());
                sluttNode = Integer.parseInt(txt_til.getText());
                System.out.println("Starter dijkstras algoritme fra node: " + startNode);
                graf.dijkstra(startNode, sluttNode);
                noder = graf.prosesserteNode;
                break;
            case "alt":
                /* sett inn kall for å kjøre ALT her */
                alg = "ALT-algoritmen ";
                startNode = Integer.parseInt(txt_fra.getText());
                sluttNode = Integer.parseInt(txt_til.getText());
                System.out.println("Starter alt-algoritmen fra node: " + startNode);
                graf.alt(startNode, sluttNode);
                noder = graf.prosesserteNode;
                break;
            case "bensinstasjoner":
                alg = "5 nærmeste bensinstasjoner";
                startNode = Integer.parseInt(txt_fra.getText());
                nærmesteInteressepunkter = graf.finnNærmesteInteressepunkt(startNode, 2);
                noder = graf.prosesserteNode;
                break;
            case "ladestasjoner":
                alg = "5 nærmeste ladestasjoner";
                startNode = Integer.parseInt(txt_fra.getText());
                nærmesteInteressepunkter = graf.finnNærmesteInteressepunkt(startNode, 4);
                noder = graf.prosesserteNode;
                break;
            case "spisesteder":
                alg = "5 nærmeste spisesteder";
                startNode = Integer.parseInt(txt_fra.getText());
                nærmesteInteressepunkter = graf.finnNærmesteInteressepunkt(startNode, 8);
                noder = graf.prosesserteNode;
                break;
            case "drikkesteder":
                alg = "5 nærmeste drikkesteder";
                startNode = Integer.parseInt(txt_fra.getText());
                nærmesteInteressepunkter = graf.finnNærmesteInteressepunkt(startNode, 16);
                noder = graf.prosesserteNode;
                break;
            case "overnattingssteder":
                alg = "5 nærmeste overnattingssteder";
                startNode = Integer.parseInt(txt_fra.getText());
                nærmesteInteressepunkter = graf.finnNærmesteInteressepunkt(startNode, 32);
                noder = graf.prosesserteNode;
                break;
            default:
                System.exit(0);
                break;
        }
        Date tid2 = new Date();
        map().removeAllMapMarkers();

        // Vise frem kjøretid for bilen, hvis målet ble funnet:
		if (alg.equals("5 nærmeste bensinstasjoner")
                || alg.equals("5 nærmeste ladestasjoner")
                || alg.equals("5 nærmeste spisesteder")
                || alg.equals("5 nærmeste drikkesteder")
                || alg.equals("5 nærmeste overnattingssteder")) {
            System.out.println("Interessepunkt");
            tegn_interessepunkt(nærmesteInteressepunkter);
            for (Node node : nærmesteInteressepunkter) {
                int tid = node.kjøretid;
                int tt = tid / 360000; tid -= 360000 * tt;
                int mm = tid / 6000; tid -= 6000 * mm;
                int ss = tid / 100;
                int hs = tid % 100;
                System.out.printf("Nodenumer: %d, %s, Kjøretid %d:%02d:%02d,%02d   ()\n", node.nodenummer, node.navn, tt, mm, ss, hs);
            }
        } else if (graf.naboListe[sluttNode].forgjenger == null) {
			tur += "  Fant ikke veien!";
		} else {
			int tid = graf.naboListe[sluttNode].kjøretid;
            System.out.println("Kjøretid i hundedels-sekunder: " + tid);
			int tt = tid / 360000; tid -= 360000 * tt;
			int mm = tid / 6000; tid -= 6000 * mm;
			int ss = tid / 100;
			int hs = tid % 100;
			tur = String.format("%s Kjøretid %d:%02d:%02d,%02d   ()", tur, tt, mm, ss, hs);
            tegn_ruta(startNode, sluttNode);
		}
		float sek = (float)(tid2.getTime() - tid1.getTime()) / 1000;
		alg = String.format("%s prosesserte %,d noder på %2.3fs. %2.0f noder/ms", alg, noder, sek, noder/sek/1000);
		lbl_tur.setText(tur);
		lbl_alg.setText(alg);
		System.out.println(tur);
		System.out.println(alg);
		System.out.println();
        //map().setDisplayToFitMapMarkers();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {

    }

    @Override
    public void removeUpdate(DocumentEvent e) {

    }

    //Skriving i tekstfelt
    public void changedUpdate(DocumentEvent ev) {
    }

    //Finn hvilket felt som ble endret.
    //Slå opp nodenumre om mulig/ønskelig

    //Noe skjer med kartet.
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

    private void updateZoomParameters() {
        if (mperpLabelValue != null)
            mperpLabelValue.setText(String.format("%s", map().getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }

    private JMapViewer map() {
        return treeMap.getViewer();
    }

}
