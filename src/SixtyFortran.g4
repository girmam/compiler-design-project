grammar SixtyFortran;

@header {
	package antlr4;
	import intermediate.type.*;
	import intermediate.scope.*;
	import backend.semantics.*;
	
	import java.util.LinkedList;
}

program : programBody ;

programBody 
	: statementList statement ;
	
statementList : (statement NEWLINE)* ;

statement 
	: typeDefinition
	| routineDefinition
	| ifStatement
	| loopStatement
	| assignmentStatement
	| varDeclaration
	| call
	| emptyStatement
	;

emptyStatement
	: NEWLINE*;
	
typeDefinition	locals [TypeSpec type = null, FileSpec file = null, Scope typeScope = null]
	: 'type' simpleTypeName ':' NEWLINE
		definitionBlock
	  END simpleTypeName
	;
	
definitionBlock: varBlock routineBlock ;

varBlock : ( (staticVarDefinition | varDeclaration) NEWLINE+)* ;

varDeclaration locals [LinkedList<ScopeEntry> entries = new LinkedList<>()]
	: typeName varName (',' varName)* ;

staticVarDefinition
	: STATIC varDeclaration '=' expression
	;
	
routineBlock : ((staticRoutineDefinition | routineDefinition) NEWLINE+)* ;
		
routineDefinition locals [Scope routineScope = null, RoutineSpec routine = null, FileSpec file = null, boolean isStatic = false]
	: 'def' routineName ('(' routineParameters? ')')? (RETURNS typeName)? ':' NEWLINE
			statementList 
			(RETURN expression NEWLINE+)?
	  END routineName
	;

staticRoutineDefinition
	: STATIC routineDefinition
	;
	
routineParameters
	: routineParameterList (',' NEWLINE* routineParameterList)* 
	;
	
routineParameterList
	: typeName varName (',' varName)* 
	;

varName : IDENTIFIER ;
routineName : IDENTIFIER ;

typeName locals [TypeSpec type = null]
	: simpleTypeName
	| routineTypeName
	//| genericTypeName 
	;

simpleTypeName
	: IDENTIFIER
	;
	
routineTypeName
	: ROUTINE (OF typeName+)? (RETURNS typeName)?
	;
	
genericTypeName
	: simpleTypeName OF typeName
	;

ifStatement:
    ifBlock // Must have 1: if(...)\n {...} END IF
    elseifBlock*  // Can have 0 or more: else if(...)\n {...} END ELSE IF
    elseBlock? // Can have 1 optional: else\n {...} END ELSE
    ;
ifBlock locals [Scope scope = null, FileSpec file = null]: // Each if block will have its own scope
    'if' expression ':' NEWLINE // "if(x == y)\n"
    statementList // {...}
    END IF // END IF
    ;
elseifBlock locals [Scope scope = null, FileSpec file = null]: // Each else if block will have its own scope
    'else if' expression ':' NEWLINE // "if(x == y)\n" 
    statementList // {...} 
    END IF ELSE // END IF ELSE
    ;
elseBlock locals [Scope scope = null, FileSpec file = null]: // Each else block will have its own scope
    'else' ':' NEWLINE // "else\n" 
    statementList // {...}
    END ELSE // END ELSE
    ;

	
loopStatement locals [Scope scope = null, FileSpec file = null]
	: 'while' expression ':' NEWLINE
   	statementList
 	END WHILE
       ;

assignmentStatement
	: assignNewVariables
	| assignExistingVariable
	;
	
assignNewVariables
	: varDeclaration '=' expression
	;
	
assignExistingVariable
	: call '=' expression
	;
	
expression locals [TypeSpec type = null]
	: comparison (boolOp expression)? ;
	
comparison locals [TypeSpec type = null]       
    : addition (relOp expression)? ;

addition locals [TypeSpec type = null]
	: multiplication (addOp expression)? ;
	
multiplication locals [TypeSpec type = null]
	: term (mulOp expression)? ;

term locals [TypeSpec type = null]              
    : (INTEGER | REAL)              #numTerm
    | (TRUE | FALSE)				#boolTerm
    | STRING       					#stringTerm
    | NOT term           			#notTerm
    | unaryMinus term				#minusTerm
    | call              			#callTerm
    | '(' expression ')' 			#parenTerm
    ;
 
call locals [ScopeEntry callEntry, TypeSpec type = null]
	: identifierCall
	| routineCall
	;
	
identifierCall locals [ScopeEntry callEntry, TypeSpec type = null]
	: IDENTIFIER indexModifier? ('.' call)?
	;
	
routineCall locals [ScopeEntry callEntry, TypeSpec type = null]
	: IDENTIFIER '(' argumentList? ')' indexModifier? ('.' call)?
	;

indexModifier
	: ('[' argumentList ']')+
	;
	
argumentList locals [ArgumentList args = null]
	: expression (',' NEWLINE* expression)* 
	;

relOp 
	: EQUALS 
	| NOT_EQUALS
	| LESS_THAN
	| LESS_EQUALS
	| GREATER_THAN
	| GREATER_EQUALS
	; 
	
addOp : PLUS | MINUS ;
boolOp : AND | OR ;
mulOp  : TIMES | DIV | MOD ;

unaryMinus : MINUS ;

IF : 'if' ;
ELSE_IF : 'else if' ;
ELSE : 'else' ;
WHILE : 'while';

END : 'end' ;
RETURNS : 'returns' ;
RETURN : 'return' ;
OF : 'of' ;
STATIC : 'static' ;

EQUALS : 'equals' | '==' ;	
IS : 'is' | '===' ;
NOT_EQUALS : 'not equals' | '!=' ;
LESS_THAN : 'less than' | '<' ;
LESS_EQUALS : 'less or equal to' | '<=' ;
GREATER_THAN : 'greater than' | '>' ;
GREATER_EQUALS : 'greater or equal to' | '>=' ;

PLUS : '+' ;
MINUS : '-' ;
AND : 'and' | '&&' ;
OR : 'or' | '||' ;
NOT : 'not' | '!' ;

TIMES : '*' ;
DIV : '/' ;
MOD : 'mod' | '%' ;

ROUTINE    : 'routine';

INTEGER    : [0-9]+ ;
REAL       : INTEGER '.' INTEGER
           | INTEGER ('e' | 'E') ('+' | '-')? INTEGER
           | INTEGER '.' INTEGER ('e' | 'E') ('+' | '-')? INTEGER
           ;

TRUE : 'true' ;
FALSE : 'false' ;

IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_-]* ;

SINGLE_QUOTE     : '\'' ;
DOUBLE_QUOTE	 : '"'  ;
STRING
	: SINGLE_QUOTE NOT_SINGLE_QUOTE* SINGLE_QUOTE 
	| DOUBLE_QUOTE NOT_DOUBLE_QUOTE* DOUBLE_QUOTE
	;

fragment NOT_SINGLE_QUOTE : ~('\'')   ;// any non-single-quote character
fragment NOT_DOUBLE_QUOTE : ~('"')    ;// any non-double-quote character
                        
           
NEWLINE : '\r'? '\n' ;
WS      : [ \t]+ -> skip ; 

COMMENT : '#' ~[\r\n]* -> skip ;
