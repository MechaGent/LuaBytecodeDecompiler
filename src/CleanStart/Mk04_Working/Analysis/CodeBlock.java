package CleanStart.Mk04_Working.Analysis;

import java.util.Iterator;

import CharList.CharList;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.FunctionObjects.RefinedFunctionObject;
import CleanStart.Mk04_Working.Instructions.ForLoop;
import CleanStart.Mk04_Working.Instructions.Jump_Branched;
import CleanStart.Mk04_Working.Instructions.Jump_Straight;
import CleanStart.Mk04_Working.Labels.Label;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public abstract class CodeBlock implements MetaCodeBlock
{
	private static int instanceCounter = 0;

	protected final int instanceNumber;
	protected final BlockTypes blockType;
	protected final Instruction[] block;
	protected final CodeBlock[] children;

	protected int scopeLevel; // if -1, is unset
	
	private SpecialBlockTypes specialBlockType;

	protected CodeBlock(BlockTypes inBlockType, SingleLinkedList<Instruction> inBlock, int numChildren)
	{
		this(inBlockType, inBlock.toArray(new Instruction[inBlock.getSize()]), numChildren);
	}

	protected CodeBlock(BlockTypes inBlockType, Instruction[] inBlock, int numChildren)
	{
		this.instanceNumber = instanceCounter++;
		this.blockType = inBlockType;
		this.block = inBlock;
		this.children = new CodeBlock[numChildren];
		this.scopeLevel = -1;
		this.specialBlockType = SpecialBlockTypes.Vanilla;
	}

	public BlockTypes getBlockType()
	{
		return this.blockType;
	}

	public Instruction[] getBlock()
	{
		return this.block;
	}

	public String getMangledName()
	{
		return this.blockType.toString() + '_' + this.instanceNumber;
	}

	public CodeBlock getFirstSharedChildBlock(CodeBlock other)
	{
		if (this != other)
		{
			final HalfByteRadixMap<CodeBlock> result = new HalfByteRadixMap<CodeBlock>();
			final SingleLinkedList<CodeBlock> queue = new SingleLinkedList<CodeBlock>();
			final CodeBlock max = (this.instanceNumber > other.instanceNumber) ? this : other;
			result.put(this.getMangledName(), this);
			result.put(other.getMangledName(), other);
			queue.add(this.children);
			queue.add(other.children);

			while (!queue.isEmpty())
			{
				// final int beforeSize = queue.getSize();
				// final Object focus = queue.pop();
				// System.out.println(focus.toString() + " before: " + beforeSize + " and after: " + queue.getSize());
				final CodeBlock current = queue.pop();
				final String key = current.getMangledName();
				final CodeBlock temp = result.get(key);

				if (temp != null)
				{
					if (temp instanceof CodeBlock)
					{
						final CodeBlock temp2 = (CodeBlock) temp;

						if (temp2.instanceNumber < max.instanceNumber)
						{
							queue.add(current);
						}
						else
						{
							return temp;
						}
					}
				}
				else
				{
					result.put(key, current);
					queue.add(current.children);

					// System.out.println("adding number of children: " + current.children.length);
				}
			}

			return null;
		}
		else
		{
			// System.out.println("here");
			return other;
		}
	}

	public CharList toCharList()
	{
		return this.toCharList(true);
	}

	public CharList toCharList(boolean includeInstructions)
	{
		return this.toCharList(includeInstructions, true);
	}

	public CharList toCharList(boolean includeInstructions, boolean includeName)
	{
		final CharList result = new CharList();

		if (includeName)
		{
			result.addNewLine();
			result.add('\t');
			result.add(this.getMangledName());
		}
		// result.addNewLine();
		// System.out.println("numInstructions: " + this.block.length);

		if (includeInstructions)
		{
			result.add(RefinedFunctionObject.disassembleInstructions(0, this.block), true);
		}

		result.addNewLine();
		result.add(this.getMangledName());
		result.add(" -> ");

		for (int i = 0; i < this.children.length; i++)
		{
			if (i != 0)
			{
				result.add(" | ");
			}

			final CodeBlock child = this.children[i];

			if (child == null)
			{
				throw new IllegalArgumentException(this.getMangledName());
			}
			result.add(this.children[i].getMangledName());
		}

		result.addNewLine();

		return result;
	}

	/*
	public static SingleLinkedList<CodeBlock> parse(Instruction[] in)
	{
		final HalfByteRadixMap<CodeBlock> result = new HalfByteRadixMap<CodeBlock>();
		final InstructionIterator itsy = new InstructionIterator(in);
		// final SingleLinkedList<CodeBlock> listy = new SingleLinkedList<CodeBlock>();
		final SingleLinkedList<CodeBlock> queue = new SingleLinkedList<CodeBlock>();
	
		while (itsy.hasNext())
		{
			final CodeBlock cur = parse_internal(result, itsy);
			queue.add(cur);
			// listy.add(cur);
		}
	
		final SingleLinkedList<CodeBlock> listy = linkBlocks(queue, result);
	
		return listy;
	}
	*/

	public static SingleLinkedList<CodeBlock> parse(Instruction[] in)
	{
		final HalfByteRadixMap<CodeBlock> result = new HalfByteRadixMap<CodeBlock>();
		final InstructionIterator itsy = new InstructionIterator(in);
		final SingleLinkedList<CodeBlock> queue = parse_internal(result, itsy);
		final SingleLinkedList<CodeBlock> listy = linkBlocks(queue, result);

		return listy;
	}

	/*
	private static void deriveScopes(CodeBlock first, int initialScopeLevel)
	{
		final SingleLinkedList<CodeBlock> queue = new SingleLinkedList<CodeBlock>(first);
		first.scopeLevel = initialScopeLevel;

		while (!queue.isEmpty())
		{
			final CodeBlock cur = queue.pop();

			switch (cur.blockType)
			{
				case Branched:
				{
					final Jump_Branched cast = (Jump_Branched) cur.block[cur.block.length - 1];
					final CodeBlock branch1 = cur.children[0];
					final CodeBlock branch2 = cur.children[1];
					final CodeBlock joiner = branch1.getFirstSharedChildBlock(branch2);
					final IfElseBlock result;

					/*
					 * note to self: good indicator is transition into joiner ie if it flows directly
					 *

					if (joiner == branch1)
					{
						result = IfElseBlock.getInstance(cast, branch2, null, joiner);
						branch2.scopeLevel = cur.scopeLevel + 1;
					}
					else if (joiner == branch2)
					{
						result = IfElseBlock.getInstance(cast, null, branch1, joiner);
						branch1.scopeLevel = cur.scopeLevel + 1;
					}
					else
					{
						result = IfElseBlock.getInstance(cast, branch2, branch1, joiner);
						branch2.scopeLevel = cur.scopeLevel + 1;
						branch1.scopeLevel = cur.scopeLevel + 1;
					}

					break;
				}
				case Linear:
				{
					final Instruction rawLast = cur.block[cur.block.length - 1];

					switch (rawLast.getIrCode())
					{
						case Jump_Straight:
						{
							final Jump_Straight cast = (Jump_Straight) rawLast;

							break;
						}
						default:
						{
							break;
						}
					}

					cur.children[0].scopeLevel = cur.scopeLevel;
					queue.add(cur.children[0]);
					break;
				}
				default:
				{
					throw new UnhandledEnumException(cur.blockType);
				}
			}
		}
	}

	private static void handleSubScoping(int startLevel, CodeBlock first, CodeBlock last)
	{

	}
	
	*/
	
	/**
	 * 
	 * @param queue
	 * @param map
	 * @return first codeblock
	 */
	private static SingleLinkedList<CodeBlock> linkBlocks(SingleLinkedList<CodeBlock> queue, HalfByteRadixMap<CodeBlock> map)
	{
		SingleLinkedList<CodeBlock> result = new SingleLinkedList<CodeBlock>();

		while (!queue.isEmpty())
		{
			final CodeBlock current = queue.pop();

			final Instruction rawLast = current.block[current.block.length - 1];
			// System.out.println("processing: " + current.getMangledName() + ", with last instruction: " + rawLast.getMangledName());

			switch (rawLast.getIrCode())
			{
				case Return_Variably:
				case Return_Concretely:
				{
					break;
				}
				case Jump_Straight:
				{
					// System.out.println("here");
					final Jump_Straight cast = (Jump_Straight) rawLast;
					final Label jumpTo = cast.getJumpTo();
					final CodeBlock child = map.get(jumpTo.getMangledName());

					if (child == null)
					{
						throw new IllegalArgumentException();
					}

					current.children[0] = child;

					break;
				}
				case Jump_Conditional:
				{
					final Jump_Branched cast = (Jump_Branched) rawLast;

					final CodeBlock child_false = map.get(cast.getJump_False().getMangledName());

					if (child_false == null)
					{
						throw new IllegalArgumentException();
					}

					current.children[0] = child_false;

					final String keyTrue = cast.getJump_True().getMangledName();
					// System.out.println("looking for key: " + keyTrue);
					final CodeBlock child_true = map.get(keyTrue);

					if (child_true == null)
					{
						throw new IllegalArgumentException("failed lookup of " + keyTrue);
					}

					current.children[1] = child_true;

					break;
				}
				case EvalLoop_For:
				{
					final ForLoop cast = (ForLoop) rawLast;

					current.children[0] = queue.getFirst();

					final String keyTrue = cast.getToAfterLoop().getMangledName();
					// System.out.println("looking for key: " + keyTrue);
					final CodeBlock child_true = map.get(keyTrue);

					if (child_true == null)
					{
						throw new IllegalArgumentException();
					}

					current.children[1] = child_true;
					break;
				}
				default:
				{
					// total linear block
					current.children[0] = queue.getFirst();
					break;
				}
			}

			result.add(current);
		}

		return result;
	}

	/*
	private static CodeBlock parse_internal(HalfByteRadixMap<CodeBlock> parsedMap, InstructionIterator itsy)
	{
		SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();
		boolean isDone = false;
		// System.out.println("first instruction: " + itsy.core[0].getVerboseCommentForm());
	
		while (!isDone && itsy.hasNext())
		{
			final Instruction loc = itsy.next();
			// System.out.println("on instruction: " + loc.getMangledName() + ", which is instruction# " + itsy.index);
	
			switch (loc.getIrCode())
			{
				case Label:
				{
					if (result.getSize() == 0)
					{
						result.add(loc);
					}
					else
					{
						itsy.index--;
						isDone = true;
					}
	
					break;
				}
				case Return_Variably:
				case Return_Concretely:
				{
					result.add(loc);
					return LinearCodeBlock.getInstance_NoKids(parsedMap, result);
				}
				case Jump_Straight:
				{
					result.add(loc);
					isDone = true;
					break;
				}
				case Jump_Conditional:
				case EvalLoop_For:
				{
					result.add(loc);
					return BranchedCodeBlock.getInstance(parsedMap, result);
				}
				default:
				{
					result.add(loc);
					break;
				}
			}
		}
	
		return LinearCodeBlock.getInstance(parsedMap, result);
	}
	*/

	private static SingleLinkedList<CodeBlock> parse_internal(HalfByteRadixMap<CodeBlock> parsedMap, InstructionIterator itsy)
	{
		final SingleLinkedList<CodeBlock> result = new SingleLinkedList<CodeBlock>();

		while (itsy.hasNext())
		{
			SingleLinkedList<Instruction> rawResult = new SingleLinkedList<Instruction>();
			boolean isDone = false;

			while (!isDone && itsy.hasNext())
			{
				final Instruction loc = itsy.next();

				switch (loc.getIrCode())
				{
					case Label:
					{
						if (rawResult.getSize() == 0)
						{
							rawResult.add(loc);
						}
						else
						{
							itsy.index--;
							result.add(LinearCodeBlock.getInstance(parsedMap, rawResult));
							isDone = true;
						}

						break;
					}
					case Return_Variably:
					case Return_Concretely:
					{
						rawResult.add(loc);
						result.add(LinearCodeBlock.getInstance_NoKids(parsedMap, rawResult));
						isDone = true;
						break;
					}
					case Jump_Straight:
					{
						rawResult.add(loc);
						result.add(LinearCodeBlock.getInstance(parsedMap, rawResult));
						isDone = true;
						break;
					}
					case Jump_Conditional:
					case EvalLoop_For:
					{
						result.add(LinearCodeBlock.getInstance(parsedMap, rawResult));
						result.add(BranchedCodeBlock.getInstance(parsedMap, new Instruction[] {
																								loc }));

						// rawResult.add(loc);
						// result.add(BranchedCodeBlock.getInstance(parsedMap, rawResult));

						isDone = true;
						break;
					}
					default:
					{
						rawResult.add(loc);
						break;
					}
				}
			}
		}

		return result;
	}
	
	private static enum SpecialBlockTypes
	{
		StartOfDoWhile, TestOfDoWhile, StartOfWhile, Vanilla;
	}

	private static class InstructionIterator implements Iterator<Instruction>
	{
		private int index;
		private final Instruction[] core;

		public InstructionIterator(Instruction[] inCore)
		{
			this.index = 0;
			this.core = inCore;
		}

		@Override
		public boolean hasNext()
		{
			return this.index < this.core.length;
		}

		@Override
		public Instruction next()
		{
			// System.out.println("Returning: " + this.core[this.index].getMangledName() + ", from corelength: " + this.core.length);
			return this.core[this.index++];
		}
	}
}
