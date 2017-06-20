package CleanStart.Mk06.Enums;

public enum RawEnums
{
	StaticVar(-1),
	WrapperVar(-2),
	NotSpecial(0);
	
	private final int value;

	private RawEnums(int inValue)
	{
		this.value = inValue;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
	public static RawEnums parse(int in)
	{
		final RawEnums result;
		
		switch(in)
		{
			case -1:
			{
				result = StaticVar;
				break;
			}
			case -2:
			{
				result = WrapperVar;
				break;
			}
			default:
			{
				result = NotSpecial;
				break;
			}
		}
		
		return result;
	}
}
