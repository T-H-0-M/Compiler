import java.util.ArrayList;
import java.util.List;

// TODO: ensure no newline/illegal char/ unterminated in string.
// TODO: handle floats that have more than one .
// TODO: account for numeric literal overflow

// INFO: This is likely the same as the output formatter
//
// INFO: TUNDF
//              lexical error {lexeme} (line: 16 col: 1)
public class OutputFormatter {
    private static final int LINE_LENGTH = 60;
    private static final int MAX_LINE_LENGTH = 66;
    private StringBuilder currentLine;
    private List<String> formattedLines;

    public OutputFormatter() {
        this.currentLine = new StringBuilder();
        this.formattedLines = new ArrayList<>();
    }

    public void addError(Token token) {
        String tokenString = formatToken(token) + "\n    lexical error " + token.getLexeme() + " (line: "
                + token.getLine() + " col: " + token.getCol() + ")\n" + formatToken(token) + "\n";

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

            // Calculate the length of the second field (lexeme + at least one space)
            int secondFieldLength = lexeme.length() + 1;
            // Round up to the next multiple of 6
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
}
