import java.util.Map;
import java.util.HashMap;

/**
 * Token class
 * 
 * This class represents a token in the lexical analysis phase of the compiler.
 * It stores information about the token's id, lexeme, and position in the
 * source code.
 * 
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 * @since 2024-08-11
 */
public class Token {
    private int tokenId;
    private String lexeme;
    private int line;
    private int col;

    private static final Map<Integer, String> tokenNames = new HashMap<>();

    static {
        for (Tokeniser.TokenType type : Tokeniser.TokenType.values()) {
            tokenNames.put(Tokeniser.getTokenCode(type), type.name());
        }
    }

    public Token() {
        this.tokenId = 0;
        this.lexeme = "";
        this.line = 0;
        this.col = 0;
    }

    public Token(int tokenId, String lexeme, int line, int col) {
        this.tokenId = tokenId;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    /**
     * Resolves a token ID to its corresponding token name.
     * 
     * @param tokenId The ID of the token to resolve
     * @return The name of the token, or "UNKNOWN" if the ID is not recognized
     */
    public static String resolveTokenName(int tokenId) {
        return tokenNames.getOrDefault(tokenId, "UNKNOWN");
    }

    @Override
    public String toString() {
        return String.format("Token[id=%d (%s), lexeme='%s', line=%d, col=%d]",
                tokenId, resolveTokenName(tokenId), lexeme, line, col);
    }

    // ------------------------- Getters and Setters ------------------------- //
    public int getTokenId() {
        return tokenId;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
