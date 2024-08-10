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
        // TODO: Do we want to use token id? or just the token name
        this.tokenId = tokenId;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    // TODO: Configure the spacing for this class
    @Override
    public String toString() {
        return String.format("Token[id=%d, lexeme='%s', line=%d, col=%d]",
                tokenId, lexeme, line, col);
    }

    // Getters
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

    // Setters
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
