package waffleoRai_zeqer64.cmml;

import java.util.List;

import waffleoRai_zeqer64.cmml.CMMLCompiler.CMMLParseException;
import waffleoRai_zeqer64.cmml.elements.CMMLElement;
import waffleoRai_zeqer64.cmml.elements.CMMLLiteralBool;

public class CMMLStatement extends CMMLStatementGroup{
	
	public static class ParseResult{
		public CMMLStatementType statement_type;
		public CMMLElement head;
		
		//For oneline blocks
		public CMMLElement inner_statement;
		
	}
	
	public CMMLStatement(CMMLStatementGroup parent_group){
		super.setParent(parent_group);
	}
	
	public boolean isGroup(){return false;}
	public boolean expectsPost(){return false;}
	
	protected List<CMMLStatementGroup> evaluate(){
		//TODO
		return null;
	}
	
	private static CMMLElement parseMMLLiteral(String element_str){
		//TODO
		return null;
	}
	
	private static CMMLElement parseElement(StatementNode root){
		return parseElement(root, null);
	}
	
	private static CMMLElement parseElement(StatementNode root, String[][] strlit){
		//TODO
		return null;
	}
	
	private static void parseConditional(ParseResult res, StatementNode rootnode, String[][] strlit){
		//if, else if, while
		//Also look for if true, elif true, and while true
	
		//Parse the condition
		StatementNode cond = rootnode.getChild(0);
		res.head = parseElement(cond, strlit);
		if(res.head instanceof CMMLLiteralBool){
			CMMLLiteralBool litbool = (CMMLLiteralBool)res.head;
			if(litbool.getValue()){
				switch(res.statement_type){
				case IF: res.statement_type = CMMLStatementType.IF_TRUE; break;
				case ELIF: res.statement_type = CMMLStatementType.ELIF_TRUE; break;
				case WHILE: res.statement_type = CMMLStatementType.WHILE_TRUE; break;
				case DO_WHILE: res.statement_type = CMMLStatementType.DO_WHILE_TRUE; break;
				default: break;
				}
			}
			else{
				switch(res.statement_type){
				case IF: res.statement_type = CMMLStatementType.IF_FALSE; break;
				case ELIF: res.statement_type = CMMLStatementType.ELIF_FALSE; break;
				case WHILE: res.statement_type = CMMLStatementType.WHILE_FALSE; break;
				case DO_WHILE: res.statement_type = CMMLStatementType.DO_WHILE_FALSE; break;
				default: break;
				}
			}
		}
		
		//Parse the predicate, if present (ie. if one line block)
		if(rootnode.contentEntryCount() > 1 || rootnode.childCount() > 1){
			//Anything after first child is predicate...
			rootnode.trimFromBeginning(2);
			res.inner_statement = parseElement(rootnode, strlit);
		}
	}
	
	public static ParseResult parseStatement(String statement) throws CMMLParseException{
		//TODO
		if(statement == null) return null;
		ParseResult res = new ParseResult();
		
		//Block out string literals.
		//TODO
		String[][] slscan = CMMLTextProcessing.extractStringLiterals(statement);
		if(slscan != null){
			statement = slscan[0][1];
		}
		
		//Check if MML literal.
		if(statement.charAt(0) == '$'){
			res.statement_type = CMMLStatementType.MML_LITERAL;
			res.head = parseMMLLiteral(statement);
			return res;
		}
		
		//Look for keywords.
		int keyword_idx = -1;
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_BREAK);
		if(keyword_idx >= 0){
			res.statement_type = CMMLStatementType.BREAK;
			return res;
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_CONTINUE);
		if(keyword_idx >= 0){
			res.statement_type = CMMLStatementType.CONTINUE;
			return res;
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_ELSE);
		if(keyword_idx >= 0){
			//Is it an else or else if? If else, can just set type and return.
			keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_ELSEIF);
			if(keyword_idx >= 0){
				res.statement_type = CMMLStatementType.ELIF;
				StatementNode root = CMMLTextProcessing.parsePhraseHierarchy(statement);
				if(root == null || root.childCount() < 1) throw new CMMLParseException("\"else if\" statement requires condition.");
				parseConditional(res, root, slscan);
			}
			else{
				res.statement_type = CMMLStatementType.ELSE;
				return res;
			}
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_IF);
		if(keyword_idx >= 0){
			res.statement_type = CMMLStatementType.IF;
			StatementNode root = CMMLTextProcessing.parsePhraseHierarchy(statement);
			if(root == null || root.childCount() < 1) throw new CMMLParseException("\"if\" statement requires condition.");
			parseConditional(res, root, slscan);
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_WHILE);
		if(keyword_idx >= 0){
			res.statement_type = CMMLStatementType.WHILE;
			StatementNode root = CMMLTextProcessing.parsePhraseHierarchy(statement);
			if(root == null || root.childCount() < 1) throw new CMMLParseException("\"while\" statement requires condition.");
			parseConditional(res, root, slscan);
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_FOR);
		if(keyword_idx >= 0){
			//TODO
			//TODO These may not be getting split together since condition contains ; 
			// ... May need to update preprocessor's statement splitter?
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_SWITCH);
		if(keyword_idx >= 0){
			//TODO
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_CASE);
		if(keyword_idx >= 0){
			//TODO
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_DEFAULT);
		if(keyword_idx >= 0){
			//TODO
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_GOTO);
		if(keyword_idx >= 0){
			//TODO
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_RETURN);
		if(keyword_idx >= 0){
			//TODO
		}
		
		
		//Look for operators.
		
		//See other misc - function decl, def, or call?
		
		return null;
	}
	
	public static ParseResult parseStatement(String pre, String post){
		//TODO
		return null;
	}

}
