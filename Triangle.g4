grammar Triangle ;

@header {
package in.zaraksh;
}

program : stmt_seq EOF ;

stmt_seq : stmt SEMICOLON? stmt_seq? ;
stmt : BEGIN stmt_seq END
     | LET decl_seq IN stmt
     | IF expr THEN stmt? ELSE (SEMICOLON | stmt?)                 // else is allowed to be not followed by a statement
     | WHILE expr DO stmt
     | LOOP stmt WHILE expr DO stmt
     | REPEAT stmt WHILE expr
     | REPEAT stmt UNTIL expr
     | ident BECOMES expr
     | expr ;

expr : INTLITERAL
     | CHARLITERAL
     | LBRACK array_seq? RBRACK
     | LBRACE field_seq? RBRACE
     | LPAREN expr RPAREN
     | LET decl_seq IN expr
     | IF expr THEN expr ELSE expr
     | ident
     | ident LPAREN arg_seq? RPAREN                 // procedure/function call
     | expr OPERATOR expr                           // binary operation
     | OPERATOR expr                                // unary operation
     | expr OPERATOR ;                              // unary operation

ident : IDENTIFIER ident_subscript* ;
ident_subscript : (DOT ident | LBRACK expr RBRACK) ;

type : IDENTIFIER
     | ARRAY INTLITERAL OF type
     | RECORD field_type_seq? END ;

arg_seq : arg (COMMA arg_seq)? ;
arg : expr
    | PROC IDENTIFIER
    | VAR ident
    | FUNC IDENTIFIER ;

array_seq : expr (COMMA array_seq)? ;

field_seq : field (COMMA field_seq)? ;
field : IDENTIFIER IS expr ;

field_type_seq : field_type (COMMA field_type_seq)? ;
field_type : IDENTIFIER COLON type ;

decl_seq : decl SEMICOLON? decl_seq? ;
decl : CONST IDENTIFIER IS expr
     | VAR IDENTIFIER COLON type
     | PROC IDENTIFIER LPAREN param_seq? RPAREN IS stmt
     | FUNC IDENTIFIER LPAREN param_seq? RPAREN COLON type IS expr
     | TYPE IDENTIFIER IS type ;

param_seq : param (COMMA param_seq)? ;
param : IDENTIFIER COLON type
      | VAR IDENTIFIER COLON type
      | PROC IDENTIFIER LPAREN param_seq? RPAREN
      | FUNC IDENTIFIER LPAREN param_seq? RPAREN COLON type ;

// Send whitespace to hidden channel instead of skipping because we need whitespace to split tokens apart at
WHITESPACE : [ \t\n\r\f]+ -> channel(HIDDEN) ;

// When '!' is encountered, keep skipping characters until a '\n' is encountered
BANG_COMMENT : '!' ~('\n')* '\n' -> skip;
// When '#' is encountered, keep skipping characters until a '\n' is encountered
HASH_COMMENT : '#' ~('\n')* '\n' -> skip;
// When '$' is encountered, keep skipping characters until a '$' is encountered
BLOCK_COMMENT : '$' ~('$')* '$' -> skip;

INTLITERAL : [0-9]+ ;
CHARLITERAL : '\'' . '\'' ;
OPERATOR : ('+'|'-'|'*'|'/'|'='|'<'|'>'|'\\'|'&'|'@'|'%'|'^'|'?'|'|')+ ;
DOT : '.' ;
BECOMES : ':=' ;
COLON : ':' ;
SEMICOLON : ';' ;
COMMA : ',' ;
IS : '~' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACK : '[' ;
RBRACK : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;

// RESERVED WORDS
ARRAY : 'array' ;
BEGIN : 'begin' ;
CONST : 'const' ;
DO : 'do' ;
ELSE : 'else' ;
END : 'end' ;
FUNC : 'func' ;
IF : 'if' ;
IN : 'in' ;
LET : 'let' ;
LOOP : 'loop' ;
OF : 'of' ;
PROC : 'proc' ;
RECORD : 'record' ;
REPEAT : 'repeat' ;
THEN : 'then' ;
TYPE : 'type' ;
UNTIL : 'until' ;
VAR : 'var' ;
WHILE : 'while' ;

// Identifiers must lex after reserved words
IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
