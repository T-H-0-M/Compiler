import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

//TODO: Handle whitespace
//TODO: Handle Comments
//TODO: Handle Strings

public class TokenTerminator {

    private HashMap<String, Integer> intermediateCodeTable = new HashMap<>();
    private FileReader fileReader;
    private int currentChar = 0;
    private int nextChar = 0;

    public TokenTerminator() {
    }

    public TokenTerminator(String filePath) {
        try {
            fileReader = new FileReader(new File(filePath));
            this.initialiseIntermediateCodeTable();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

    }

    public int getNextChar() {
        try {
            // INFO: File reader returns a char in unicode. Unicode is indenitcal to ascii
            // for the first 127 characters. We will just calculate all characters in ascii.
            // We will throw an error for any characters that are >127
            int tempChar = fileReader.read();
            if (tempChar == -1) {
            }
            if (tempChar > 127) {
                // TODO: Handle non-ascii characters
            }
            return tempChar;
        } catch (IOException e) {
            System.out.println("Exception " + e);
            return 0;
        }
    }

    public Token getNextToken() {
        ArrayList<Integer> asciiCharList = new ArrayList<>();
        boolean isSameState = true;
        boolean isFirstIteration = true;
        while (isSameState) {
            if (!isFirstIteration) {
                if (nextChar == 0) {
                    currentChar = this.getNextChar();
                }
            }
            if (currentChar == -1) {
                return new Token(1, "TEOF", 0, 0);
            }
            isFirstIteration = false;
            if (!asciiCharList.isEmpty()) {
                int previousChar = asciiCharList.get(asciiCharList.size() - 1);
                isSameState = checkState(currentChar, previousChar);
                if ((int) currentChar == 46) {
                    nextChar = this.getNextChar();
                    if (getType(nextChar).equals("number")) {
                        asciiCharList.add(currentChar);
                        currentChar = nextChar;
                        isSameState = true;
                    }
                }
                if (isSameState) {
                    asciiCharList.add(currentChar);
                } else {
                    break;
                }
            } else {
                asciiCharList.add(currentChar);
            }

        }
        return findToken(asciiArrayListToString(asciiCharList));
    }

    private boolean checkState(int char1, int char2) {
        return this.getType(char1).equals(this.getType(char2));
    }

    private String getType(int incomingChar) {
        if (incomingChar == 32)
            return "whitespace";
        if (incomingChar == 10)
            return "linefeed";
        if (incomingChar == 13)
            return "carriage_return";
        if (incomingChar >= 48 && incomingChar <= 57)
            return "number";
        if ((incomingChar >= 65 && incomingChar <= 90) || (incomingChar >= 97 && incomingChar <= 122))
            return "letter";
        if ((incomingChar >= 33 && incomingChar <= 47) || (incomingChar >= 58 && incomingChar <= 64) ||
                (incomingChar >= 91 && incomingChar <= 96) || (incomingChar >= 123 && incomingChar <= 126))
            return "potential_delimiter";
        return "other";
    }

    private String asciiArrayListToString(ArrayList<Integer> asciiList) {
        if (asciiList == null || asciiList.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(asciiList.size());
        for (int asciiChar : asciiList) {
            stringBuilder.append((char) asciiChar);
        }
        return stringBuilder.toString();
    }

    private Token findToken(String lexeme) {

        if (intermediateCodeTable.containsKey(lexeme)) {
            return new Token(intermediateCodeTable.get(lexeme), "", 0, 0);
        }
        return new Token(2, lexeme, 0, 0);

        // TODO: need to add comments and string flags
        // TODO: if identifier then need to handle storing the lexeme
        // TODO: Also need to handle lines and chars

        // if contains . then it is a float
        // if contains all numbers then it is a int
        // if contains all letters then it is a identifier
        // if starts with number and contains letter then unid
        // if starts with a letter and contains number the identifier
        // if is a symbol the unid
        // if starts with " and ends with " then string

        // TODO: Account for strings

        // find token from hash table for the given lexeme

    }

    private void initialiseIntermediateCodeTable() {
        intermediateCodeTable = new HashMap<>();

        // Token value for end of file
        intermediateCodeTable.put("TTEOF", 0);

        // The 38 keywords
        intermediateCodeTable.put("TCD24", 1);
        intermediateCodeTable.put("TCONS", 2);
        intermediateCodeTable.put("TTYPD", 3);
        intermediateCodeTable.put("TTDEF", 4);
        intermediateCodeTable.put("TARRD", 5);
        intermediateCodeTable.put("TMAIN", 6);
        intermediateCodeTable.put("TBEGN", 7);
        intermediateCodeTable.put("TTEND", 8);
        intermediateCodeTable.put("TARAY", 9);
        intermediateCodeTable.put("TTTOF", 10);
        intermediateCodeTable.put("TFUNC", 11);
        intermediateCodeTable.put("TVOID", 12);
        intermediateCodeTable.put("TCNST", 13);
        intermediateCodeTable.put("TINTG", 14);
        intermediateCodeTable.put("TFLOT", 15);
        intermediateCodeTable.put("TBOOL", 16);
        intermediateCodeTable.put("TTFOR", 17);
        intermediateCodeTable.put("TREPT", 18);
        intermediateCodeTable.put("TUNTL", 19);
        intermediateCodeTable.put("TTTDO", 20);
        intermediateCodeTable.put("TWHIL", 21);
        intermediateCodeTable.put("TIFTH", 22);
        intermediateCodeTable.put("TELSE", 23);
        intermediateCodeTable.put("TELIF", 24);
        intermediateCodeTable.put("TSWTH", 25);
        intermediateCodeTable.put("TCASE", 26);
        intermediateCodeTable.put("TDFLT", 27);
        intermediateCodeTable.put("TBREK", 28);
        intermediateCodeTable.put("TINPT", 29);
        intermediateCodeTable.put("TPRNT", 30);
        intermediateCodeTable.put("TPRLN", 31);
        intermediateCodeTable.put("TRETN", 32);
        intermediateCodeTable.put("TNOTT", 33);
        intermediateCodeTable.put("TTAND", 34);
        intermediateCodeTable.put("TTTOR", 35);
        intermediateCodeTable.put("TTXOR", 36);
        intermediateCodeTable.put("TTRUE", 37);
        intermediateCodeTable.put("TFALS", 38);

        // The operators and delimiters
        intermediateCodeTable.put("TCOMA", 39);
        intermediateCodeTable.put("TLBRK", 40);
        intermediateCodeTable.put("TRBRK", 41);
        intermediateCodeTable.put("TLPAR", 42);
        intermediateCodeTable.put("TRPAR", 43);
        intermediateCodeTable.put("TEQUL", 44);
        intermediateCodeTable.put("TPLUS", 45);
        intermediateCodeTable.put("TMINS", 46);
        intermediateCodeTable.put("TSTAR", 47);
        intermediateCodeTable.put("TDIVD", 48);
        intermediateCodeTable.put("TPERC", 49);
        intermediateCodeTable.put("TCART", 50);
        intermediateCodeTable.put("TLESS", 51);
        intermediateCodeTable.put("TGRTR", 52);
        intermediateCodeTable.put("TCOLN", 53);
        intermediateCodeTable.put("TSEMI", 54);
        intermediateCodeTable.put("TDOTT", 55);
        intermediateCodeTable.put("TLEQL", 56);
        intermediateCodeTable.put("TGEQL", 57);
        intermediateCodeTable.put("TNEQL", 58);
        intermediateCodeTable.put("TEQEQ", 59);
        intermediateCodeTable.put("TPLEQ", 60);
        intermediateCodeTable.put("TMNEQ", 61);
        intermediateCodeTable.put("TSTEQ", 62);
        intermediateCodeTable.put("TDVEQ", 63);

        // The tokens which need tuple values
        intermediateCodeTable.put("TIDEN", 64);
        intermediateCodeTable.put("TILIT", 65);
        intermediateCodeTable.put("TFLIT", 66);
        intermediateCodeTable.put("TSTRG", 67);
        intermediateCodeTable.put("TUNDF", 68);
    }

}
