import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Linter klasse for å sjekke om en fil er balansert. Kjør java Linter.java filnavnet, feks java Linter.js hello.java
 */
public class Linter {      
  public static void main(String[] args) {
    Stakk stakk = new Stakk();  
    String filNavn = "hello.java";

    if (args.length > 0) {
      filNavn = args[0];
    }
    
    // Lese fil
    try (BufferedReader filLeser = new BufferedReader(new FileReader(filNavn))) {
      String linje;
      while ((linje = filLeser.readLine()) != null) {        
        // Ta vekk alle tegn etter // i en linje eller mellom ""
        int indeksAvKommentar = linje.indexOf("//");
        if (indeksAvKommentar != -1) {
          linje = linje.substring(0, indeksAvKommentar);
        }        
        // Ta vekk alle tegn i en streng 
        linje = linje.replaceAll("\"[^\"]*\"", "");

        for (char c : linje.toCharArray()) {          
          sjekkTegn(stakk, c);
        }
      }

      if (stakk.hode != null) {
        throw new Exception("nøstingsfeil");  
      }
      System.out.println("Programmet ser bra ut!");
    } catch (Exception e) {
      if (e.getMessage() == "nøstingsfeil") {
        System.out.println("Det er nøstingsfeil i programmet");
      } else {
        System.out.println("Error reading file");
        e.printStackTrace();
      }
    }
  }

  /**
   * Methode for å sjekke tegnet og legge til/trekke fra stakken.
   * Hvis det er {, (, eller [ så sette inn i stakk.
   * Hvis det er }, ), eller ] så må vi sjekke om det siste tegnet er tilhørende, hvis ikke er det nøstingsfeil.
   */
  public static void sjekkTegn(Stakk stakk, char tegn) throws Exception {
    switch (tegn) {
      case '{', '(', '[':
        stakk.settInn(tegn);
        break;
      case '}':
        if (stakk.hode != null && stakk.hode.verdi == '{') {
          stakk.taUt();
          break;
        }
        throw new Exception("nøstingsfeil");   
      case ']':
        if (stakk.hode != null && stakk.hode.verdi == '[') {
          stakk.taUt();
          break;
        } 
        throw new Exception("nøstingsfeil");
      case ')':
        if (stakk.hode != null && stakk.hode.verdi == '(') {
          stakk.taUt();
          break;
        } 
        throw new Exception("nøstingsfeil");        
    }
  }
}

/**
 * Stakk for å holde på noder
 */
class Stakk {  
  public Node hode = null;

  public void settInn(char verdi) {
    hode = new Node(verdi, hode);    
  }

  public void taUt() {    
    hode = hode.neste;
  }

  @Override
  public String toString() {
    String resultat = "";
    Node node = hode;
    while (node != null) {
      resultat += node.verdi;
      node = node.neste;
    }
    return resultat;
  }
}

/**
 * Node for å holde på tegn
 */
class Node {
  public Node neste;
  public char verdi;

  public Node(char verdi, Node neste) {
    this.verdi = verdi;
    this.neste = neste;
  }
}