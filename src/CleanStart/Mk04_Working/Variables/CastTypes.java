package CleanStart.Mk04_Working.Variables;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;

public enum CastTypes
{
	Legal,
	
	/**
	 * use this to temporarily allow illegal casts through, for diagnosis
	 */
	ShakilyLegal,
	Illegal,
	Unnecessary,
	Coerce,
	wasCoercedTo,
	isString,
	;
	
	public static CastTypes parse(String in)
	{
		final CastTypes result;
		
		switch(in.toLowerCase())
		{
			case "legal":
			{
				result = Legal;
				break;
			}
			case "shakilylegal":
			{
				result = ShakilyLegal;
				break;
			}
			case "illegal":
			{
				result = Illegal;
				break;
			}
			case "isstring":
			{
				result = isString;
				break;
			}
			case "unnecessary":
			{
				result = Unnecessary;
				break;
			}
			case "coerce":
			{
				result = Coerce;
				break;
			}
			case "wascoercedto":
			{
				result = wasCoercedTo;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in);
			}
		}
		
		return result;
	}
}
