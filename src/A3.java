import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * A2 class
 * 
 * This class serves as the main entry point for the compiler or lexical
 * analyser.
 * It handles the processing of input files, manages token and error lists,
 * and coordinates the output of results.
 * 
 * Date: 15-08-2024
 * 
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 */
public class A3 {
    private static ArrayList<Token> tokenList = new ArrayList<>();
    private static ArrayList<Token> errorList = new ArrayList<>();
    private static OutputController outputController;
    private static SemanticAnalyser semanticAnalyser;
    // private static CodeGenerator codeGenerator;

    // TODO: standardise use of global and local vars
    public static void main(String[] args) throws ParseException, IOException {
        if (args.length < 1) {
            System.out.println("Please provide a file path as an argument.");
            return;
        }
        String sourceFilePath = args[0];
        File sourceFile = new File(sourceFilePath);

        String sourceDir = ".";
        String sourceFileName = sourceFile.getName();
        String baseName = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
        // String outputFilePath = Paths.get(sourceDir, baseName + ".lst").toString();
        String outputFilePath = Paths.get(sourceDir, baseName).toString();
        outputController = new OutputController(outputFilePath);
        Scanner scanner = new Scanner(sourceFilePath, outputController);
        Parser parser = new Parser(scanner, outputController);
        semanticAnalyser = new SemanticAnalyser();

        run(parser);
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
    private static void run(Parser parser) throws ParseException, IOException {
        Node root = parser.parse();
        if (root != null) {
            root.printPreOrderTraversal();
            root.printTree();
            semanticAnalyser = new SemanticAnalyser(parser.getSymbolTable(), outputController);
            SymbolTable symbolTable = semanticAnalyser.analyse(root);
            CodeGenerator codeGenerator = new CodeGenerator(root, symbolTable, outputController);
            codeGenerator.generateCode();

        } else {
            System.out.println("No parse tree generated.");
        }
    }

    private static void runParser(Parser parser) throws ParseException {
        Node root = parser.parse();
        if (root != null) {
            root.printPreOrderTraversal();
            root.printTree();

            // System.out.println(parser.getSymbolTable());
            semanticAnalyser = new SemanticAnalyser(parser.getSymbolTable(), outputController);
            SymbolTable symbolTable = semanticAnalyser.analyse(root);
            System.out.println(symbolTable);
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
                outputController.addLexicalError(currentToken);
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
