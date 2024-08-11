public class A1 {

    public static void main(String[] args) {
        String filePath = args[0];

        TokenTerminator tokenTerminator = new TokenTerminator(filePath);

        run(tokenTerminator);
    }

    // NOTE: For debug
    private static void run(TokenTerminator tokenTerminator) {
        Token currentToken = tokenTerminator.getNextToken();
        while (true) {
            System.out.println(currentToken.toString());
            currentToken = tokenTerminator.getNextToken();
            if (currentToken.getTokenId() == 0) {
                System.out.println("Breaking");
                break;
            }
        }

    }

}
