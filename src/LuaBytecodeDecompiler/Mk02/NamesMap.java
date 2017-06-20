package LuaBytecodeDecompiler.Mk02;

import java.io.PrintWriter;
import java.util.Map.Entry;

import DataStructures.Maps.HalfByteRadix.Mk02.HalfByteRadixMap;
import LiterallyDictionaries.English.Mk01.Dictionary_English;
import LiterallyDictionaries.English.Mk01.Dictionary_English.RandomWordsModes;

public class NamesMap
{
	private final HalfByteRadixMap<String> oldNamesToNewMap;
	private final boolean replaceUnknownNames;

	private NamesMap(HalfByteRadixMap<String> inOldNamesToNewMap, boolean inReplaceUnknownNames)
	{
		this.oldNamesToNewMap = inOldNamesToNewMap;
		this.replaceUnknownNames = inReplaceUnknownNames;
	}

	public static NamesMap getInstance(boolean replaceUnknownNames)
	{
		return new NamesMap(new HalfByteRadixMap<String>(), replaceUnknownNames);
	}

	public static NamesMap getInstance(boolean replaceUnknownNames, String rawCsv)
	{
		final HalfByteRadixMap<String> map = new HalfByteRadixMap<String>();

		final String[] lines = rawCsv.split("(\\r\\n)|(\\n)");

		for (String line : lines)
		{
			final String[] split = line.split(",");

			map.add(split[0], split[1]);
		}

		return new NamesMap(map, replaceUnknownNames);
	}

	public String getCorrectName(String oldName)
	{
		final String result = this.oldNamesToNewMap.get(oldName);

		if (result == null || result.length() == 0)
		{
			final String next;
			
			if (this.replaceUnknownNames)
			{
				next = "Anon_" + Dictionary_English.getRandomWord(RandomWordsModes.RollLetter_RollWord_ThenAlpha);
			}
			else
			{
				next = oldName;
			}
			
			this.oldNamesToNewMap.add(oldName, next);
			
			return next;
		}
		else
		{
			return result;
		}
	}
	
	public void serialize(PrintWriter in)
	{
		for(Entry<String, String> entry: this.oldNamesToNewMap.entrySet())
		{
			in.println(entry.getKey() + ',' + entry.getValue());
		}
		
		in.flush();
	}
}
