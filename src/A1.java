import java.util.ArrayList;

public class A1 {

    private static ArrayList<Token> tokenList = new ArrayList<>();

    public static void main(String[] args) {
        String filePath = args[0];
        TokenTerminator tokenTerminator = new TokenTerminator(filePath);
        run(tokenTerminator);
        printTokenList();
    }

    // NOTE: For debug
    private static void run(TokenTerminator tokenTerminator) {
        Token currentToken = tokenTerminator.getNextToken();
        while (true) {
            currentToken = tokenTerminator.getNextToken();
            tokenList.add(currentToken);
            if (currentToken.getTokenId() == 0) {
                break;
            }
        }

    }

    /**
     * Prints all tokens in the tokenList.
     * Each token is printed on a new line with its index in the list.
     */
    private static void printTokenList() {
        System.out.println("\n--- All Tokens in tokenList ---");
        for (int i = 0; i < tokenList.size(); i++) {
            System.out.printf("%d: %s%n", i, tokenList.get(i).toString());
        }
        System.out.println("--- End of tokenList ---\n");
    }
}
