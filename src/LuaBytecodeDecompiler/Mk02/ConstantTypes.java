package LuaBytecodeDecompiler.Mk02;

public enum ConstantTypes
{
	Unknown,
	Null,
	Bool,
	Num,
	String;
	
	public static ConstantTypes parse(int flag)
	{
		final ConstantTypes result;

		switch (flag)
		{
			case 0:
			{
				result = Null;
				break;
			}
			case 1:
			{
				result = Bool;
				break;
			}
			case 3:
			{
				result = Num;
				break;
			}
			case 4:
			{
				result = String;
				break;
			}
			/*
			case 0x40:
			{
				result = Unknown_length9;
				break;
			}
			case 0x7:
			{
				result = Unknown_length8;
				break;
			}
			*/
			default:
			{
				//throw new IllegalArgumentException("Unhandled ConstantFlag: " + flag);
				result = Unknown;
				System.out.println("unknown ConstantFlag: " + Integer.toHexString(flag));
				break;
			}
		}

		return result;
	}
}
