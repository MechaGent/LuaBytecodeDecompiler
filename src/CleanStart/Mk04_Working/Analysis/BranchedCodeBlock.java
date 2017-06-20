package CleanStart.Mk04_Working.Analysis;

import CleanStart.Mk04_Working.Instruction;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class BranchedCodeBlock extends CodeBlock
{
	/*
	 * true and false jumps are stored in their coerced respective indeces
	 */
	
	protected BranchedCodeBlock(SingleLinkedList<Instruction> inBlock)
	{
		super(BlockTypes.Branched, inBlock, 2);
	}
	
	protected BranchedCodeBlock(Instruction[] inBlock)
	{
		super(BlockTypes.Branched, inBlock, 2);
	}
	
	public static BranchedCodeBlock getInstance(HalfByteRadixMap<CodeBlock> parsedMap, SingleLinkedList<Instruction> inBlock)
	{
		final BranchedCodeBlock result = new BranchedCodeBlock(inBlock);
		//System.out.println("adding BranchedCodeBlock under key " + result.block[0].getMangledName());
		parsedMap.put(result.block[0].getMangledName(), result);
		return result;
	}
	
	public static BranchedCodeBlock getInstance(HalfByteRadixMap<CodeBlock> parsedMap, Instruction[] inBlock)
	{
		final BranchedCodeBlock result = new BranchedCodeBlock(inBlock);
		//System.out.println("adding BranchedCodeBlock under key " + result.block[0].getMangledName());
		parsedMap.put(result.block[0].getMangledName(), result);
		return result;
	}
}
