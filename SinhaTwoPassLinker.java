import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * This program implements a two pass linker that reads in a set of modules.
 * It returns a symbol table with the absolute addresses of the variables defined in the modules.
 * It returns a memory map that relocates addresses and resolves external references.
 * Various errors are printed out accordingly. 
 * @author Flavia Alka Sinha 
 * Operating Systems Fall 2018
 * September 19, 2018 
 */
public class SinhaTwoPassLinker {
    public static void main(String[] args){
        
        Scanner scanner1 = new Scanner(System.in);

        // symbol table hashmap
        HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();

        // hashmap that maps each symbol definition to the module in which it is defined
        HashMap<String, Integer> definitions = new HashMap<String, Integer>();

        // array list of use lines
        ArrayList<String> uses = new ArrayList<String>();

        // hashmap of total use strings
        HashMap<String, ArrayList<Integer>> totalusehash = new HashMap<String, ArrayList<Integer>>();

        // array list of instructions
        ArrayList<String> instructions = new ArrayList<String>();

        // array list of memory map
        ArrayList<String> memory = new ArrayList<String>();

        // array list of memory errors
        HashMap<Integer, String> memErrors = new HashMap<Integer, String>();

        // warning array list for when a symbol is defined but not used
        ArrayList<String> definedNotUsed = new ArrayList<String>();

        // error hashmap for symbol table
        HashMap<String, ArrayList<String>> errorSymbol = new HashMap<String, ArrayList<String>>();

        // this counts the first number which tells us the number of modules
        int numOfModules = scanner1.nextInt();
        int[] moduleAddresses = new int[numOfModules + 1];
        moduleAddresses[0] = 0; // address of first module is 0
        // base address, also relocation constant
        int offset = 0;
        // loop through each module
        for (int i = 0; i < numOfModules; i++) {
            // first number in definition list tells us num of modules defined
            int symbolsInMod = scanner1.nextInt();
            // loop through definition list to get name and relative loc of each symbol
            for (int j = 0; j < symbolsInMod; j++) {
                ArrayList<String> errorstoAdd = new ArrayList<String>();
                // store the name and relative address of the symbol
                String sym = scanner1.next();
                int loc = 0;
                definitions.put(sym, i);
                // if symbol is multiply defined, print error message
                if (symbolTable.containsKey(sym)) {
                    errorstoAdd.add("Error: This variable is multiply defined; last value used.");
                    loc = Integer.parseInt(scanner1.next()) + moduleAddresses[i];
                    symbolTable.put(sym, loc);
                } else 
                {
                    loc = Integer.parseInt(scanner1.next()) + moduleAddresses[i];
                    symbolTable.put(sym, loc);
                }   
                errorSymbol.put(sym, errorstoAdd);
            }

            int numOfUses = scanner1.nextInt();
            String useAdding = numOfUses + " ";
            for (int u = 0; u < numOfUses; u++) {
                String str = "";
                String next = scanner1.next();
                while (!next.equals("-1")) {
                    str += next + " ";
                    next = scanner1.next();
                }
                useAdding += str + "-1 ";
            }
            uses.add(useAdding);
            int numOfInstructions = scanner1.nextInt();
            String instructionlist = numOfInstructions + " ";
            for (int q = 0; q < numOfInstructions; q++) {
                instructionlist += scanner1.nextInt() + " ";
            }
            instructions.add(instructionlist);
            offset += numOfInstructions;
            moduleAddresses[i + 1] = offset;
        }
        
        //this for loop checks whether the definition exceeds the module size.
        for (String key : definitions.keySet())
        {
            int val = definitions.get(key);
            if (symbolTable.get(key) >= moduleAddresses[val+1])
            {
                symbolTable.put(key, moduleAddresses[val+1]-1);
                errorSymbol.get(key).add("Error: Definition exceeds module size; last word in module used.");
            }
        }

        // Now we print out the symbol table hashmap
        System.out.printf("Symbol Table \n");

        for (String key : symbolTable.keySet()) {
            String printerror = errorSymbol.get(key).toString().replace(",", "").replace("[", "").replace("]", "")
                    .trim();
            System.out.printf("%s = %d %s \n", key, symbolTable.get(key), printerror);
        }
        scanner1.close();

        // PASS 2 BEGINS
        System.out.printf("\nMemory Map \n");

        for (int a = 0; a < numOfModules; a++) {
            String useString = uses.get(a);
            //System.out.println(useString);
            
            HashMap<String, ArrayList<Integer>> usehash = new HashMap<String, ArrayList<Integer>>();

            // this creates a hashmap of the corresponding use string
            // each key is a symbol mapping to the value in which it is used.
            String separate[] = useString.split("-1");
            
            // loop through number of uses in use list
            for (int i = 0; i < separate.length; i++) 
            {
                ArrayList<Integer> usevalues = new ArrayList<Integer>();
                String usekey = "";
                String[] arr = separate[i].trim().split("\\s+");
                if (i == 0 && arr.length != 1) 
                {
                    usekey = arr[1];
                    for (int k = 2; k < arr.length; k++) 
                    {
                        usevalues.add(Integer.parseInt(arr[k]));
                    }
                } 
                else 
                {
                    usekey = arr[0];
                    for (int m = 1; m < arr.length; m++) 
                    {
                        usevalues.add(Integer.parseInt(arr[m]));
                    }
                }
                usehash.put(usekey, usevalues);
                totalusehash.put(usekey, usevalues);
            }
            

            String instr = instructions.get(a);
            String[] split = instr.trim().split("\\s+");
            int numOfInstructions = Integer.parseInt(split[0]);
            // this creates a hashmap where each key is a relative instruction number
            // each value is the symbol in the use line.
            // this allows for error checking, i.e. if two symbols are used in the same
            // instruction.
            HashMap<Integer, String> flippedUse = new HashMap<Integer, String>();
            //System.out.println("use hash is: " + usehash.keySet());
            //System.out.println("flipped use is " + flippedUse.keySet());
            for (int i = 0; i < numOfInstructions; i++) 
            {
                for (String k : usehash.keySet()) 
                {
                    if (usehash.get(k).contains(i)) 
                    {
                        if (flippedUse.containsKey(i)) 
                        {
                            memErrors.put(i, "Error: Multiple variables used in instruction; all but last ignored");
                            //flippedUse.put(i, k + "error" );
                        }
                        //System.out.println(i + " " + k);
                        flippedUse.put(i, k);
                    }
                }
            }
            //System.out.println("mem errors is: " + memErrors.keySet());

            //System.out.println("final flipped use is: " + flippedUse.keySet());
            //System.out.println();
            // loop through each instruction number and parse the last integer 
            // according to project specs, handle each case accordingly 
            for (int b = 1; b < split.length; b++) 
            {
                String num = split[b];
                if (num.charAt(4) == '1') 
                {
                    memory.add(num.substring(0, 4));
                } else if (num.charAt(4) == '2') 
                {
                    String address = num.substring(1, 4);
                    if (Integer.parseInt(address) >= 300) 
                    {
                        String newaddress = "299";
                        memory.add("" + num.charAt(0) + newaddress + " Error: Absolute address exceeds machine size; "
                                + "largest legal value used.");
                    } else 
                    {
                        memory.add("" + num.charAt(0) + address);
                    }
                } else if (num.charAt(4) == '3') 
                {
                    int relocated = Integer.parseInt(num.substring(1, 4)) + moduleAddresses[a];
                    String relocatedstring = relocated + "";
                    String append = "";
                    if (relocatedstring.length() != 3) {
                        int missing = 3 - relocatedstring.length();

                        for (int p = 0; p < missing; p++) 
                        {
                            append += "0";
                        }
                        append += relocatedstring;
                        memory.add("" + num.charAt(0) + append);
                    } else 
                    {
                        memory.add("" + num.charAt(0) + relocated);
                    }
                    //deal with external reference case 
                } else if (num.charAt(4) == '4') 
                {
                    String addressToAdd = "";
                    // now iterate through the hashmap
                    String k = flippedUse.get(b - 1);
            
                    {
                        
                        {
                            if (!symbolTable.containsKey(k)) {
                                addressToAdd += "111 Error: " + k + " is not defined, 111 is used.";
                            }
                            // this is the absolute address we have to add to the instruction.
                            else 
                            {
                                addressToAdd += symbolTable.get(k) + "";
                            }
                        }
                    
                    }
                    // now we replace the old address in the instruction with the new absolute
                    // address
                    String zeros = "";
                    if (addressToAdd.length() != 3) {
                        for (int i = 0; i < 3 - addressToAdd.length(); i++) {
                            zeros += "0";
                        }
                    }
                    memory.add("" + num.charAt(0) + zeros + addressToAdd);
                }
            }
        }

        //now print out the rest of the memory map, along with the errors. 
        int count = 0;
        for (String m : memory) {
            System.out.print("" + count + ": " + m);
            if (memErrors.get(count) != null)
            {
                System.out.print(" " + memErrors.get(count));
            }
            else { System.out.print(""); } 
            System.out.println();
            count++;
        }

        for (String key : symbolTable.keySet()) {
            if (!totalusehash.containsKey(key)) {
                String warning1 = "Warning: " + key + " was defined in module " + definitions.get(key)
                        + " but never used.";
                definedNotUsed.add(warning1);
            }
        }
        for (String warning : definedNotUsed) {
            System.out.println(warning);
        }

}
}
