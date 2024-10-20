import java.util.List;
import java.util.ArrayList;

public class SemanticAnalyser {
    private SymbolTable symbolTable;
    private List<String> errors;

    public SemanticAnalyser() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
    }

    public SemanticAnalyser(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
    }

    public SymbolTable analyse(Node root) {
        traverse(root);
        if (!errors.isEmpty()) {
            System.out.println("Semantic Errors:");
            for (String error : errors) {
                System.out.println(error);
            }
        } else {
            System.out.println("Semantic analysis completed without errors.");
        }
        return this.symbolTable;
    }

    private void traverse(Node node) {
        if (node == null)
            return;
        String nodeType = node.getType();

        switch (nodeType) {
            case "NASGN":
                handleAssignment(node);
                break;
            case "NPLEQ":
                handlePlusEqual(node);
                break;
            case "NMNEQ":
                handleMinusEqual(node);
                break;
            case "NSTEQ":
                handleStarEqual(node);
                break;
            case "NDVEQ":
                handleDivEqual(node);
                break;
            default:
                for (Node child : node.getChildren()) {
                    traverse(child);
                }
                break;
        }
    }

    // TODO: add checks for all relevant nodes here

    // private void handleVarDecl(Node node) {
    // String varName = node.getValue();
    // if (symbolTable.isDeclared(varName)) {
    // errors.add("Variable '" + varName + "' is already declared.");
    // } else {
    // symbolTable.declare(varName);
    // }
    // }

    private void handleAssignment(Node node) {
        List<Node> children = node.getChildren();
        if (children.size() >= 2) {
            Node varNode = children.get(0);
            Node exprNode = children.get(1);
            String varName = varNode.getValue();
            if (!symbolTable.isDeclared(varName)) {
                errors.add("Variable '" + varName + "' is not declared.");
            }

            SymbolTableEntry currentEntry = this.symbolTable.find(varName);
            // INFO: Type check
            if (currentEntry.getDataType() == SymbolTableEntry.nodeTypeConversion(exprNode.getType())) {
                currentEntry.setValue(exprNode.getValue());
                currentEntry.setInitialised(true);
            } else {
                System.out.println("does not match");
            }
        } else {
            errors.add("Assignment node does not have enough children.");
        }
    }

    private void handleVarUsage(Node node) {
        String varName = node.getValue();
        if (!symbolTable.isDeclared(varName)) {
            node.addError("Variable '" + varName + "' is not declared.");
        }
    }

    private void handlePlusEqual(Node node) {
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);
        SymbolTableEntry entry1 = symbolTable.find(child1.getValue());

        if (child2.getValue() == "NSIMV") {
            SymbolTableEntry entry2 = symbolTable.find(child2.getValue());
            if (entry1.getDataType() == entry2.getDataType()) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) + Integer.parseInt(entry2.getValue())));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) + Double.parseDouble(entry2.getValue())));
                } else {
                    // TODO: Throw Error
                }
            }
        } else {

            String entry2 = child2.getValue();
            DataType entry2DataType = SymbolTableEntry.nodeTypeConversion(child2.getType());
            if (entry1.getDataType() == entry2DataType) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) + Integer.parseInt(entry2)));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) + Double.parseDouble(entry2)));
                } else {
                    // TODO: Throw Error
                }
            }
        }

    }

    // TODO: modify the <>Equal to improve code read ability

    private void handleMinusEqual(Node node) {
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);
        SymbolTableEntry entry1 = symbolTable.find(child1.getValue());

        if (child2.getValue() == "NSIMV") {
            SymbolTableEntry entry2 = symbolTable.find(child2.getValue());
            if (entry1.getDataType() == entry2.getDataType()) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) - Integer.parseInt(entry2.getValue())));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) - Double.parseDouble(entry2.getValue())));
                } else {
                    // TODO: Throw Error
                }
            }
        } else {

            String entry2 = child2.getValue();
            DataType entry2DataType = SymbolTableEntry.nodeTypeConversion(child2.getType());
            if (entry1.getDataType() == entry2DataType) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) - Integer.parseInt(entry2)));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) - Double.parseDouble(entry2)));
                } else {
                    // TODO: Throw Error
                }
            }
        }
    }

    private void handleStarEqual(Node node) {
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);
        SymbolTableEntry entry1 = symbolTable.find(child1.getValue());

        if (child2.getValue() == "NSIMV") {
            SymbolTableEntry entry2 = symbolTable.find(child2.getValue());
            if (entry1.getDataType() == entry2.getDataType()) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) * Integer.parseInt(entry2.getValue())));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) * Double.parseDouble(entry2.getValue())));
                } else {
                    // TODO: Throw Error
                }
            }
        } else {

            String entry2 = child2.getValue();
            DataType entry2DataType = SymbolTableEntry.nodeTypeConversion(child2.getType());
            if (entry1.getDataType() == entry2DataType) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) * Integer.parseInt(entry2)));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) * Double.parseDouble(entry2)));
                } else {
                    // TODO: Throw Error
                }
            }
        }
    }

    private void handleDivEqual(Node node) {
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);
        SymbolTableEntry entry1 = symbolTable.find(child1.getValue());

        if (child2.getValue() == "NSIMV") {
            SymbolTableEntry entry2 = symbolTable.find(child2.getValue());
            if (entry1.getDataType() == entry2.getDataType()) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) / Integer.parseInt(entry2.getValue())));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) / Double.parseDouble(entry2.getValue())));
                } else {
                    // TODO: Throw Error
                }
            }
        } else {

            String entry2 = child2.getValue();
            DataType entry2DataType = SymbolTableEntry.nodeTypeConversion(child2.getType());
            if (entry1.getDataType() == entry2DataType) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) / Integer.parseInt(entry2)));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) / Double.parseDouble(entry2)));
                } else {
                    // TODO: Throw Error
                }
            }
        }

    }

    private void handleAdd(Node node) {
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);
        SymbolTableEntry entry1 = symbolTable.find(child1.getValue());

        if (child2.getValue() == "NSIMV") {
            SymbolTableEntry entry2 = symbolTable.find(child2.getValue());
            if (entry1.getDataType() == entry2.getDataType()) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) / Integer.parseInt(entry2.getValue())));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) / Double.parseDouble(entry2.getValue())));
                } else {
                    // TODO: Throw Error
                }
            }
        } else {

            String entry2 = child2.getValue();
            DataType entry2DataType = SymbolTableEntry.nodeTypeConversion(child2.getType());
            if (entry1.getDataType() == entry2DataType) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    System.out.println();
                    entry1.setValue(
                            String.valueOf(Integer.parseInt(entry1.getValue()) / Integer.parseInt(entry2)));
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    entry1.setValue(
                            String.valueOf(
                                    Double.parseDouble(entry1.getValue()) / Double.parseDouble(entry2)));
                } else {
                    // TODO: Throw Error
                }
            }
        }

    }
}
