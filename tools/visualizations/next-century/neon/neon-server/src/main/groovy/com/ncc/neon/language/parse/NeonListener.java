// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.language.parse;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface NeonListener extends ParseTreeListener {
	void enterLimit(NeonParser.LimitContext ctx);
	void exitLimit(NeonParser.LimitContext ctx);

	void enterWhereClause(NeonParser.WhereClauseContext ctx);
	void exitWhereClause(NeonParser.WhereClauseContext ctx);

	void enterSort(NeonParser.SortContext ctx);
	void exitSort(NeonParser.SortContext ctx);

	void enterQuery(NeonParser.QueryContext ctx);
	void exitQuery(NeonParser.QueryContext ctx);

	void enterSortClause(NeonParser.SortClauseContext ctx);
	void exitSortClause(NeonParser.SortClauseContext ctx);

	void enterWhere(NeonParser.WhereContext ctx);
	void exitWhere(NeonParser.WhereContext ctx);

	void enterDatabase(NeonParser.DatabaseContext ctx);
	void exitDatabase(NeonParser.DatabaseContext ctx);

	void enterOperator(NeonParser.OperatorContext ctx);
	void exitOperator(NeonParser.OperatorContext ctx);

	void enterSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx);
	void exitSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx);

	void enterFunction(NeonParser.FunctionContext ctx);
	void exitFunction(NeonParser.FunctionContext ctx);

	void enterStatement(NeonParser.StatementContext ctx);
	void exitStatement(NeonParser.StatementContext ctx);

	void enterGroupClause(NeonParser.GroupClauseContext ctx);
	void exitGroupClause(NeonParser.GroupClauseContext ctx);

	void enterGroup(NeonParser.GroupContext ctx);
	void exitGroup(NeonParser.GroupContext ctx);

	void enterFunctionName(NeonParser.FunctionNameContext ctx);
	void exitFunctionName(NeonParser.FunctionNameContext ctx);
}