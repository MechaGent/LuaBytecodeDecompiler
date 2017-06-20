package CleanStart.Mk04_Working.VarFormula;

public interface FormulaToken
{
	public boolean isOperator();
	public boolean isOperand();
	public boolean isConstant();
	public int getPrecedence();
	public String valueToString();
	public String valueToString(boolean showInferredTypes);
}
