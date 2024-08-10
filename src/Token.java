public class Token {
    private int tokenId;
    private string lexeme;
    private int line;
    private int col;

    public Token() {
        this.tokenId = 0;
        this.lexeme = "";
        this.line = 0;
        this.col = 0;
    }

    public Token(int tokenId, string lexeme, int line, int col) {
        this.tokenId = tokenId;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    // TODO: Configure this to return a formatted token
    @Override
    public static toString(){

    }
}
