package CleanStart.Mk06.Enums;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;

public enum IrCodes
{
	NonOp,
	VarArgsHandler,

	/**
	 * the equivalent of a MOV op.
	 * 
	 * @param target
	 *            can be a global, a local, an array element, or a map element
	 * @param source
	 *            can be a global, a local, an array element, a map element, or a constant (String/number/boolean)
	 */
	SetVar,

	/**
	 * this instruction bulk-copies {@code sourceVar} into the specified array positions.
	 * 
	 * @param targetArray
	 * @param startIndex
	 * @param endIndex
	 *            <- inclusive
	 * @param sourceVar
	 */
	SetVar_Bulk,

	/**
	 * @param length
	 * @param targetArray
	 * @param targetArrayStartOffset
	 * @param sourceArray
	 * @param sourceArrayStartOffset
	 * 
	 */
	CopyArray,

	/**
	 * creates a new map. The equivalent of NewTable
	 * 
	 * @param target
	 */
	CreateNewTable,

	/**
	 * @param target
	 * @param sourceFunction
	 * @param arrayOfUpvalues
	 */
	CreateFunctionInstance,

	/**
	 * goes from {@code firstLocalVarIndex} to the end of the stack, saving any upvalues found to, and I quote, "somewhere [safe]".
	 * 
	 * @param arrayToCheck
	 * @param firstLocalVarIndex
	 */
	Stasisify,

	/**
	 * stores a potentially complex boolean or algebraic statement, in postfix format
	 * 
	 * @param target
	 *            where to store the final result
	 * @param OpTypes[]
	 *            an array of the operators to use - can be either boolean or math operators, as long as the operands match type
	 * @param Operands[]
	 *            an array of the operands to use
	 */
	MathOp,

	/**
	 * @param target
	 * @param sourceLengthableThing
	 */
	LengthOf,

	/**
	 * concatenates two or more Strings
	 * 
	 * @param target
	 * @param sourceTable
	 * @param startIndex
	 * @param endIndex
	 *            <- is inclusive
	 */
	Concat,

	/**
	 * @param boolStatement
	 * @param jumpLabelIfTrue
	 *            will be a String label
	 * @param jumpLabelIfFalse
	 *            will be a String label
	 */
	Jump_Branched,

	/**
	 * @param jumpLabel
	 */
	Jump_Straight,

	Loop_While,

	/**
	 * designates a target to which can be jumped
	 */
	Label,

	/**
	 * @param StartRegister
	 *            (holds MethodId)
	 * @param NumArgs
	 *            if >= 0, indicates number of args. Otherwise, indicates args go to top of stack
	 * @param NumReturns
	 *            if >= 0, indicates number of returns. Otherwise, indicates returns go to top of stack
	 */
	CallObject,

	/**
	 * @param StartRegister
	 *            (holds MethodId)
	 * @param NumArgs
	 *            if >= 0, indicates number of args. Otherwise, indicates args go to top of stack
	 */
	TailcallFunctionObject,

	/**
	 * signifies the end of a function, and that no variables are returned
	 */
	Return_Concretely,

	/**
	 * @param startIndex
	 * @param numReturnValues
	 *            if > 0, indicates number of values. Otherwise, indicates values go to top of stack
	 */
	Return_Variably,

	/**
	 * @param initialIndexValue
	 * @param indexVar
	 * @param limitVar
	 * @param firstLoopInstruction
	 *            stored in [0]
	 * @param LoopEndLabel
	 *            stored in [1]
	 */
	PrepLoop_For,

	/**
	 * @param indexVar
	 *            will always be an int
	 * @param limitVar
	 * @param stepVar
	 * @param firstLoopInstruction
	 *            stored in [0]
	 * @param LoopEndLabel
	 *            stored in [1]
	 */
	EvalLoop_For,

	/**
	 * @param iteratorFunction
	 * @param state
	 * @param enumIndex
	 * @param internalLoopVars[]
	 */
	EvalLoop_ForEach,

	MetaOp;

	public boolean decompiledSourceIsMultiline()
	{
		final boolean result;

		switch (this)
		{
			case CallObject:
			case Concat:
			case CopyArray:
			case CreateFunctionInstance:
			case EvalLoop_For:
			case EvalLoop_ForEach:
			case MetaOp:
			case SetVar_Bulk:
			{
				result = true;
				break;
			}
			case CreateNewTable:
			case Jump_Branched:
			case Jump_Straight:
			case Label:
			case LengthOf:
			case Loop_While:
			case MathOp:
			case NonOp:
			case PrepLoop_For:
			case Return_Concretely:
			case Return_Variably:
			case SetVar:
			case Stasisify:
			case TailcallFunctionObject:
			case VarArgsHandler:
			{
				result = false;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(this);
			}
		}

		return result;
	}
}
