package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.BoolBrancher;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Int;

public class AnnoInstruction_Test extends AnnoInstruction implements BoolBrancher
{
	/*
	 * note to self: spaceship operator is equivalent to a compareTo() operation, so the result is coerced to a boolean. 1 == true, 0 | -1 == false.
	 */

	private final boolean valueC;
	private AnnoVar source;

	public AnnoInstruction_Test(int inRaw, int inI, AnnoVar inSource, boolean inIsAndOp)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.TestLogicalComparison);
		this.valueC = inIsAndOp;
		this.source = inSource;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		/*
		 * if(!(R(A) == C)), then PC++
		 * if(R(A) == !C), then PC++
		 * 
		 * c==false
		 * if(R(A))
		 * 
		 * c==true
		 * if(!R(A))
		 */

		result.add("if (");

		if (this.valueC)
		{
			result.add('!');
		}

		final int value;

		if (this.source instanceof AnnoVar_Int)
		{
			value = ((AnnoVar_Int) this.source).getValue();
		}
		else
		{
			value = -1; // dummy value to get the switch to default out
		}

		switch (value)
		{
			case 0:
			{
				result.add("(true)");
				break;
			}
			case 1:
			{
				result.add("(false)");
				break;
			}
			default:
			{
				result.add(MiscAutobot.GlobalMethods_toBool);
				result.add(this.source.getAdjustedName());
				result.add(')');
				break;
			}
		}

		result.add("), then ");
		result.add(MiscAutobot.programCounter);
		result.add("++");

		return result;
	}

	public boolean getValueC()
	{
		return this.valueC;
	}

	public AnnoVar getSource()
	{
		return this.source;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		in.addVarRead(this.source, this, this.getInstructionAddress(), 0);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.source = inReplacement;
		}
		else
		{
			throw new IllegalArgumentException("bad index: " + inSubstep);
		}

		return this;
	}

	@Override
	public CharList toBooleanStatement(boolean invertStatement)
	{
		final CharList result = new CharList();

		//result.add("if (");
		
		/*
		 * this is equivalent to:
		 * 
		 * boolean temp;
		 * 
		 * if(invertStatement)
		 * {
		 * 		temp = !this.valueC;
		 * }
		 * else
		 * {
		 * 		temp = this.valueC;
		 * }
		 * 
		 * if(!temp)
		 * {
		 * 		result.add('!');
		 * }
		 */
		if (invertStatement ^ !this.valueC)
		{
			result.add('!');
		}

		final int value;

		if (this.source instanceof AnnoVar_Int)
		{
			value = ((AnnoVar_Int) this.source).getValue();
		}
		else
		{
			value = -1; // dummy value to get the switch to default out
		}

		switch (value)
		{
			case 0:
			{
				result.add("(true)");
				break;
			}
			case 1:
			{
				result.add("(false)");
				break;
			}
			default:
			{
				result.add(MiscAutobot.GlobalMethods_toBool);
				result.add(this.source.getAdjustedName());
				result.add(')');
				break;
			}
		}

		//result.add("), then ");
		//result.add(MiscAutobot.programCounter);
		//result.add("++");

		return result;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		throw new UnsupportedOperationException();	//handled elsewhere
	}
}
