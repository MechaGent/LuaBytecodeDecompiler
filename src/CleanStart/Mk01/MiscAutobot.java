package CleanStart.Mk01;

import CharList.CharList;
import CleanStart.Mk01.Enums.OpCodes;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class MiscAutobot
{
	public static int[] parseAs_iABC(int inRaw, int numFieldsDesired)
	{
		final int[] result = new int[numFieldsDesired];

		switch (numFieldsDesired)
		{
			case 3:
			{
				result[2] = (inRaw >>> 14) & 0x1ff;
			}
			case 2:
			{
				result[1] = (inRaw >>> 23) & 0x1ff;
			}
			case 1:
			{
				result[0] = ((inRaw >>> 6) & 0xff);
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}

		return result;
	}

	public static int[] parseAs_iABx(int inRaw, int numFieldsDesired)
	{
		final int[] result = new int[numFieldsDesired];

		switch (numFieldsDesired)
		{
			case 2:
			{
				result[1] = ((inRaw >>> 14) & 0x3ffff);
			}
			case 1:
			{
				result[0] = ((inRaw >>> 6) & 0xff);
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}

		return result;
	}

	public static int[] parseAs_iAsBx(int inRaw, int numFieldsDesired)
	{
		final int[] result = new int[numFieldsDesired];

		switch (numFieldsDesired)
		{
			case 2:
			{
				/*
				 * this funkiness ensures sign is preserved
				 */
				//final byte cast = (byte) ((inRaw >>> 14) & 0x3ffff);
				
				result[1] = ((inRaw >>> 14) & 0x3ffff) - 131071;
			}
			case 1:
			{
				result[0] = ((inRaw >>> 6) & 0xff);
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}

		return result;
	}

	public static double parseAsFloatingByte(int in)
	{
		final int mantissa = in & 0b00000111;
		final int exponent = (in >>> 3) & 0b000111111;

		if (exponent == 0)
		{
			return mantissa;
		}
		else
		{
			return ((double) (mantissa & 0b1111)) * (1 << (exponent - 1));
		}
	}

	public static String parseNextLuaString(BytesStreamer in, int size_t_byteLength)
	{
		final int stringLength;
		final String result;

		switch (size_t_byteLength)
		{
			case 1:
			{
				stringLength = in.getNextByte() - 1;
				break;
			}
			case 2:
			{
				stringLength = in.getNextShort() - 1;
				break;
			}
			case 4:
			{
				stringLength = in.getNextInt() - 1;
				break;
			}
			case 8:
			{
				final long test = in.getNextLong() - 1;
				final int cast = (int) test;

				if (cast != test)
				{
					throw new IllegalArgumentException();
				}

				stringLength = cast;

				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException(size_t_byteLength + "");
			}
		}

		if (stringLength != -1)
		{
			if (in.canSafelyConsume(stringLength + 1))
			{
				result = new String(in.getNextCharArr(stringLength, EndianSettings.LeftBitLeftByte));
				in.getNextByte();
			}
			else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		else
		{
			return "";
		}

		return result;
	}

	public static CharList rawInstructionToCharList(int raw)
	{
		final CharList result = new CharList();
		final OpCodes type = OpCodes.parse(raw & 0x3f);
		result.add(type.toString());
		result.add('\t');

		switch (type.getRawInstructionType())
		{
			case iABC:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(raw, 3);
				result.add(Integer.toString(fields[0]));
				result.add('\t');
				result.add(Integer.toString(fields[1]));
				result.add('\t');
				result.add(Integer.toString(fields[2]));
				break;
			}
			case iABx:
			{
				final int[] fields = MiscAutobot.parseAs_iABx(raw, 2);
				result.add(Integer.toString(fields[0]));
				result.add('\t');
				result.add(Integer.toString(fields[1]));
				break;
			}
			case iAsBx:
			{
				final int[] fields = MiscAutobot.parseAs_iAsBx(raw, 2);
				result.add(Integer.toString(fields[0]));
				result.add('\t');
				result.add(Integer.toString(fields[1]));
				break;
			}
			default:
			{
				throw new UnhandledEnumException(type);
			}
		}

		return result;
	}
}
