package LuaBytecodeDecompiler.Mk01;

import DataStructures.Linkages.CharList.Mk03.CharList;
import HandyStuff.FileParser;

public class OpCodeTempParser
{
	public static void main(String[] args)
	{
		final String rawPath = "H:/Users/Thrawnboo/workspace - Java/LuaBytecodeDecompiler/src/LuaOpcodesListing.txt";
		final String[] raw = FileParser.parseFileAsString(rawPath).split("\r\n");
		final int numTopLines = 3;

		for (int i = numTopLines; i < raw.length; i++)
		{
			final String rawLine = raw[i];

			if (rawLine.length() > 0)
			{
				final OpCode curr = new OpCode(rawLine);
				System.out.println(curr.toEnumFormat().toString() + "\r\n");
			}
		}
	}

	private static class OpCode
	{
		private static final int length_Name = 16;
		private static final int length_Args = 8;
		@SuppressWarnings("unused")
		private static final int length_Desc = 50;

		private final String Name;
		private final String[] Args;
		private final String Desc;

		public OpCode(String rawLine)
		{
			this.Name = rawLine.substring(0, length_Name).split("(,/)|(/)")[0];

			final int endArgs = length_Name + length_Args;
			this.Args = rawLine.substring(length_Name, endArgs).split(" +");
			this.Desc = rawLine.substring(endArgs).split(" .\\*/")[0];
		}

		@Override
		public String toString()
		{
			return this.toCharList().toString();
		}

		public CharList toCharList()
		{
			final CharList result = new CharList();

			result.add("Name=");
			result.add(this.Name);
			result.add(", Args=[");

			for (int i = 0; i < this.Args.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(this.Args[i]);
			}

			result.add("], Desc=");
			result.add(this.Desc);

			return result;
		}
		
		public CharList toEnumFormat()
		{
			final CharList result = new CharList();
			
			result.add("/**\r\n* Args: ");
			
			for (int i = 0; i < this.Args.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(this.Args[i]);
			}
			
			result.add("\r\n* Desc: ");
			result.add(this.Desc);
			result.add("\r\n*/\r\n");
			result.add(this.Name.toLowerCase());
			result.add(',');
			
			return result;
		}
	}
}
