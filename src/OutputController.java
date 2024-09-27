import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// TODO: create flag for scanner output and parser output
/**
 * OutputController class
 * 
 * This class is responsible for managing the output of tokens and errors during
 * the lexical analysis phase. It handles formatting, line management, and
 * writing to an output file.
 * 
 * Date: 27-09-2024
 *
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.1
 */
public class OutputController {
    private static final int LINE_LENGTH = 60;
    private StringBuilder currentLine;
    private List<String> formattedLines;
    private PrintWriter writer;
    private int lineNumber;
    private boolean isFirstOutput = true;
    private int currentCol = 4;

    public OutputController() {
    }

    public OutputController(String outputFileName) {
        this.currentLine = new StringBuilder();
        this.formattedLines = new ArrayList<>();
        this.lineNumber = 1;
        initialiseWriter(outputFileName);
    }

    private String getReadableNonTerminal(String nodeType) {
        switch (nodeType) {
            case "NPROG":
                return "Program";
            case "NGLOB":
                return "Globals";
            case "NILIST":
                return "Initialization List";
            case "NINIT":
                return "Initialization";
            case "NTYPEL":
                return "Type List";
            case "NRTYPE":
                return "Record Type Definition";
            case "NATYPE":
                return "Array Type Definition";
            case "NFLIST":
                return "Field List";
            case "NALIST":
                return "Array Declaration List";
            case "NARRD":
                return "Array Declaration";
            case "NFUNCS":
                return "Functions";
            case "NFUND":
                return "Function Definition";
            case "NPLIST":
                return "Parameter List";
            case "NSIMP":
                return "Simple Parameter";
            case "NARRP":
                return "Array Parameter";
            case "NARRC":
                return "Constant Array Parameter";
            case "NDLIST":
                return "Declaration List";
            case "NMAIN":
                return "Main Body";
            case "NSDLST":
                return "Simple Declaration List";
            case "NSDECL":
                return "Simple Declaration";
            case "NTDECL":
                return "Type Declaration";
            case "NSTATS":
                return "Statements";
            case "NFORL":
                return "For Loop";
            case "NREPT":
                return "Repeat Statement";
            case "NDOWL":
                return "Do While Loop";
            case "NASGNS":
                return "Assignment List";
            case "NIFTH":
                return "If Then Statement";
            case "NIFTE":
                return "If Then Else Statement";
            case "NIFEF":
                return "If Else If Statement";
            case "NSWTCH":
                return "Switch Statement";
            case "NCASLT":
                return "Case List";
            case "NASGN":
                return "Assignment Operator '='";
            case "NPLEQ":
                return "Plus Equals Operator '+='";
            case "NMNEQ":
                return "Minus Equals Operator '-='";
            case "NSTEA":
                return "Times Equals Operator '*='";
            case "NDVEQ":
                return "Divide Equals Operator '/='";
            case "NINPUT":
                return "Input Statement";
            case "NPRINT":
                return "Print Statement";
            case "NPRLN":
                return "Print Line Statement";
            case "NCALL":
                return "Function Call";
            case "NRETN":
                return "Return Statement";
            case "NVLIST":
                return "Variable List";
            case "NSIMV":
                return "Simple Variable";
            case "NAELT":
                return "Array Element";
            case "NARRV":
                return "Record Element";
            case "NEXPL":
                return "Expression List";
            case "NBOOL":
                return "Boolean Expression";
            case "NAND":
                return "Logical AND Operator";
            case "NOR":
                return "Logical OR Operator";
            case "NXOR":
                return "Logical XOR Operator";
            case "NEQL":
                return "Equality Operator '=='";
            case "NNEQ":
                return "Not Equal Operator '!='";
            case "NGRT":
                return "Greater Than Operator '>'";
            case "NLSS":
                return "Less Than Operator '<'";
            case "NLEQ":
                return "Less Than or Equal Operator '<='";
            case "NGEQ":
                return "Greater Than or Equal Operator '>='";
            case "NADD":
                return "Addition";
            case "NSUB":
                return "Subtraction";
            case "NMUL":
                return "Multiplication";
            case "NDIV":
                return "Division";
            case "NMOD":
                return "Modulus";
            case "NPOW":
                return "Power Operator";
            case "NILIT":
                return "Integer Literal";
            case "NFLIT":
                return "Floating Point Literal";
            case "NTRUE":
                return "Boolean True";
            case "NFALS":
                return "Boolean False";
            case "NFCALL":
                return "Function Call";
            case "NPRLST":
                return "Print List";
            case "NSTRG":
                return "String Literal";
            default:
                return nodeType;
        }
    }

    private String sanitizeLexeme(String lexeme) {
        return lexeme.replace("\n", "").replace("\r", "");
    }

    /**
     * Adds an error token to the output.
     * 
     * @param token The token that caused the error.
     */
    public void addLexicalError(Token token) {
        outputErrorToListing("lexical error: " + token.getLexeme() + " (line: "
                + token.getLine() + " col: " + token.getCol() + ")\n", token.getCol());
        String tokenString = "\n" + formatToken(token) + "\n lexical error: " +
                sanitizeLexeme(token.getLexeme()) + " (line: "
                + token.getLine() + " col: " + token.getCol() + ")";
        currentLine.append(tokenString);
        formattedLines.add(currentLine.toString());
        currentLine = new StringBuilder();
    }

    public void addParseError(Tokeniser.TokenType expectedType, Token currentToken, Node parentNode) {
        String nonTerminal = (parentNode != null) ? getReadableNonTerminal(parentNode.getType()) : "Unknown";
        String errorMsg = "Syntax Error in " + nonTerminal + ": Expected " + expectedType + ", but found "
                + currentToken.getType() +
                " on line " + currentToken.getLine() + ", col " + currentToken.getCol();
        outputErrorToListing(errorMsg, currentToken.getCol());
        currentLine.append(errorMsg);
        formattedLines.add(currentLine.toString());
        currentLine = new StringBuilder();
    }

    /**
     * Adds a token to the output.
     * 
     * @param token The token to be added to the output.
     */
    public void addToken(Token token) {
        String tokenString = formatToken(token);
        currentLine.append(tokenString);
        if (currentLine.length() > LINE_LENGTH) {
            formattedLines.add(currentLine.toString());
            currentLine = new StringBuilder();
        }
    }

    /**
     * Formats a token for output.
     * 
     * @param token The token to be formatted.
     * @return A formatted string representation of the token.
     */
    private String formatToken(Token token) {
        String tokenName = Token.resolveTokenName(token.getTokenId());
        String lexeme = token.getLexeme();

        if (token.getTokenId() == Tokeniser.getTokenCode(Tokeniser.TokenType.TIDEN)
                ||
                token.getTokenId() == Tokeniser.getTokenCode(Tokeniser.TokenType.TILIT) ||
                token.getTokenId() == Tokeniser.getTokenCode(Tokeniser.TokenType.TFLIT) ||
                token.getTokenId() == Tokeniser.getTokenCode(Tokeniser.TokenType.TSTRG)) {

            if (token.getTokenId() == Tokeniser.getTokenCode(Tokeniser.TokenType.TSTRG)) {
                lexeme = "\"" + lexeme + "\"";
            }

            int secondFieldLength = lexeme.length() + 1;
            int paddedLength = ((secondFieldLength + 5) / 6) * 6;

            return String.format("%-6s%-" + paddedLength + "s", tokenName, lexeme + "");
        } else if (token.getTokenId() == Tokeniser.getTokenCode(Tokeniser.TokenType.TUNDF)) {
            return String.format("%-6s", tokenName);
        } else {
            return String.format("%-6s", tokenName);
        }
    }

    /**
     * Retrieves the formatted output as a list of strings.
     * 
     * @return A list of formatted output strings.
     */
    public List<String> getFormattedOutput() {
        if (currentLine.length() > 0) {
            formattedLines.add(currentLine.toString());
        }
        return formattedLines;
    }

    /**
     * Initialises the PrintWriter for file output.
     * 
     * @param outputFilePath The path of the file to write to.
     */
    private void initialiseWriter(String outputFilePath) {
        try {
            File file = new File(outputFilePath);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            this.writer = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            System.err.println("Error creating or opening output file: " + e.getMessage());
        }
    }

    /**
     * Outputs a character to the listing file at a specific column.
     * 
     * @param c         The character to output.
     * @param targetCol The target column for the character.
     */
    public void outputToListing(char c, int targetCol) {
        if (writer == null) {
            System.err.println("Writer is not initialised. Cannot output to listing.");
            return;
        }
        targetCol += 4;
        if (isValidChar(c)) {
            if (isFirstOutput) {
                String lineNumberStr = String.format("%4d ", lineNumber);
                writer.print(lineNumberStr);
                isFirstOutput = false;
            }

            if (currentCol < targetCol) {
                int spacesToAdd = targetCol - currentCol;
                writer.print(" ".repeat(spacesToAdd));
                currentCol = targetCol;
            }

            writer.print(c);
            currentCol++;

            if (c == '\n') {
                lineNumber++;
                String lineNumberStr = String.format("%4d ", lineNumber);
                writer.print(lineNumberStr);
                currentCol = 5;
            }

            writer.flush();
        }
    }

    /**
     * Outputs an error message to the listing file.
     * 
     * @param error The error message to output.
     * @param col   The column where the error occurred.
     */
    public void outputErrorToListing(String error, int col) {
        if (writer == null) {
            System.err.println("Writer is not initialised. Cannot output to listing.");
            return;
        }
        String indentation = " ".repeat(Math.max(0, col + 4));
        String formattedError = String.format("\n%s^%s", indentation, error);
        writer.print(formattedError);
        currentCol = 0;
        writer.flush();
    }

    /**
     * Checks if a character is valid for output.
     * 
     * @param c The character to check.
     * @return true if the character is valid, false otherwise.
     */
    private boolean isValidChar(char c) {
        return c != '\uFFFF';
    }

    /**
     * Closes the output writer.
     */
    public void closeOutput() {
        if (writer != null) {
            writer.close();
        }
    }
}
