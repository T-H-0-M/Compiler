import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Paths;

/**
 * A2 class
 * 
 * This class serves as the main entry point for the compiler or lexical
 * analyser.
 * It handles the processing of input files, manages token and error lists,
 * and coordinates the output of results.
 * 
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 * @since 2024-08-15
 */
public class A2 {
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

        String sourceDir = ".";
        String sourceFileName = sourceFile.getName();
        System.out.println(sourceFileName);
        String baseName = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));

        String outputFilePath = Paths.get(sourceDir, baseName + ".lst").toString();

        outputController = new OutputController(outputFilePath);
        Scanner scanner = new Scanner(sourceFilePath, outputController);
        Parser parser = new Parser(scanner);

        run(parser);
        // runScanner(scanner);
        outputController.closeOutput();
        printFormattedOutput();
    }

    /**
     * Runs the main processing loop of the compiler or lexical analyser.
     * It repeatedly fetches tokens from the Scanner and processes them,
     * adding them to either the tokenList or errorList as appropriate.
     *
     * @param scanner The Scanner object used to fetch tokens.
     */
    private static void run(Parser parser) {
        Node parseTree = parser.parse();
        if (parseTree != null) {
            System.out.println("Parse tree (may be partial if errors occurred):");
            parseTree.printTree();
        } else {
            System.out.println("No parse tree generated.");
        }
    }

    private static void runScanner(Scanner scanner) {
        Token currentToken;
        do {
            currentToken = scanner.nextToken();
            if (currentToken.getTokenId() == 68) {
                errorList.add(currentToken);
                outputController.addError(currentToken);
            } else {
                tokenList.add(currentToken);
                outputController.addToken(currentToken);
            }
        } while (currentToken.getTokenId() != 0);
    }

    /**
     * Prints the formatted output of the compilation or lexical analysis process.
     * This method retrieves the formatted output from the OutputController
     * and prints it to the console.
     */
    private static void printFormattedOutput() {
        List<String> formattedOutput = outputController.getFormattedOutput();
        for (String line : formattedOutput) {
            System.out.println(line);
        }
    }
}
