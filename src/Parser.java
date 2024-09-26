import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private Node rootNode;
    private OutputController outputController;

    public Parser() {
        this.scanner = null;
        this.currentToken = null;
        this.rootNode = null;
        this.outputController = null;
    }

    public Parser(Scanner scanner, OutputController outputController) {
        this.scanner = scanner;
        this.currentToken = scanner.nextToken();
        this.rootNode = null;
        this.outputController = outputController;
    }

    private Node consume(Tokeniser.TokenType expectedType, Node parentNode, Set<Tokeniser.TokenType> syncSet)
            throws ParseException {
        Node node = null;
        if (currentToken.getType() == expectedType) {
            Token consumedToken = currentToken;
            node = new Node(consumedToken.getType().toString(), consumedToken.getLexeme());
            if (parentNode != null && (consumedToken.getType() == Tokeniser.TokenType.TIDEN
                    || consumedToken.getType() == Tokeniser.TokenType.TILIT)) {
                parentNode.setValue(consumedToken.getLexeme());
            }
            currentToken = scanner.nextToken();
            return node;
        } else {
            outputController.addParseError(expectedType, currentToken, parentNode);
            node = new Node("NUNDEF", "");
            if (parentNode != null) {
                parentNode.addChild(node);
            }
            if (syncSet == null || syncSet.isEmpty()) {
                throw new ParseException("Fatal Error: Unable to synchronize.");
            } else {
                while (!syncSet.contains(currentToken.getType()) &&
                        currentToken.getType() != Tokeniser.TokenType.TTEOF) {
                    System.out.println("Skipping token " + currentToken.getType().toString());
                    currentToken = scanner.nextToken();
                }
                if (currentToken.getType() == Tokeniser.TokenType.TTEOF) {
                    throw new ParseException("Fatal Error: Reached EOF while synchronizing.");
                }
            }
        }
        return node;
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

        consume(Tokeniser.TokenType.TCD24, node, programSyncSet);
        consume(Tokeniser.TokenType.TIDEN, node, programSyncSet);
        node.addChild(globals(programSyncSet));
        node.addChild(funcs(programSyncSet));
        node.addChild(mainBody(programSyncSet));

        return node;
    }

    private Node globals(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NGLOB", "");
        syncSet.add(Tokeniser.TokenType.TFUNC);
        syncSet.add(Tokeniser.TokenType.TMAIN);
        node.addChild(consts(syncSet));
        node.addChild(types(syncSet));
        Node arraysNode = arrays(syncSet);
        if (!arraysNode.isSpecial()) {
            node.addChild(arrays(syncSet));
        }
        return node;
    }

    private Node consts(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Set<Tokeniser.TokenType> constsSyncSet = new HashSet<>(Arrays.asList(
                Tokeniser.TokenType.TTYPD,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TMAIN));

        if (match(Tokeniser.TokenType.TCONS)) {
            consume(Tokeniser.TokenType.TCONS, node, constsSyncSet);
            node = initList(constsSyncSet);
        }
        return node;
    }

    private Node initList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NILIST", "");

        Set<Tokeniser.TokenType> initListSyncSet = new HashSet<>(Arrays.asList(
                Tokeniser.TokenType.TCOMA,
                Tokeniser.TokenType.TTYPD,
                Tokeniser.TokenType.TARRD,
                Tokeniser.TokenType.TMAIN));

        node.addChild(init(initListSyncSet));
        while (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA, node, initListSyncSet);
            node.addChild(init(initListSyncSet));
        }
        return node;
    }

    private Node init(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NINIT", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        consume(Tokeniser.TokenType.TEQUL, node, syncSet);
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
        Node typeNode = type(syncSet);
        // node.addChild(type(syncSet));
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
        Node node = new Node("NRTYPE", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        consume(Tokeniser.TokenType.TTDEF, node, syncSet);
        if (match(Tokeniser.TokenType.TARAY)) {
            consume(Tokeniser.TokenType.TARAY, node, syncSet);
            consume(Tokeniser.TokenType.TLBRK, node, syncSet);
            node.addChild(expr(true, syncSet));
            consume(Tokeniser.TokenType.TRBRK, node, syncSet);
            consume(Tokeniser.TokenType.TTTOF, node, syncSet);
            consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        } else {
            node.addChild(fields(syncSet));
        }
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
        return node;
    }

    private Node fields(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        Node sDeclNode = sDecl(syncSet);
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NFLIST");
            node.addChild(sDeclNode);
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(fields(syncSet));
        } else {
            node = sDeclNode;
        }
        return node;
    }

    private Node arrays(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TARRD)) {
            consume(Tokeniser.TokenType.TARRD, node, syncSet);
            node = arrDecls(syncSet);
        }
        return node;
    }

    private Node arrDecls(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(arrDecl(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NALIST");
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(arrDecls(syncSet));
        }
        return node;
    }

    private Node arrDecl(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NARRD", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        consume(Tokeniser.TokenType.TCOLN, node, syncSet);
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
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
        // INFO: I did this to create a deep copy of the syncset, to avoid it impacting
        // other statements
        syncSet = new HashSet<>(syncSet);
        syncSet.addAll(Arrays.asList(
                Tokeniser.TokenType.TFUNC,
                Tokeniser.TokenType.TMAIN,
                Tokeniser.TokenType.TTEOF));
        Node node = new Node("NFUND", "");
        try {
            consume(Tokeniser.TokenType.TFUNC, node, syncSet);
            consume(Tokeniser.TokenType.TIDEN, node, syncSet);
            consume(Tokeniser.TokenType.TLPAR, node, syncSet);
            node.addChild(pList(syncSet));
            consume(Tokeniser.TokenType.TRPAR, node, syncSet);
            consume(Tokeniser.TokenType.TCOLN, node, syncSet);
            node.addChild(rType(syncSet));
            node.addChild(funcBody(syncSet));
        } catch (ParseException e) {
            // TODO: idk what do do with this yet
            System.out.println("ehhehehehe");
            System.err.println(e.getMessage());
        }
        return node;
    }

    private Node rType(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TVOID)) {
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
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(params(syncSet));
        }
        return node;
    }

    // TODO: find out what to do with array decl
    private Node param(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSIMP", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            node.setType("NARRC");
            consume(Tokeniser.TokenType.TCONS, node, syncSet);
            node.addChild(arrDecl(syncSet));
            arrDecl(syncSet);
        } else {
            node.addChild(sDecl(syncSet));
        }
        return node;
    }

    private Node funcBody(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(locals(syncSet));
        consume(Tokeniser.TokenType.TBEGN, node, syncSet);
        node.addChild(stats(syncSet));
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
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
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(dList(syncSet));
        }
        return node;
    }

    // TODO: Complete this, the grammar is currently ambiguous
    private Node decl(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        return new Node("SPECIAL", "");
    }

    private Node mainBody(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NMAIN", "");

        consume(Tokeniser.TokenType.TMAIN, node, syncSet);
        node.addChild(sList(syncSet));
        consume(Tokeniser.TokenType.TBEGN, node, syncSet);
        node.addChild(stats(syncSet));
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
        consume(Tokeniser.TokenType.TCD24, node, syncSet);
        // INFO: pass node null as is at the end of the program (otherwise main node
        // will be name set to the identifier)
        consume(Tokeniser.TokenType.TIDEN, null, syncSet);
        return node;
    }

    private Node sList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl(syncSet));
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NSDLST");
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(sList(syncSet));
        }
        return node;
    }

    private Node sDecl(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSDECL", "");
        consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        consume(Tokeniser.TokenType.TCOLN, node, syncSet);
        if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN, node, syncSet);
        } else {
            node.addChild(sType(syncSet));
        }
        return node;
    }

    private Node sType(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TINTG)) {
            consume(Tokeniser.TokenType.TINTG, node, syncSet);
        } else if (match(Tokeniser.TokenType.TFLOT)) {
            consume(Tokeniser.TokenType.TFLOT, node, syncSet);
        } else {
            consume(Tokeniser.TokenType.TBOOL, node, syncSet);
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
            consume(Tokeniser.TokenType.TSEMI, node, syncSet);
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
            node.addChild(forStat(syncSet));
        } else if (match(Tokeniser.TokenType.TIFTH)) {
            node.addChild(ifStat(syncSet));
        } else if (match(Tokeniser.TokenType.TSWTH)) {
            node.addChild(switchStat(syncSet));
        } else {
            node.addChild(doStat(syncSet));
        }
        return node;
    }

    // TODO: Check this
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
            // TODO: fix this with symbol table - asgnstat and callstatt both start with
            // TIDEN
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(asgnStat(syncSet));
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(callStat(syncSet));
        } else if (match(Tokeniser.TokenType.TINPT) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.addChild(ioStat(syncSet));
        } else if (match(Tokeniser.TokenType.TRETN)) {
            node.addChild(returnStat(syncSet));
        }
        return node;
    }

    private Node forStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        // TODO: increase scope
        Node node = new Node("NFOR", "");
        consume(Tokeniser.TokenType.TTFOR, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        node.addChild(asgnList(syncSet));
        consume(Tokeniser.TokenType.TSEMI, node, syncSet);
        node.addChild(bool(syncSet));
        consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        node.addChild(stats(syncSet));
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
        // TODO: decrease scope
        return node;
    }

    private Node repStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NREPT", "");
        consume(Tokeniser.TokenType.TREPT, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        node.addChild(asgnList(syncSet));
        consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        node.addChild(stats(syncSet));
        consume(Tokeniser.TokenType.TUNTL, node, null);
        node.addChild(bool(syncSet));
        return node;
    }

    private Node doStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NDOWL", "");
        consume(Tokeniser.TokenType.TTTDO, node, syncSet);
        node.addChild(stats(syncSet));
        consume(Tokeniser.TokenType.TWHIL, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        node.addChild(bool(syncSet));
        consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
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
            consume(Tokeniser.TokenType.TCOMA, node, syncSet);
            node.addChild(aList(syncSet));
        }
        return node;
    }

    private Node ifStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NIFITH", "");
        consume(Tokeniser.TokenType.TIFTH, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        node.addChild(bool(syncSet));
        consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        node.addChild(stats(syncSet));
        if (match(Tokeniser.TokenType.TELSE)) {
            node.setType("NIFTE");
            consume(Tokeniser.TokenType.TELSE, node, syncSet);
            node.addChild(stats(syncSet));
        } else if (match(Tokeniser.TokenType.TELIF)) {
            node.setType("NIFEF");
            consume(Tokeniser.TokenType.TLPAR, node, syncSet);
            node.addChild(bool(syncSet));
            consume(Tokeniser.TokenType.TRPAR, node, syncSet);
            node.addChild(stats(syncSet));
        }
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
        return node;
    }

    private Node switchStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NSWTCH", "");
        consume(Tokeniser.TokenType.TSWTH, node, syncSet);
        consume(Tokeniser.TokenType.TLPAR, node, syncSet);
        node.addChild(expr(true, syncSet));
        consume(Tokeniser.TokenType.TRPAR, node, syncSet);
        consume(Tokeniser.TokenType.TBEGN, node, syncSet);
        node.addChild(caseList(syncSet));
        consume(Tokeniser.TokenType.TTEND, node, syncSet);
        return node;
    }

    private Node caseList(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("NCASLT", "");
        if (match(Tokeniser.TokenType.TCASE)) {
            consume(Tokeniser.TokenType.TCASE, node, syncSet);
            node.addChild(expr(true, syncSet));
            consume(Tokeniser.TokenType.TCOLN, node, syncSet);
            node.addChild(stats(syncSet));
            consume(Tokeniser.TokenType.TBREK, node, syncSet);
            consume(Tokeniser.TokenType.TSEMI, node, syncSet);
            node.addChild(caseList(syncSet));
        } else {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TDFLT, node, syncSet);
            consume(Tokeniser.TokenType.TCOLN, node, syncSet);
            node.addChild(stats(syncSet));
        }
        return node;
    }

    private Node asgnStat(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node tempNode = var(syncSet);
        Node node = asgnOp(syncSet);
        node.addChild(tempNode);
        node.addChild(bool(syncSet));
        return node;
    }

    private Node asgnOp(Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TPLEQ)) {
            node.setType("NPLEQ");
            consume(Tokeniser.TokenType.TPLEQ, node, syncSet);
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            node.setType("NMNEQ");
            consume(Tokeniser.TokenType.TMNEQ, node, syncSet);
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            node.setType("NSTEA");
            consume(Tokeniser.TokenType.TSTEQ, node, syncSet);
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            node.setType("NDVEQ");
            consume(Tokeniser.TokenType.TDVEQ, node, syncSet);
        } else {
            node.setType("NASGN");
            consume(Tokeniser.TokenType.TEQUL, node, syncSet);
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
        if (match(Tokeniser.TokenType.TNOTT)) {
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
            node.addChild(rel(syncSet));

            if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR)
                    || match(Tokeniser.TokenType.TTXOR)) {
                node.setType("NBOOL");
                node.addChild(logOp(syncSet));
                node.addChild(rel(syncSet));
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
        node.addChild(expr(true, syncSet));
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            node.addChild(relOp(syncSet));
            node.addChild(expr(true, syncSet));
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
        if (termNeeded) {
            node.addChild(term(true, syncSet));
        }

        if (match(Tokeniser.TokenType.TPLUS)) {
            node.setType("NADD");
            consume(Tokeniser.TokenType.TPLUS, node, syncSet);
            node.addChild(term(true, syncSet));
            node.addChild(expr(false, syncSet));
        } else if (match(Tokeniser.TokenType.TMINS)) {
            node.setType("NSUB");
            consume(Tokeniser.TokenType.TMINS, node, syncSet);
            node.addChild(term(true, syncSet));
            node.addChild(expr(false, syncSet));
        }
        return node;
    }

    private Node term(Boolean factNeeded, Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (factNeeded) {
            node.addChild(fact(true, syncSet));
        }
        if (match(Tokeniser.TokenType.TSTAR)) {
            node.setType("NMUL");
            consume(Tokeniser.TokenType.TSTAR, node, syncSet);
            node.addChild(fact(true, syncSet));
            node.addChild(term(false, syncSet));
        } else if (match(Tokeniser.TokenType.TDIVD)) {
            node.setType("NDIV");
            consume(Tokeniser.TokenType.TDIVD, node, syncSet);
            node.addChild(fact(true, syncSet));
            node.addChild(term(false, syncSet));
        } else if (match(Tokeniser.TokenType.TPERC)) {
            node.setType("NMOD");
            consume(Tokeniser.TokenType.TPERC, node, null);
            node.addChild(fact(true, syncSet));
            node.addChild(term(false, syncSet));
        }
        // ε
        return node;
    }

    private Node fact(Boolean exponentNeeded, Set<Tokeniser.TokenType> syncSet) throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (exponentNeeded) {
            node.addChild(exponent(syncSet));
        }
        if (match(Tokeniser.TokenType.TCART)) {
            node.setType("NPOW");
            consume(Tokeniser.TokenType.TCART, node, syncSet);
            node.addChild(exponent(syncSet));
            node.addChild(fact(false, syncSet));
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
            node.setType("SPECIAL");
            node.addChild(var(syncSet));
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
            node.addChild(fnCall(syncSet));
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
}
