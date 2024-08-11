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

    @Override
    public String toString() {
        // TODO: Configure the spacing for this class
        return String.format("Token[id=%d, lexeme='%s', line=%d, col=%d]",
                tokenId, lexeme, line, col);
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
