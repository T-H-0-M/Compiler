import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Paths;

/**
 * A1 class
 * 
 * This class serves as the main entry point for the compiler or lexical
 * analyzer.
 * It handles the processing of input files, manages token and error lists,
 * and coordinates the output of results.
 * 
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 * @since 2024-08-15
 */
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

        String sourceDir = ".";
        String sourceFileName = sourceFile.getName();
        System.out.println(sourceFileName);
        String baseName = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));

        String outputFilePath = Paths.get(sourceDir, baseName + ".lst").toString();

        outputController = new OutputController(outputFilePath);
        CompilerScanner compilerScanner = new CompilerScanner(sourceFilePath, outputController);

        run(compilerScanner);
        outputController.closeOutput();
        printFormattedOutput();
    }

    /**
     * Runs the main processing loop of the compiler or lexical analyzer.
     * It repeatedly fetches tokens from the CompilerScanner and processes them,
     * adding them to either the tokenList or errorList as appropriate.
     *
     * @param compilerScanner The CompilerScanner object used to fetch tokens.
     */
    private static void run(CompilerScanner compilerScanner) {
        Token currentToken;
        do {
            currentToken = compilerScanner.getNextToken();
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
