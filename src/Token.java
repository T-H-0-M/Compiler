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

    // TODO: Configure the spacing for this class
    @Override
    public String toString() {
        return String.format("Token[id=%d, lexeme='%s', line=%d, col=%d]",
                tokenId, lexeme, line, col);
    }
}
