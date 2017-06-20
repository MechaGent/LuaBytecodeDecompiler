package CleanStart.Mk04_Working;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;

public enum SpecialInstanceNums
{
	notAnInstanceNum,
	staticInstanceNum,
	tempInstanceNum,
	InstanceNum,;

	public int getValue()
	{
		final int result;

		switch (this)
		{
			case InstanceNum:
			{
				throw new IllegalArgumentException();
			}
			case notAnInstanceNum:
			{
				result = -1;
				break;
			}
			case staticInstanceNum:
			{
				result = -2;
				break;
			}
			case tempInstanceNum:
			{
				result = -3;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(this);
			}
		}

		return result;
	}

	public static SpecialInstanceNums parse(int instanceNum)
	{
		final SpecialInstanceNums result;

		switch (instanceNum)
		{
			case -1:
			{
				result = notAnInstanceNum;
				break;
			}
			case -2:
			{
				result = staticInstanceNum;
				break;
			}
			case -3:
			{
				result = tempInstanceNum;
				break;
			}
			default:
			{
				result = InstanceNum;
				break;
			}
		}

		return result;
	}
}
