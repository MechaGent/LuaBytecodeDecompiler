package LuaBytecodeDecompiler.Mk04;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.OneLinkIntList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_ForLoop;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_ForPrep;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_Jump;

public class CodeBlock
{
	private static final CodeBlock NullCodeBlock = new NullCodeBlock();

	private final CodeBlockTags tag;
	private final int tagNum;
	private final int blockNum;
	private final AnnoInstruction[] lines;
	private final int startLineNum;
	private CodeBlock trueNext;
	private CodeBlock falseNext;
	private final HalfByteRadixMap<CodeBlock> Ancestors;
	private LoopStartTypes loopStartType;

	public CodeBlock(CodeBlockTags inTag, int inTagNum, int inBlockNum, AnnoInstruction[] inLines, int inStartLineNum)
	{
		this.tag = inTag;
		this.tagNum = inTagNum;
		this.blockNum = inBlockNum;
		this.lines = inLines;
		this.startLineNum = inStartLineNum;
		this.trueNext = NullCodeBlock;
		this.falseNext = NullCodeBlock;
		this.Ancestors = new HalfByteRadixMap<CodeBlock>();
		this.loopStartType = LoopStartTypes.NoLoop;
	}

	public void setTrueNext(CodeBlock inTrueNext)
	{
		this.trueNext = inTrueNext;

		if (!this.trueNext.isNull())
		{
			this.trueNext.setAncestor(this);

			if (!this.trueNextIsAdvancing())
			{
				switch (this.tag)
				{
					case InNum:
						break;
					case ItNum:
					{
						this.trueNext.loopStartType = LoopStartTypes.ForLoop;
						break;
					}
					case Jump:
					{
						/*
						 * is a while loop - do-whiles will have Tests instead
						 */

						this.trueNext.loopStartType = LoopStartTypes.WhileLoop;

						break;
					}
					case Test:
					{
						this.trueNext.loopStartType = LoopStartTypes.DoWhileLoop;
						break;
					}
					case Linear:
					case Return:
					default:
					{
						throw new IllegalArgumentException();
					}
				}
			}
		}
	}

	public void setFalseNext(CodeBlock inFalseNext)
	{
		this.falseNext = inFalseNext;

		if (!this.falseNext.isNull())
		{
			this.falseNext.setAncestor(this);
			
			if (!this.falseNextIsAdvancing())
			{
				switch (this.tag)
				{
					case InNum:
						break;
					case ItNum:
					{
						this.falseNext.loopStartType = LoopStartTypes.ForLoop;
						break;
					}
					case Jump:
					{
						/*
						 * is a while loop - do-whiles will have Tests instead
						 */

						this.falseNext.loopStartType = LoopStartTypes.WhileLoop;

						break;
					}
					case Test:
					{
						this.falseNext.loopStartType = LoopStartTypes.DoWhileLoop;
						break;
					}
					case Linear:
					case Return:
					default:
					{
						throw new IllegalArgumentException();
					}
				}
			}
		}
	}

	private void setAncestor(CodeBlock in)
	{
		/*
		//System.out.println("setting " + in.getMangledName() + " as ancestor for " + this.getMangledName());
		SingleLinkedList<CodeBlock> queue = new SingleLinkedList<CodeBlock>();
		queue.add(this);
		
		while (!queue.isEmpty())
		{
			final CodeBlock current = queue.pop();
			
			System.out.println("setting " + current.getMangledName() + " as ancestor for " + this.getMangledName());
		
			if (current.blockNum >= this.blockNum)
			{
				current.Ancestors.put(in.blockNum, in);
		
				if (current.hasTrueNext())
				{
					queue.add(current.trueNext);
				}
		
				if (current.hasFalseNext())
				{
					queue.add(current.falseNext);
				}
			}
		}
		*/

		this.Ancestors.put(in.blockNum, in);

		if (this.hasTrueNext() && this.trueNext.blockNum > this.blockNum)
		{
			this.trueNext.setAncestor(in);
		}

		if (this.hasFalseNext() && this.falseNext.blockNum > this.blockNum)
		{
			this.falseNext.setAncestor(in);
		}
	}

	public AnnoInstruction[] getLines()
	{
		return this.lines;
	}

	public int getStartLineNum()
	{
		return this.startLineNum;
	}

	public boolean hasTrueNext()
	{
		return !this.trueNext.isNull();
	}

	public boolean trueNextIsAdvancing()
	{
		return this.trueNext.blockNum > this.blockNum;
	}

	public CodeBlock getTrueNext()
	{
		// System.out.println("true next of " + this.getMangledName() + " is " + this.trueNext.getMangledName());
		return this.trueNext;
	}

	public boolean hasFalseNext()
	{
		return !this.falseNext.isNull();
	}

	public boolean falseNextIsAdvancing()
	{
		return this.falseNext.blockNum > this.blockNum;
	}

	public CodeBlock getFalseNext()
	{
		// System.out.println("false next of " + this.getMangledName() + " is " + this.falseNext.getMangledName());
		return this.falseNext;
	}

	public boolean isNull()
	{
		return false;
	}

	public CodeBlockTags getTag()
	{
		return this.tag;
	}

	public int getTagNum()
	{
		return this.tagNum;
	}

	public String getMangledName()
	{
		return this.tag.toString() + this.tagNum;
	}

	public CodeBlock findLowestCommonAncestorWith(CodeBlock in)
	{
		final SingleLinkedList<CodeBlock> queue = new SingleLinkedList<CodeBlock>();
		queue.add(this);
		final HalfByteRadixMap<String> visited = new HalfByteRadixMap<String>();

		while (!queue.isEmpty())
		{
			// System.out.println("looping here?");
			final CodeBlock current = queue.pop();

			if (!visited.contains(current.blockNum))
			{
				visited.put(current.blockNum, "");

				if (current.Ancestors.contains(in.blockNum) && current.blockNum >= this.blockNum && current.blockNum >= in.blockNum)
				{
					// System.out.println("found lowest common ancestor with " + this.getMangledName() + ": " + current.getMangledName());

					return current;
				}

				if (current.hasTrueNext())
				{
					queue.add(current.trueNext);
				}

				if (current.hasFalseNext())
				{
					queue.add(current.falseNext);
				}
			}
		}

		return NullCodeBlock;
	}

	@Override
	public String toString()
	{
		return this.toCharList().toString();
	}

	public CharList toCharList()
	{
		return this.toCharList(true);
	}

	public CharList toCharList(boolean showCodeLines)
	{
		final CharList result = new CharList();

		result.add("Block: ");
		result.add(this.tag.toString());
		result.add(Integer.toString(this.tagNum));
		result.add(" - starts on line ");
		result.add(Integer.toString(this.startLineNum));

		if (showCodeLines)
		{
			for (int i = 0; i < this.lines.length; i++)
			{
				result.addNewLine();

				result.add(this.lines[i].toCharList(true), true);
			}
		}

		result.addNewLine();

		if (this.hasTrueNext())
		{
			result.add("Goes to: ");
			result.add(this.trueNext.tag.toString());
			result.add(Integer.toString(this.trueNext.tagNum));

			if (this.hasFalseNext())
			{
				result.add(" | ");
				result.add(this.falseNext.tag.toString());
				result.add(Integer.toString(this.falseNext.tagNum));
			}
		}
		else
		{
			result.add("this block is terminal!");
		}

		return result;
	}

	public int getBlockNum()
	{
		return this.blockNum;
	}

	public static CodeBlock getNullCodeBlock()
	{
		return NullCodeBlock;
	}

	public static CodeBlock[] splitIntoCodeBlocks(AnnoInstruction[] in)
	{
		final boolean[] markers = markBreaks(in);
		final OneLinkIntList lengths = new OneLinkIntList();
		int count = 0;

		for (int i = 0; i < markers.length; i++)
		{
			count++;

			if (markers[i])
			{
				lengths.add(count);
				count = 0;
			}
		}

		if (count != 0)
		{
			lengths.add(count);
		}

		final CodeBlock[] result = new CodeBlock[lengths.size()];
		final HalfByteRadixMap<CodeBlock> starts = new HalfByteRadixMap<CodeBlock>();
		int instructionPlace = 0;
		int resultPlace = 0;

		int numBlock = 0;
		int numJump = 0;
		int numTest = 0;
		int numInitNum = 0;
		int numIterNum = 0;
		int numReturn = 0;
		int numLinear = 0;

		while (!lengths.isEmpty())
		{
			final int length = lengths.pop();

			final AnnoInstruction[] subResult = new AnnoInstruction[length];

			final int startLineNum = instructionPlace;

			for (int i = 0; i < length; i++)
			{
				subResult[i] = in[instructionPlace++];
			}

			/*
			final CharList diag = new CharList();
			diag.add("current block: ");
			for(int i = 0; i < subResult.length; i++)
			{
				diag.addNewLine();
				diag.add(subResult[i].toCharList(true), true);
			}
			System.out.println(diag.toString());
			*/

			final CodeBlockTags tag;
			final int tagNum;

			switch (subResult[length - 1].getOpCode())
			{
				case Jump:
				{
					if (length > 1)
					{
						switch (subResult[length - 2].getOpCode())
						{
							case TestIfLessThan:
							case TestIfEqual:
							case TestIfLessThanOrEqual:
							case TestLogicalComparison:
							case TestAndSaveLogicalComparison:
							{
								tag = CodeBlockTags.Test;
								tagNum = numTest++;
								break;
							}
							default:
							{
								tag = CodeBlockTags.Jump;
								tagNum = numJump++;
								break;
							}
						}
					}
					else
					{
						tag = CodeBlockTags.Jump;
						tagNum = numJump++;
					}

					break;
				}
				case InitNumericForLoop:
				{
					tag = CodeBlockTags.InNum;
					tagNum = numInitNum++;
					break;
				}
				case IterateNumericForLoop:
				{
					tag = CodeBlockTags.ItNum;
					tagNum = numIterNum++;
					break;
				}
				case Return:
				{
					tag = CodeBlockTags.Return;
					tagNum = numReturn++;
					break;
				}
				default:
				{
					tag = CodeBlockTags.Linear;
					tagNum = numLinear++;
					break;
				}
			}

			final CodeBlock realSubResult = new CodeBlock(tag, tagNum, numBlock++, subResult, startLineNum);
			starts.put(startLineNum, realSubResult);
			result[resultPlace++] = realSubResult;
		}

		return linkBlocks(result, starts);
	}

	private static CodeBlock[] linkBlocks(CodeBlock[] result, HalfByteRadixMap<CodeBlock> starts)
	{
		for (int i = 0; i < result.length; i++)
		{
			final CodeBlock block = result[i];

			switch (block.tag)
			{
				case Return:
				{
					// doesn't link to anything
					break;
				}
				case Linear:
				{
					if (i + 1 == result.length)
					{
						throw new IndexOutOfBoundsException(block.lines[block.lines.length - 1].toString());
					}

					block.setTrueNext(result[i + 1]);
					break;
				}
				case Test:
				{
					block.setFalseNext(result[i + 1]);
				}
				case Jump:
				{
					final AnnoInstruction[] lines = block.lines;
					final int lastLine = lines.length - 1;
					final AnnoInstruction_Jump cast = (AnnoInstruction_Jump) lines[lastLine];
					final int absOffset = block.startLineNum + lastLine + 1 + cast.getOffset();
					final CodeBlock trueNext = starts.get(absOffset);

					if (trueNext == null || trueNext == NullCodeBlock)
					{
						throw new NullPointerException("bad lookup! " + absOffset);
					}

					block.setTrueNext(trueNext);

					break;
				}
				case InNum:
				{
					final AnnoInstruction_ForPrep cast = (AnnoInstruction_ForPrep) block.lines[0]; // block.lines should always be of length 1 in this case
					final int absOffset = block.startLineNum + cast.getJumpOffset() + 1;
					final CodeBlock trueNext = starts.get(absOffset);

					if (trueNext == null)
					{
						throw new NullPointerException("bad lookup!");
					}

					block.setTrueNext(trueNext);

					break;
				}
				case ItNum:
				{
					final AnnoInstruction_ForLoop cast = (AnnoInstruction_ForLoop) block.lines[0]; // block.lines should always be of length 1 in this case
					final int absOffset = block.startLineNum + cast.getOffset() + 1;
					final CodeBlock trueNext = starts.get(absOffset);

					if (trueNext == null)
					{
						throw new NullPointerException("bad lookup!");
					}

					block.setTrueNext(trueNext);
					block.setFalseNext(result[i + 1]);

					break;
				}
				default:
				{
					throw new UnhandledEnumException(block.tag);
				}
			}
		}

		return result;
	}

	private static boolean[] markBreaks(AnnoInstruction[] in)
	{
		final boolean[] result = new boolean[in.length]; // if element is true, there is a break after the element's index

		for (int i = 0; i < result.length; i++)
		{
			final AbstractOpCodes opCode = in[i].getOpCode();

			// mark both before and after
			switch (opCode)
			{
				case TestIfLessThan:
				case TestIfEqual:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				case TestAndSaveLogicalComparison:
				{
					if (i != 0)
					{
						// result[i - 1] = true;
						// result[++i] = true;
						// result[i] = true;
					}
					break;
				}
				case InitNumericForLoop:
				case IterateNumericForLoop:
				{
					result[i] = true;

					if (i != 0)
					{
						result[i - 1] = true;
					}

					break;
				}
				case Jump:
				{
					final AnnoInstruction_Jump cast = (AnnoInstruction_Jump) in[i];
					result[i + cast.getOffset()] = true;
				}
				case Return:
				{
					result[i] = true;
					break;
				}
				default:
				{
					break;
				}
			}
		}

		return result;
	}

	private static class NullCodeBlock extends CodeBlock
	{
		public NullCodeBlock()
		{
			super(CodeBlockTags.Linear, -1, -1, new AnnoInstruction[0], -1);
		}

		@Override
		public boolean isNull()
		{
			return true;
		}

		@Override
		public CodeBlock getTrueNext()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public CodeBlock getFalseNext()
		{
			throw new UnsupportedOperationException();
		}
	}
}
