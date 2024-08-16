import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * CompilerScanner class
 * 
 * This class is responsible for tokenising input from a file during the lexical
 * analysis phase of the compiler. It reads characters, identifies tokens, and
 * handles various lexical elements such as comments, strings, and different
 * types of literals. The class maintains state information about the current
 * position in the input and provides methods for token identification and
 * processing.
 * 
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 * @since 2024-08-15
 */
public class CompilerScanner {

    private static final String[] CHAR_TYPES = new String[128];
    private FileReader fileReader;
    private int currentChar = 0;
    private int nextChar = 0;
    private ArrayList<Integer> charBuffer = new ArrayList<>();
    private boolean commentMode = false;
    private OutputController outputController;

    private int currentLine = 1;
    private int currentColumn = 0;
    private int tokenStartLine = 1;
    private int tokenStartColumn = 0;

    // INFO: Precomputing the types table for O(1) lookup time complexity
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

    /**
     * Default constructor
     */
    public CompilerScanner() {
    }

    /**
     * Constructs a CompilerScanner with a specified file path and output
     * controller.
     *
     * @param filePath         The path to the file to be tokenised.
     * @param outputController The controller for managing output.
     */
    public CompilerScanner(String filePath, OutputController outputController) {
        try {
            fileReader = new FileReader(new File(filePath));
            this.outputController = outputController;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

    }

    /**
     * Reads the next character from the file input stream.
     * 
     * @return The next character from the file as an integer representing its ASCII
     *         value. Returns -1 if the end of the file is reached or 0 if an
     *         IOException occurs.
     * 
     * @throws RuntimeException if a non-ASCII character (value > 127) is
     *                          encountered.
     * 
     * @implNote This method handles the reading of characters, updates the current
     *           position, and outputs the character to the listing. It also handles
     *           potential IOExceptions.
     */
    public int getNextChar() {
        try {
            int tempChar = fileReader.read();
            if (tempChar < 0) {
                tempChar = fileReader.read();
                updatePosition(tempChar);
            }
            if (tempChar > 127) {
                System.out.println("Error: Non Ascii character");
            }
            updatePosition(tempChar);
            outputController.outputToListing((char) tempChar, currentColumn);
            return tempChar;
        } catch (IOException e) {
            System.out.println("Exception " + e);
            return 0;
        }
    }

    /**
     * Retrieves the next token from the input.
     * 
     * @return A Token object representing the next identified token in the input.
     *         Returns a token with ID 0 if the end of file is reached.
     * 
     * @implNote This method handles various token types including comments,
     *           strings, numbers, and identifiers. It also manages the state of the
     *           tokeniser and handles special cases like combined operators.
     */
    public Token getNextToken() {
        ArrayList<Integer> asciiCharList = new ArrayList<>();
        boolean isSameState = true;
        boolean isFirstIteration = true;
        boolean hasDot = false;

        handleCommentOccurence();
        tokenStartLine = currentLine;
        tokenStartColumn = currentColumn;

        while (isSameState) {

            // INFO: Handle new start
            if (currentChar == 0) {
                currentChar = getNextChar();
            }

            // INFO: Handle EOF
            if (currentChar == -1) {
                return new Token(0, "", 0, 0);
            }

            // INFO: Handle Dead Characters
            if (handleDeadCharacters(asciiCharList, isSameState)) {
                continue;
            }

            // INFO: Ensure that the held char is accounted for
            if (!isFirstIteration) {
                if (charBuffer.size() != 0) {
                    currentChar = charBuffer.get(0);
                    charBuffer.remove(0);
                } else if (nextChar == 0) {
                    currentChar = this.getNextChar();
                } else {
                    currentChar = nextChar;
                    nextChar = 0;
                }
            } else {
                if (charBuffer.size() != 0) {
                    currentChar = charBuffer.remove(0);
                    charBuffer.remove(0);
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

                // INFO: Handles lexemes with one letter and numbers
                if (asciiCharList.size() == 1 && getType(previousChar).equals("letter")
                        && getType(currentChar).equals("number")) {
                    asciiCharList.add(currentChar);
                    isSameState = true;
                    currentChar = getNextChar();

                }
                // INFO: Handles lexemes with numbers
                if (getType(currentChar).equals("letter")) {
                    nextChar = this.getNextChar();
                    if (getType(nextChar).equals("number")) {
                        asciiCharList.add(currentChar);
                        currentChar = nextChar;
                        nextChar = 0;
                        isSameState = true;
                    }
                }
                // INFO: Handles doubles
                if (currentChar == 46) {
                    nextChar = this.getNextChar();
                    if (getType(nextChar).equals("number") && !hasDot) {
                        hasDot = true;
                        asciiCharList.add(currentChar);
                        currentChar = nextChar;
                        nextChar = 0;
                        isSameState = true;
                    }
                }
                // INFO: Handles Delimiters
                if (getType(previousChar).equals("potential_delimiter") && isSameState) {
                    if (handlePotentialDelimiters(asciiCharList, previousChar, isSameState)) {
                        break;
                    }
                } else if (isSameState) {
                    asciiCharList.add(currentChar);
                } else {
                    break;
                }
            } else {
                asciiCharList.add(currentChar);
                if (isValidChar(currentChar) && getType(currentChar).equals("potential_delimiter")
                        && (currentChar != 47) && !isCombinedOperator(currentChar, 61)) {
                    isSameState = false;
                    currentChar = getNextChar();
                }

            }

        }

        Token tempToken = findToken(asciiArrayListToString(asciiCharList));

        // INFO: Recursive goodness?
        if (tempToken.getTokenId() == -1) {
            tempToken = getNextToken();
        }
        return tempToken;
    }

    /**
     * Handles dead characters (whitespace, line feed, carriage return) in the
     * input.
     * 
     * @param asciiCharList The list of ASCII characters being processed.
     * @param isSameState   Boolean indicating if the current state is the same as
     *                      the previous.
     * @return true if a dead character was handled and the loop should continue,
     *         false otherwise.
     */
    private boolean handleDeadCharacters(ArrayList<Integer> asciiCharList, boolean isSameState) {

        if (currentChar == 32 || currentChar == 10 || currentChar == 13) {
            currentChar = this.getNextChar();
            tokenStartLine = currentLine;
            tokenStartColumn = currentColumn;
            return true;
        }
        return false;
    }

    /**
     * Handles potential delimiter characters in the input.
     * 
     * @param asciiCharList The list of ASCII characters being processed.
     * @param previousChar  The previous character encountered.
     * @param isSameState   Boolean indicating if the current state is the same as
     *                      the previous.
     * @return true if the loop should break after handling the delimiter, false
     *         otherwise.
     */
    private boolean handlePotentialDelimiters(ArrayList<Integer> asciiCharList, int previousChar, boolean isSameState) {
        if (!isValidChar(currentChar) && !isValidChar(previousChar)) {
            while (!isValidChar(currentChar) && getType(currentChar).equals("potential_delimiter")) {
                asciiCharList.add(currentChar);
                currentChar = getNextChar();
            }
            // currentChar = getNextChar();
            return true;
        }
        if (isCombinedOperator(previousChar, currentChar)) {
            asciiCharList.add(currentChar);
            currentChar = getNextChar();
            return true;
        }
        if (isValidChar(previousChar) && previousChar == 47
                && (currentChar != 42 && currentChar != 45)) {
            return true;
        } else if (isValidChar(previousChar) && previousChar == 47
                && (currentChar == 42 || currentChar == 45)) {
            nextChar = getNextChar();
            if (nextChar == 42 && currentChar == 42 || nextChar == 45 && currentChar == 45) {
                asciiCharList.add(currentChar);
                asciiCharList.add(nextChar);
                nextChar = 0;
                currentChar = getNextChar();
            } else {
                charBuffer.add(currentChar);
                charBuffer.add(nextChar);
                nextChar = 0;
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Checks if two characters form a combined operator.
     * 
     * @param char1 The first character of the potential combined operator.
     * @param char2 The second character of the potential combined operator.
     * @return true if the characters form a valid combined operator, false
     *         otherwise.
     */
    private boolean isCombinedOperator(int char1, int char2) {
        if (char2 == 61) {
            if (char1 == 60 || char1 == 62 || char1 == 61 || char1 == 33 || char1 == 42 || char1 == 43 || char1 == 45
                    || char1 == 47) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given lexeme is a comment start.
     *
     * @param lexeme The lexeme to check.
     * @return true if the lexeme is a comment start, false otherwise.
     */
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
            nextChar = 0;
            currentChar = getNextChar();
            return true;
        }
        return false;
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
            nextChar = 0;
        }
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
    private Token handleStringOccurrence() {
        ArrayList<Integer> asciiCharList = new ArrayList<>();
        tokenStartLine = currentLine;
        tokenStartColumn = currentColumn;

        currentChar = getNextChar();
        while (currentChar != 34) {
            if (currentChar == 10) {
                return new Token(68, "Unterminated string: \"" + asciiArrayListToString(asciiCharList), tokenStartLine,
                        tokenStartColumn);
            }
            asciiCharList.add(currentChar);
            currentChar = getNextChar();
        }
        currentChar = getNextChar();
        return new Token(67, asciiArrayListToString(asciiCharList), tokenStartLine, tokenStartColumn);
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
     * @return A String representing the type of the character.
     */
    private String getType(int incomingChar) {
        if (incomingChar == -1) {
            return "EOF";
        }
        return incomingChar < 128 ? CHAR_TYPES[incomingChar] : "other";
    }

    /**
     * Converts an ArrayList of ASCII character codes to a String.
     *
     * @param asciiList An ArrayList of Integer objects, where each Integer
     *                  represents the ASCII code of a character.
     * @return A String formed by converting each ASCII code to its corresponding
     *         character and concatenating them.
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
     * @param lexeme The string representation of the lexeme to be tokenised.
     * @return A Token object representing the lexeme.
     */
    private Token findToken(String lexeme) {
        if (commentCheck(lexeme)) {
            return new Token(-1, "comment", tokenStartLine, tokenStartColumn);
        }

        Tokeniser.TokenType keywordType = Tokeniser.getKeywordTokenType(lexeme);
        if (keywordType != null) {
            return new Token(Tokeniser.getTokenCode(keywordType), "", tokenStartLine, tokenStartColumn);
        }

        if (isIntegerLiteral(lexeme)) {
            return handleIntegerLiteral(lexeme);
        }

        if (isFloatLiteral(lexeme)) {
            return handleFloatLiteral(lexeme);
        }

        if (isIdentifier(lexeme)) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TIDEN), lexeme, tokenStartLine,
                    tokenStartColumn);
        }

        Tokeniser.TokenType operatorType = Tokeniser.getOperatorTokenType(lexeme);
        if (operatorType != null) {
            return new Token(Tokeniser.getTokenCode(operatorType), "", tokenStartLine, tokenStartColumn);
        }

        return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF), lexeme, tokenStartLine, tokenStartColumn);
    }

    /**
     * Checks if the given lexeme is an integer literal.
     * 
     * @param lexeme The string to be checked.
     * @return true if the lexeme consists only of digits (0-9), false otherwise.
     */
    private boolean isIntegerLiteral(String lexeme) {
        if (lexeme.isEmpty())
            return false;
        for (char c : lexeme.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    /**
     * Checks if the given lexeme is a float literal.
     * 
     * @param lexeme The string to be checked.
     * @return true if the lexeme is a valid float representation, false otherwise.
     */
    private boolean isFloatLiteral(String lexeme) {
        if (lexeme.isEmpty())
            return false;
        boolean hasDecimalPoint = false;
        if (lexeme.length() == 1 && lexeme.charAt(0) == '.') {
            return false;
        }
        for (int i = 0; i < lexeme.length(); i++) {
            char c = lexeme.charAt(i);
            if (c == '.') {
                if (hasDecimalPoint)
                    return false; // More than one decimal point
                hasDecimalPoint = true;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return hasDecimalPoint;
    }

    /**
     * Checks if the given lexeme is a valid identifier.
     * 
     * @param lexeme The string to be checked.
     * @return true if the lexeme is a valid identifier, false otherwise.
     */
    private boolean isIdentifier(String lexeme) {
        if (lexeme.isEmpty())
            return false;
        if (!Character.isLetter(lexeme.charAt(0)))
            return false;
        for (int i = 1; i < lexeme.length(); i++) {
            char c = lexeme.charAt(i);
            if (!Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }

    /**
     * Handles the processing of an integer literal.
     * 
     * @param lexeme The string representation of the integer literal.
     * @return A Token object representing the integer literal or an error token if
     *         invalid.
     */
    private Token handleIntegerLiteral(String lexeme) {
        try {
            long value = Long.parseLong(lexeme);
            // TODO: Modify the output to remove undef for integer overflows
            if (value > Integer.MAX_VALUE) {
                return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF),
                        "Integer Literal Overflow", tokenStartLine,
                        tokenStartColumn);
            }
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TILIT), lexeme, tokenStartLine,
                    tokenStartColumn);
        } catch (NumberFormatException e) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF),
                    "lexical error: Invalid Integer Literal", tokenStartLine,
                    tokenStartColumn);
        }
    }

    /**
     * Handles the processing of a float literal.
     * 
     * @param lexeme The string representation of the float literal.
     * @return A Token object representing the float literal or an error token if
     *         invalid.
     */
    private Token handleFloatLiteral(String lexeme) {
        try {
            double value = Double.parseDouble(lexeme);
            if (value > 1.7976931348623158e+308) {
                return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF), "Float Literal Overflow",
                        tokenStartLine, tokenStartColumn);
            }
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TFLIT), lexeme, tokenStartLine,
                    tokenStartColumn);
        } catch (NumberFormatException e) {
            return new Token(Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF), "Invalid Float Literal", tokenStartLine,
                    tokenStartColumn);
        }
    }

    /**
     * Checks if the given character is a valid character for tokenisation.
     * 
     * @param ch The ASCII value of the character to check.
     * @return true if the character is valid for tokenisation, false otherwise.
     */
    private boolean isValidChar(int ch) {
        if ((ch < 33 || ch > 126) || (ch != 44 && ch != 91 && ch != 93 && ch != 40 && ch != 41 &&
                ch != 61 && ch != 43 && ch != 45 && ch != 42 && ch != 47 && ch != 37 && ch != 94 &&
                ch != 60 && ch != 62 && ch != 58 && ch != 59 && ch != 46)) {
            return false;
        }
        return true;
    }

    /**
     * Updates the current line and column position based on the input character.
     * 
     * @param tempChar The character to process for position updating.
     */
    private void updatePosition(int tempChar) {
        if (tempChar == 10) {
            currentLine++;
            currentColumn = 0;
        } else {
            currentColumn++;
        }
    }
}
