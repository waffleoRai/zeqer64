package waffleoRai_zeqer64.cmml;

public enum CMMLStatementType {

	//Conditionals
	IF,
	ELSE,
	ELIF,
	WHILE,
	DO_WHILE,
	FOR,
	WHILE_TRUE,
	DO_WHILE_TRUE,
	IF_TRUE,
	
	//Blocks
	FUNC_DECL,
	FUNC_DEF,
	GOTO,
	GOTO_BLOCK,
	DATA_DECL,
	DATA_DEF,
	
	//C-like
	TYPEDEF,
	RETURN,
	BREAK,
	CONTINUE,
	LOCALVAR_DECL, //May also assign
	LOCALVAR_ASSIGN,
	LOCALVAR_READ, //Complex statements with multiple steps get broken up into sub-statements during statement assessment
	C_STATEMENT, //misc
	
	//Direct MML
	MML_LITERAL,
	;
	
}
