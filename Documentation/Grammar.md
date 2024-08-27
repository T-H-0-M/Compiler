# CD24 Grammar

<!-- prettier-ignore-start -->
<program> ::= CD24 <id> <globals> <funcs> <mainbody>

<globals> ::= <consts> <types> <arrays>

<consts> ::= constants <initlist> | ε
<initlist> ::= <init> | <init> , <initlist>
<init> ::= <id> = <expr>

<types> ::= typedef <typelist> | ε
<typelist> ::= <type> <typelist> | <type>
<type> ::= <structid> def <fields> end
<type> ::= <typeid> def array [ <expr> ] of <structid> end
<fields> ::= <sdecl> | <sdecl> , <fields>

<arrays> ::= arraydef <arrdecls> | ε
<arrdecls> ::= <arrdecl> | <arrdecl> , <arrdecls>
<arrdecl> ::= <id> : <typeid>

<funcs> ::= <func> <funcs> | ε
<func> ::= func <id> ( <plist> ) : <rtype> <funcbody>
<rtype> ::= <stype> | void
<plist> ::= <params> | ε
<params> ::= <param> | <param> , <params>
<param> ::= <sdecl> | <arrdecl> | const <arrdecl>
<funcbody> ::= <locals> begin <stats> end

<locals> ::= <dlist> | ε
<dlist> ::= <decl> | <decl> , <dlist>
<decl> ::= <sdecl> | <arrdecl>

<mainbody> ::= main <slist> begin <stats> end CD24 <id>
<slist> ::= <sdecl> | <sdecl> , <slist>
<sdecl> ::= <id> : <stype> | <id> : <structid>
<stype> ::= int | float | bool

<stats> ::= <stat> ; <stats> | <strstat> <stats> | <stat>; | <strstat>
<strstat> ::= <forstat> | <ifstat> | <switchstat> | <dostat>
<stat> ::= <reptstat> | <asgnstat> | <iostat> | <callstat> | <returnstat>

<forstat> ::= for ( <asgnlist> ; <bool> ) <stats> end
<repstat> ::= repeat ( <asgnlist> ) <stats> until <bool>
<dostat> ::= do <stats> while ( <bool> ) end
<asgnlist> ::= <alist> | ε
<alist> ::= <asgnstat> | <asgnstat> , <alist>

<ifstat> ::= if ( <bool> ) <stats> end
<ifstat> ::= if ( <bool> ) <stats> else <stats> end
<ifstat> ::= if ( <bool> ) <stats> elif (<bool>) <stats> end

<switchstat> ::= switch ( <expr> ) begin <caselist> end
<caselist> ::= case <expr> : <stats> <caselist> | default : <stats>

<asgnstat> ::= <var> <asgnop> <bool>
<asgnop> ::= = | += | -= | *= | /=

<iostat> ::= input <vlist> | print <prlist> | printline <prlist>
<callstat> ::= <id> ( <elist> ) | <id> ( )
<returnstat> ::= return void | return <expr>

<vlist> ::= <var> , <vlist> | <var>
<var> ::= <id> | <id>[<expr>] | <id>[<expr>] . <id>
<elist> ::= <bool> , <elist> | <bool>

<bool> ::= not <bool> | <bool><logop> <rel> | <rel>
<rel> ::= <expr> <relop><expr> | <expr>
<logop> ::= and | or | xor
<relop> ::= == | != | > | <= | < | >=

<expr> ::= <expr> + <term> | <expr> - <term> | <term>
<term> ::= <term> * <fact> | <term> / <fact> | <term> % <fact> | <fact>
<fact> ::= <fact> ^ <exponent> | <exponent>
<exponent> ::= <var> | <intlit> | <reallit> | <fncall> | true | false
<exponent> ::= ( <bool> )

<fncall> ::= <id> ( <elist> ) | <id> ( )

<prlist> ::= <printitem> , <prlist> | <printitem>
<printitem> ::= <expr> | <string>
<!-- prettier-ignore-end -->
