import java.util.List;
import java.util.ArrayList;

public class SemanticAnalyser {
    private SymbolTable symbolTable;
    private List<String> errors;

    public SemanticAnalyser() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
    }

    public void analyse(Node root) {
        traverse(root);
        if (!errors.isEmpty()) {
            System.out.println("Semantic Errors:");
            for (String error : errors) {
                System.out.println(error);
            }
        } else {
            System.out.println("Semantic analysis completed without errors.");
        }
    }

    private void traverse(Node node) {
        if (node == null)
            return;
        String nodeType = node.getType();

        switch (nodeType) {
            case "NSDECL":
                handleVarDecl(node);
                break;
            case "NASGN":
                handleAssignment(node);
                break;
            case "NADD":
                handleAddition(node);
                break;
            case "NSUB":
                handleSubtraction(node);
                break;
            default:

                for (Node child : node.getChildren()) {
                    traverse(child);
                }
                break;
        }
    }

    private void handleSubtraction(Node node) {
        if (node.getChildren().size() != 2) {
            errors.add("Subtraction node does not have exactly 2 children.");
            return;
        }

        if (node.getChildren().get(0).getType() != node.getChildren().get(1).getType()) {
            errors.add("Types must match for subtraction operation.");
            return;
        }
    }

    private void handleAddition(Node node) {
        if (node.getChildren().size() != 2) {
            errors.add("Subtraction node does not have exactly 2 children.");
            return;
        }

        if (node.getChildren().get(0).getType() != node.getChildren().get(1).getType()) {
            errors.add("Types must match for subtraction operation.");
            return;
        }
    }

    // TODO: add checks for all relevant nodes here
    private void handleVarDecl(Node node) {
        String varName = node.getValue();
        if (symbolTable.isDeclared(varName)) {
            errors.add("Variable '" + varName + "' is already declared.");
        } else {
            symbolTable.declare(varName);
        }
    }

    private void handleAssignment(Node node) {
        List<Node> children = node.getChildren();
        if (children.size() >= 2) {
            Node varNode = children.get(0);
            Node exprNode = children.get(1);

            String varName = varNode.getValue();
            if (!symbolTable.isDeclared(varName)) {
                errors.add("Variable '" + varName + "' is not declared.");
            }
            // TODO: add value to symbol table
            System.out.println(exprNode.getValue());
            System.out.println(exprNode.getType());
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
}
