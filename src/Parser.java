import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parser class
 * 
 * A recursive descent parser for compiling a custom programming language.
 * It takes tokens from a Scanner and produces an abstract syntax tree (AST).
 * The Parser handles syntactic analysis, builds the AST, and performs error
 * handling and recovery.
 * 
 * Date: 2024-09-27
 *
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 */
public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private Node rootNode;
    private OutputController outputController;
    private SymbolTable symbolTable;
    private SymbolTableEntry currentEntry;
    private String programIdentifier;

    public Parser() {
        this.scanner = null;
        this.currentToken = null;
        this.rootNode = null;
        this.outputController = null;
        this.symbolTable = new SymbolTable();
        this.currentEntry = new SymbolTableEntry();
        this.programIdentifier = "";

    }

    public Parser(Scanner scanner, OutputController outputController) {
        this.scanner = scanner;
        this.currentToken = scanner.nextToken();
        this.rootNode = null;
        this.outputController = outputController;
        this.symbolTable = new SymbolTable();
        this.currentEntry = new SymbolTableEntry();
        this.programIdentifier = "";
    }

    private void moveToNextValidToken(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        this.currentEntry = null;
        while (!syncSet.contains(currentToken.getType()) &&
                currentToken.getType() != Tokeniser.TokenType.TTEOF) {
            currentToken = scanner.nextToken();
        }
        if (currentToken.getType() == Tokeniser.TokenType.TTEOF) {
            throw new ParseException("Fatal Error: Reached EOF while synchronising.");
        }

    }

    private boolean consume(Tokeniser.TokenType expectedType, Node parentNode, Set<Tokeniser.TokenType> syncSet)
            throws ParseException {
        if (currentToken.getType() == expectedType) {
            Token consumedToken = currentToken;
            if (parentNode != null && (consumedToken.getType() == Tokeniser.TokenType.TIDEN
                    || consumedToken.getType() == Tokeniser.TokenType.TILIT
                    || consumedToken.getType() == Tokeniser.TokenType.TFLIT)) {
                parentNode.setValue(consumedToken.getLexeme());
                parentNode.setLine(consumedToken.getLine());
                parentNode.setCol(consumedToken.getCol());
            }
            currentToken = scanner.nextToken();
            return false;
        } else {
            String errorDescription = "Expected '" + expectedType + "', but found '" + currentToken.getType() + "'";
            outputController.addParseError(errorDescription, currentToken, parentNode);

            if (parentNode != null) {
                parentNode.setType("NUNDEF");
            } else {
                parentNode = new Node("NUNDEF", "");
            }
            if (syncSet == null || syncSet.isEmpty()) {
                throw new ParseException("Fatal Error: Unable to synchronise.");
            }
            parentNode.setLine(currentToken.getLine());
            parentNode.setCol(currentToken.getCol());
        }
        return parentNode.getType().equals("NUNDEF");
    }

    private boolean match(Tokeniser.TokenType expectedType) throws ParseException {
        return currentToken.getType() == expectedType;
    }

    public Node parse() throws ParseException {
        rootNode = program();
        return rootNode;
    }

    private Node program() throws ParseException {
        Node node = new Node("NPROG", "");
        Set<Tokeniser.TokenType> programSyncSet = new HashSet<>(Arrays.asList(
                Tokeniser.TokenType.TCONS,
                Tokeniser.TokenType.TTYPD,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TMAIN,
                Tokeniser.TokenType.TBEGN));

        if (consume(Tokeniser.TokenType.TCD24, node, programSyncSet)) {
            moveToNextValidToken(programSyncSet);
            return node;
        }
        this.programIdentifier = currentToken.getLexeme();
        if (consume(Tokeniser.TokenType.TIDEN, node, programSyncSet)) {
            moveToNextValidToken(programSyncSet);
            return node;
        }
        node.addChild(globals(programSyncSet));
        node.addChild(funcs(programSyncSet));
        node.addChild(mainBody(programSyncSet));
        return node;
    }

    private Node globals(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NGLOB", "");
        syncSet.addAll(Arrays.asList(Tokeniser.TokenType.TFUNC,
                Tokeniser.TokenType.TMAIN));
        node.addChild(consts(syncSet));
        node.addChild(types(syncSet));
        Node arraysNode = arrays(syncSet);
        if (!arraysNode.isSpecial()) {
            node.addChild(arraysNode);
        }
        return node;
    }

    private Node consts(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TTYPD,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TMAIN));

        if (match(Tokeniser.TokenType.TCONS)) {
            this.currentEntry = new SymbolTableEntry(SymbolType.CONSTANT);
            if (consume(Tokeniser.TokenType.TCONS, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node = initList(syncSet);
        }
        return node;

    }

    private Node initList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NILIST", "");

        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TCOMA,
                Tokeniser.TokenType.TTYPD,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TMAIN));

        node.addChild(init(syncSet));
        while (match(Tokeniser.TokenType.TCOMA)) {
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            SymbolType temp = this.currentEntry.getSymbolType();
            this.currentEntry = new SymbolTableEntry(temp);
            node.addChild(init(syncSet));
        }
        return node;
    }

    private Node init(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NINIT", "");
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TCOMA,
                Tokeniser.TokenType.TTYPD,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TMAIN));
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TEQUL, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        this.currentEntry.setName(node.getValue());
        this.symbolTable.enter(this.currentEntry);
        node.addChild(expr(true, syncSet));
        return node;
    }

    private Node types(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTYPD)) {
            consume(Tokeniser.TokenType.TTYPD, node, syncSet);
            node = typeList(syncSet);
        }
        return node;
    }

    private Node typeList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TFUNC,
                Tokeniser.TokenType.TMAIN));
        Node typeNode = type(syncSet);
        if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("NTYPEL");
            node.addChild(typeNode);
            node.addChild(typeList(syncSet));
        } else {
            node = typeNode;
        }
        return node;
    }

    private Node type(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NATYPE", "");
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TIDEN,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TFUNC,
                Tokeniser.TokenType.TMAIN));
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TTDEF, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (match(Tokeniser.TokenType.TARAY)) {
            if (consume(Tokeniser.TokenType.TARAY, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            if (consume(Tokeniser.TokenType.TLBRK, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(expr(true, syncSet));
            if (consume(Tokeniser.TokenType.TRBRK, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            if (consume(Tokeniser.TokenType.TTTOF, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else {
            node.setType("NRTYPE");
            node.addChild(fields(syncSet));
        }
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node fields(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node sDeclNode = sDecl(syncSet);
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NFLIST");
            node.addChild(sDeclNode);
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(fields(syncSet));
        } else {
            node = sDeclNode;
        }
        return node;
    }

    private Node arrays(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TARRD)) {
            if (consume(Tokeniser.TokenType.TARRD, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node = arrDecls(syncSet);
        }
        return node;
    }

    private Node arrDecls(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node arrayNode = arrDecl(syncSet);
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NALIST");
            node.addChild(arrayNode);
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(arrDecls(syncSet));
        }
        node = arrayNode;
        return node;
    }

    private Node arrDecl(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NARRD", "");
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TCOLN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TIDEN, null, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node funcs(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TFUNC)) {
            node.setType("NFUNCS");
            node.addChild(func(syncSet));
            node.addChild(funcs(syncSet));
        }
        return node;
    }

    private Node func(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TFUNC,
                Tokeniser.TokenType.TTEND,
                Tokeniser.TokenType.TMAIN,
                Tokeniser.TokenType.TCD24));
        Node node = new Node("NFUND", "");
        this.currentEntry = new SymbolTableEntry(SymbolType.FUNCTION);
        if (consume(Tokeniser.TokenType.TFUNC, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        this.currentEntry.setName(this.currentToken.getLexeme());
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }

        if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(pList(syncSet));
        if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TCOLN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(rType(syncSet));
        node.addChild(funcBody(syncSet));
        this.symbolTable.enter(this.currentEntry);

        return node;
    }

    private Node rType(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TVOID)) {
            this.currentEntry.setDataType(DataType.VOID);
            consume(Tokeniser.TokenType.TVOID, node, syncSet);
            return node;
        } else {
            return sType(syncSet);
        }
    }

    private Node pList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TCONS)) {
            return params(syncSet);
        }
        return node;
    }

    private Node params(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(param(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPLIST");
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(params(syncSet));
        }
        return node;
    }

    private Node param(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSIMP", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            node.setType("NARRC");
            if (consume(Tokeniser.TokenType.TCONS, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(arrDecl(syncSet));
            // arrDecl(syncSet);
        } else {
            node.addChild(sDecl(syncSet));
        }
        return node;
    }

    private Node funcBody(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(locals(syncSet));
        if (consume(Tokeniser.TokenType.TBEGN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(stats(syncSet));
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node locals(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            return dList(syncSet);
        }
        return node;
    }

    private Node dList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(decl(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NDLIST");
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(dList(syncSet));
        }
        return node;
    }

    private Node decl(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSDECL", "");
        this.currentEntry = new SymbolTableEntry(SymbolType.VARIABLE);
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TCOLN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node mainBody(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NMAIN", "");
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TTEND,
                Tokeniser.TokenType.TCD24));
        if (consume(Tokeniser.TokenType.TMAIN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(sList(syncSet));
        if (consume(Tokeniser.TokenType.TBEGN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(stats(syncSet));
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TCD24, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (!this.programIdentifier.equals(currentToken.getLexeme())) {
            outputController.addSemanticError("Program names to not match", currentToken.getCol(),
                    currentToken.getLine());
        }
        // INFO: pass node null as is at the end of the program (otherwise main node
        // will be name set to the identifier)
        if (consume(Tokeniser.TokenType.TIDEN, null, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node sList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        this.currentEntry = new SymbolTableEntry(SymbolType.VARIABLE);
        node.addChild(sDecl(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NSDLST");
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(sList(syncSet));
        }
        return node;
    }

    private Node sDecl(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSDECL", "");
        if (consume(Tokeniser.TokenType.TIDEN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TCOLN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        } else {
            node.addChild(sType(syncSet));
        }
        this.currentEntry.setName(node.getValue());
        if (this.symbolTable.find(this.currentEntry.getName()) != null) {
            outputController.addSemanticError("Variable name " + this.currentEntry.getName() + " already used",
                    currentToken.getCol(), currentToken.getLine());
        } else {
            this.symbolTable.enter(this.currentEntry);
        }

        return node;
    }

    private Node sType(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TINTG)) {
            this.currentEntry.setDataType(DataType.INTEGER);
            if (consume(Tokeniser.TokenType.TINTG, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else if (match(Tokeniser.TokenType.TFLOT)) {
            this.currentEntry.setDataType(DataType.FLOAT);
            if (consume(Tokeniser.TokenType.TFLOT, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else {
            this.currentEntry.setDataType(DataType.BOOLEAN);
            if (consume(Tokeniser.TokenType.TBOOL, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        }
        return node;
    }

    private Node stats(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO)) {
            node.addChild(strStat(syncSet));
        } else {
            node.addChild(stat(syncSet));
            if (consume(Tokeniser.TokenType.TSEMI, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        }
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO) || match(Tokeniser.TokenType.TREPT)
                || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TINPT)
                || match(Tokeniser.TokenType.TRETN) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.setType("NSTATS");
            node.addChild(stats(syncSet));
        }
        return node;
    }

    private Node strStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTFOR)) {
            // node.addChild(forStat(syncSet));
            node = forStat(syncSet);
        } else if (match(Tokeniser.TokenType.TIFTH)) {
            // node.addChild(ifStat(syncSet));
            node = ifStat(syncSet);
        } else if (match(Tokeniser.TokenType.TSWTH)) {
            // node.addChild(switchStat(syncSet));
            node = switchStat(syncSet);
        } else {
            // node.addChild(doStat(syncSet));
            node = doStat(syncSet);

        }
        return node;
    }

    // TODO: use symbol table to differentiate
    private Node stat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        syncSet = new HashSet<>(Arrays.asList(
                Tokeniser.TokenType.TSEMI, Tokeniser.TokenType.TTEND, Tokeniser.TokenType.TELSE,
                Tokeniser.TokenType.TELIF,
                Tokeniser.TokenType.TIDEN, Tokeniser.TokenType.TTFOR, Tokeniser.TokenType.TIFTH,
                Tokeniser.TokenType.TSWTH,
                Tokeniser.TokenType.TTTDO, Tokeniser.TokenType.TREPT, Tokeniser.TokenType.TINPT,
                Tokeniser.TokenType.TPRNT,
                Tokeniser.TokenType.TPRLN, Tokeniser.TokenType.TRETN));

        if (match(Tokeniser.TokenType.TREPT)) {
            node.addChild(repStat(syncSet));
        } else if (match(Tokeniser.TokenType.TIDEN)
                && this.symbolTable.find(this.currentToken.getLexeme()) != null &&
                this.symbolTable.find(this.currentToken.getLexeme()).getSymbolType() == SymbolType.FUNCTION) {
            node.addChild(callStat(syncSet));
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(asgnStat(syncSet));
        } else if (match(Tokeniser.TokenType.TINPT) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.addChild(ioStat(syncSet));
        } else if (match(Tokeniser.TokenType.TRETN)) {
            node.addChild(returnStat(syncSet));
        }
        return node;
    }

    private Node forStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NFOR", "");
        if (consume(Tokeniser.TokenType.TTFOR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(asgnList(syncSet));
        if (consume(Tokeniser.TokenType.TSEMI, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(bool(syncSet));
        if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(stats(syncSet));
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node repStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NREPT", "");
        if (consume(Tokeniser.TokenType.TREPT, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(asgnList(syncSet));
        if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(stats(syncSet));
        if (consume(Tokeniser.TokenType.TUNTL, node, null)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(bool(syncSet));
        return node;
    }

    private Node doStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NDOWL", "");
        if (consume(Tokeniser.TokenType.TTTDO, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(stats(syncSet));
        if (consume(Tokeniser.TokenType.TWHIL, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(bool(syncSet));
        if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node asgnList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            return aList(syncSet);
        }
        return node;
    }

    private Node aList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(asgnStat(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NASGNS");
            if (consume(Tokeniser.TokenType.TCOMA, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(aList(syncSet));
        }
        return node;
    }

    private Node ifStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NIFTH", "");
        if (consume(Tokeniser.TokenType.TIFTH, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(bool(syncSet));
        if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(stats(syncSet));
        if (match(Tokeniser.TokenType.TELSE)) {
            node.setType("NIFTE");
            if (consume(Tokeniser.TokenType.TELSE, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(stats(syncSet));
        } else if (match(Tokeniser.TokenType.TELIF)) {
            node.setType("NIFEF");
            if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(bool(syncSet));
            if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(stats(syncSet));
        }
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node switchStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSWTCH", "");
        if (consume(Tokeniser.TokenType.TSWTH, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TLPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(expr(true, syncSet));
        if (consume(Tokeniser.TokenType.TRPAR, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        if (consume(Tokeniser.TokenType.TBEGN, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        node.addChild(caseList(syncSet));
        if (consume(Tokeniser.TokenType.TTEND, node, syncSet)) {
            moveToNextValidToken(syncSet);
            return node;
        }
        return node;
    }

    private Node caseList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NCASLT", "");
        if (match(Tokeniser.TokenType.TCASE)) {
            if (consume(Tokeniser.TokenType.TCASE, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(expr(true, syncSet));
            if (consume(Tokeniser.TokenType.TCOLN, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(stats(syncSet));
            if (consume(Tokeniser.TokenType.TBREK, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            if (consume(Tokeniser.TokenType.TSEMI, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(caseList(syncSet));
        } else {
            node.setType("SPECIAL");
            if (consume(Tokeniser.TokenType.TDFLT, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            if (consume(Tokeniser.TokenType.TCOLN, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
            node.addChild(stats(syncSet));
        }
        return node;
    }

    private Node asgnStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node varNode = var(syncSet);
        Node node = asgnOp(syncSet);
        node.addChild(varNode);
        Node boolNode = bool(syncSet);
        node.addChild(boolNode);
        return node;
    }

    private Node asgnOp(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("", "");
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TNOTT,
                Tokeniser.TokenType.TIDEN,
                Tokeniser.TokenType.TILIT,
                Tokeniser.TokenType.TFLIT,
                Tokeniser.TokenType.TTRUE,
                Tokeniser.TokenType.TFALS));
        if (match(Tokeniser.TokenType.TPLEQ)) {
            node.setType("NPLEQ");
            if (consume(Tokeniser.TokenType.TPLEQ, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            node.setType("NMNEQ");
            if (consume(Tokeniser.TokenType.TMNEQ, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            node.setType("NSTEQ");
            if (consume(Tokeniser.TokenType.TSTEQ, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            node.setType("NDVEQ");
            if (consume(Tokeniser.TokenType.TDVEQ, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        } else {
            node.setType("NASGN");
            if (consume(Tokeniser.TokenType.TEQUL, node, syncSet)) {
                moveToNextValidToken(syncSet);
                return node;
            }
        }
        return node;
    }

    private Node ioStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NINPUT", "");
        if (match(Tokeniser.TokenType.TINPT)) {
            consume(Tokeniser.TokenType.TINPT, node, syncSet);
            node.addChild(vList(syncSet));
        } else if (match(Tokeniser.TokenType.TPRNT)) {
            consume(Tokeniser.TokenType.TPRNT, node, syncSet);
            node.setType("NPRINT");
            node.addChild(prList(syncSet));
        } else {
            consume(Tokeniser.TokenType.TPRLN, node, syncSet);
            node.setType("NPRLN");
            node.addChild(prList(syncSet));
        }
        return node;
    }

    private Node callStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        if (match(Tokeniser.TokenType.TNOTT) || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TILIT)
                || match(Tokeniser.TokenType.TFLIT) || match(Tokeniser.TokenType.TTRUE)
                || match(Tokeniser.TokenType.TFALS)) {
            node.addChild(eList(syncSet));
        }
        consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        return node;
    }

    private Node returnStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NRETN", "");
        consume(Tokeniser.TokenType.TRETN, node, syncSet);
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node, syncSet);
        } else {
            node.addChild(expr(true, syncSet));
        }
        return node;
    }

    private Node vList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(var(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NVLIST");
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(vList(syncSet));
        }
        return node;
    }

    private Node var(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSIMV", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        if (match(Tokeniser.TokenType.TLBRK)) {
            node.setType("NAELT");
            consume(Tokeniser.TokenType.TLBRK, node, syncSet);
            node.addChild(expr(true, syncSet));
            consume(Tokeniser.TokenType.TRBRK, node, syncSet);
            if (match(Tokeniser.TokenType.TDOTT)) {
                node.setType("NARRV");
                consume(Tokeniser.TokenType.TDOTT, node, syncSet);
                consume(Tokeniser.TokenType.TIDEN, node, syncSet);
            }
        }
        return node;
    }

    private Node eList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(bool(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NEXPL");
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(eList(syncSet));
        }
        return node;
    }

    private Node bool(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NBOOL", "");
        if (match(Tokeniser.TokenType.TNOTT)) {
            consume(Tokeniser.TokenType.TNOTT, node, syncSet);
            node.addChild(bool(syncSet));
        } else if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TILIT)
                || match(Tokeniser.TokenType.TFLIT) || match(Tokeniser.TokenType.TTRUE)
                || match(Tokeniser.TokenType.TFALS)) {
            node.setType("SPECIAL");
            Node relNode = rel(syncSet);

            if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR)
                    || match(Tokeniser.TokenType.TTXOR)) {
                node.setType("NBOOL");
                node.addChild(relNode);
                node.addChild(logOp(syncSet));
                node.addChild(rel(syncSet));

            } else {
                node = relNode;
            }
        } else {
            node.addChild(bool(syncSet));
            node.addChild(logOp(syncSet));
            node.addChild(rel(syncSet));
        }
        return node;
    }

    private Node rel(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node exprNode = expr(true, syncSet);
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            node = relOp(syncSet);
            node.addChild(exprNode);
            node.addChild(expr(true, syncSet));
        } else {
            node = exprNode;
        }
        return node;
    }

    private Node logOp(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TTAND)) {
            node.setType("NAND");
            consume(Tokeniser.TokenType.TTAND, node, syncSet);
        } else if (match(Tokeniser.TokenType.TTTOR)) {
            node.setType("NOR");
            consume(Tokeniser.TokenType.TTTOR, node, syncSet);
        } else {
            node.setType("NXOR");
            consume(Tokeniser.TokenType.TTXOR, node, syncSet);
        }
        return node;
    }

    private Node relOp(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TEQEQ)) {
            node.setType("NEQL");
            consume(Tokeniser.TokenType.TEQEQ, node, syncSet);
        } else if (match(Tokeniser.TokenType.TNEQL)) {
            node.setType("NNEQ");
            consume(Tokeniser.TokenType.TNEQL, node, syncSet);
        } else if (match(Tokeniser.TokenType.TGRTR)) {
            node.setType("NGRT");
            consume(Tokeniser.TokenType.TGRTR, node, syncSet);
        } else if (match(Tokeniser.TokenType.TLEQL)) {
            node.setType("NLEQ");
            consume(Tokeniser.TokenType.TLEQL, node, syncSet);
        } else if (match(Tokeniser.TokenType.TLESS)) {
            node.setType("NLSS");
            consume(Tokeniser.TokenType.TLESS, node, syncSet);
        } else {
            node.setType("NGEQ");
            consume(Tokeniser.TokenType.TGEQL, node, syncSet);
        }
        return node;
    }

    private Node expr(boolean termNeeded, Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node termNode = null;
        if (termNeeded) {
            termNode = term(true, syncSet);
        }

        if (match(Tokeniser.TokenType.TPLUS)) {
            node.setType("NADD");
            if (termNode != null) {
                node.addChild(termNode);
            }
            consume(Tokeniser.TokenType.TPLUS, node, syncSet);
            node.addChild(term(true, syncSet));
            node.addChild(expr(false, syncSet));
        } else if (match(Tokeniser.TokenType.TMINS)) {
            node.setType("NSUB");
            if (termNode != null) {
                node.addChild(termNode);
            }
            consume(Tokeniser.TokenType.TMINS, node, syncSet);
            node.addChild(term(true, syncSet));
            node.addChild(expr(false, syncSet));
        } else {
            if (termNode != null) {
                node = termNode;
            }
        }
        return node;
    }

    private Node term(Boolean factNeeded, Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node factNode = null;
        if (factNeeded) {
            factNode = fact(true, syncSet);
        }
        if (match(Tokeniser.TokenType.TSTAR)) {
            node.setType("NMUL");
            if (factNode != null) {
                node.addChild(factNode);
            }
            consume(Tokeniser.TokenType.TSTAR, node, syncSet);
            node.addChild(fact(true, syncSet));
            node.addChild(term(false, syncSet));
        } else if (match(Tokeniser.TokenType.TDIVD)) {
            node.setType("NDIV");
            if (factNode != null) {
                node.addChild(factNode);
            }
            consume(Tokeniser.TokenType.TDIVD, node, syncSet);
            node.addChild(fact(true, syncSet));
            node.addChild(term(false, syncSet));
        } else if (match(Tokeniser.TokenType.TPERC)) {
            node.setType("NMOD");
            if (factNode != null) {
                node.addChild(factNode);
            }
            consume(Tokeniser.TokenType.TPERC, node, null);
            node.addChild(fact(true, syncSet));
            node.addChild(term(false, syncSet));
        } else {
            if (factNode != null) {
                node = factNode;
            }
        }
        // ε
        return node;
    }

    private Node fact(Boolean exponentNeeded, Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node exponentNode = null;
        if (exponentNeeded) {
            exponentNode = exponent(syncSet);
            // node.addChild(exponent(syncSet));
        }
        if (match(Tokeniser.TokenType.TCART)) {
            node.setType("NPOW");
            if (exponentNode != null) {
                node.addChild(exponentNode);
            }
            consume(Tokeniser.TokenType.TCART, node, syncSet);
            node.addChild(exponent(syncSet));
            node.addChild(fact(false, syncSet));
        } else {
            if (exponentNode != null) {
                node = exponentNode;
            }
        }
        return node;
    }

    private Node exponent(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTRUE)) {
            node.setType("NTRUE");
            consume(Tokeniser.TokenType.TTRUE, node, syncSet);
        } else if (match(Tokeniser.TokenType.TFALS)) {
            node.setType("NFALS");
            consume(Tokeniser.TokenType.TFALS, node, syncSet);
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node = var(syncSet);
        } else if (match(Tokeniser.TokenType.TILIT)) {
            node.setType("NILIT");
            consume(Tokeniser.TokenType.TILIT, node, syncSet);
        } else if (match(Tokeniser.TokenType.TFLIT)) {
            node.setType("NFLIT");
            consume(Tokeniser.TokenType.TFLIT, node, syncSet);
        } else if (match(Tokeniser.TokenType.TLPAR)) {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TLPAR, node, syncSet);
            node.addChild(bool(syncSet));
            consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        } else {
            node = fnCall(syncSet);
        }
        return node;
    }

    private Node fnCall(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NFCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList(syncSet));
        }
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        return node;
    }

    private Node prList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(printItem(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPRLST");
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(prList(syncSet));
        }
        return node;
    }

    private Node printItem(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TSTRG)) {
            node.setType("NSTRG");
            consume(Tokeniser.TokenType.TSTRG, node, syncSet);
        } else {
            node.addChild(expr(true, syncSet));
        }
        return node;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

}
