package CleanStart.Mk06.CodeBlock2;

public enum NumbersOfChildren
{
	Zero(0), One(1), Two(2);
	
	private final int value;

	private NumbersOfChildren(int inValue)
	{
		this.value = inValue;
	}

	public int getValue()
	{
		return this.value;
	}
}