import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

// TODO: Need to handle lines and cols
// TODO: Need to handle lexical errors
// TODO: Need a hash map that maps all keyword to a number
// TODO: Slim down java docs once up to speed

public class TokenTerminator {

    private HashMap<String, Integer> intermediateCodeTable = new HashMap<>();
    private static final String[] CHAR_TYPES = new String[128];
    private FileReader fileReader;
    private int currentChar = 0;
    private int nextChar = 0;
    private boolean commentMode = false;

    // INFO: Precomputing the types table for O(1) lookup
    static {
        Arrays.fill(CHAR_TYPES, "other");

        CHAR_TYPES[32] = "whitespace";
        CHAR_TYPES[10] = "linefeed";
        CHAR_TYPES[13] = "carriage_return";
        for (int i = 48; i <= 57; i++)
            CHAR_TYPES[i] = "number";
        for (int i = 65; i <= 90; i++)
            CHAR_TYPES[i] = "letter";
        for (int i = 97; i <= 122; i++)
            CHAR_TYPES[i] = "letter";
        for (int i = 33; i <= 47; i++)
            CHAR_TYPES[i] = "potential_delimiter";
        for (int i = 58; i <= 64; i++)
            CHAR_TYPES[i] = "potential_delimiter";
        for (int i = 91; i <= 96; i++)
            CHAR_TYPES[i] = "potential_delimiter";
        for (int i = 123; i <= 126; i++)
            CHAR_TYPES[i] = "potential_delimiter";
    }

    public TokenTerminator() {
    }

    public TokenTerminator(String filePath) {
        try {
            fileReader = new FileReader(new File(filePath));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

    }

    // INFO: File reader returns a char in unicode. Unicode is identical to ASCII
    // for the first 127 characters. We will just calculate all characters in ASCII.
    // We will throw an error for any characters that are >127
    /**
     * Reads the next character from the file input stream.
     * 
     * @return The next character from the file as an integer representing its ASCII
     *         value. Returns 0 if an IOException occurs during reading.
     * 
     * @throws RuntimeException if a non-ASCII character (value > 127) is
     *                          encountered.
     * 
     * @implNote This method currently does not handle non-ASCII characters (Unicode
     *           values > 127). Handling of such characters is marked as a TODO.
     * 
     * @implSpec If an IOException occurs during file reading, the method catches
     *           the exception, prints it to the console, and returns 0.
     */
    public int getNextChar() {
        try {
            int tempChar = fileReader.read();
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

        handleCommentOccurence();
        while (isSameState) {

            // INFO: Handle EOF
            if (currentChar == -1) {
                return new Token(0, "", 0, 0);
            }

            // INFO: Handle dead characters
            if (currentChar == 32 || currentChar == 10 || currentChar == 13) {
                currentChar = this.getNextChar();
                continue;
            }

            // INFO: Ensure that the held char is accounted for
            if (!isFirstIteration) {
                if (nextChar == 0) {
                    currentChar = this.getNextChar();
                } else {
                    currentChar = nextChar;
                    nextChar = 0;
                }
            }

            // INFO: Handle strings when " found
            if (currentChar == 34) {
                return handleStringOccurrence();
            }

            isFirstIteration = false;

            if (!asciiCharList.isEmpty()) {
                int previousChar = asciiCharList.get(asciiCharList.size() - 1);
                isSameState = checkState(currentChar, previousChar);
                // INFO: Handles doubles
                if (currentChar == 46) {
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

        Token tempToken = findToken(asciiArrayListToString(asciiCharList));

        // INFO: Recursive goodness?
        if (tempToken.getTokenId() == -1) {
            tempToken = getNextToken();
        }
        return tempToken;
    }

    private boolean commentCheck(String lexeme) {

        if (lexeme.equals("/**")) {
            this.commentMode = true;
            return true;
        }
        if (lexeme.equals("/--")) {
            int tempChar = getNextChar();
            while (tempChar != 10) {
                tempChar = getNextChar();
            }
            return true;
        }
        return false;
    }

    /**
     * Handles the occurrence of a string literal in the input.
     * This method is called when a double quote (") is encountered, denoting the
     * start of a string. It reads characters until the closing double quote is
     * found, constructing the string token.
     *
     * @return A Token object representing the string literal. The token has an ID
     *         of 67 (TSTRG),and its value is the content of the string (excluding
     *         the quotation marks).
     *
     * @implNote This method assumes that the opening quotation mark has already
     *           been consumed. It does not include the quotation marks in the
     *           returned token's value. After processing the string, it advances
     *           the currentChar to the character following the closing quote.
     */
    private void handleCommentOccurence() {
        if (commentMode) {
            String delimiterBuffer = "";
            while (true) {
                if (currentChar == -1) {
                    commentMode = false;
                    break;
                }
                if (currentChar == 42 || currentChar == 47) {
                    delimiterBuffer += (char) currentChar;
                    if (delimiterBuffer.equals("**/")) {
                        commentMode = false;
                        break;
                    }
                } else {
                    delimiterBuffer = "";
                }
                currentChar = getNextChar();
            }
            currentChar = getNextChar();
        }
    }

    /**
     * Handles the occurrence of comments in the input.
     * This method is called when the scanner is in comment mode (commentMode is
     * true). It processes characters until the end of the comment is reached or the
     * end of file is encountered.
     *
     * @implNote This method handles multi-line comments that start with "/**" and
     *           end with "**\/". It updates the commentMode flag to false when the
     *           end of a comment is reached or EOF is encountered. The method does
     *           not return anything but updates the currentChar to the first
     *           character after the comment.
     */
    private Token handleStringOccurrence() {
        ArrayList<Integer> asciiCharList = new ArrayList<>();
        currentChar = getNextChar();
        while (currentChar != 34) {
            asciiCharList.add(currentChar);
            currentChar = getNextChar();
        }
        currentChar = getNextChar();
        return new Token(67, asciiArrayListToString(asciiCharList), 0, 0);
    }

    /**
     * Checks if two characters are of the same type, determining if they belong to
     * the same lexical state.
     *
     * @param char1 The ASCII value of the first character.
     * @param char2 The ASCII value of the second character.
     * @return true if both characters are of the same type (as determined by the
     *         getType method), false otherwise.
     *
     * @see #getType(int)
     */
    private boolean checkState(int char1, int char2) {
        return this.getType(char1).equals(this.getType(char2));
    }

    /**
     * Determines the type of a given character based on its ASCII value.
     *
     * @param incomingChar The ASCII value of the character to be typed.
     * @return A String representing the type of the character. Possible return
     *         values are:
     *         - "whitespace" for space character (ASCII 32)
     *         - "linefeed" for line feed character (ASCII 10)
     *         - "carriage_return" for carriage return character (ASCII 13)
     *         - "number" for digits 0-9 (ASCII 48-57)
     *         - "letter" for uppercase and lowercase letters (ASCII 65-90 and
     *         97-122)
     *         - "potential_delimiter" for certain special characters (ASCII ranges
     *         33-47, 58-64, 91-96, 123-126)
     *         - "other" for any character not falling into the above categories
     */
    private String getType(int incomingChar) {
        return incomingChar < 128 ? CHAR_TYPES[incomingChar] : "other";
    }

    /**
     * Converts an ArrayList of ASCII character codes to a String.
     *
     * @param asciiList An ArrayList of Integer objects, where each Integer
     *                  represents the ASCII code of a character.
     *
     * @return A String formed by converting each ASCII code to its corresponding
     *         character and concatenating them. Returns an empty string if the
     *         input list is null or empty.
     */
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

    /**
     * Finds and returns a Token object based on the given lexeme.
     *
     * @param lexeme The string representation of the lexeme to be tokenized.
     * @return A Token object representing the lexeme. The returned Token can be one
     *         of three types:
     *         1. A comment token (id: -1) if the lexeme is identified as a comment.
     *         2. A token with an id from the intermediateCodeTable if the lexeme is
     *         a known keyword or symbol.
     *         3. A default token (id: 2) if the lexeme is not recognized as a
     *         comment or known symbol.
     */
    private Token findToken(String lexeme) {
        if (commentCheck(lexeme)) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF), "comment", 0, 0);
        }

        Tokeniser.TokenType keywordType = Tokeniser.getKeywordTokenType(lexeme);
        if (keywordType != null) {
            return new Token(Tokeniser.getTokenCode(keywordType), "", 0, 0);
        }

        if (lexeme.matches("\\d+")) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TILIT), lexeme, 0, 0);
        }
        if (lexeme.matches("\\d+\\.\\d+")) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TFLIT), lexeme, 0, 0);
        }
        if (lexeme.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TIDEN), lexeme, 0, 0);
        }

        Tokeniser.TokenType operatorType = Tokeniser.getOperatorTokenType(lexeme);
        if (operatorType != null) {
            return new Token(Tokeniser.getTokenCode(operatorType), "", 0, 0);
        }

        // If no match found, return TUNDF
        return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF), lexeme, 0, 0);
    }

}
