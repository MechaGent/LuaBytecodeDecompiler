package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;
import HalfByteRadixMap.HalfByteRadixMap;

public class Var_Table extends BaseVariable
{
	private final HalfByteRadixMap<Var_Ref> mapSide;
	
	private final int hashSize;
	private final Var_Ref[] arrSide;	//might need to switch this to an ArrayList, and/or a Var_Ref[]
	
	public Var_Table(int tableSize, int hashSize, StepStage inTime)
	{
		this("Var_Table_" + instanceCounter++, tableSize, hashSize, inTime);
	}
	
	public Var_Table(String inMangledName, int tableSize, int hashSize, StepStage inTime)
	{
		super(inMangledName, inTime);
		this.mapSide = new HalfByteRadixMap<Var_Ref>();
		this.hashSize = hashSize;
		this.arrSide = BaseVariable.getRefArray_NullInit(tableSize, inMangledName + "_tableVar_", 0, inTime);
	}
	
	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.getEvaluatedValueAtTime(inTime).getEvaluatedValueAsStringAtTime(inTime);
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return new Var_TableInstanced("instanceOf:" + this.mangledName, inTime, this);
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.TableOfReferences;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		return super.compareWith_base(in);
	}

	@Override
	public String valueToString()
	{
		return super.getIndeterminantValue();
	}
	
	public Var_Ref attemptToGet(Variable in)
	{
		switch(in.getType())
		{
			case Constant_String:
			{
				return this.attemptToGet((Var_String) in); 
			}
			case Constant_Int:
			{
				return this.attemptToGet((Var_Int) in); 
			}
			case InstancedReference:
			{
				return this.attemptToGet(((Var_InstancedRef) in).getEvaluatedValue());
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}
	}
	
	public Var_Ref attemptToGet(Var_String in)
	{
		return this.mapSide.get(in.getCargo());
	}
	
	public Var_Ref attemptToGet(Var_Int in)
	{
		return this.arrSide[in.getValue()];
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}
}
