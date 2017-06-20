package CleanStart.Mk04_Working;

import CharList.CharList;
import CharList.Mk01.linearCharsIterator;
import CleanStart.Mk04_Working.Instructions.CallObject;
import CleanStart.Mk04_Working.Instructions.Concat;
import CleanStart.Mk04_Working.Instructions.CopyArray;
import CleanStart.Mk04_Working.Instructions.CreateFunctionObject;
import CleanStart.Mk04_Working.Instructions.ForLoop;
import CleanStart.Mk04_Working.Instructions.Jump_Branched;
import CleanStart.Mk04_Working.Instructions.Jump_Straight;
import CleanStart.Mk04_Working.Instructions.LengthOf;
import CleanStart.Mk04_Working.Instructions.MathOp;
import CleanStart.Mk04_Working.Instructions.Return_Concretely;
import CleanStart.Mk04_Working.Instructions.Return_Variably;
import CleanStart.Mk04_Working.Instructions.SetVar;
import CleanStart.Mk04_Working.Instructions.SetVar_Bulk;
import CleanStart.Mk04_Working.Instructions.TailcallFunctionObject;
import CleanStart.Mk04_Working.Labels.Label;
import CleanStart.Mk04_Working.Variables.MystVar;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodePrinter
{
	private int numFloatingClosers;

	public CodePrinter()
	{
		this.numFloatingClosers = 0;
	}

	private CharList addNextInstruction_toCode(Instruction current, int offset, boolean showInferredTypes)
	{
		final CharList result = new CharList();
		result.add('\t', offset);

		switch (current.getIrCode())
		{
			case CallObject:
			{
				final CallObject cast = (CallObject) current;

				result.add(cast.getTargetObject().getMostRelevantIdValue(showInferredTypes));
				result.add(".call(");
				final MystVar[] args = cast.getArgs();

				for (int i = 0; i < args.length; i++)
				{
					if (i != 0)
					{
						result.add(", ");
					}

					result.add(args[i].getMostRelevantIdValue(showInferredTypes));
				}

				result.add("); //returns: {");

				final MystVar[] Returns = cast.getReturns();

				for (int i = 0; i < Returns.length; i++)
				{
					if (i != 0)
					{
						result.add(", ");
					}

					result.add(Returns[i].getMostRelevantIdValue(showInferredTypes));
				}

				if (cast.isReturnsAreVariable())
				{
					result.add("...");
				}

				result.add('}');

				break;
			}
			case Concat:
			{
				final Concat cast = (Concat) current;

				result.add(cast.getTarget().getMostRelevantIdValue(showInferredTypes));
				result.add(" is set to (");
				final Variable[] sources = cast.getSources();

				for (int i = 0; i < sources.length; i++)
				{
					if (i != 0)
					{
						result.add(" + ");
					}

					result.add(sources[i].getMostRelevantIdValue(showInferredTypes));
				}

				result.add(");");

				break;
			}
			case CopyArray: // multiline
			{
				final CopyArray cast = (CopyArray) current;
				final int firstSourceIndex = cast.getFirstSourceIndex();

				result.addNewLine();
				result.add('\t', offset);
				result.add("for (int i = 0; i < ");

				if (cast.sourcesAreVariable())
				{
					final int dif = 1 - firstSourceIndex;

					if (dif != 0)
					{
						result.add("(InternalGlobals.TopOfStackIndex");
						result.add(" - ");
						result.addAsString(-dif);
						result.add(')');
					}
					else
					{
						result.add("InternalGlobals.TopOfStackIndex");
					}
				}
				else
				{
					result.addAsString(cast.getSourceArray().length);
				}

				result.add("; i++)");
				result.addNewLine();
				result.add('\t', offset);
				result.add('{');
				result.addNewLine();
				result.add('\t', offset + 1);
				result.add(cast.getTargetTable().getMostRelevantIdValue(showInferredTypes));
				result.add("[i");
				final int offsetOffset = cast.getOffsetOffset();

				if (offsetOffset != 0)
				{
					result.add(" + ");
					result.addAsString(offsetOffset);
				}

				result.add("] = ");
				result.add(cast.getSourceArrayName());
				result.add("[i + ");
				result.addAsString(cast.getFirstSourceIndex());
				result.add("];");
				result.addNewLine();
				result.add('\t', offset);
				result.add('}');
				result.addNewLine();

				break;
			}
			case CreateFunctionInstance:
			{
				final CreateFunctionObject cast = (CreateFunctionObject) current;

				result.add(cast.getTarget().getMostRelevantIdValue(showInferredTypes));
				result.add(" = new ");
				result.add(cast.getPrototype().getName());
				result.add('(');

				final MystVar[] upvals = cast.getUpvalues_initialArgs();

				for (int i = 0; i < upvals.length; i++)
				{
					if (i != 0)
					{
						result.add(", ");
					}

					result.add(upvals[i].getMostRelevantIdValue(showInferredTypes));
				}

				result.add(");");

				break;
			}
			case CreateNewTable:
			{
				result.add(current.getVerboseCommentForm(), true);
				break;
			}
			case EvalLoop_For:
			{
				final ForLoop cast = (ForLoop) current;

				final String indexVarName = cast.getIndexVar().getMostRelevantIdValue(showInferredTypes);
				result.add("for (;");
				result.add(indexVarName);
				result.add(" < ");
				result.add(cast.getLimitVar().getMostRelevantIdValue(showInferredTypes));
				result.add("; ");
				result.add(indexVarName);
				result.add("++ )");
				result.addNewLine();
				result.add('\t', offset);
				result.add('{');
				result.addNewLine();

				cast.getToAfterLoop().incrementNumCloseBracesToPrepend();

				break;
			}
			case EvalLoop_ForEach:
			{
				// final ForEachLoop cast = (ForEachLoop) current;

				// TODO
				result.add("//<> To Do - ");
				result.add(current.getVerboseCommentForm(), true);

				break;
			}
			case Jump_Conditional:
			{
				final Jump_Branched cast = (Jump_Branched) current;

				switch (cast.getBranchType())
				{
					case DoWhile:
					{
						result.add('}');
						result.addNewLine();
						result.add("while (");
						result.add(cast.getTest().getMostRelevantIdValue(showInferredTypes));
						result.add(");");
						cast.getJump_False().incrementNumCloseBracesToPrepend();
						break;
					}
					case ElselessIf:
					{
						// TODO
						result.add("//<> To Do - ");
						result.add(current.getVerboseCommentForm(), true);
						break;
					}
					case IfElse: // the jump_straight will take care of the ifblock's closing brace
					{
						result.add("if (");
						result.add(cast.getTest().getMostRelevantIdValue(showInferredTypes));
						result.add(")");
						result.addNewLine();
						result.add('\t', offset);
						result.add('{');

						final Instruction joiner = cast.getJump_True().getFirstSharedInstructionWith(cast.getJump_False());

						if (joiner != null)
						{
							switch (joiner.getIrCode())
							{
								case Label:
								{
									final Label cast2 = (Label) joiner;
									cast2.incrementNumCloseBracesToPrepend();
									cast2.prependElseOpener();
									break;
								}
								case Return_Concretely:
								{
									final Return_Concretely cast2 = (Return_Concretely) joiner;
									cast2.incrementNumCloseBracesToAppend();
									break;
								}
								case Return_Variably:
								{
									final Return_Variably cast2 = (Return_Variably) joiner;
									cast2.incrementNumCloseBracesToAppend();
									break;
								}
								default:
								{
									throw new UnhandledEnumException(joiner.getIrCode());
								}
							}
						}
						else
						{
							this.numFloatingClosers++;
						}
						break;
					}
					case IflessElse:
					{
						// TODO
						result.add("//<> To Do - ");
						result.add(current.getVerboseCommentForm(), true);
						break;
					}
					case While:
					{
						result.add("//The preceding ");
						result.addAsString(cast.getNumPrecedingLines());
						result.add(" lines should be treated as inlined to the following line, if applicable");
						result.addNewLine();
						result.add('\t', offset);
						result.add("while (");
						result.add(cast.getTest().getMostRelevantIdValue(showInferredTypes));
						result.add(")");

						result.add("//jump_true = ");
						result.add(cast.getJump_True().getLabelValue());
						result.add(" jump_false - ");
						result.add(cast.getJump_False().getLabelValue());

						result.addNewLine();
						result.add('\t', offset);
						result.add('{');

						final Instruction joiner = cast.getJump_True().getFirstSharedInstructionWith(cast.getJump_False());

						if (joiner != null)
						{
							switch (joiner.getIrCode())
							{
								case Label:
								{
									final Label cast2 = (Label) joiner;
									cast2.incrementNumCloseBracesToPrepend();
									break;
								}
								case Return_Concretely:
								{
									final Return_Concretely cast2 = (Return_Concretely) joiner;
									cast2.incrementNumCloseBracesToAppend();
									break;
								}
								case Return_Variably:
								{
									final Return_Variably cast2 = (Return_Variably) joiner;
									cast2.incrementNumCloseBracesToAppend();
									break;
								}
								default:
								{
									throw new UnhandledEnumException(joiner.getIrCode());
								}
							}
						}
						break;
					}
					default:
					{
						throw new UnhandledEnumException(cast.getBranchType());
					}
				}

				/*
				result.add("<> ToDo: branch statements: ");
				result.add(current.getVerboseCommentForm(), true);
				result.add(" with Next of: ");
				result.add(current.getNext().getVerboseCommentForm(), true);
				*/

				break;
			}
			case Jump_Straight:
			{
				final Jump_Straight cast = (Jump_Straight) current;
				//result.add('}');
				result.add('\t');
				result.add("// jump to - ");
				result.add(cast.getJumpTo().getLabelValue());

				break;
			}
			case Label:
			{
				final Label cast = (Label) current;

				result.add(cast.getCodeForm(offset, true), true);

				break;
			}
			case LengthOf:
			{
				final LengthOf cast = (LengthOf) current;

				result.add(cast.getTarget().getMostRelevantIdValue(showInferredTypes));
				result.add(" = lengthOf(");
				result.add(cast.getSource().getMostRelevantIdValue(showInferredTypes));
				result.add(");");
				break;
			}
			case MathOp:
			{
				final MathOp cast = (MathOp) current;
				result.add(cast.getTarget().getMostRelevantIdValue(showInferredTypes));
				result.add(" = ");
				result.add(cast.getCargo().getMostRelevantIdValue(showInferredTypes));
				result.add(';');
				break;
			}
			case NonOp:
			{
				result.add("//NonOp");
				break;
			}
			case Return_Concretely:
			{
				final Return_Concretely cast = (Return_Concretely) current;
				final MystVar[] sources = cast.getSources();

				switch (sources.length)
				{
					case 0:
					{
						result.add("// return");
						break;
					}
					case 1:
					{
						result.add("return ");
						result.add(sources[0].getMostRelevantIdValue(showInferredTypes));
						result.add(';');
						break;
					}
					default:
					{
						result.add("return new Object[]{");

						for (int i = 0; i < sources.length; i++)
						{
							if (i != 0)
							{
								result.add(", ");
							}

							result.add(sources[i].getMostRelevantIdValue(showInferredTypes));
						}

						result.add("};\t//Concretely");

						break;
					}
				}

				final int append = cast.getNumCloseBracesToAppend();

				if (append != 0)
				{
					result.addNewLine();
					result.add('\t', offset);
					result.add('}', append);
				}
				
				if(this.numFloatingClosers > 0)
				{
					result.add('}');
					this.numFloatingClosers--;
				}

				break;
			}
			case Return_Variably:
			{
				final Return_Variably cast = (Return_Variably) current;
				final MystVar[] sources = cast.getSources();

				result.add("return new Object[]{");

				for (int i = 0; i < sources.length; i++)
				{
					if (i != 0)
					{
						result.add(", ");
					}

					result.add(sources[i].getMostRelevantIdValue(showInferredTypes));
				}

				result.add("};\t//Variably");

				final int append = cast.getNumCloseBracesToAppend();

				if (append != 0)
				{
					result.addNewLine();
					result.add('\t', offset);
					result.add('}', append);
				}
				
				if(this.numFloatingClosers > 0)
				{
					result.add('}');
					this.numFloatingClosers--;
				}

				break;
			}
			case SetVar:
			{
				final SetVar cast = (SetVar) current;

				result.add(cast.getTarget_AsVariable().getMostRelevantIdValue(showInferredTypes));
				result.add(" = ");
				result.add(cast.getSource().getMostRelevantIdValue(showInferredTypes));
				result.add(';');
				break;
			}
			case SetVar_Bulk:
			{
				final SetVar_Bulk cast = (SetVar_Bulk) current;
				final MystVar[] targets = cast.getTargets();

				for (int i = 0; i < targets.length; i++)
				{
					result.add(targets[i].getMostRelevantIdValue(showInferredTypes));
					result.add(" = ");
				}

				result.add(cast.getSource().getMostRelevantIdValue(showInferredTypes));
				result.add(';');
				break;
			}
			case Stasisify:
			{
				// TODO
				result.add("//<> To Do - ");
				result.add(current.getIrCode().toString());
				break;
			}
			case TailcallFunctionObject:
			{
				final TailcallFunctionObject cast = (TailcallFunctionObject) current;

				result.add(cast.getFunctionObject().getMostRelevantIdValue(showInferredTypes));
				result.add(".tailcall(");
				final MystVar[] args = cast.getArgs();

				for (int i = 0; i < args.length; i++)
				{
					if (i != 0)
					{
						result.add(", ");
					}

					result.add(args[i].getMostRelevantIdValue(showInferredTypes));
				}

				result.add(')');
				break;
			}
			case VarArgsHandler:
			{
				// TODO
				result.add("//<> To Do - ");
				result.add(current.getIrCode().toString());
				break;
			}
			default:
			{
				throw new UnhandledEnumException(current.getIrCode());
			}
		}

		return result;
	}

	public CharList toCode(Instruction[] lines, int offset, boolean showInferredTypes)
	{
		final CharList result = new CharList();

		for (int i = 0; i < lines.length; i++)
		{
			if (i != 0)
			{
				result.addNewLine();
			}

			result.add(addNextInstruction_toCode(lines[i], offset, showInferredTypes), true);
		}

		result.addNewLine();
		result.add('}', this.numFloatingClosers);

		return prettify(result, offset);
	}

	public CharList toCode(InstructionList first)
	{
		final CharList result = new CharList();

		first.popLeadingNonOps();

		while (!first.isEmpty())
		{
			result.add(addNextInstruction_toCode(first.getHead(), 0, true), true);
			first.setHead(first.getHead().getNext());
			result.addNewLine();
		}

		result.addNewLine();
		result.add('}', this.numFloatingClosers);

		return result;
	}

	public static CharList prettify(CharList in, int offset)
	{
		final CharList result = new CharList();

		final linearCharsIterator itsy = in.getLinearCharsIterator();
		boolean isLeading = true;
		boolean isReturning = false;

		while (itsy.hasNextChar())
		{
			final char curr = itsy.nextChar();

			//System.out.println(curr);

			switch (curr)
			{
				case '\r':
				{
					break;
				}
				case '\n':
				{
					if (!isLeading)
					{
						result.add(curr);
						isLeading = true;
					}

					break;
				}
				case '\t':
				case ' ':
				{
					if (!isLeading)
					{
						result.add(curr);
					}

					break;
				}
				case ':':
				{
					isReturning = true;
					result.add(curr);
					break;
				}
				case '{':
				{
					/*
					result.add("<isLeading: ");
					result.addAsString(isLeading);
					result.add("> <isReturning: ");
					result.addAsString(isReturning);
					result.add('>');
					*/
					
					if (isReturning)
					{
						result.add(curr);
					}
					else
					{
						if (!isLeading)
						{
							result.addNewLine();

							isLeading = true;
						}

						result.add('\t', offset++);
						result.add(curr);
						result.addNewLine();
					}

					break;
				}
				case '}':
				{
					if (isReturning)
					{
						result.add(curr);
						isReturning = false;
					}
					else
					{
						if (!isLeading)
						{
							result.addNewLine();
							isLeading = true;
						}

						result.add('\t', --offset);
						result.add(curr);
						result.addNewLine();
					}

					break;
				}
				default:
				{
					if (isLeading)
					{
						isLeading = false;
						result.add('\t', offset);
					}

					result.add(curr);
				}
			}

			if (offset < 0)
			{
				throw new NullPointerException(result.toString());
				//System.err.println("negative final offset: " + offset);
			}
		}

		return result;
	}
}
