package CleanStart.Mk06;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk06.Enums.OpCodes;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class MiscAutobot
{
	public static enum ParseTypes
	{
		iA,
		iABC,
		iAB,
		iAC,
		iABx,
		iBx,
		iAsBx,
		isBx;
	}

	public static int[] parseRawInstruction(int inRaw, ParseTypes desiredMethod)
	{
		final int[] result;

		switch (desiredMethod)
		{
			case iA:
			{
				result = parseAs_iABC(inRaw, 1);
				break;
			}
			case iAB:
			{
				result = parseAs_iABC(inRaw, 2);
				break;
			}
			case iABC:
			{
				result = parseAs_iABC(inRaw, 3);
				break;
			}
			case iABx:
			{
				result = parseAs_iABx(inRaw, 2);
				break;
			}
			case iAC:
			{
				result = new int[2];

				result[0] = ((inRaw >>> 6) & 0xff);
				result[1] = (inRaw >>> 14) & 0x1ff;

				break;
			}
			case iAsBx:
			{
				result = parseAs_iAsBx(inRaw, 2);
				break;
			}
			case iBx:
			{
				result = new int[1];

				result[0] = ((inRaw >>> 14) & 0x3ffff);
				break;
			}
			case isBx:
			{
				result = new int[1];

				result[0] = ((inRaw >>> 14) & 0x3ffff) - 131071;
				//System.out.println("isBx[0] parsed as " + result[0]);
				break;
			}
			default:
			{
				throw new UnhandledEnumException(desiredMethod);
			}
		}

		return result;
	}

	private static int[] parseAs_iABC(int inRaw, int numFieldsDesired)
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

	private static int[] parseAs_iABx(int inRaw, int numFieldsDesired)
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

	private static int[] parseAs_iAsBx(int inRaw, int numFieldsDesired)
	{
		final int[] result = new int[numFieldsDesired];

		switch (numFieldsDesired)
		{
			case 2:
			{
				/*
				 * this funkiness ensures sign is preserved
				 */
				// final byte cast = (byte) ((inRaw >>> 14) & 0x3ffff);

				result[1] = ((inRaw >>> 14) & 0x3ffff) - 131071;
				
				//System.out.println("iAsBx[1] parsed as " + result[1]);
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
	
	public static CharList generatedSpacedCharList(String core, int minChars, boolean addToFront)
	{
		final CharList result = new CharList();
		
		result.add(core);
		final int dif = minChars - core.length();
		
		if(dif > 0)
		{
			if(addToFront)
			{
				result.push(' ', dif);
			}
			else
			{
				result.add(' ', dif);
			}
		}
		
		return result;
	}
	
	public static CharList prettyPrintVarRefArray(VarRef[] in, int offset, int numElementsPerRow, StepNum time)
	{
		final CharList result = new CharList();
		int counter = 0;
		result.add('\t', offset);
		
		for(int i = 0; i < in.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			if(counter == numElementsPerRow)
			{
				result.addNewLine();
				result.add('\t', offset);
				counter = 0;
			}
			
			result.add(in[i].getValueAtTime(time).getValueAsString());
		}
		
		return result;
	}
	
	public static CharList prettyPrintVarRefMap(HalfByteRadixMap<VarRef> in, int offset, int numElementsPerRow)
	{
		final CharList result = new CharList();
		boolean isFirst = true;
		int counter = 0;
		result.add('\t', offset);
		
		for(Entry<HalfByteArray, VarRef> entry: in)
		{
			if(isFirst)
			{
				isFirst = false;
			}
			else
			{
				result.add(", ");
			}
			
			if(counter == numElementsPerRow)
			{
				counter = 0;
				result.addNewLine();
				result.add('\t', offset);
			}
			
			result.add(entry.getValue().getMangledName());
		}
		
		return result;
	}
}
