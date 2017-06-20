package CleanStart.Mk01.VarFormula;

import CleanStart.Mk01.StepStage;

public interface FormulaToken
{
	public boolean isOperator();
	public boolean isOperand();
	public boolean isConstant();
	public int getPrecedence();
	public String valueToString();
	public String valueToStringAtTime(StepStage time);
}
