package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.SpecialInstanceNums;
import CleanStart.Mk04_Working.Variable;
import HalfByteRadixMap.HalfByteRadixMap;

public abstract class BaseVar implements Variable
{
	//protected static final int notAnInstanceNum = SpecialInstanceNums.notAnInstanceNum.getValue();
	//protected static final int staticInstanceNum = SpecialInstanceNums.staticInstanceNum.getValue();
	//protected static final int tempInstanceNum = SpecialInstanceNums.tempInstanceNum.getValue();
	
	protected final String namePrefix;
	protected final VarTypes varType;
	protected final int instanceNumber;
	protected final Instruction AssignmentInstruction;
	
	protected final HalfByteRadixMap<Variable> childVars;
	
	protected final HalfByteRadixMap<Instruction> inlineRequests;
	
	protected int numReads;
	
	public BaseVar(String inNamePrefix, VarTypes inVarType, Instruction inAssignmentInstruction)
	{
		this(inNamePrefix, inVarType, SpecialInstanceNums.notAnInstanceNum.getValue(), inAssignmentInstruction);
	}
	
	public BaseVar(String inNamePrefix, VarTypes inVarType, int inInstanceNumber, Instruction inAssignmentInstruction)
	{
		this.namePrefix = inNamePrefix;
		this.varType = inVarType;
		this.instanceNumber = inInstanceNumber;
		this.AssignmentInstruction = inAssignmentInstruction;
		this.childVars = new HalfByteRadixMap<Variable>();
		this.inlineRequests = new HalfByteRadixMap<Instruction>();
	}

	@Override
	public Instruction getAssignmentInstruction()
	{
		return this.AssignmentInstruction;
	}
	
	@Override
	public void logInlineRequest(int lineNum, Instruction inRequester)
	{
		this.inlineRequests.put(lineNum, inRequester);
	}
	
	@Override
	public int getNumInlineRequests()
	{
		return this.inlineRequests.size();
	}
	
	@Override
	public int getNumChildVars()
	{
		return this.childVars.size();
	}
	
	@Override
	public void addChildVar(Variable inChild)
	{
		this.childVars.put(inChild.getMangledName(false), inChild);
	}
	
	@Override
	public void revVarType()
	{
		
	}

	@Override
	public VarTypes getVarType()
	{
		return this.varType;
	}
	
	@Override
	public int getInstanceNumber()
	{
		return this.instanceNumber;
	}
	
	/*
	@Override
	public String getMangledName()
	{
		if(this.instanceNumber != SpecialInstanceNums.notAnInstanceNum.getValue())
		{
			return this.namePrefix + '_' + this.instanceNumber + '(' + this.varType.toString() + ')';
		}
		else
		{
			return this.namePrefix + '(' + this.varType.toString() + ')';
		}
	}
	*/
	
	@Override
	public String getMangledName()
	{
		final CharList result = new CharList();
		
		result.add(this.namePrefix);
		
		switch(SpecialInstanceNums.parse(this.instanceNumber))
		{
			case notAnInstanceNum:
			{
				result.add("_nain");
				
				break;
			}
			case staticInstanceNum:
			{
				result.add("_static");
				break;
			}
			case tempInstanceNum:
			{
				result.add("_temp");
				break;
			}
			default:
			{
				result.add('_');
				result.addAsString(this.instanceNumber);
				
				break;
			}
		}
		
		return result.toString();
	}
	
	@Override
	public String getMangledName(boolean inShowInferredTypes)
	{
		return this.getMangledName();
	}
	
	@Override
	public String getMostRelevantIdValue()
	{
		return this.getMangledName();
	}
	
	@Override
	public String getMostRelevantIdValue(boolean showInferredTypes)
	{
		return this.getMangledName();
	}
	
	@Override
	public String valueToString(boolean showInferredTypes)
	{
		return this.valueToString();
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
	
	@Override
	public void incrementNumReads()
	{
		this.numReads++;
	}
	
	@Override
	public int getNumReads()
	{
		return this.numReads;
	}
}
