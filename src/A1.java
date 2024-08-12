import java.util.ArrayList;
import java.util.List;

public class A1 {
    private static ArrayList<Token> tokenList = new ArrayList<>();
    private static OutputFormatter outputFormatter = new OutputFormatter();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a file path as an argument.");
            return;
        }

        String filePath = args[0];
        TokenTerminator tokenTerminator = new TokenTerminator(filePath);
        run(tokenTerminator);
        printFormattedOutput();
        // printTokenList();
    }

    private static void run(TokenTerminator tokenTerminator) {
        Token currentToken;
        do {
            currentToken = tokenTerminator.getNextToken();
            tokenList.add(currentToken);
            outputFormatter.addToken(currentToken);
        } while (currentToken.getTokenId() != 0);
    }

    private static void printFormattedOutput() {
        List<String> formattedOutput = outputFormatter.getFormattedOutput();
        for (String line : formattedOutput) {
            System.out.println(line);
        }
    }

    /**
     * Prints all tokens in the tokenList.
     * Each token is printed on a new line with its index in the list.
     */
    private static void printTokenList() {
        System.out.println("\n--- All Tokens in tokenList ---");
        for (Token token : tokenList) {
            System.out.println(token.toString());
        }
        System.out.println("--- End of tokenList ---\n");
    }
}
