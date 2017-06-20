package LuaBytecodeDecompiler.Mk01.v5_1;

import java.util.EnumMap;

import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;

public class OpCodeMapper_v5_1 implements OpCodeMapper
{
	private static final OpCodeMapper_v5_1 single = new OpCodeMapper_v5_1();

	private final AbstractOpCodes[] IntToCodesMap;
	private final EnumMap<AbstractOpCodes, Integer> CodesToIntMap;

	private OpCodeMapper_v5_1()
	{
		final AbstractOpCodes[] ordering = new AbstractOpCodes[] {
																	AbstractOpCodes.Move,
																	AbstractOpCodes.LoadK,
																	AbstractOpCodes.LoadBool,
																	AbstractOpCodes.LoadNull,
																	AbstractOpCodes.GetUpvalue,
																	AbstractOpCodes.GetGlobal,
																	AbstractOpCodes.GetTable,
																	AbstractOpCodes.SetGlobal,
																	AbstractOpCodes.SetUpvalue,
																	AbstractOpCodes.SetTable,
																	AbstractOpCodes.NewTable,
																	AbstractOpCodes.Self,
																	AbstractOpCodes.Add,
																	AbstractOpCodes.Subtract,
																	AbstractOpCodes.Multiply,
																	AbstractOpCodes.Divide,
																	AbstractOpCodes.Modulo,
																	AbstractOpCodes.Power,
																	AbstractOpCodes.ArithmeticNegation,
																	AbstractOpCodes.LogicalNegation,
																	AbstractOpCodes.Length,
																	AbstractOpCodes.Concatenation,
																	AbstractOpCodes.Jump,
																	AbstractOpCodes.TestIfEqual,
																	AbstractOpCodes.TestIfLessThan,
																	AbstractOpCodes.TestIfLessThanOrEqual,
																	AbstractOpCodes.TestLogicalComparison,
																	AbstractOpCodes.TestAndSaveLogicalComparison,
																	AbstractOpCodes.Call,
																	AbstractOpCodes.TailCall,
																	AbstractOpCodes.Return,
																	AbstractOpCodes.IterateNumericForLoop,
																	AbstractOpCodes.InitNumericForLoop,
																	AbstractOpCodes.IterateGenericForLoop,
																	AbstractOpCodes.SetList,
																	AbstractOpCodes.Close,
																	AbstractOpCodes.Closure,
																	AbstractOpCodes.VarArg };

		this.IntToCodesMap = ordering;
		this.CodesToIntMap = initCodesToIntMap(ordering);
	}

	private static EnumMap<AbstractOpCodes, Integer> initCodesToIntMap(AbstractOpCodes[] in)
	{
		final EnumMap<AbstractOpCodes, Integer> result = new EnumMap<AbstractOpCodes, Integer>(AbstractOpCodes.class);

		for (int i = 0; i < in.length; i++)
		{
			result.put(in[i], i);
		}

		return result;
	}

	public static OpCodeMapper_v5_1 getInstance()
	{
		return single;
	}

	@Override
	public int getByteCodeFor(AbstractOpCodes inIn)
	{
		return this.CodesToIntMap.get(inIn);
	}

	@Override
	public AbstractOpCodes getOpCodeFor(int ByteCode)
	{
		//System.out.println("potential bytecode: " + ByteCode);
		
		if (ByteCode < this.IntToCodesMap.length)
		{
			return this.IntToCodesMap[ByteCode];
		}
		else
		{
			System.out.println("Unknown OpCode: 0x" + Integer.toHexString(ByteCode));
			return AbstractOpCodes.UnknownOpCode;
		}
	}

	@Override
	public InstructionTypes getTypeOf(byte inOp)
	{
		return this.getTypeOf(this.getOpCodeFor(inOp));
	}

	@Override
	public InstructionTypes getTypeOf(AbstractOpCodes inOp)
	{
		final InstructionTypes result;

		switch (inOp)
		{
			case SetTable:
			case Add:
			case Subtract:
			case Multiply:
			case Divide:
			case Modulo:
			case Power:
			case TestIfEqual:
			case TestIfLessThan:
			case TestIfLessThanOrEqual:
			case IterateGenericForLoop:
			case Close:
			case Move:
			case LoadNull:
			case GetTable:
			case Self:
			case ArithmeticNegation:
			case LogicalNegation:
			case Length:
			case Concatenation:
			case TestLogicalComparison:
			case TestAndSaveLogicalComparison:
			case LoadBool:
			case GetUpvalue:
			case SetUpvalue:
			case NewTable:
			case Call:
			case TailCall:
			case Return:
			case SetList:
			case VarArg:
			{
				result = InstructionTypes.iABC;
				break;
			}
			case LoadK:
			case GetGlobal:
			case SetGlobal:
			case Closure:
			{
				result = InstructionTypes.iABx;
				break;
			}
			case Jump:
			case IterateNumericForLoop:
			case InitNumericForLoop:
			{
				result = InstructionTypes.iAsBx;
				break;
			}
			case UnknownOpCode:
			{
				result = InstructionTypes.iABC;
				break;
			}
			default:
			{
				throw new IllegalArgumentException(inOp.toString());
			}
		}

		return result;
	}
}
