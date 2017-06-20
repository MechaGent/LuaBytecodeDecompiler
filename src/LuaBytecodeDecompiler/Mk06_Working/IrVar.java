package LuaBytecodeDecompiler.Mk06_Working;

import java.util.Map.Entry;

import CharList.CharList;

import java.util.TreeMap;

import ExpressionParser.FormulaToken;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;

public abstract class IrVar implements FormulaToken
{
	private static final IrVar GlobalsTable = new IrVar_Ref("GlobalsTable", ScopeLevels.Global, false);
	protected static final String GlobalVarsPrefix = "GlobalVars.";
	
	protected final String label;
	private final VarTypes type;
	protected final ScopeLevels scope;
	private final boolean isFromMethodReturn;
	private final boolean isFinal;
	protected final TreeMap<StepStage, IrVar> value;
	
	public IrVar(String inLabel, VarTypes inType, ScopeLevels inScope, boolean inIsFromMethodReturn)
	{
		this(inLabel, inType, inScope, inIsFromMethodReturn, false);
	}

	public IrVar(String inLabel, VarTypes inType, ScopeLevels inScope, boolean inIsFromMethodReturn, boolean inIsFinal)
	{
		this.label = inLabel;
		this.type = inType;
		this.scope = inScope;
		this.isFromMethodReturn = inIsFromMethodReturn;
		this.isFinal = inIsFinal;
		this.value = new TreeMap<StepStage, IrVar>();
		this.value.put(StepStage.getZeroStepStage(), this);
	}
	
	public void logWriteToAtTime(StepStage time, IrVar newValue)
	{
		this.value.put(time, newValue);
	}

	public String getLabel()
	{
		return this.label;
	}

	public VarTypes getType()
	{
		return this.type;
	}

	public ScopeLevels getScope()
	{
		return this.scope;
	}

	public boolean isFromMethodReturn()
	{
		return this.isFromMethodReturn;
	}
	
	public boolean isFinal()
	{
		return this.isFinal;
	}

	public static IrVar getGlobalsTable()
	{
		return GlobalsTable;
	}
	
	public CharList toWordyVersion()
	{
		final CharList result = new CharList();
		
		result.add(this.scope.toString());
		result.add(':');
		result.add(this.label);
		result.add('(');
		result.add(this.valueToString());
		result.add(')');
		
		return result;
	}
	
	@Override
	public abstract String valueToString();
	
	public IrVar getPriorWrite(StepStage time)
	{
		Entry<StepStage, IrVar> result = this.value.floorEntry(time);
		
		if(result != null)
		{
			return result.getValue();
		}
		else
		{
			return this;
		}
	}
	
	@Override
	public boolean isOperator()
	{
		return false;
	}

	@Override
	public boolean isOperand()
	{
		return true;
	}

	@Override
	public int getPrecedence()
	{
		return -1;
	}
}
