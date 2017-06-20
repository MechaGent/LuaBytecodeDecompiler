package CleanStart.Mk06.VarFormula;

public interface FormulaToken
{
	public boolean isOperator();
	public boolean isOperand();
	public boolean isConstant();
	public int getPrecedence();
	public abstract String getValueAsString();
}
