import java.util.*;

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
        List<String> lines = new ArrayList<>();
        buildTreeString(this, lines, "", "");
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void buildTreeString(Node node, List<String> lines, String prefix, String childrenPrefix) {
        String content = node.type + (node.value.isEmpty() ? "" : " (" + node.value + ")");
        lines.add(prefix + content);

        for (Iterator<Node> it = node.children.iterator(); it.hasNext();) {
            Node child = it.next();
            if (it.hasNext()) {
                buildTreeString(child, lines, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                buildTreeString(child, lines, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
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
