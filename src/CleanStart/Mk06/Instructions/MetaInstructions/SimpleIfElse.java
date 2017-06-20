package CleanStart.Mk06.Instructions.MetaInstructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.FunctionObjects.CodeBlock;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.VarFormula.Formula;
import SingleLinkedList.Mk01.SingleLinkedList;

public class SimpleIfElse extends MetaInstruction
{
	private static int instanceCounter = 0;
	private static SingleLinkedList<SimpleIfElse> all = new SingleLinkedList<SimpleIfElse>();

	private final Formula BoolStatement;
	private Instruction IfTrue;
	private Instruction IfFalse;

	private SimpleIfElse(StepNum inStartTime, Formula inBoolStatement, Instruction inIfTrue, Instruction inIfFalse)
	{
		super(MetaIrCodes.SimpleIfElse, instanceCounter++, inStartTime);
		this.BoolStatement = inBoolStatement;
		this.IfTrue = inIfTrue;
		this.IfFalse = inIfFalse;
	}

	/**
	 * 
	 * @param inStartTime
	 * @param inBoolStatement
	 * @param inIfTrue
	 *            if this is an iflessElse, set this to null
	 * @param inIfFalse
	 *            if this is an elselessIf, set this to null
	 * @param LiteralPrev
	 * @param LiteralNext
	 * @return
	 */
	public static SimpleIfElse getInstance(StepNum inStartTime, Formula inBoolStatement, Instruction inIfTrue, Instruction inIfFalse, Instruction LiteralPrev, Instruction LiteralNext)
	{
		final SimpleIfElse result = new SimpleIfElse(inStartTime, inBoolStatement, inIfTrue, inIfFalse);
		Instruction.link(LiteralPrev, result);
		Instruction.link(result, LiteralNext);
		all.add(result);

		return result;
	}
	
	public static void snipAll()
	{
		for(SimpleIfElse current: all)
		{
			Instruction.link(null, current.IfTrue);
			Instruction.link(null, current.IfFalse);
		}
	}

	public Formula getBoolStatement()
	{
		return this.BoolStatement;
	}

	public Instruction getIfTrue()
	{
		return this.IfTrue;
	}

	public Instruction getIfFalse()
	{
		return this.IfFalse;
	}
	
	public void nullifyFalse()
	{
		this.IfFalse = null;
	}
	
	public void nullifyLiteralNext()
	{
		Instruction.link(this, null);
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{

	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.NonApplicable;
	}

	@Override
	public void logAllReadsAndWrites()
	{

	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add("if (");
		result.add(this.BoolStatement.getValueAsString());
		result.add(')');
		result.add('\t');
		result.add("// ");

		if (this.IfTrue != null)
		{
			result.add(this.IfTrue.toVerboseCommentForm(false), true);
		}
		else
		{
			result.add("null");
		}
		result.add('|');

		if (this.IfFalse != null)
		{
			result.add(this.IfFalse.toVerboseCommentForm(false), true);
		}
		else
		{
			result.add("null");
		}

		result.addNewLine();
		result.add('{');
		// result.addNewLine();

		if (this.IfTrue != null)
		{
			Instruction current = this.IfTrue;

			while (current != null)
			{
				result.addNewLine();
				result.add(current.toVerboseCommentForm(inAppendMetadata), true);
				current = current.getLiteralNext();
			}
		}

		result.addNewLine();
		result.add('}');
		result.addNewLine();

		if (this.IfFalse != null)
		{
			result.add("else {");
			Instruction current = this.IfFalse;

			while (current != null)
			{
				result.addNewLine();
				result.add(current.toVerboseCommentForm(inAppendMetadata), true);
				current = current.getLiteralNext();
			}

			result.addNewLine();
			result.add('}');
		}

		if (this.getLiteralNext() == null)
		{
			result.addNewLine();
			result.add("null-ended!");
		}
		result.addNewLine();

		return result;
	}

	public static void convert_fromCodeBlocks(CodeBlock start)
	{
		//System.out.println("triggering");
		switch (start.getNumChildren())
		{
			case 0:
			{
				// do nothing
				start.setProcessed(true);
				break;
			}
			case 1:
			{
				// do nothing
				start.setProcessed(true);
				break;
			}
			case 2:
			{
				final CodeBlock[] children = start.getChildren();

				if (!children[0].isProcessed())
				{
					convert_fromCodeBlocks(children[0]);
				}

				if (!children[1].isProcessed())
				{
					convert_fromCodeBlocks(children[1]);
				}

				final int numKids_i0 = children[0].getNumChildren();
				final int numKids_i1 = children[1].getNumChildren();
				
				final CodeBlock[] zerosKids = children[0].getChildren();
				//final CodeBlock[] onesKids = children[1].getChildren();
				
				final Instruction rawFirst = start.getFirstInstruction();
				//final Instruction rawPrev = rawFirst.getLiteralPrev();
				//System.out.println("rawPrev: " + rawPrev.getMangledName(false));
				Instruction rawNext = children[1].getFirstInstruction();

				Instruction current = cleanLines(rawFirst);
				
				//Instruction.link(current, null);
				final Instruction ifTrue = children[0].getFirstInstruction();
				final Instruction ifFalse;

				if (numKids_i0 == 1)
				{
					if (numKids_i1 == 1)
					{
						if (zerosKids[0] == children[1])
						{
							// it's an elselessIf
							
							ifFalse = null;
						}
						else
						{
							// it's an elseIf
							
							ifFalse = children[1].getFirstInstruction();
							final Instruction last_F = cleanLines(ifFalse);
							//rawNext = last_F;
							//rawNext = children[1].getLastInstruction();
							/*
							if(last_F.getIrCode() == IrCodes.Jump_Straight)
							{
								rawNext = ((Jump_Straight) last_F).getJumpTo();
							}
							*/
							rawNext = null;
							//Instruction.link(children[1].getLastInstruction(), null);
							//Instruction.link(last_F, null);
						}
					}
					else
					{
						// numKids_i1 == 0
						ifFalse = children[1].getFirstInstruction();
						final Instruction last_F = cleanLines(ifFalse);
						//rawNext = last_F;
						//rawNext = children[1].getLastInstruction();
						/*
						if(last_F.getIrCode() == IrCodes.Jump_Straight)
						{
							rawNext = ((Jump_Straight) last_F).getJumpTo();
						}
						*/
						rawNext = null;
						//Instruction.link(children[1].getLastInstruction(), null);
						//Instruction.link(last_F, null);
					}
				}
				else
				{
					// numKids_i0 == 0
					if (numKids_i1 == 1)
					{
						ifFalse = null;
					}
					else
					{
						// numKids_i1 == 0
						ifFalse = children[1].getFirstInstruction();
						
						final Instruction last_F = cleanLines(ifFalse);
						//rawNext = last_F;
						//rawNext = children[1].getLastInstruction();
						/*
						if(last_F.getIrCode() == IrCodes.Jump_Straight)
						{
							rawNext = ((Jump_Straight) last_F).getJumpTo();
						}
						*/
						rawNext = null;
						//Instruction.link(children[1].getLastInstruction(), null);
						//Instruction.link(last_F, null);
					}
				}

				@SuppressWarnings("unused")
				final SimpleIfElse replace = SimpleIfElse.getInstance(rawFirst.getStartTime(), ((Jump_Branched) current.getLiteralNext()).getTest(), ifTrue, ifFalse, current, rawNext);
				
				if(current == start.getLastInstruction())
				{
					throw new IllegalArgumentException();
				}
				
				//Instruction.link(current, null);
				start.setProcessed(true);
				//System.err.println(replace.toVerboseCommentForm(false).toString());
				break;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}
	}
	
	private static Instruction cleanLines(Instruction start)
	{
		Instruction current = start;
		boolean isDone = false;

		while (!isDone)
		{
			switch (current.getIrCode())
			{
				case Jump_Straight:
				{
					Instruction.link(current, null);
				}
				case Return_Concretely:
				case Return_Variably:
				{
					//throw new UnhandledEnumException(current.getIrCode());
					
					isDone = true;
					break;
				}
				case Jump_Branched:
				{
					//current = current.getLiteralNext();	//should always be non-op
					current = current.getLiteralPrev();
					isDone = true;
		
					break;
				}
				default:
				{
					final Instruction next = current.getLiteralNext();

					if (next != null)
					{
						if (next != start)
						{
							current = next;
						}
						else
						{
							isDone = true;
						}
					}
					else
					{
						isDone = true;
					}
					break;
				}
			}
		}
		
		return current;
	}

	@Override
	protected CharList toCodeForm_internal(boolean inAppendMetadata, int inOffset)
	{
		final CharList result = new CharList();

		result.addNewLine();
		result.add('\t', inOffset);
		result.add("if (");
		result.add(this.BoolStatement.getValueAsString());
		result.add(')');
		result.add('\t');
		result.add("// ");

		if (this.IfTrue != null)
		{
			result.add(this.IfTrue.toVerboseCommentForm(false), true);
		}
		else
		{
			result.add("null");
		}
		result.add('|');

		if (this.IfFalse != null)
		{
			result.add(this.IfFalse.toVerboseCommentForm(false), true);
		}
		else
		{
			result.add("null");
		}

		result.addNewLine();
		result.add('\t', inOffset);
		result.add('{');
		// result.addNewLine();

		if (this.IfTrue != null)
		{
			Instruction current = this.IfTrue;

			while (current != null)
			{
				result.addNewLine();
				//result.add('\t', inOffset + 1);
				result.add(current.toCodeForm(inAppendMetadata, inOffset + 1), true);
				current = current.getLiteralNext();
			}
		}

		result.addNewLine();
		result.add('\t', inOffset);
		result.add('}');
		result.addNewLine();
		result.add('\t', inOffset);

		if (this.IfFalse != null)
		{
			result.add("else {");
			Instruction current = this.IfFalse;

			while (current != null)
			{
				result.addNewLine();
				//result.add('\t', inOffset + 1);
				result.add(current.toCodeForm(inAppendMetadata, inOffset + 1), true);
				current = current.getLiteralNext();
			}

			result.addNewLine();
			result.add('\t', inOffset);
			result.add('}');
		}

		/*
		if (this.getLiteralNext() == null)
		{
			result.addNewLine();
			result.add("null-ended!");
		}
		*/
		
		//result.addNewLine();

		return result;
	}

	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("//\t<> NeedToImplement: ");
		result.add(this.irCode.toString());
		result.add(" - ");
		result.add(this.getMangledName(false));

		return result;
	}
}
