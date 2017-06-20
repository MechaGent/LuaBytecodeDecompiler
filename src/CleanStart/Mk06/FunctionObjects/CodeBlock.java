package CleanStart.Mk06.FunctionObjects;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import CleanStart.Mk06.Instructions.NonOp;
import CleanStart.Mk06.Instructions.MetaInstructions.SimpleIfElse;
import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodeBlock
{
	protected Instruction firstInstruction;

	/**
	 * should always be one of the following:
	 * <br>
	 * Return_Concretely
	 * <br>
	 * Return_Variably
	 * <br>
	 * Jump_Branched
	 * <br>
	 * Jump_Straight
	 * <br>
	 * &ltother&gt
	 */
	protected final Instruction lastInstruction;

	protected final CodeBlock[] children;

	private boolean isProcessed;
	private SimpleIfElse currentAssignment;
	private boolean isTrue;

	protected CodeBlock(Instruction inLabel, Instruction inLastInstruction, int numChildren)
	{
		this.firstInstruction = inLabel;
		this.lastInstruction = inLastInstruction;
		this.children = new CodeBlock[numChildren];
		this.isProcessed = false;
		this.currentAssignment = null;
		this.isTrue = true;
	}

	protected static CodeBlock getInstance(Instruction inLabel, Instruction inLastInstruction, int numChildren)
	{
		// final Instruction dummy = NonOp.getInstance(inLabel.getStartTime());
		// Instruction.link(dummy, inLabel);
		return new CodeBlock(inLabel, inLastInstruction, numChildren);
	}

	public static CodeBlock getInstance(Instruction inLabel, Instruction inLastInstruction)
	{
		// final Instruction dummy = NonOp.getInstance(inLabel.getStartTime());
		// Instruction.link(dummy, inLabel);

		return new CodeBlock(inLabel, inLastInstruction, 1);
	}

	public static CodeBlock getInstance_Cascaded(Instruction first)
	{
		final CodeBlockChainFactory factory = new CodeBlockChainFactory(getFirstLabel(first));

		return factory.buildChain();
	}

	private static Label getFirstLabel(Instruction first)
	{
		while (first.getIrCode() != IrCodes.Label)
		{
			first = first.getLiteralNext();
		}

		return (Label) first;
	}

	public Instruction getFirstInstruction()
	{
		// System.out.println("first instruction: " + this.firstInstruction.toVerboseCommentForm(false).toString());
		return this.firstInstruction;
	}

	public void setFirstInstruction(Instruction in)
	{
		this.firstInstruction = in;
	}

	public Instruction getLastInstruction()
	{
		return this.lastInstruction;
	}

	public int getNumChildren()
	{
		if (this.isProcessed && this.children.length == 2)
		{
			return 1;
		}
		else
		{
			return this.children.length;
		}
	}

	public CodeBlock[] getChildren()
	{
		return this.children;
	}

	public void setChild(int index, CodeBlock child)
	{
		this.children[index] = child;
	}

	public boolean isAlreadyAssigned()
	{
		return this.currentAssignment != null;
	}

	public void reassign(SimpleIfElse in, boolean isTrueBlock)
	{
		if (this.currentAssignment.getLiteralNext() != this.firstInstruction)
		{
			if (this.isTrue)
			{
				throw new IllegalArgumentException();
			}
			else
			{
				this.currentAssignment.nullifyFalse();
			}
		}
		else
		{
			this.currentAssignment.nullifyLiteralNext();
		}
		
		this.currentAssignment = in;
		this.isTrue = isTrueBlock;
	}

	public boolean isProcessed()
	{
		return this.isProcessed;
	}

	public void setProcessed(boolean inIsProcessed)
	{
		this.isProcessed = inIsProcessed;
	}

	public CharList toCharList()
	{
		final CharList result = new CharList();

		result.add("{label: ");
		result.add(this.firstInstruction.getMangledName(false));
		result.addNewLine();
		result.add('\t');
		result.add("lastLine: ");
		result.add(this.lastInstruction.toVerboseCommentForm(false), true);
		result.addNewLine();
		result.add('\t');
		result.add("secondLast line: ");
		result.add(this.lastInstruction.getLiteralPrev().toVerboseCommentForm(false), true);
		result.addNewLine();
		result.add('\t');
		result.add("next block(s): {");

		for (int i = 0; i < this.children.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.children[i].firstInstruction.getMangledName(false));
		}

		result.add('}');

		return result;
	}

	private static class CodeBlockPrepObject
	{
		private final CodeBlock block;
		private final Instruction[] nextBlockLabels;

		public CodeBlockPrepObject(CodeBlock inBlock, Instruction[] inNextBlockLabels)
		{
			this.block = inBlock;
			this.nextBlockLabels = inNextBlockLabels;

			/*
			if(this.block.children.length != this.nextBlockLabels.length)
			{
				throw new IllegalArgumentException(this.block.children.length + " does not equal " + this.nextBlockLabels.length);
			}
			*/
		}
	}

	public static class CodeBlockChainFactory
	{
		private final HalfByteRadixMap<CodeBlockPrepObject> blocks;
		private final Label start;
		private final SingleLinkedList<Instruction> queue;

		private CodeBlockChainFactory(Label inStart)
		{
			this.blocks = new HalfByteRadixMap<CodeBlockPrepObject>();
			this.start = inStart;
			this.queue = new SingleLinkedList<Instruction>();
			this.queue.add(inStart);
		}

		public static CodeBlockChainFactory getInstance(Instruction inStart)
		{
			return new CodeBlockChainFactory(getFirstLabel(inStart));
		}

		public static CodeBlockChainFactory getInstance(Label inStart)
		{
			return new CodeBlockChainFactory(inStart);
		}

		public boolean queueIsNotEmpty()
		{
			return this.queue.isNotEmpty();
		}

		public Instruction popQueue()
		{
			return this.queue.pop();
		}

		public void handleAddingNexts(Instruction currentLabel, Instruction current)
		{
			// final Instruction dummy = NonOp.getInstance(StepNum.getInitializerStep());
			// Instruction.insertAhead(current, dummy);
			final CodeBlock product = CodeBlock.getInstance(currentLabel, current, 0);
			final CodeBlockPrepObject temp = new CodeBlockPrepObject(product, new Instruction[] {});
			this.blocks.put(currentLabel.getMangledName(false), temp);
		}

		public void handleAddingNexts(Instruction currentLabel, Instruction current, Instruction next)
		{
			// final Instruction dummy = NonOp.getInstance(StepNum.getInitializerStep());
			// Instruction.insertAhead(current, dummy);
			final CodeBlock product = CodeBlock.getInstance(currentLabel, current);
			final CodeBlockPrepObject temp = new CodeBlockPrepObject(product, new Instruction[] {
																									next });
			this.blocks.put(currentLabel.getMangledName(false), temp);

			this.addToQueue(next);
		}

		public void handleAddingNexts(Instruction currentLabel, Instruction current, Instruction next_T, Instruction next_F)
		{
			// final Instruction dummy = NonOp.getInstance(StepNum.getInitializerStep());
			// Instruction.insertAhead(current, dummy);
			final CodeBlock product = CodeBlock.getInstance(currentLabel, current, 2);
			final CodeBlockPrepObject temp = new CodeBlockPrepObject(product, new Instruction[] {
																									next_T,
																									next_F });
			this.blocks.put(currentLabel.getMangledName(false), temp);

			this.addToQueue(next_T);
			this.addToQueue(next_F);
		}

		public void addToQueue(Instruction next)
		{
			final String nextName = next.getMangledName(false);

			if (!this.blocks.contains(nextName))
			{
				this.queue.add(next);
				// System.out.println("adding to map " + this.blocks.getRawObjectId() + ": " + nextName);
			}
		}

		public CodeBlock buildChain()
		{
			this.processLabels();
			final CodeBlock result = this.linkChain();
			this.substituteIfs();
			return result;
		}

		private void processLabels()
		{
			// this.queue.add(this.start);

			// System.out.println("starting");

			while (this.queueIsNotEmpty())
			{
				final Instruction currentLabel = this.popQueue();
				// System.out.println("processing label: " + currentLabel.getMangledName(false));

				if (!this.blocks.contains(currentLabel.getMangledName(false)))
				{
					Instruction current = currentLabel.getLiteralNext();

					boolean isDone = false;

					while (!isDone)
					{
						// System.out.println("processing instruction: " + current.getMangledName(false));

						switch (current.getIrCode())
						{
							case Label:
							{
								this.addToQueue(current);
								this.handleAddingNexts(currentLabel, current.getLiteralPrev(), current);
								isDone = true;
								break;
							}
							case Jump_Straight:
							{
								final Jump_Straight cast = (Jump_Straight) current;

								if (cast.isNegativeJump())
								{
									this.handleAddingNexts(currentLabel, current);
								}
								else
								{
									this.handleAddingNexts(currentLabel, current, cast.getJumpTo());
								}

								isDone = true;
								break;
							}
							case Jump_Branched:
							{
								final Jump_Branched cast = (Jump_Branched) current;

								this.handleAddingNexts(currentLabel, current, cast.getJumpTo(), cast.getJumpTo_ifFalse());

								isDone = true;
								break;
							}
							case Return_Concretely:
							case Return_Variably:
							{
								this.handleAddingNexts(currentLabel, current);
								isDone = true;
								break;
							}
							default:
							{
								current = current.getLiteralNext();
								break;
							}
						}
					}

					final Instruction rawNext = current.getLiteralNext();

					if (rawNext != null)
					{
						if (rawNext.getIrCode() == IrCodes.Label)
						{
							this.addToQueue((Label) rawNext);
						}
					}
				}
			}
		}

		private CodeBlock linkChain()
		{
			CodeBlockPrepObject first = null;

			for (Entry<HalfByteArray, CodeBlockPrepObject> entry : blocks)
			{
				final CodeBlockPrepObject current = entry.getValue();

				if (current.block.firstInstruction == start)
				{
					first = current;
				}

				// Instruction.link(null, current.block.firstInstruction);
				// Instruction.link(current.block.lastInstruction, null);

				for (int i = 0; i < current.nextBlockLabels.length; i++)
				{
					current.block.setChild(i, blocks.get(current.nextBlockLabels[i].getMangledName(false)).block);
				}

				// System.out.println("link established: " + current.block.toCharList().toString());
			}

			if (first == null)
			{
				throw new NullPointerException("Null first, with startLabel of: " + start.getMangledName(false));
			}

			return first.block;
		}

		private void substituteIfs()
		{
			// System.out.println("triggering 'substituteIfs()'");
			for (Entry<HalfByteArray, CodeBlockPrepObject> entry : blocks)
			{
				final CodeBlockPrepObject current = entry.getValue();

				if (!current.block.isProcessed())
				{
					SimpleIfElse.convert_fromCodeBlocks(current.block);
				}
			}

			SimpleIfElse.snipAll();
		}

		public CharList toCharList()
		{
			final CharList result = new CharList();

			result.add("blocks chained: ");
			result.addNewLine();

			final String startName = this.start.getMangledName(false);
			final CodeBlockPrepObject first = this.blocks.get(startName);

			result.add(first.block.toCharList(), true);

			for (Entry<HalfByteArray, CodeBlockPrepObject> entry : this.blocks)
			{
				final CodeBlockPrepObject current = entry.getValue();

				if (current.block.firstInstruction != this.start)
				{
					result.addNewLine();
					result.add(current.block.toCharList(), true);
				}
			}

			return result;
		}
	}
}
