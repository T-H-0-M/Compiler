import java.util.List;
import java.util.ArrayList;

public class Node {
    private String type;
    private String value;
    private List<Node> children;
    private List<String> errors;

    public Node(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public void printTree() {
        printTree(0);
    }

    private void printTree(int indent) {
        String indentation = " ".repeat(indent * 2);
        System.out.println(indentation + type + (value.isEmpty() ? "" : ": " + value));
        for (Node child : children) {
            child.printTree(indent + 1);
        }
    }

    public void addError(String error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setType(String type) {
        this.type = type;
    }
}
