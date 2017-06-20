package CleanStart.Mk03;

public enum ComponentTypes
{
	Op_Assign(true, false);
	
	private final boolean isOperator;
	private final boolean isOperand;
	
	private ComponentTypes(boolean inIsOperator, boolean inIsOperand)
	{
		this.isOperator = inIsOperator;
		this.isOperand = inIsOperand;
	}
}
