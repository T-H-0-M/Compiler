
public class A1 {

    public static void main(String[] args) {
        String filePath = args[0];

        TokenTerminator tokenTerminator = new TokenTerminator(filePath);

        run(tokenTerminator);
    }

    // NOTE: For debug
    private static void run(TokenTerminator tokenTerminator) {
        Token currentToken = tokenTerminator.getNextToken();
        while (currentToken.getTokenId() != 0) {
            System.out.println(currentToken.toString());
            currentToken = tokenTerminator.getNextToken();
            if (currentToken.getLexeme().equals("TEOF")) {
                break;
            }
        }

    }

}
