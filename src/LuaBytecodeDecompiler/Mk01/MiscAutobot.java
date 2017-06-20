package LuaBytecodeDecompiler.Mk01;

import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class MiscAutobot
{
	public static String parseNextLuaString(BytesStreamer in, int sizeTLength)
	{
		final int length;
		
		switch(sizeTLength)
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
				//System.out.println("result: " + (test == length));
				break;
			}
			default:
			{
				throw new IllegalArgumentException("need to write new case for size: " + sizeTLength);
			}
		}
		
		//System.out.println("Length: " + Integer.toHexString(length) + " at pos: " + Integer.toHexString(in.getPosition()));
		
		final String result;
		
		if(length > 0)
		{
			result = new String(in.getNextCharArr(length - 1, EndianSettings.LeftBitLeftByte));
			//System.out.println("resulting string: " + result);
			in.getNextByte();	//chucking null char
		}
		else
		{
			result = "";
		}
		
		return result;
	}
	
	public static String[] parseNextLuaStringArr(BytesStreamer in, int sizeTLength, int ArrLength)
	{
		final String[] result = new String[ArrLength];
		
		for(int i = 0; i < ArrLength; i++)
		{
			result[i] = parseNextLuaString(in, sizeTLength);
		}
		
		return result;
	}
}
