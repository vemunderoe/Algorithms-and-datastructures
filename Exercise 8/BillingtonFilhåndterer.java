import java.io.*;
import java.util.ArrayList;
import java.util.List;

class BillingtonFilhåndterer {
    DataOutputStream utfil;
    DataInputStream innfil;
    int buffer;
    int bitPos;
    int bitsIgjen;
    int nesteByte;
    // Holde styr på hvor mange bits som allerede er skrevet

    // Fylle opp med så mange som mulig fra venstre til høyre

    // Til slutt skrive resterende bits

    public BillingtonFilhåndterer(DataOutputStream utfil, DataInputStream innfil) {
        this.utfil = utfil;
        this.innfil = innfil;
        this.buffer = 0;
        this.bitPos = 0;
    }

    public BillingtonFilhåndterer(DataOutputStream utfil) {
        this.utfil = utfil;
        this.buffer = 0;
        this.bitPos = 0;
    }

    public BillingtonFilhåndterer(DataInputStream innfil) {
        this.innfil = innfil;
        this.buffer = 0;
        this.bitPos = 0;
    }

    public void writeBit(char bit) throws IOException {
        // Kontroller at bit er '0' eller '1'.
        if (bit != '0' && bit != '1') {
            throw new IllegalArgumentException("Argumentet må være '0' eller '1'.");
        }

        // Legg til biten til buffer.
        buffer = (buffer << 1) | (bit - '0');
        bitPos++;

        // Hvis currentByte er full (8 bits), skriv det ut og nullstill.
        if (bitPos == 8) {
            utfil.write(buffer);
            buffer = 0;
            bitPos = 0;
        }
    }

    void flushBuffer() throws IOException {
        System.out.println(buffer);
        System.out.println(bitPos);
        if (bitPos > 0) {
            buffer <<= (8-bitPos);
            utfil.writeByte(buffer);
        }
    }
}