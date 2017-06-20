package CleanStart.Mk04_Working.ControlStructures;

import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Instructions.Jump_Branched;
import SingleLinkedList.Mk01.SingleLinkedList;

public class Converter
{
	public static FlowStruct convert(SingleLinkedList<Instruction> in)
	{
		FlowStructTypes currentType = FlowStructTypes.None;
		FlowStruct result = null;
		FlowStruct current = null;

		while (in.isNotEmpty())
		{
			final Instruction first = in.pop();

			switch (first.getIrCode())
			{
				case Jump_Conditional:
				{
					final Jump_Branched cast = (Jump_Branched) first;

					switch (cast.getBranchType())
					{
						case DoWhile:
							break;
						case ElselessIf:
							break;
						case IfElse:
							break;
						case IflessElse:
							break;
						case While:
						{
							
							break;
						}
						default:
							break;
					}

					break;
				}

				case CallObject:
					break;
				case Concat:
					break;
				case CopyArray:
					break;
				case CreateFunctionInstance:
					break;
				case CreateNewTable:
					break;
				case EvalLoop_For:
					break;
				case EvalLoop_ForEach:
					break;
				case EvalLoop_While:
					break;

				case Jump_Straight:
					break;
				case Label:
					break;
				case LengthOf:
					break;
				case MathOp:
					break;
				case NonOp:
					break;
				case NullOp:
					break;
				case PlaceHolderOp:
					break;
				case PrepLoop_For:
					break;
				case PrepLoop_While:
					break;
				case Return_Concretely:
					break;
				case Return_Variably:
					break;
				case SetVar:
					break;
				case SetVar_Bulk:
					break;
				case Stasisify:
					break;
				case TailcallFunctionObject:
					break;
				case VarArgsHandler:
					break;
				default:
					break;
			}
		}
	}

	private static enum FlowStructTypes
	{
		None,
		Linear;
	}
	
	private static class Block
	{
		private FlowStructTypes type;
		private final SingleLinkedList<Instruction> lines;
		
		public Block()
		{
			this.type = FlowStructTypes.None;
			this.lines = new SingleLinkedList<Instruction>();
		}
	}
}
