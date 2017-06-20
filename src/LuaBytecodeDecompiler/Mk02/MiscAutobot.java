package LuaBytecodeDecompiler.Mk02;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk02.Constant.StringConstant;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class MiscAutobot
{
	public static final String GlobalVarsPrefix = "GlobalVariables.";

	public static final String GlobalMethodsPrefix = "GlobalMethods.";

	/**
	 * lengthOf(&ltdataType&gt in): if it's a string, returns string length; if it's a table, returns table length; otherwise, calls 'the metamethod'
	 */
	public static final String GlobalMethods_lengthOf = GlobalMethodsPrefix + "lengthOf(";

	/**
	 * concat(String ... in): meant for String concatenation, but didn't want to use '+'s so as to avoid patternSkimming confusion
	 */
	public static final String GlobalMethods_Concatenate = GlobalMethodsPrefix + "concat(";

	/**
	 * 0 == false, otherwise true
	 */
	public static final String GlobalMethods_toBool = GlobalMethodsPrefix + "toBool(";

	/**
	 * takes params: int length, int hashSize
	 */
	public static final String GlobalMethods_createLuaTable = GlobalMethodsPrefix + "createLuaTable(";

	public static final String GlobalMethods_createClosure = GlobalMethodsPrefix + "createNewClosure(";

	/**
	 * parameter is a varArg, all args are closed
	 */
	public static final String GlobalMethods_close = GlobalMethodsPrefix + "close(";

	public static final String TableMethods_LookupVar = ".get(";

	public static final String programCounter = "Internal.ProgramCounter";

	public static final String prependGlobalVarsPrefix(String in)
	{
		return GlobalVarsPrefix + in;
	}

	public static final void prettify_TableGetMethod(CharList result, String tableName, String indexName)
	{
		result.add(tableName);
		result.add(MiscAutobot.TableMethods_LookupVar);

		result.add(indexName);
		result.add(')');
	}

	/**
	 * 
	 * @param in
	 * @param sizeTLength
	 * @param lengthSigEnd
	 * @param charsSigEnd
	 * @return the string, if possible; null otherwise
	 */
	public static final String parseNextLuaStringDefensively(BytesStreamer in, int sizeTLength, EndianSettings lengthSigEnd, EndianSettings charsSigEnd)
	{
		if (in.canSafelyConsume(sizeTLength))
		{
			final int length;

			switch (sizeTLength)
			{
				case 1:
				{
					length = in.getNextByte();
					break;
				}
				case 2:
				{
					length = in.getNextShort();
					break;
				}
				case 4:
				{
					length = in.getNextInt();
					break;
				}
				case 8:
				{
					final long test = in.getNextLong();
					length = (int) test;
					// System.out.println("result: " + (test == length));
					break;
				}
				default:
				{
					throw new IllegalArgumentException("need to write new case for size: " + sizeTLength);
				}
			}

			if (in.canSafelyConsume(length))
			{
				if (length == 0)
				{
					return "";
				}
				else
				{
					final String next = new String(in.getNextCharArr(length - 1, EndianSettings.LeftBitLeftByte));
					in.getNextByte();
					return next;
				}
			}
		}

		return null;
	}

	public static final String parseNextLuaString(BytesStreamer in, int size_tLength, EndianSettings lengthSigEnd, EndianSettings charsSigEnd)
	{
		final int length;

		switch (size_tLength)
		{
			case 1:
			{
				length = in.getNextByte(lengthSigEnd);
				break;
			}
			case 2:
			{
				length = in.getNextShort(lengthSigEnd);
				break;
			}
			case 4:
			{
				length = in.getNextInt(lengthSigEnd);
				break;
			}
			case 8:
			{
				final long test = in.getNextLong(lengthSigEnd);
				length = (int) test;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("need to write new case for size: " + size_tLength);
			}
		}

		if (length != 0)
		{
			final char[] result = in.getNextCharArr(length - 1, charsSigEnd);
			in.getNextByte(); // discard the terminating nullChar
			final String out = new String(result);
			// System.out.println(out);
			return out;
		}
		else
		{
			return "";
		}
	}

	public static final String[] parseNextLuaStringArr(BytesStreamer in, int size_tLength, EndianSettings lengthSigEnd, EndianSettings charsSigEnd)
	{
		final int length = in.getNextInt();
		return parseNextLuaStringArr(in, size_tLength, lengthSigEnd, charsSigEnd, length);
	}

	public static final String[] parseNextLuaStringArr(BytesStreamer in, int size_tLength, EndianSettings lengthSigEnd, EndianSettings charsSigEnd, int length)
	{
		final String[] result = new String[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = parseNextLuaStringDefensively(in, size_tLength, lengthSigEnd, charsSigEnd);
		}

		return result;
	}

	public static final void prepend(String append, CharList result, char delim, int minOffset)
	{
		final int dif = minOffset - append.length();

		if (dif > 0)
		{
			result.add(delim, dif);
		}

		result.add(append);
	}

	public static final void append(String append, CharList result, char delim, int minOffset)
	{
		final int dif = minOffset - append.length();

		result.add(append);

		if (dif > 0)
		{
			result.add(delim, dif);
		}
	}

	public static double parseAsFloatingPointByte(int in)
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
}
