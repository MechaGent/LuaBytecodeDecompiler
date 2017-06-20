package CleanStart.Mk04_Working.Analysis;

import CleanStart.Mk04_Working.Instruction;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class LinearCodeBlock extends CodeBlock
{
	protected LinearCodeBlock(Instruction[] inBlock)
	{
		super(BlockTypes.Linear, inBlock, 1);
	}

	protected LinearCodeBlock(SingleLinkedList<Instruction> inBlock)
	{
		super(BlockTypes.Linear, inBlock, 1);
	}
	
	protected LinearCodeBlock(SingleLinkedList<Instruction> inBlock, int numKids)
	{
		super(BlockTypes.Linear, inBlock, numKids);
	}
	
	public static LinearCodeBlock getInstance(HalfByteRadixMap<CodeBlock> parsedMap, SingleLinkedList<Instruction> inBlock)
	{
		final LinearCodeBlock result = new LinearCodeBlock(inBlock);
		final String key = result.block[0].getMangledName();
		//System.out.println("adding LinearCodeBlock under key " + result.block[0].getMangledName());
		parsedMap.put(key, result);
		return result;
	}
	
	public static LinearCodeBlock getInstance_NoKids(HalfByteRadixMap<CodeBlock> parsedMap, SingleLinkedList<Instruction> inBlock)
	{
		final LinearCodeBlock result = new LinearCodeBlock(inBlock, 0);
		final String key = result.block[0].getMangledName();
		//System.out.println("adding LinearCodeBlock_noKids under key " + result.block[0].getMangledName());
		parsedMap.put(key, result);
		return result;
	}
}
