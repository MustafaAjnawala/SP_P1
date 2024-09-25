package C2P2;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// Macro Name Table Entry
class MNTEntry {
    String macroName;
    int mdtIndex;
    int PP;

    public MNTEntry(String macroName,int PP, int mdtIndex) {
        this.macroName = macroName;
        this.mdtIndex = mdtIndex;
        this.PP = PP;
    }

    public String toString() {
        return "Name: " + macroName +", #PP:"+ PP+" #KP: 0"+", #EV: 0"+", MDTP: " + mdtIndex+", KPDTP: 0"+", SSTAB: 0";
    }
}

// Macro Processor Class
public class C2P2 {
    List<String> MDT = new ArrayList<>();          // Macro Definition Table
    List<MNTEntry> MNT = new ArrayList<>();        // Macro Name Table
    List<String> PNT = new ArrayList<>();          // Parameter Name Table (actual names)
    int mdtIndex = 0;

    // Function to define a macro
    public void defineMacro(String[] macroLines) {
        String[] firstLineTokens = macroLines[0].split(" ");
        String macroName = firstLineTokens[1];
    
        // Extract parameter names and add them to the PNT
        String[] parameters = firstLineTokens[2].split(",");
        PNT.addAll(Arrays.asList(parameters));
    
        // Add the macro name to MNT with the starting index of MDT
        MNT.add(new MNTEntry(macroName,PNT.size(), mdtIndex));
    
        // Process macro definition line by line and store in MDT
        for (int i = 1; i < macroLines.length; i++) {
            String line = macroLines[i];
            
            // Replace parameter names with their indices from PNT
            for (int j = 0; j < parameters.length; j++) {
                line = line.replace(parameters[j], "P" + j); // Using "P" + index format
            }
            
            MDT.add(line); // Add modified line to MDT
            mdtIndex++;
        }
    }
    

    public List<String> expandMacro(String callLine) {
        String[] tokens = callLine.split(" ");
        String macroName = tokens[0];
        String[] arguments = tokens[1].split(",");

        // Find macro in MNT
        MNTEntry macro = null;
        for (MNTEntry entry : MNT) {
            if (entry.macroName.equals(macroName)) {
                macro = entry;
                break;
            }
        }

        if (macro == null) {
            System.out.println("Macro not found!");
            return Collections.emptyList();
        }

        // List to store expanded macro instructions
        List<String> expandedCode = new ArrayList<>();

        // Expand the macro using MDT and replace parameters with arguments
        for (int i = macro.mdtIndex + 1; i < MDT.size(); i++) {
            String line = MDT.get(i);
            if (line.equals("ENDM")) break;

            // Replace parameter names (PNT) with the corresponding arguments
            for (int j = 0; j < PNT.size(); j++) {
                line = line.replace(PNT.get(j), arguments[j]);  // Replace actual parameter names
            }
            expandedCode.add(line);
        }

        return expandedCode;
    }
    

    // Function to process ALP and expand macros
    public void processALP(String[] alpLines) {
        System.out.println("Processed ALP with Macro Expansion:");
        for (String line : alpLines) {
            if (line.startsWith("SWAPPING")) {
                // Expand the macro when found in ALP
                List<String> expandedMacro = expandMacro(line);
                for (String expandedLine : expandedMacro) {
                    System.out.println(expandedLine);  // Print expanded macro
                }
            } else {
                System.out.println(line);  // Print ALP lines as is
            }
        }
    }

    // Print Tables (MDT, MNT, PNT)
    public void printTables() {
        System.out.println("\nMDT (Macro Definition Table):");
        for (int i = 0; i < MDT.size(); i++) {
            System.out.println(i + ": " + MDT.get(i));
        }

        System.out.println("\nMNT (Macro Name Table):");
        for (MNTEntry entry : MNT) {
            System.out.println(entry);
        }

        System.out.println("\nPNT (Parameter Name Table):");
        for (int i = 0; i < PNT.size(); i++) {
            System.out.println(i+": " + PNT.get(i));
        }
    }

    public static void main(String[] args) {
        C2P2 processor = new C2P2();

        
       
        // Macro definition (Example)
      /*  String[] macroDefinition = {
            "MACRO SWAPPING &i1,&i2",
            "MOV AL,grades[&i1]",
            "CMP AL,grades[&i2]",
            "JAE SKIP",
            "XCHG grades[&i1],grades[&i2]",
            "XCHG students[&i1],students[&i2]",
            "SKIP:",
            "ENDM"
        }; */
        List<String> macroLinesList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("C2P2\\macro.txt"))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                macroLinesList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
         // Convert List<String> to String[]
         String[] macroDefinition = macroLinesList.toArray(new String[0]);

        // Defining the macro
        processor.defineMacro(macroDefinition);
        System.out.println();

        // Print the macro tables (MDT, MNT, PNT)
        processor.printTables();
        System.out.println();

        // ALP with macro invocation
        // String[] alpLines = {
        //     "START",
        //     "MOV CX,0",
        //     "OUTER_LOOP: MOV SI,CX",
        //     "INNER_LOOP:",
        //     "MOV DI, SI",
        //     "INC DI",
        //     "SWAPPING SI,DI",  // Macro call to be expanded
        //     "INC SI",
        //     "CMP SI,3",
        //     "JLE INNER_LOOP",
        //     "INC CX",
        //     "CMP CX,3",
        //     "JLE OUTER_LOOP",
        //     "students: DB 1,2,3,4;",
        //     "grades: DS 5,1,6,2;",
        //     "END"
        // };
        List<String> alpLinesList = new ArrayList<>();
        boolean startReading = false;
        try (BufferedReader br = new BufferedReader(new FileReader("C2P2\\macro.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("START")) {
                    startReading = true;  // Start reading ALP lines from "START"
                }
                if (startReading) {
                    alpLinesList.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Convert List<String> to String[]
        String[] alpLines = alpLinesList.toArray(new String[0]);

        // Processing ALP and expanding the macro
        processor.processALP(alpLines);
    }
}