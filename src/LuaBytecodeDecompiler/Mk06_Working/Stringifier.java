package LuaBytecodeDecompiler.Mk06_Working;

import CharList.CharList;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import ExpressionParser.Formula;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CallMethod;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Concat;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CopyArray;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CreateFunctionInstance;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CreateNewMap;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.EvalLoop_For;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.EvalLoop_ForEach;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.EvalLoop_While;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.FormulaOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.JumpLabel;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Jump_Conditional;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Jump_Unconditional;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.LengthOf;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.NoOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.PrepLoop_For;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.PrepLoop_While;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Return_Stuff;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Return_Void;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.SetVar;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.SetVar_Bulk;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Stasisify;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.TailcallMethod;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;
import SingleLinkedList.Mk01.SingleLinkedList;

public class Stringifier
{
	private static final boolean shouldPrintExpired = false;
	private static final String builderStub = ".set";
	protected static final String GlobalVarsPrefix = "GlobalVars";
	protected static final String GlobalMethodsPrefix = "GlobalMethods";

	private static final int inlineLimit_Concat = 5;
	private static int instanceCounter_Builder = 0;

	private int currentScope;

	public Stringifier()
	{
		this.currentScope = 0;
	}

	public CharList translateAllInstructions(SingleLinkedList<IrInstruction> in)
	{
		final CharList result = new CharList();

		while (!in.isEmpty())
		{
			result.addNewLine();
			result.add(translateNextInstruction(in), true);
		}

		return result;
	}

	/*
	public CharList translateNextInstruction(SingleLinkedList<IrInstruction> in)
	{
		return this.translateNextInstruction(in.pop());
	}
	*/

	public CharList translateNextInstruction(SingleLinkedList<IrInstruction> in)
	{
		final CharList result;
		IrInstruction first = in.pop();

		while (!shouldPrintExpired && first.hasExpired())
		{
			if (!in.isEmpty())
			{
				first = in.pop();
			}
			else
			{
				return new CharList();
			}
		}

		// System.out.println("interpreting instruction: " + first.mangledName);

		switch (first.getOpCode())
		{
			case CallMethod:
			{
				result = getCode((CallMethod) first);
				break;
			}
			case Concat:
			{
				result = getCode((Concat) first);
				break;
			}
			case CopyArray:
			{
				result = getCode((CopyArray) first);
				break;
			}
			case CreateFunctionInstance:
			{
				result = getCode((CreateFunctionInstance) first);
				break;
			}
			case CreateNewMap:
			{
				result = getCode((CreateNewMap) first);
				break;
			}
			case EvalLoop_For:
			{
				result = getCode((EvalLoop_For) first);
				break;
			}
			case EvalLoop_ForEach:
			{
				result = getCode((EvalLoop_ForEach) first);
				break;
			}
			case EvalLoop_While:
			{
				result = getCode((EvalLoop_While) first);
				break;
			}
			case FormulaOp:
			{
				result = getCode((FormulaOp) first);
				break;
			}
			case Jump_Conditional:
			{
				result = getCode((Jump_Conditional) first, in);
				break;
			}
			case Jump_Unconditional:
			{
				result = getCode((Jump_Unconditional) first);
				break;
			}
			case Jump_Label:
			{
				result = getCode((JumpLabel) first);
				break;
			}
			case LengthOf:
			{
				result = getCode((LengthOf) first);
				break;
			}
			case NoOp:
			{
				result = getCode((NoOp) first);
				break;
			}
			case PrepLoop_For:
			{
				result = getCode((PrepLoop_For) first);
				break;
			}
			case PrepLoop_While:
			{
				result = getCode((PrepLoop_While) first);
				break;
			}
			case Return_Stuff:
			{
				result = getCode((Return_Stuff) first);
				break;
			}
			case Return_Void:
			{
				result = getCode((Return_Void) first);
				break;
			}
			case SetVar_Bulk:
			{
				result = getCode((SetVar_Bulk) first);
				break;
			}
			case SetVar:
			{
				result = getCode((SetVar) first);
				break;
			}
			case Stasisify:
			{
				result = getCode((Stasisify) first);
				break;
			}
			case TailcallMethod:
			{
				result = getCode((TailcallMethod) first);
				break;
			}
			case PlaceHolderOp:
			{
				result = first.toCharList(true, false);
				result.push("//");
				break;
			}
			default:
			{
				throw new UnhandledEnumException(first.getOpCode());
			}
		}

		if (first.hasExpired())
		{
			result.push(">\t");
			result.push(first.getExpiredStateReason());
			result.push("//\t<");
		}

		return result;
	}

	private CharList getCode(CallMethod inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		// result.add("//TODO: CallMethod");
		IrVar[] sourceRegisters = inFirst.getReturns();

		result.add('{');

		if (inFirst.isVarReturns())
		{
			result.add("<varReturns>");
		}
		else
		{
			for (int i = 0; i < sourceRegisters.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(sourceRegisters[i].valueToString());
			}
		}

		result.add("} = ");

		result.add(inFirst.getStart().valueToString());
		result.add(".run(");
		sourceRegisters = inFirst.getArgs();

		if (inFirst.isVarArgs())
		{
			result.add("<varArgs>");
		}
		else
		{
			// System.out.println("length: " + sourceRegisters.length + ", reach of: " + (functionIndex + 1 + numArgs));
			for (int i = 0; i < sourceRegisters.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(sourceRegisters[i].valueToString());
			}
		}

		result.add(");");

		return result;
	}

	private CharList getCode(Concat in)
	{
		final CharList result = new CharList();
		final IrVar target = in.getTarget();
		final IrVar[] sourceTable = in.getSourceTable();

		result.add('\t', this.currentScope);

		if (sourceTable.length <= inlineLimit_Concat)
		{
			result.add(target.getLabel());
			result.add(" = ");

			for (int i = 0; i < sourceTable.length; i++)
			{
				if (i != 0)
				{
					result.add(" + ");
				}

				result.add(sourceTable[i].getLabel());
			}

			result.add(';');
		}
		else
		{
			result.add(target.getLabel());
			result.add(" = ");
			result.add(sourceTable[0].getLabel());
			result.add(';');
			result.addNewLine();

			for (int i = 1; i < sourceTable.length; i++)
			{
				result.addNewLine();
				result.add(target.getLabel());
				result.add(" += ");
				result.add(sourceTable[i].getLabel());
				result.add(';');
			}

			result.addNewLine();
		}

		return result;
	}

	private CharList getCode(CopyArray in)
	{
		final CharList result = new CharList();

		final IrVar length = in.getLength();
		final IrVar targetArray = in.getTargetArray();
		final IrVar targetArrayStartOffset = in.getTargetArrayStartOffset();
		final IrVar sourceArray = in.getTargetArray();
		final IrVar sourceArrayStartOffset = in.getTargetArrayStartOffset();
		final String name = "copier" + instanceCounter_Builder++;

		result.add('\t', this.currentScope);
		result.add("final ArrayCopier ");
		result.add(name);
		result.add(" = new ArrayCopier();");
		result.add('\t', this.currentScope);
		result.add(name);
		result.add(".setLength(");
		result.add(length.valueToString());
		result.add(");");
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add(name);
		result.add(".setTargetArray(");
		result.add(targetArray.valueToString());
		result.add(");");
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add(name);
		result.add(".setTargetArrayOffset(");
		result.add(targetArrayStartOffset.valueToString());
		result.add(");");
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add(name);
		result.add(".setSourceArray(");
		result.add(sourceArray.valueToString());
		result.add(");");
		result.add(name);
		result.add(".setSourceArrayOffset(");
		result.add(sourceArrayStartOffset.valueToString());
		result.add(");");
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add(name);
		result.add(".copyElements();");

		return result;
	}

	private CharList getCode(CreateFunctionInstance in)
	{
		final CharList result = new CharList();
		final Function sourceFunction = in.getSourceFunction().getCore();
		final IrVar[] upvals = in.getUpvals();
		final IrVar target = in.getTarget();
		final String name = "buildy" + instanceCounter_Builder++;

		result.addNewLine();
		result.add('\t', this.currentScope++);
		result.add("final FunctionBuilder ");
		result.add(name);
		result.add(" = new FunctionBuilder();");
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add(name);
		result.add(builderStub);
		result.add("SourceFunction(");
		result.add(sourceFunction.getFunctionName());
		result.add(".function);");

		for (int i = 0; i < upvals.length; i++)
		{
			result.addNewLine();
			result.add('\t', this.currentScope);
			result.add(name);
			result.add(builderStub);
			result.add("Upvalue(");
			result.add(upvals[i].valueToString());
			result.add(");");
		}

		result.addNewLine();
		result.add('\t', this.currentScope--);
		result.add(target.valueToString());
		result.add(" = ");
		result.add(name);
		result.add(".buildFunctionInstance();");
		result.addNewLine();

		return result;
	}

	private CharList getCode(CreateNewMap in)
	{
		final CharList result = new CharList();

		result.add(in.getTarget().valueToString());
		result.add(" = new KeyValueMap();");

		return result;
	}

	private CharList getCode(EvalLoop_For inFirst)
	{
		final CharList result = new CharList();

		// result.add("//TODO: EvalLoop_For");
		result.addNewLine();
		this.currentScope--;
		result.add('\t', this.currentScope);
		result.add('}');
		result.addNewLine();

		return result;
	}

	private CharList getCode(EvalLoop_ForEach inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		result.add("//TODO: EvalLoop_ForEach");

		return result;
	}

	private CharList getCode(EvalLoop_While inFirst)
	{
		final CharList result = new CharList();

		// result.add("//TODO: EvalLoop_While");
		result.addNewLine();
		this.currentScope--;
		result.add('\t', this.currentScope);
		result.add('}');
		result.addNewLine();

		return result;
	}

	private CharList getCode(FormulaOp inFirst)
	{
		final CharList result = new CharList();

		// result.add("//TODO: FormulaOp");
		result.add('\t', this.currentScope);
		result.add(inFirst.getTarget().valueToString());
		result.add(" = ");
		result.add(inFirst.getFormula().toCharList(), true);
		result.add(';');

		return result;
	}

	private CharList getCode(Jump_Conditional inFirst, SingleLinkedList<IrInstruction> in)
	{
		final CharList result = new CharList();
		// System.out.println("interpreting jumpConditional: " + inFirst.mangledName);

		result.add('\t', this.currentScope);
		// result.add("//TODO: Jump_Conditional");

		result.add("if (");
		final Formula boolStatement = inFirst.getBoolStatement();
		result.add(boolStatement.toCharList(), true);
		result.add(')');
		result.addNewLine();

		// final int before = this.currentScope;

		result.add('\t', this.currentScope++);
		// final int during = this.currentScope;
		result.add('{');

		final IrInstruction jumpIfTrue = inFirst.getNext(0);
		final IrInstruction jumpIfFalse = inFirst.getNext(1);
		final IrInstruction meetpoint = jumpIfTrue.getFirstCommonInstructionWith(jumpIfFalse);

		// IrInstruction next;

		IrInstruction next = null;

		while (!in.isEmpty() && (next = in.getFirst()) != jumpIfFalse)
		{
			// System.out.println("here");
			final IrCodes code = next.getOpCode();

			if (code == IrCodes.Jump_Unconditional && next.getNext() == meetpoint)
			{
				in.pop();
				break;
			}

			result.addNewLine();
			result.add(this.translateNextInstruction(in), true);

			if (code == IrCodes.Return_Stuff || code == IrCodes.Return_Void)
			{
				break;
			}
		}

		result.addNewLine();
		// final int after = --this.currentScope;
		// System.out.println("before: " + before + ", during: " + during + ", after: " + after);
		result.add('\t', --this.currentScope);
		result.add('}');
		result.addNewLine();

		in.pop(); // to discard the jumpLabel
		boolean hasElse = false;

		while (!in.isEmpty() && (next = in.getFirst()) != meetpoint)
		{
			if (!hasElse)
			{
				hasElse = true;
				result.add('\t', this.currentScope);
				result.add("else");
				result.addNewLine();
				result.add('\t', this.currentScope++);
				result.add('{');
			}

			final IrCodes code = next.getOpCode();

			if (code == IrCodes.Jump_Unconditional && next.getNext() == meetpoint)
			{
				in.pop();
				break;
			}

			result.addNewLine();
			result.add(this.translateNextInstruction(in), true);

			if (code == IrCodes.Return_Stuff || code == IrCodes.Return_Void)
			{
				break;
			}
		}

		// System.out.println(next.mangledName);

		if (hasElse)
		{
			result.addNewLine();
			result.add('\t', --this.currentScope);
			result.add('}');
		}

		return result;
	}

	private CharList getCode(Jump_Unconditional inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		result.add("//TODO: Jump_Unconditional");
		result.add('\t');
		result.add(inFirst.toCharList(false, false), true);

		return result;
	}

	private CharList getCode(JumpLabel inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		// result.add("//TODO: ");
		result.add("// ");
		result.add(inFirst.mangledName);

		return result;
	}

	private CharList getCode(LengthOf inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		result.add("//TODO: LengthOf");
		// result.add(GlobalMethodsPrefix);

		return result;
	}

	private CharList getCode(NoOp inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		result.add("//TODO: NoOp");

		return result;
	}

	private CharList getCode(PrepLoop_For inFirst)
	{
		final CharList result = new CharList();

		// result.add("//TODO: PrepLoop_For");
		result.add('\t', this.currentScope);
		result.add("for (");
		result.add(inFirst.getHeader(), true);
		result.add(')');
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add('{');
		result.addNewLine();
		this.currentScope++;

		return result;
	}

	private CharList getCode(PrepLoop_While inFirst)
	{
		final CharList result = new CharList();

		// result.add("//TODO: PrepLoop_While");
		result.add('\t', this.currentScope);
		result.add("while (");
		result.add(inFirst.getBoolStatement().toCharList(), true);
		result.add(')');
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add('{');
		result.addNewLine();
		this.currentScope++;

		return result;
	}

	private CharList getCode(Return_Stuff inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		// result.add("//TODO: Return_Stuff");
		result.add("return ");

		final int numReturns = inFirst.getNumReturns();

		if (numReturns == -1)
		{
			result.add("<stuff, from var: ");
			result.add(inFirst.getStartRegister().valueToString());
			result.add(" to the top of the stack>;");
		}
		else
		{
			result.add('{');
			final IrVar[] sourceTable = inFirst.getSourceTable();

			for (int i = 0; i < sourceTable.length; i++)
			{
				if (i != 0)
				{
					result.add(',');
				}

				result.add(' ');
				result.add(sourceTable[i].valueToString());
			}

			result.add("};");
		}

		return result;
	}

	private CharList getCode(Return_Void inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		result.add("return;  //(nothing)");

		return result;
	}

	private CharList getCode(SetVar_Bulk inFirst)
	{
		final CharList result = new CharList();

		result.addNewLine();
		result.add('\t', this.currentScope);
		// result.add("//TODO: SetVar_Bulk");

		result.add("for (int i = ");
		result.addAsString(inFirst.getStartIndex());
		result.add("; i <= ");
		result.addAsString(inFirst.getEndIndex());
		result.add("; i++)\r\n");
		result.add('\t', this.currentScope++);
		result.add('{');
		result.addNewLine();
		result.add('\t', this.currentScope--);
		result.add("Locals[i] = ");
		result.add(inFirst.getSource().valueToString());
		result.add(';');
		result.addNewLine();
		result.add('\t', this.currentScope);
		result.add('}');
		result.addNewLine();

		return result;
	}

	private CharList getCode(SetVar inFirst)
	{
		final CharList result = new CharList();

		// result.add("//TODO: SetVar");
		result.add('\t', this.currentScope);
		result.add(inFirst.getTarget().getPriorWrite(inFirst.linePosData).valueToString());
		result.add(" = ");
		result.add(inFirst.getSource().getPriorWrite(inFirst.linePosData).valueToString());
		result.add(';');
		// result.addNewLine();

		return result;
	}

	private CharList getCode(Stasisify inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		result.add("//TODO: Stasisify");

		return result;
	}

	private CharList getCode(TailcallMethod inFirst)
	{
		final CharList result = new CharList();

		result.add('\t', this.currentScope);
		// result.add("//TODO: CallMethod");
		IrVar[] sourceRegisters = inFirst.getArgs();

		result.add('{');

		result.add("<varReturns>");

		result.add("} = ");

		result.add(inFirst.getStartRegister().valueToString());
		result.add(".run(");
		sourceRegisters = inFirst.getArgs();

		if (inFirst.isVarArgs())
		{
			result.add("<varArgs>");
		}
		else
		{
			// System.out.println("length: " + sourceRegisters.length + ", reach of: " + (functionIndex + 1 + numArgs));
			for (int i = 0; i < sourceRegisters.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(sourceRegisters[i].valueToString());
			}
		}

		result.add(");");

		return result;
	}
}
