package CleanStart.Mk06.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk06.ChunkHeader;
import CleanStart.Mk06.MiscAutobot;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Instructions.MathOp;
import CleanStart.Mk06.Instructions.SetVar;
import CleanStart.Mk06.MiscAutobot.ParseTypes;
import CleanStart.Mk06.SubVars.StringVar;
import CleanStart.Mk06.VarFormula.Operators;
import CleanStart.Mk06.VarFormula.Formula.FormulaBuilder;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteRadixMap.HalfByteRadixMap;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class RawFunctionObject extends FunctionObject
{
	/**
	 * 0 == yes, with culling
	 * <br>
	 * 1 == yes, without culling
	 * <br>
	 * 2 == no
	 */
	private static final int shouldReinsertJumpLabels = 0;
	protected static final int FieldsPerFlush = 50;
	
	protected final int[] rawInstructions;
	protected final RawFunctionObject[] rawFunctionPrototypes;
	
	protected RawFunctionObject(String inName, int inNumUpvalues, int inNumParams, VarArgFlagSet inVarArgFlags, int inNumRegisters_max, int[] inSourceLinePositions, VarRef[] inConstants, VarRef[] inLocals, VarRef[] inUpvalues, HalfByteRadixMap<VarRef> inGlobals, int[] inRawInstructions, RawFunctionObject[] inRawFunctionPrototypes)
	{
		super(inName, inNumUpvalues, inNumParams, inVarArgFlags, inNumRegisters_max, inSourceLinePositions, inConstants, inLocals, inUpvalues, inGlobals);
		this.rawInstructions = inRawInstructions;
		this.rawFunctionPrototypes = inRawFunctionPrototypes;
	}
	
	protected RawFunctionObject(RawFunctionObject old)
	{
		super(old);
		this.rawInstructions = old.rawInstructions;
		this.rawFunctionPrototypes = old.rawFunctionPrototypes;
	}
	
	/**
	 * assumes that nothing has been read from the stream yet
	 * 
	 * @param in
	 * @return
	 */
	public static RawFunctionObject getNext(BytesStreamer in, String functionName)
	{
		final ChunkHeader header = ChunkHeader.getNext(in);
		final RawFunctionObject result = RawFunctionObjectHelper.getNext(in, header, new HalfByteRadixMap<VarRef>());
		result.name = functionName;
		return result;
	}

	public int[] getRawInstructions()
	{
		return this.rawInstructions;
	}

	@Override
	public FunctionObject[] getFunctionPrototypes()
	{
		return this.rawFunctionPrototypes;
	}

	@Override
	protected CharList disassembleInstructions(int offset, boolean inShowInferredTypes)
	{
		final CharList result = new CharList();

		for (int i = 0; i < this.rawInstructions.length;)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.addAsPaddedString(Integer.toString(i), 4, true);
			result.add('\t');
			final int numLinesConsumed = RawFunctionObjectHelper.translateRawToBytecode(this, i, offset, result);
			i += numLinesConsumed;
		}

		return result;
	}
	
	@Override
	protected CharList decompileInstructions(int inOffset, boolean inShowInferredTypes)
	{
		throw new UnsupportedOperationException();
	}
	
	protected void addRkVarIndex(int index, CharList result)
	{
		final int compare = index & RK_Mask;

		if (compare == RK_Mask)
		{
			result.add("(<constant> ");
			final int masked = index & ~RK_Mask;

			if (masked >= this.Constants.length)
			{
				throw new IllegalArgumentException("index: " + index + ", masked: " + masked);
			}

			result.add(this.Constants[masked].getValueAtTime(StepNum.getFirstStep()).getValueAsString());
			result.add(')');
		}
		else
		{
			result.add("Locals[");
			result.addAsString(index);
			result.add(']');
		}
	}
	
	protected VarRef getRkVar(int index)
	{
		final VarRef result;
		
		final int compare = index & RK_Mask;

		if (compare == RK_Mask)
		{
			final int masked = index & ~RK_Mask;
			
			result = this.Constants[masked];
		}
		else
		{
			result = this.Locals[index];
		}
		
		return result;
	}
	
	protected MathOp getMathOp_FromIndex(int lineIndex, Operators operator, StepNum startTime)
	{
		final int[] fields;
		final FormulaBuilder formy = new FormulaBuilder();
		final VarRef target;

		switch (operator)
		{
			case Arith_Add:
			case Arith_Div:
			case Arith_Mod:
			case Arith_Mul:
			case Arith_Pow:
			case Arith_Sub:
			{
				fields = MiscAutobot.parseRawInstruction(this.rawInstructions[lineIndex], ParseTypes.iABC);

				final VarRef operand1 = this.getRkVar(fields[1]);
				final VarRef operand2 = this.getRkVar(fields[2]);

				formy.add(operand1);
				formy.add(operator);
				formy.add(operand2);

				break;
			}
			case Logic_Not:
			case Arith_Neg:
			{
				fields = MiscAutobot.parseRawInstruction(this.rawInstructions[lineIndex], ParseTypes.iAB);

				final VarRef operand1 = this.getRkVar(fields[1]);

				formy.add(operator);
				formy.add(operand1);
				break;
			}
			default:
			{
				throw new UnhandledEnumException(operator);
			}
		}

		target = this.Locals[fields[0]];

		return MathOp.getInstance(startTime, target, formy.build("MathOpGen"));
	}
	
	@Deprecated
	protected MathOp getMathOp(int raw, Operators operator, StepNum startTime)
	{
		final int[] fields;
		final FormulaBuilder formy = new FormulaBuilder();
		final VarRef target;

		switch (operator)
		{
			case Arith_Add:
			case Arith_Div:
			case Arith_Mod:
			case Arith_Mul:
			case Arith_Pow:
			case Arith_Sub:
			{
				fields = MiscAutobot.parseRawInstruction(raw, ParseTypes.iABC);

				final VarRef operand1 = this.getRkVar(fields[1]);
				final VarRef operand2 = this.getRkVar(fields[2]);

				formy.add(operand1);
				formy.add(operator);
				formy.add(operand2);

				break;
			}
			case Logic_Not:
			case Arith_Neg:
			{
				fields = MiscAutobot.parseRawInstruction(raw, ParseTypes.iAB);

				final VarRef operand1 = this.getRkVar(fields[1]);

				formy.add(operator);
				formy.add(operand1);
				break;
			}
			default:
			{
				throw new UnhandledEnumException(operator);
			}
		}

		target = this.Locals[fields[0]];

		return MathOp.getInstance(startTime, target, formy.build("MathOpGen"));
	}
	
	protected SetVar composeGlobalGetterOrSetter_FromIndex(int lineIndex, StepNum currentTime, boolean isGetter)
	{
		final int[] fields = MiscAutobot.parseRawInstruction(this.rawInstructions[lineIndex], ParseTypes.iABx);

		final VarRef localVar = this.Locals[fields[0]];

		final VarRef rawKeyConstant = this.Constants[fields[1]];
		final StringVar rawKey = (StringVar) rawKeyConstant.getLatestValue();
		String key = rawKey.getCore();

		VarRef globalVar = this.globals.get(key);
		
		if(globalVar == null)
		{
			globalVar = VarRef.getInstance("globalVar_" + key);
			this.globals.put(key, globalVar);
		}

		final SetVar result;
		
		if (isGetter)
		{
			result = SetVar.getInstance(currentTime, localVar, globalVar);
		}
		else
		{
			result = SetVar.getInstance(currentTime, globalVar, localVar);
		}
		
		return result;
	}
	
	@Deprecated
	protected SetVar composeGlobalGetterOrSetter(int raw, StepNum currentTime, boolean isGetter)
	{
		final int[] fields = MiscAutobot.parseRawInstruction(raw, ParseTypes.iABx);

		final VarRef localVar = this.Locals[fields[0]];

		final VarRef rawKeyConstant = this.Constants[fields[1]];
		final StringVar rawKey = (StringVar) rawKeyConstant.getLatestValue();
		String key = rawKey.getCore();

		VarRef globalVar = this.globals.get(key);
		
		if(globalVar == null)
		{
			globalVar = VarRef.getInstance("globalVar_" + key);
			this.globals.put(key, globalVar);
		}

		if (isGetter)
		{
			return SetVar.getInstance(currentTime, localVar, globalVar);
		}
		else
		{
			return SetVar.getInstance(currentTime, globalVar, localVar);
		}
	}
}
