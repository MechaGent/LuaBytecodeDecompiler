package LuaBytecodeDecompiler.Mk03;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_GetTable;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_GetUpvalue;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_LoadBool;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_LoadNull;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Move;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Self;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_SetList;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_SetTable;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_SetUpvalue;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_GetGlobal;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_LoadK;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_SetGlobal;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Indexed;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Int;

public class OpCodeExpander
{
	public static AnnoInstruction[] expand(AnnoInstruction in)
	{
		final AnnoInstruction[] result;

		switch (in.getOpCode())
		{
			case Add:
			case ArithmeticNegation:
			case Concatenation:
			case Divide:
			case Jump:
			case Length:
			case LogicalNegation:
			case Modulo:
			case Move:
			case Multiply:
			case NewTable:
			case NoOp:
			case Power:
			case Subtract:
			{
				/*
				 * unexpandable opcodes
				 */

				result = new AnnoInstruction[] {
													in };
				break;
			}
			case GetGlobal:
			{
				// convert to a Move, as AnnoVar can track whether a var is global or not
				final AnnoInstruction_GetGlobal cast0 = (AnnoInstruction_GetGlobal) in;
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast0.getRawCode(), cast0.getInstructionAddress(), cast0.getTarget(), cast0.getSourceGlobal());
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case GetTable:
			{
				// convert to a move, with the source AnnoVar being an AnnoVar_Indexed
				final AnnoInstruction_GetTable cast0 = (AnnoInstruction_GetTable) in;
				final AnnoVar_Indexed newSource = new AnnoVar_Indexed("", ScopeLevels.Temporary, cast0.getSourceTable(), cast0.getSourceIndex());
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast0.getRawCode(), cast0.getInstructionAddress(), cast0.getTarget(), newSource);
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case GetUpvalue:
			{
				// convert to a Move
				final AnnoInstruction_GetUpvalue cast0 = (AnnoInstruction_GetUpvalue) in;
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast0.getRawCode(), cast0.getInstructionAddress(), cast0.getTarget(), cast0.getSource());
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case LoadBool:
			{
				final AnnoInstruction_LoadBool cast0 = (AnnoInstruction_LoadBool) in;
				final AnnoVar_Int hard = AnnoVar_Int.getHardCodedBool(cast0.getSource());
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast0.getRawCode(), cast0.getInstructionAddress(), cast0.getTarget(), hard);
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case LoadK:
			{
				final AnnoInstruction_LoadK cast = (AnnoInstruction_LoadK) in;
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast.getRawCode(), cast.getInstructionAddress(), cast.getTarget(), cast.getSourceConstant());
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case LoadNull:
			{
				final AnnoInstruction_LoadNull cast = (AnnoInstruction_LoadNull) in;
				final int rawCode = cast.getRawCode();
				final int instructAddr = cast.getInstructionAddress();
				final AnnoVar[] range = cast.getTargetRange();
				final AnnoVar NullVar = AnnoVar.getNullAnnoVar();

				result = new AnnoInstruction[range.length];

				for (int i = 0; i < result.length; i++)
				{
					result[i] = new AnnoInstruction_Move(rawCode, instructAddr, range[i], NullVar);
				}

				break;
			}
			case Self:
			{
				final AnnoInstruction_Self cast = (AnnoInstruction_Self) in;
				final int rawCode = cast.getRawCode();
				final int instructAddr = cast.getInstructionAddress();
				final AnnoVar sourceTable = cast.getFirstSource();
				final AnnoInstruction_Move first = new AnnoInstruction_Move(rawCode, instructAddr, cast.getFirstTarget(), sourceTable);
				final AnnoVar_Indexed newSource = new AnnoVar_Indexed("", ScopeLevels.Temporary, sourceTable, cast.getSecondSource().getIndex());
				final AnnoInstruction_Move next = new AnnoInstruction_Move(rawCode, instructAddr, cast.getSecondTarget(), newSource);
				result = new AnnoInstruction[] {
													first,
													next };
				break;
			}
			case SetGlobal:
			{
				final AnnoInstruction_SetGlobal cast = (AnnoInstruction_SetGlobal) in;
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast.getRawCode(), cast.getInstructionAddress(), cast.getTargetVar(), cast.getSourceVar());
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case SetList:
			{
				final AnnoInstruction_SetList cast = (AnnoInstruction_SetList) in;
				final int rawCode = cast.getRawCode();
				final int instructAddr = cast.getInstructionAddress();
				final AnnoVar[] range = cast.getSources();
				result = new AnnoInstruction[range.length];
				final int offset = cast.getC() * AnnoInstruction_SetList.getFieldsPerFlush();
				final AnnoVar tableTarget = cast.getTargetArray();

				for (int i = 0; i < result.length; i++)
				{
					final AnnoVar_Indexed cur = new AnnoVar_Indexed("", ScopeLevels.Temporary, tableTarget, new AnnoVar_Int("", ScopeLevels.Hardcoded_Int, offset + i - 1));
					result[i] = new AnnoInstruction_Move(rawCode, instructAddr, cur, range[i]);
				}

				break;
			}
			case SetTable:
			{
				final AnnoInstruction_SetTable cast = (AnnoInstruction_SetTable) in;
				final AnnoVar_Indexed index = new AnnoVar_Indexed("", ScopeLevels.Temporary, cast.getTargetArray(), cast.getTargetIndex());
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast.getRawCode(), cast.getInstructionAddress(), index, cast.getSource());
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case SetUpvalue:
			{
				final AnnoInstruction_SetUpvalue cast = (AnnoInstruction_SetUpvalue) in;
				final AnnoInstruction_Move next = new AnnoInstruction_Move(cast.getRawCode(), cast.getInstructionAddress(), cast.getTarget(), cast.getSource());
				result = new AnnoInstruction[] {
													next };
				break;
			}
			case Call:
			case Close:
			case Closure:
			case CompositeOpCode:
			case InitNumericForLoop:
			case IterateGenericForLoop:
			case IterateNumericForLoop:
			case Return:
			case TailCall:
			case TestAndSaveLogicalComparison:
			case TestIfEqual:
			case TestIfLessThan:
			case TestIfLessThanOrEqual:
			case TestLogicalComparison:
			case UnknownOpCode:
			case VarArg:
			{
				/*
				 * these can probably be broken down, just haven't done it yet
				 */
				result = new AnnoInstruction[]{in};
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}
}
