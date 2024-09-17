import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private Node rootNode;
    private static final Set<Tokeniser.TokenType> GLOBAL_SYNC_TOKENS = new HashSet<>(Arrays.asList(
            Tokeniser.TokenType.TCD24,
            Tokeniser.TokenType.TFUNC,
            Tokeniser.TokenType.TTYPD,
            Tokeniser.TokenType.TARRD,
            Tokeniser.TokenType.TCONS,
            Tokeniser.TokenType.TBEGN,
            Tokeniser.TokenType.TTEND,
            Tokeniser.TokenType.TSEMI,
            Tokeniser.TokenType.TCOMA));

    public Parser() {
        this.scanner = null;
        this.currentToken = null;
        this.rootNode = null;
    }

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.nextToken();
        this.rootNode = null;
    }

    private void consume(Tokeniser.TokenType expectedType, Node parentNode) throws ParseException {
        if (currentToken.getType() == expectedType) {
            Token consumedToken = currentToken;
            Node node = new Node(consumedToken.getType().toString(), consumedToken.getLexeme());
            if (parentNode != null && consumedToken.getType() == Tokeniser.TokenType.TIDEN) {
                System.out.println("setting " + parentNode.getType() + " to " + node.getValue());
                parentNode.setValue(consumedToken.getLexeme());
            }
            currentToken = scanner.nextToken();
        } else {
            String errorMsg = "Expected " + expectedType + ", but found " + currentToken.getType() +
                    " on line " + currentToken.getLine() + " and col " + currentToken.getCol();
            System.out.println(new ParseException(errorMsg));
            if (parentNode != null) {
                parentNode.addChild(new Node("NUNDEF", ""));
            }
            while (!GLOBAL_SYNC_TOKENS.contains(currentToken.getType()) &&
                    currentToken.getType() != Tokeniser.TokenType.TTEOF) {
                currentToken = scanner.nextToken();
            }
        }
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
        consume(Tokeniser.TokenType.TCD24, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        node.addChild(globals());
        node.addChild(funcs());
        node.addChild(mainBody());
        return node;
    }

    private Node globals() throws ParseException {
        Node node = new Node("NGLOB", "");
        node.addChild(consts());
        node.addChild(types());
        node.addChild(arrays());
        return node;
    }

    private Node consts() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(initList());
            return node;
        }
        return node;
    }

    // TODO: check the NILIST here
    private Node initList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(init());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NILIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(initList());
        }
        return node;
    }

    private Node init() throws ParseException {
        // System.out.println("NINIT hit");
        Node node = new Node("NINIT", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TEQUL, node);
        node.addChild(expr(true));
        return node;
    }

    private Node types() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTYPD)) {
            consume(Tokeniser.TokenType.TTYPD, node);
            node.addChild(typeList());
        }
        return node;
    }

    private Node typeList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(type());
        // node.addChild(typeListTail());
        if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("NTYPEL");
            return typeList();
        }
        return node;
    }

    private Node type() throws ParseException {
        Node node = new Node("NRTYPE", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TTDEF, node);
        if (match(Tokeniser.TokenType.TARAY)) {
            consume(Tokeniser.TokenType.TARAY, node);
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(expr(true));
            consume(Tokeniser.TokenType.TRBRK, node);
            consume(Tokeniser.TokenType.TTTOF, node);
            consume(Tokeniser.TokenType.TIDEN, node);
        } else {
            node.addChild(fields());
        }
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node fields() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NFLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(fields());
        }
        return node;
    }

    private Node arrays() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TARRD)) {
            consume(Tokeniser.TokenType.TARRD, node);
            node.addChild(arrDecls());
            return node;
        }
        return node;
    }

    private Node arrDecls() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(arrDecl());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NALIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(arrDecls());
        }
        return node;
    }

    private Node arrDecl() throws ParseException {
        Node node = new Node("NARRD", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        return node;
    }

    private Node funcs() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TFUNC)) {
            node.setType("NFUNCS");
            node.addChild(func());
            node.addChild(funcs());
            return node;
        }
        return node;
    }

    private Node func() throws ParseException {
        // System.out.println("NFUND Hit");
        Node node = new Node("NFUND", "");
        consume(Tokeniser.TokenType.TFUNC, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(pList());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        node.addChild(rType());
        node.addChild(funcBody());
        return node;
    }

    private Node rType() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node);
            return node;
        } else {
            return sType();
        }
    }

    private Node pList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TCONS)) {
            return params();
        }
        return node;
    }

    private Node params() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(param());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(params());
        }
        return node;
    }

    // TODO: find out what to do with array decl
    private Node param() throws ParseException {
        Node node = new Node("NSIMP", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            node.setType("NARRC");
            node.addChild(new Node("TCONS", currentToken.getLexeme()));
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(arrDecl());
            arrDecl();
        } else {
            node.addChild(sDecl());
        }
        return node;
    }

    private Node funcBody() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(locals());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node locals() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            return dList();
        }
        return node;
    }

    private Node dList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(decl());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NDLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(dList());
        }
        return node;
    }

    // TODO: Complete this, the grammar is currently ambiguous
    private Node decl() throws ParseException {
        return new Node("SPECIAL", "");
    }

    private Node mainBody() throws ParseException {
        Node node = new Node("NMAIN", "");
        consume(Tokeniser.TokenType.TMAIN, node);
        node.addChild(sList());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        consume(Tokeniser.TokenType.TCD24, node);
        // pass node null as is at the end of the program (otherwise main node will be
        // name set to the indentifier)
        consume(Tokeniser.TokenType.TIDEN, null);
        return node;
    }

    private Node sList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NSDLST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(sList());
        }
        return node;
    }

    private Node sDecl() throws ParseException {
        Node node = new Node("NSDECL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN, node);
        } else {
            node.addChild(sType());
        }
        return node;
    }

    private Node sType() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TINTG)) {
            consume(Tokeniser.TokenType.TINTG, node);
        } else if (match(Tokeniser.TokenType.TFLOT)) {
            consume(Tokeniser.TokenType.TFLOT, node);
        } else {
            consume(Tokeniser.TokenType.TBOOL, node);
        }
        return node;
    }

    private Node stats() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO)) {
            node.addChild(strStat());
        } else {
            node.addChild(stat());
            consume(Tokeniser.TokenType.TSEMI, node);
        }
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO) || match(Tokeniser.TokenType.TREPT)
                || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TINPT)
                || match(Tokeniser.TokenType.TRETN) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.setType("NSTATS");
            node.addChild(stats());
        }
        return node;
    }

    private Node strStat() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTFOR)) {
            node.addChild(forStat());
        } else if (match(Tokeniser.TokenType.TIFTH)) {
            node.addChild(ifStat());
        } else if (match(Tokeniser.TokenType.TSWTH)) {
            node.addChild(switchStat());
        } else {
            node.addChild(doStat());
        }
        return node;
    }

    private Node stat() throws ParseException {
        // TODO: This is wrong, tf was i smoking
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TREPT)) {
            node.addChild(repStat());
            // TODO: fix this with symbol table - asgnstat and callstatt both start with
            // TIDEN
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(asgnStat());
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(callStat());
        } else if (match(Tokeniser.TokenType.TINPT) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.addChild(ioStat());
        } else {
            node.addChild(returnStat());
        }
        return node;
    }

    private Node forStat() throws ParseException {
        // TODO: increase scope
        // System.out.println("nfor hit");
        Node node = new Node("NFOR", "");
        consume(Tokeniser.TokenType.TTFOR, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(asgnList());
        consume(Tokeniser.TokenType.TSEMI, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        // TODO: decrease scope
        return node;
    }

    private Node repStat() throws ParseException {
        // System.out.println("nrept hit");
        Node node = new Node("NREPT", "");
        consume(Tokeniser.TokenType.TREPT, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(asgnList());
        consume(Tokeniser.TokenType.TRPAR, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TUNTL, node);
        node.addChild(bool());
        return node;
    }

    private Node doStat() throws ParseException {

        Node node = new Node("NDOWL", "");
        consume(Tokeniser.TokenType.TTTDO, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TWHIL, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node asgnList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            return aList();
        }
        return node;
    }

    private Node aList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(asgnStat());
        // node.addChild(aListTail());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NASGNS");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(aList());
        }
        return node;
    }

    private Node ifStat() throws ParseException {
        // System.out.println("NIFITH hit");
        Node node = new Node("NIFITH", "");
        consume(Tokeniser.TokenType.TIFTH, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TRPAR, node);
        node.addChild(stats());
        if (match(Tokeniser.TokenType.TELSE)) {
            node.setType("NIFTE");
            consume(Tokeniser.TokenType.TELSE, node);
            node.addChild(stats());
        } else if (match(Tokeniser.TokenType.TELIF)) {
            node.setType("NIFEF");
            Node elifNode = new Node("ElseIf", "");
            consume(Tokeniser.TokenType.TLPAR, node);
            elifNode.addChild(bool());
            consume(Tokeniser.TokenType.TRPAR, node);
            elifNode.addChild(stats());
            node.addChild(elifNode);
        }
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node switchStat() throws ParseException {
        Node node = new Node("NSWTCH", "");
        consume(Tokeniser.TokenType.TSWTH, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(expr(true));
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(caseList());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node caseList() throws ParseException {
        Node node = new Node("NCASLT", "");
        if (match(Tokeniser.TokenType.TCASE)) {
            consume(Tokeniser.TokenType.TCASE, node);
            node.addChild(expr(true));
            consume(Tokeniser.TokenType.TCOLN, node);
            node.addChild(stats());
            consume(Tokeniser.TokenType.TBREK, node);
            consume(Tokeniser.TokenType.TSEMI, node);
            node.addChild(caseList());
        } else {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TDFLT, node);
            consume(Tokeniser.TokenType.TCOLN, node);
            node.addChild(stats());
        }
        return node;
    }

    private Node asgnStat() throws ParseException {
        // System.out.println("asgnStat hit");
        // TODO: ensure that dan agrees with this
        Node node = new Node("NASGN", "");
        node.addChild(var());
        node.addChild(asgnOp());
        node.addChild(bool());
        return node;
    }

    private Node asgnOp() throws ParseException {
        // System.out.println("asgnop hit");
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TPLEQ)) {
            node.setType("NPLEQ");
            consume(Tokeniser.TokenType.TPLEQ, node);
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            node.setType("NMNEQ");
            consume(Tokeniser.TokenType.TMNEQ, node);
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            node.setType("NSTEA");
            consume(Tokeniser.TokenType.TSTEQ, node);
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            node.setType("NDVEQ");
            consume(Tokeniser.TokenType.TDVEQ, node);
        } else {
            // TODO: ensure that dan agrees with this, otherwise change to NASGN
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TEQUL, node);
        }
        return node;
    }

    private Node ioStat() throws ParseException {
        // System.out.println("iostat hit");
        Node node = new Node("NINPUT", "");
        if (match(Tokeniser.TokenType.TINPT)) {
            consume(Tokeniser.TokenType.TINPT, node);
            node.addChild(vList());
        } else if (match(Tokeniser.TokenType.TPRNT)) {
            consume(Tokeniser.TokenType.TPRNT, node);
            node.setType("NPRINT");
            node.addChild(prList());
        } else {
            consume(Tokeniser.TokenType.TPRLN, node);
            node.setType("NPRLN");
            node.addChild(prList());
        }
        return node;
    }

    private Node callStat() throws ParseException {
        // System.out.println("NCALL hit");
        Node node = new Node("NCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRPAR, node);
        return node;
    }

    private Node returnStat() throws ParseException {
        // System.out.println("return hit");
        Node node = new Node("NRETN", "");
        consume(Tokeniser.TokenType.TRETN, node);
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node);
        } else {
            node.addChild(expr(true));
        }
        return node;
    }

    private Node vList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(var());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NVLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(vList());
        }
        return node;
    }

    private Node var() throws ParseException {
        // System.out.println("var hit");
        Node node = new Node("NSIMV", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        if (match(Tokeniser.TokenType.TLBRK)) {
            node.setType("NAELT");
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(expr(true));
            consume(Tokeniser.TokenType.TRBRK, node);
            if (match(Tokeniser.TokenType.TDOTT)) {
                node.setType("NARRV");
                consume(Tokeniser.TokenType.TDOTT, node);
                consume(Tokeniser.TokenType.TIDEN, node);
            }
        }
        return node;
    }

    private Node eList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(bool());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NEXPL");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(eList());
        }
        return node;
    }

    private Node bool() throws ParseException {
        // System.out.println("bool hit");
        Node node = new Node("NBOOL", "");
        if (match(Tokeniser.TokenType.TNOTT)) {
            consume(Tokeniser.TokenType.TNOTT, node);
            node.addChild(bool());
        } else if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TILIT)
                || match(Tokeniser.TokenType.TFLIT) || match(Tokeniser.TokenType.TTRUE)
                || match(Tokeniser.TokenType.TFALS)) {
            node.setType("SPECIAL");
            node.addChild(rel());

            if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR)
                    || match(Tokeniser.TokenType.TTXOR)) {
                node.setType("NBOOL");
                node.addChild(logOp());
                node.addChild(rel());
            }
        } else {
            node.addChild(bool());
            node.addChild(logOp());
            node.addChild(rel());
        }
        return node;
    }

    private Node rel() throws ParseException {
        // System.out.println("Rel hit");
        Node node = new Node("SPECIAL", "");
        node.addChild(expr(true));
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            node.addChild(relOp());
            node.addChild(expr(true));
        }
        return node;
    }

    private Node logOp() throws ParseException {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TTAND)) {
            node.setType("NAND");
            consume(Tokeniser.TokenType.TTAND, node);
        } else if (match(Tokeniser.TokenType.TTTOR)) {
            node.setType("NOR");
            consume(Tokeniser.TokenType.TTTOR, node);
        } else {
            node.setType("NXOR");
            consume(Tokeniser.TokenType.TTXOR, node);
        }
        return node;
    }

    private Node relOp() throws ParseException {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TEQEQ)) {
            node.setType("NEQL");
            consume(Tokeniser.TokenType.TEQEQ, node);
        } else if (match(Tokeniser.TokenType.TNEQL)) {
            node.setType("NNEQ");
            consume(Tokeniser.TokenType.TNEQL, node);
        } else if (match(Tokeniser.TokenType.TGRTR)) {
            node.setType("NGRT");
            consume(Tokeniser.TokenType.TGRTR, node);
        } else if (match(Tokeniser.TokenType.TLEQL)) {
            node.setType("NLEQ");
            consume(Tokeniser.TokenType.TLEQL, node);
        } else if (match(Tokeniser.TokenType.TLESS)) {
            node.setType("NLSS");
            consume(Tokeniser.TokenType.TLESS, node);
        } else {
            node.setType("NGEQ");
            consume(Tokeniser.TokenType.TGEQL, node);
        }
        return node;
    }

    private Node expr(boolean termNeeded) throws ParseException {
        // System.out.println("expr hit");
        Node node = new Node("SPECIAL", "");
        if (termNeeded) {
            node.addChild(term(true));
        }

        if (match(Tokeniser.TokenType.TPLUS)) {
            node.setType("NADD");
            consume(Tokeniser.TokenType.TPLUS, node);
            node.addChild(term(true));
            node.addChild(expr(false));
        } else if (match(Tokeniser.TokenType.TMINS)) {
            node.setType("NSUB");
            consume(Tokeniser.TokenType.TMINS, node);
            node.addChild(term(true));
            node.addChild(expr(false));
        }
        return node;
    }

    private Node term(Boolean factNeeded) throws ParseException {
        // System.out.println("Term hit");
        Node node = new Node("SPECIAL", "");
        if (factNeeded) {
            node.addChild(fact(true));
        }
        if (match(Tokeniser.TokenType.TSTAR)) {
            node.setType("NMUL");
            consume(Tokeniser.TokenType.TSTAR, node);
            node.addChild(fact(true));
            node.addChild(term(false));
        } else if (match(Tokeniser.TokenType.TDIVD)) {
            node.setType("NDIV");
            consume(Tokeniser.TokenType.TDIVD, node);
            node.addChild(fact(true));
            node.addChild(term(false));
        } else if (match(Tokeniser.TokenType.TPERC)) {
            node.setType("NMOD");
            consume(Tokeniser.TokenType.TPERC, node);
            node.addChild(fact(true));
            node.addChild(term(false));
        }
        // ε
        return node;
    }

    private Node fact(Boolean exponentNeeded) throws ParseException {
        // System.out.println("fact hit");
        Node node = new Node("SPECIAL", "");
        if (exponentNeeded) {
            node.addChild(exponent());
        }
        if (match(Tokeniser.TokenType.TCART)) {
            node.setType("NPOW");
            consume(Tokeniser.TokenType.TCART, node);
            node.addChild(exponent());
            node.addChild(fact(false));
        }
        return node;
    }

    private Node exponent() throws ParseException {
        // System.out.println("exponent hit " + currentToken.getType().toString());
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTRUE)) {
            node.setType("NTRUE");
            consume(Tokeniser.TokenType.TTRUE, node);
        } else if (match(Tokeniser.TokenType.TFALS)) {
            node.setType("NFALS");
            consume(Tokeniser.TokenType.TFALS, node);
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("SPECIAL");
            // consume(Tokeniser.TokenType.TIDEN, node);
            node.addChild(var());
        } else if (match(Tokeniser.TokenType.TILIT)) {
            node.setType("NILIT");
            consume(Tokeniser.TokenType.TILIT, node);
        } else if (match(Tokeniser.TokenType.TFLIT)) {
            node.setType("NFLIT");
            consume(Tokeniser.TokenType.TFLIT, node);
        } else if (match(Tokeniser.TokenType.TLPAR)) {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TLPAR, node);
            node.addChild(bool());
            consume(Tokeniser.TokenType.TRPAR, node);
        } else {
            node.addChild(fnCall());
        }
        return node;
    }

    private Node fnCall() throws ParseException {
        // System.out.println("fncall hit");
        Node node = new Node("NFCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TLPAR, node);
        return node;
    }

    private Node prList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(printItem());
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPRLST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(prList());
        }
        return node;
    }

    private Node printItem() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TSTRG)) {
            node.setType("NSTRG");
            consume(Tokeniser.TokenType.TSTRG, node);
        } else {
            node.addChild(expr(true));
        }
        return node;
    }
}
