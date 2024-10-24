import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CodeGenerator {

    private Node rootNode;
    private SymbolTable symbolTable;
    private List<String> code; // Array structure to store generated code
    // TODO: add in constants
    private int programCounter; // Tracks the address of the next instruction
    private int labelCounter; // For generating unique labels
    private Map<String, String> opcodeMap; // Maps mnemonic to opcode
    private OutputController outputController;

    /**
     * Constructor for CodeGenerator.
     *
     * @param rootNode    The root node of the AST.
     * @param symbolTable The symbol table containing variable information.
     */
    public CodeGenerator(Node rootNode, SymbolTable symbolTable, OutputController outputController) {
        this.rootNode = rootNode;
        this.symbolTable = symbolTable;
        this.code = new ArrayList<>();
        this.programCounter = 0;
        this.labelCounter = 0;
        this.outputController = outputController;
        initialiseOpcodeMap();
    }

    /**
     * Initiates the code generation process.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void generateCode() throws IOException {
        generateNode(rootNode);
        writeInstruction("HALT");
        // TODO: go back and correct machine code here
        writeToFile();
        System.out.println(code);
    }

    /**
     * Recursively generates code for a given AST node.
     *
     * @param node The current AST node.
     * @throws IOException If an I/O error occurs.
     */
    private void generateNode(Node node) throws IOException {
        if (node == null) {
            return;
        }

        switch (node.getType()) {
            case "NPROG":
                writeComment("Program Start");
                for (Node child : node.getChildren()) {
                    generateNode(child);
                }
                writeComment("Program End");
                break;
            case "NSDLST":
                handleVarDeclaration(node);
                break;
            case "NASGN":
                handleAssignment(node);
                break;
            case "NPRINT":
                handlePrint(node);
                break;
            default:
                for (Node child : node.getChildren()) {
                    generateNode(child);
                }
                break;
        }
    }

    /**
     * Handles variable declarations by allocating space and storing offsets in
     * the
     * symbol table.
     *
     * @param node The variable declaration AST node.
     * @throws IOException If an I/O error occurs.
     */
    private void handleVarDeclaration(Node node) throws IOException {
        // TODO: update this to use all data types, not just ints
        ArrayList<Integer> offsetList = processNSDLST(node);
        writeInstruction("LH");
        writePaddedInstruction(offsetList.size(), 2);
        writeInstruction("ALLOC");
        for (int i : offsetList) {
            writeInstruction("LA1");
            writePaddedInstruction(i, 4);
            writeInstruction("LH");
            writePaddedInstruction(0, 2);
            writeInstruction("ST");

        }
    }

    /**
     * Allocates memory for a variable and returns its offset.
     *
     * @param varName The name of the variable.
     * @param scope   The scope of the variable ("global" or "local").
     * @return The memory offset assigned to the variable.
     */
    private int allocateVariable(String varName, String scope) {
        int offset;
        offset = symbolTable.allocateGlobal();
        return offset;
    }

    /**
     * Handles assignment statements by generating code for the expression and
     * storing the result.
     *
     * @param node The assignment AST node.
     * @throws IOException If an I/O error occurs.
     */
    private void handleAssignment(Node node) throws IOException {
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);
        System.out.println("Child 1 - " + child1.getType());
        System.out.println("Child 2 - " + child2.getType());

        // Load the address of the variable
        // LA 1
        handleLoadAddress(child1);
        handleExpression(child2);
        // Store the value from the top of the stack into the variable
        writeInstruction("ST");
    }

    private void loadByte(int numberBytes) {
        writeInstruction("LB");
        writeInstruction(String.valueOf(numberBytes));
        writeInstruction("ALLOC");
    }

    /**
     * Handles loading the address of a variable (LHS) onto the stack.
     *
     * @param node The LHS node representing the variable.
     * @throws IOException If an I/O error occurs.
     */
    private void handleLoadAddress(Node node) throws IOException {
        String varName = node.getValue();
        int offset = symbolTable.getOffset(varName);
        System.out.println("Offset  - " + offset);
        writeInstruction("LA1");
        writePaddedInstruction(offset, 4);
    }

    /**
     * Handles the evaluation of an expression and generates corresponding SM24
     * instructions.
     *
     * @param node The expression node.
     * @throws IOException If an I/O error occurs.
     */
    private void handleExpression(Node node) throws IOException {
        switch (node.getType()) {
            case "NADD":
                handleAdd(node);
                break;
            case "NSUB":
                handleSub(node);
                break;
            case "NMUL":
                handleMul(node);
                break;
            case "NDIV":
                handleDiv(node);
                break;
            case "NILIT":
                handleIntegerLiteral(node);
                break;
            case "NSIMV":
                handleVariable(node);
                break;

            // TODO: add bool and float
            default:
                throw new UnsupportedOperationException("Unsupported expression type: " + node.getType());
        }
    }

    private void handlePrint(Node node) {
        Node variableNode = node.getChildren().getFirst();
        String variableName = variableNode.getValue();
        int offset = symbolTable.getOffset(variableName);
        writeInstruction("LV1");
        writePaddedInstruction(offset, 4);
        writeInstruction("VALPR");
        writeInstruction("NEWLN");
    }

    /**
     * Handles loading the value of a variable (RHS) onto the stack.
     *
     * @param node The variable node representing the variable.
     * @throws IOException If an I/O error occurs.
     */
    private void handleVariable(Node node) throws IOException {
        String varName = node.getValue();
        int offset = symbolTable.getOffset(varName);
        String baseRegister = "LV1";
        writeInstruction(baseRegister);
        writePaddedInstruction(offset, 4);
    }

    private void handleIntegerLiteral(Node node) {
        int value = Integer.parseInt(node.getValue());
        loadInteger(value);
    }

    private void handleAdd(Node node) throws IOException {
        List<Node> children = node.getChildren();
        Node left = children.get(0);
        Node right = children.get(1);
        handleExpression(left);
        handleExpression(right);
        writeInstruction("ADD");
    }

    private void handleSub(Node node) throws IOException {
        List<Node> children = node.getChildren();
        Node left = children.get(0);
        Node right = children.get(1);
        handleExpression(left);
        handleExpression(right);
        writeInstruction("SUB");
    }

    private void handleMul(Node node) throws IOException {
        List<Node> children = node.getChildren();
        Node left = children.get(0);
        Node right = children.get(1);
        handleExpression(left);
        handleExpression(right);
        writeInstruction("MUL");
    }

    private void handleDiv(Node node) throws IOException {
        List<Node> children = node.getChildren();
        Node left = children.get(0);
        Node right = children.get(1);
        handleExpression(left);
        handleExpression(right);
        writeInstruction("DIV");
    }

    public void writePaddedInstruction(int value, int padding) {
        String[] bytes = new String[padding];
        String decimalValue;

        for (int i = 0; i < padding; i++) {
            bytes[i] = "00";
        }

        if (padding == 2) {
            decimalValue = String.format("%04d", value);
        } else {
            decimalValue = String.format("%08d", value);
        }
        for (int i = 0; i < padding; i++) {
            bytes[i] = decimalValue.substring(i * 2, (i * 2) + 2); // Each byte is represented by 2 digits
        }

        for (String byteStr : bytes) {
            writeInstruction(byteStr);
        }
    }

    /**
     * Handles binary operations by emitting the corresponding instruction.
     *
     * @param operator The binary operator.
     * @throws IOException If an I/O error occurs.
     */
    private void handleBinaryOperation(String operator) throws IOException {
        switch (operator) {
            case "+":
                writeInstruction("ADD");
                break;
            case "-":
                writeInstruction("SUB");
                break;
            case "*":
                writeInstruction("MUL");
                break;
            case "/":
                writeInstruction("DIV");
                break;
            case "&&":
                writeInstruction("AND");
                break;
            case "||":
                writeInstruction("OR");
                break;
            case "==":
                writeInstruction("EQ");
                break;
            case "!=":
                writeInstruction("NE");
                break;
            case "<":
                writeInstruction("LT");
                break;
            case "<=":
                writeInstruction("LE");
                break;
            case ">":
                writeInstruction("GT");
                break;
            case ">=":
                writeInstruction("GE");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported binary operator: " +
                        operator);
        }
    }

    private void loadInteger(int decimal) {
        if (decimal < -32768 || decimal > 32767) {
            throw new IllegalArgumentException("Value must be a 16-bit signed integer (-32768 to 32767).");
        }

        String binaryString = String.format("%16s", Integer.toBinaryString(decimal & 0xFFFF)).replace(' ', '0');

        String msbBinary = binaryString.substring(0, 8); // INFO: Most significant byte (first 8 bits)
        String lsbBinary = binaryString.substring(8, 16); // INFO: Least significant byte (last 8 bits)

        // Convert each binary byte to its decimal equivalent
        int msb = Integer.parseInt(msbBinary, 2); // Convert MSB to decimal
        int lsb = Integer.parseInt(lsbBinary, 2); // Convert LSB to decimal

        writeInstruction("LH");
        writeInstruction(String.format("%02d", msb));
        writeInstruction(String.format("%02d", lsb));
    }

    /**
     * Loads the value of a variable onto the stack.
     *
     * @param varName The name of the variable.
     * @param scope   The scope of the variable ("global" or "local").
     * @throws IOException If an I/O error occurs.
     */
    private void loadVariableValue(String varName, String scope) throws IOException {
        int offset = symbolTable.getOffset(varName);
        int baseRegister = 1;
        writeInstruction("LV " + baseRegister + ", " + offset);
    }

    /**
     * Writes an instruction to the code array and increments the program counter.
     *
     * @param instruction The instruction to write.
     */
    private void writeInstruction(String instruction) {
        if (opcodeMap.get(instruction) != null) {
            code.add(opcodeMap.get(instruction));
        } else {
            code.add(instruction);
        }
        programCounter++;
    }

    /**
     * Writes a comment to the code array. Comments are prefixed with a semicolon.
     *
     * @param comment The comment text.
     */
    private void writeComment(String comment) {
        // code.add("; " + comment);
        // programCounter++;
    }

    /**
     * Generates a unique label.
     *
     * @return A unique label string.
     */
    private String generateLabel() {
        return "L" + (labelCounter++);
    }

    /**
     * Writes a label to the code array. Labels are suffixed with a colon.
     *
     * @param label The label name.
     */
    private void writeLabel(String label) {
        code.add(label + ":");
        // Labels do not increment the program counter as they are markers
    }

    /**
     * Writes the generated code to a .mod file.
     *
     * @param fileName The name of the output file.
     * @throws IOException If an I/O error occurs.
     */
    private void writeToFile() throws IOException {
        String fileName = outputController.getOutputFileName() + ".mod";
        try (FileWriter writer = new FileWriter(fileName)) {

            int codeWordCount = (code.size() + 7) / 8; // Divide by 8 and round up
            writer.write(codeWordCount + "\n"); // Write the count as the first line

            int instructionCount = 0;
            StringBuilder line = new StringBuilder();

            for (String instruction : code) {
                if (instructionCount > 0) {
                    line.append(" ");
                }
                line.append(instruction);
                instructionCount++;

                if (instructionCount == 8) {
                    writer.write("  " + line.toString() + "\n"); // Indent by 2 spaces
                    line.setLength(0); // Clear the line
                    instructionCount = 0; // Reset the count
                }
            }

            if (instructionCount > 0) {
                while (instructionCount < 8) {

                    line.append(" 00");
                    instructionCount++;
                }
                writer.write("  " + line.toString() + "\n");
            }

            writer.write("0\n0\n0\n");
        }
    }

    private void initialiseOpcodeMap() {
        opcodeMap = new HashMap<>();
        opcodeMap.put("HALT", "00");
        opcodeMap.put("NO-OP", "01");
        opcodeMap.put("TRAP", "02");
        opcodeMap.put("ZERO", "03");
        opcodeMap.put("FALSE", "04");
        opcodeMap.put("TRUE", "05");
        opcodeMap.put("TYPE", "07");
        opcodeMap.put("ITYPE", "08");
        opcodeMap.put("FTYPE", "09");
        opcodeMap.put("ADD", "11");
        opcodeMap.put("SUB", "12");
        opcodeMap.put("MUL", "13");
        opcodeMap.put("DIV", "14");
        opcodeMap.put("REM", "15");
        opcodeMap.put("POW", "16");
        opcodeMap.put("CHS", "17");
        opcodeMap.put("ABS", "18");
        opcodeMap.put("GT", "21");
        opcodeMap.put("GE", "22");
        opcodeMap.put("LT", "23");
        opcodeMap.put("LE", "24");
        opcodeMap.put("EQ", "25");
        opcodeMap.put("NE", "26");
        opcodeMap.put("AND", "31");
        opcodeMap.put("OR", "32");
        opcodeMap.put("XOR", "33");
        opcodeMap.put("NOT", "34");
        opcodeMap.put("BT", "35");
        opcodeMap.put("BF", "36");
        opcodeMap.put("BR", "37");
        opcodeMap.put("L", "40");
        opcodeMap.put("LB", "41");
        opcodeMap.put("LH", "42");
        opcodeMap.put("ST", "43");
        opcodeMap.put("STEP", "51");
        opcodeMap.put("ALLOC", "52");
        opcodeMap.put("ARRAY", "53");
        opcodeMap.put("INDEX", "54");
        opcodeMap.put("SIZE", "55");
        opcodeMap.put("DUP", "56");
        opcodeMap.put("READF", "60");
        opcodeMap.put("READI", "61");
        opcodeMap.put("VALPR", "62");
        opcodeMap.put("STRPR", "63");
        opcodeMap.put("CHRPR", "64");
        opcodeMap.put("NEWLN", "65");
        opcodeMap.put("SPACE", "66");
        opcodeMap.put("RVAL", "70");
        opcodeMap.put("RETN", "71");
        opcodeMap.put("JS2", "72");
        opcodeMap.put("LV0", "80");
        opcodeMap.put("LV1", "81");
        opcodeMap.put("LV2", "82");
        opcodeMap.put("LA0", "90");
        opcodeMap.put("LA1", "91");
        opcodeMap.put("LA2", "92");
    }

    /**
     * Recursively processes the NSDLST subtree and handles NSDECL nodes.
     *
     * @param node The current AST node to process.
     * @return An ArrayList of all variable offsets
     * @throws IOException If an I/O error occurs.
     */
    private ArrayList<Integer> processNSDLST(Node node) throws IOException {
        ArrayList<Integer> totalVariables = new ArrayList<>();

        if (node.getType().equals("NSDLST")) {
            List<Node> children = node.getChildren();

            for (Node child : children) {
                // INFO: If the child is another NSDLST, recurse
                if (child.getType().equals("NSDLST")) {
                    totalVariables.addAll(processNSDLST(child));
                } else if (child.getType().equals("NSDECL")) {
                    totalVariables.add(handleNSDECL(child));
                } else {
                    throw new IOException("Unexpected node type: " + child.getType());
                }
            }
        } else if (node.getType().equals("NSDECL")) {
            totalVariables.add(handleNSDECL(node));
        } else {
            throw new IOException("Unexpected node type: " + node.getType());
        }

        return totalVariables;
    }

    /**
     * Handles a single NSDECL node (variable declaration).
     *
     * @param node The NSDECL node to handle.
     * @return The offset of the variable
     * @throws IOException If an I/O error occurs.
     */
    private int handleNSDECL(Node node) throws IOException {
        String varName = node.getValue();
        String scope = node.getScope();
        writeComment("Declare variable: " + varName + " (" + scope + ")");
        int offset = allocateVariable(varName, scope);
        symbolTable.setOffset(varName, offset);
        return offset;
    }
}
