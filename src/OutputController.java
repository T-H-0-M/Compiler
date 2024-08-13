import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// TODO: ensure no newline/illegal char/ unterminated in string.

// INFO: This is likely the same as the output formatter

public class OutputController {
    private static final int LINE_LENGTH = 60;
    private static final int MAX_LINE_LENGTH = 66;
    private StringBuilder currentLine;
    private List<String> formattedLines;
    private String outputFileName;
    private PrintWriter writer;
    private int lineNumber;
    private StringBuilder currentOutputLine;

    public OutputController() {
    }

    public OutputController(String outputFileName) {
        this.currentLine = new StringBuilder();
        this.formattedLines = new ArrayList<>();
        this.outputFileName = outputFileName;
        this.lineNumber = 1;
        this.currentOutputLine = new StringBuilder();
        initialiseWriter(outputFileName);
    }

    public void addError(Token token) {
        String tokenString = formatToken(token) + "\n    lexical error " + token.getLexeme() + " (line: "
                + token.getLine() + " col: " + token.getCol() + ")\n";

        if (currentLine.length() + tokenString.length() > MAX_LINE_LENGTH) {
            formattedLines.add(currentLine.toString());
            currentLine = new StringBuilder();
        }

        if (currentLine.length() >= LINE_LENGTH) {
            currentLine.append(tokenString);
            formattedLines.add(currentLine.toString());
            currentLine = new StringBuilder();
        } else {
            currentLine.append(tokenString);
        }
    }

    public void addToken(Token token) {
        String tokenString = formatToken(token);

        if (currentLine.length() + tokenString.length() > MAX_LINE_LENGTH) {
            formattedLines.add(currentLine.toString());
            currentLine = new StringBuilder();
        }

        if (currentLine.length() >= LINE_LENGTH) {
            currentLine.append(tokenString);
            formattedLines.add(currentLine.toString());
            currentLine = new StringBuilder();
        } else {
            currentLine.append(tokenString);
        }
    }

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

    public List<String> getFormattedOutput() {
        if (currentLine.length() > 0) {
            formattedLines.add(currentLine.toString());
        }
        return formattedLines;
    }

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

    public void outputToListing(char c) {
        if (writer == null) {
            System.err.println("Writer is not initialized. Cannot output to listing.");
            return;
        }
        if (isValidChar(c)) {
            if (c == '\n') {
                writer.println();
                lineNumber++;
                String lineNumberStr = String.format("%4d ", lineNumber);
                writer.print(lineNumberStr);
            } else {
                writer.print(c);
            }
            writer.flush();
        }
    }

    private boolean isValidChar(char c) {
        return c != '\uFFFF';
    }

    public void closeOutput() {
        if (writer != null) {
            writer.close();
        }
    }
}
