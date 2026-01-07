package com.compiler.cd24.codegen;

import com.compiler.cd24.semantic.SymbolTable;
import com.compiler.cd24.semantic.SymbolTableEntry;
import com.compiler.cd24.util.DataType;
import com.compiler.cd24.util.Node;
import com.compiler.cd24.util.OutputController;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CodeGenerator {

    private Node rootNode;
    private SymbolTable symbolTable;
    private List<String> code;
    // TODO: add in constants
    private int programCounter;
    private Map<Integer, Integer> labelMap;
    private Map<String, String> opcodeMap;
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
        this.programCounter = 10000;
        this.outputController = outputController;
        this.labelMap = new HashMap<>();
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
        writeToFile();
        System.out.println(outputController.getOutputFileName().replace("./", "") + " compiled successfully");
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
                for (Node child : node.getChildren()) {
                    generateNode(child);
                }
                break;
            case "NSDLST":
                handleVarDeclaration(node);
                break;
            case "NASGN":
                handleAssignment(node);
                break;
            case "NINPUT":
                handleInput(node);
                break;
            case "NPRINT":
                handlePrint(node);
                break;
            case "NPLEQ":
                handleCompoundAssignment(node, "ADD");
                break;
            case "NMNEQ":
                handleCompoundAssignment(node, "SUB");
                break;
            case "NSTEQ":
                handleCompoundAssignment(node, "MUL");
                break;
            case "NDVEQ":
                handleCompoundAssignment(node, "DIV");
                break;
            case "NIFTH":
                handleIF(node);
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
        ArrayList<Integer> offsetList = processNSDLST(node);
        loadInteger(offsetList.size());
        writeInstruction("ALLOC");
        for (int i : offsetList) {
            if (symbolTable.findWithOffset(i).getDataType() == DataType.INTEGER
                    || symbolTable.findWithOffset(i).getDataType() == DataType.BOOLEAN) {
                writeInstruction("LA1");
                writePaddedInstruction(i, 4);
                loadInteger(0);
                writeInstruction("ST");
            } else if (symbolTable.findWithOffset(i).getDataType() == DataType.FLOAT) {
                writeInstruction("LA1");
                writePaddedInstruction(i, 4);
                loadInteger(0);
                writeInstruction("FTYPE");
                writeInstruction("ST");
            }
        }
    }

    private void handleInput(Node node) {
        List<Node> children = node.getChildren();
        Node varNode = children.get(0);
        int offset = symbolTable.find(varNode.getValue()).getOffset();
        writeInstruction("LA1");
        writePaddedInstruction(offset, 4);
        writeInstruction("READI");
        writeInstruction("ST");

    }

    private void handleIF(Node node) throws IOException {
        List<Node> children = node.getChildren();
        Node expressionNode = children.get(0);
        Node actionNode = children.get(1);
        writeInstruction("LA1");
        int beforeLoop = programCounter;
        writePaddedInstruction(programCounter, 4);
        handleExpression(expressionNode);
        writeInstruction("BF");
        generateNode(actionNode);
        int endLoop = programCounter;
        updatePaddedInstruction(endLoop, beforeLoop);
    }

    private void updateInstruction(String instruction, int programCounter) {
        if (opcodeMap.get(instruction) != null) {
            code.set(programCounter - 1000, opcodeMap.get(instruction));
        } else {
            code.set(programCounter - 1000, instruction);
        }
    }

    private void updatePaddedInstruction(int value, int programCounter) {
        String[] bytes = new String[4];
        String decimalValue;

        for (int i = 0; i < 4; i++) {
            bytes[i] = "00";
        }
        decimalValue = String.format("%08d", value);

        for (int i = 0; i < 4; i++) {
            bytes[i] = decimalValue.substring(i * 2, (i * 2) + 2);
        }

        int count = programCounter - 10000;
        for (String byteStr : bytes) {
            code.set(count, byteStr);
            count++;
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
        handleLoadAddress(child1);
        handleExpression(child2);
        writeInstruction("ST");
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
                handleArithmetic(node, "ADD");
                break;
            case "NSUB":
                handleArithmetic(node, "SUB");
                break;
            case "NMUL":
                handleArithmetic(node, "MUL");
                break;
            case "NDIV":
                handleArithmetic(node, "DIV");
                break;
            case "NILIT":
                handleIntegerLiteral(node);
                break;
            case "NFLIT":
                handleFloatLiteral(node);
                break;
            case "NTRUE":
                handleTrue(node);
                break;
            case "NFALS":
                handleFalse(node);
                break;
            case "NGRT":
                handleComparison(node, "GT");
                break;
            case "NLSS":
                handleComparison(node, "LT");
                break;
            case "NEQL":
                handleComparison(node, "EQ");
                break;
            case "NNEQ":
                handleComparison(node, "NE");
                break;
            case "NLEQ":
                handleComparison(node, "LE");
                break;
            case "NGEQ":
                handleComparison(node, "GE");
                break;
            case "NSIMV":
                handleVariable(node);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported expression type: " + node.getType());
        }
    }

    private void handlePrint(Node node) {
        Node variableNode = node.getChildren().get(0);
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

    private void handleFloatLiteral(Node node) {
        double value = Double.parseDouble(node.getValue());
        loadFloat(value);
    }

    private void handleFalse(Node node) {
        writeInstruction("FALSE");
    }

    private void handleTrue(Node node) {
        writeInstruction("TRUE");
    }

    private void handleIntegerLiteral(Node node) {
        int value = Integer.parseInt(node.getValue());
        loadInteger(value);
    }

    private void handleArithmetic(Node node, String operation) throws IOException {
        List<Node> children = node.getChildren();
        Node left = children.get(0);
        Node right = children.get(1);
        handleExpression(left);
        handleExpression(right);
        writeInstruction(operation);
    }

    private void handleCompoundAssignment(Node node, String operation) {
        List<Node> children = node.getChildren();
        Node left = children.get(0);
        int offset = symbolTable.find(left.getValue()).getOffset();
        Node right = children.get(1);
        SymbolTableEntry rightEntry = symbolTable.find(right.getValue());
        DataType type = rightEntry.getDataType();

        writeInstruction("LA1");
        writePaddedInstruction(offset, 4);
        writeInstruction("LV1");
        writePaddedInstruction(offset, 4);
        if (right.getType().equals("NFLIT") || type == DataType.FLOAT) {
            if (type != null) {
                writeInstruction("LV1");
                writePaddedInstruction(rightEntry.getOffset(), 4);
            } else {
                loadFloat(Double.valueOf(right.getValue()));
            }
        } else if (right.getType().equals("NILIT") || type == DataType.INTEGER) {
            if (type != null) {

                writeInstruction("LV1");
                writePaddedInstruction(rightEntry.getOffset(), 4);
            } else {
                loadInteger(Integer.valueOf(right.getValue()));
            }
        }
        writeInstruction(operation);
        writeInstruction("ST");
    }

    private void handleComparison(Node node, String operation) throws IOException {
        handleArithmetic(node, "SUB");
        writeInstruction(operation);
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
            bytes[i] = decimalValue.substring(i * 2, (i * 2) + 2);
        }

        for (String byteStr : bytes) {
            writeInstruction(byteStr);
        }
    }

    private void loadInteger(int decimal) {
        if (decimal < -32768 || decimal > 32767) {
            throw new IllegalArgumentException("Value must be a 16-bit signed integer (-32768 to 32767).");
        }

        String binaryString = String.format("%16s", Integer.toBinaryString(decimal & 0xFFFF)).replace(' ', '0');
        String msbBinary = binaryString.substring(0, 8); // INFO: Most significant byte (first 8 bits)
        String lsbBinary = binaryString.substring(8, 16); // INFO: Least significant byte (last 8 bits)
        int msb = Integer.parseInt(msbBinary, 2); // INFO: Convert MSB to decimal
        int lsb = Integer.parseInt(lsbBinary, 2); // INFO: Convert LSB to decimal

        writeInstruction("LH");
        writeInstruction(String.format("%02d", msb));
        writeInstruction(String.format("%02d", lsb));
    }

    private void loadFloat(double decimal) {
        if (decimal < -32768 || decimal > 32767) {
            throw new IllegalArgumentException("Value must be a 16-bit signed integer (-32768 to 32767).");
        }

        String valueStr = String.valueOf(decimal);

        String[] parts = valueStr.split("\\.");
        int integerPart;
        try {
            integerPart = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer part: " + parts[0], e);
        }

        int fractionalPart = 0;
        String[] divisorString = String.valueOf(Math.pow(10, parts[1].length())).split("\\.");
        int divisor = Integer.parseInt(divisorString[0]);
        if (parts.length > 1) {
            try {
                fractionalPart = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid fractional part: " + parts[1], e);
            }
        }

        loadInteger(fractionalPart);
        writeInstruction("FTYPE");
        loadInteger(divisor);
        writeInstruction("DIV");
        loadInteger(integerPart);
        writeInstruction("ADD");

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
     * Writes the generated code to a .mod file.
     *
     * @param fileName The name of the output file.
     * @throws IOException If an I/O error occurs.
     */
    private void writeToFile() throws IOException {
        String fileName = outputController.getOutputFileName() + ".mod";
        try (FileWriter writer = new FileWriter(fileName)) {

            int codeWordCount = (code.size() + 7) / 8;
            writer.write(codeWordCount + "\n");

            int instructionCount = 0;
            StringBuilder line = new StringBuilder();

            for (String instruction : code) {
                if (instructionCount > 0) {
                    line.append(" ");
                }
                line.append(instruction);
                instructionCount++;

                if (instructionCount == 8) {
                    writer.write("  " + line.toString() + "\n");
                    line.setLength(0);
                    instructionCount = 0;
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
        int offset = allocateVariable(varName, scope);
        symbolTable.setOffset(varName, offset);
        return offset;
    }
}
