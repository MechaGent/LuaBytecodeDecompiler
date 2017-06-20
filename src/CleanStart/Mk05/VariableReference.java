package CleanStart.Mk05;

public interface VariableReference
{
	public String getVariableName();
	public Variable getValue();
	public void assignValue(Variable value);
}
