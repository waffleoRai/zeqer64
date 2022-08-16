package waffleoRai_zeqer64.cmml;

import java.util.List;

import waffleoRai_zeqer64.cmml.elements.CMMLElement;

public class CMMLStatement extends CMMLStatementGroup{
	
	public static class ParseResult{
		public CMMLStatementType statement_type;
		public CMMLElement head;
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
	
	private static CMMLElement parseElement(String element_str){
		//TODO
		return null;
	}
	
	public static ParseResult parseStatement(String statement){
		//TODO
		if(statement == null) return null;
		ParseResult res = new ParseResult();
		
		//Block out string literals.
		//TODO
		
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
			//TODO
			//Is it an else or else if? If else, can just set type and return.
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_IF);
		if(keyword_idx >= 0){
			//TODO
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_WHILE);
		if(keyword_idx >= 0){
			//TODO
		}
		
		keyword_idx = CMMLTextProcessing.findStandalone(statement, CMMLConstants.KEYWORD_FOR);
		if(keyword_idx >= 0){
			//TODO
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
