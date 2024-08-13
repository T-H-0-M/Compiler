import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Paths;

public class A1 {
    private static ArrayList<Token> tokenList = new ArrayList<>();
    private static ArrayList<Token> errorList = new ArrayList<>();
    private static OutputController outputController;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a file path as an argument.");
            return;
        }
        String sourceFilePath = args[0];
        File sourceFile = new File(sourceFilePath);

        // Get the directory of the source file
        String sourceDir = ".";

        String sourceFileName = sourceFile.getName();
        String baseName = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));

        String outputFilePath = Paths.get(sourceDir, baseName + ".lst").toString();

        outputController = new OutputController(outputFilePath);
        TokenTerminator tokenTerminator = new TokenTerminator(sourceFilePath, outputController);

        run(tokenTerminator);
        outputController.closeOutput();
        printFormattedOutput();
    }

    private static void run(TokenTerminator tokenTerminator) {
        Token currentToken;
        do {
            currentToken = tokenTerminator.getNextToken();
            if (currentToken.getTokenId() == 68) {
                errorList.add(currentToken);
                outputController.addError(currentToken);
            } else {
                tokenList.add(currentToken);
                outputController.addToken(currentToken);
            }
        } while (currentToken.getTokenId() != 0);
    }

    private static void printFormattedOutput() {
        List<String> formattedOutput = outputController.getFormattedOutput();
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
