package CleanStart.Mk01.Variables;

import CharList.CharList;
import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;

public class Var_TableLookup extends BaseVariable
{
	private final Var_Ref table;
	private final Variable indexer;
	
	public Var_TableLookup(String inMangledName, StepStage inTimeOfCreation, Var_Ref inTable, Variable inIndexer)
	{
		super(inMangledName, inTimeOfCreation);
		
		if(inMangledName.length() == 0)
		{
			throw new IllegalArgumentException();
		}
		
		this.table = inTable;
		inTable.logReadAtTime(inTimeOfCreation);
		this.indexer = inIndexer;
		
		if(this.table == this.indexer)
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		final CharList result = new CharList();
		
		/*
		result.add(this.table.getEvaluatedValueAsStringAtTime(inTime));
		result.add('[');
		result.add(this.indexer.getEvaluatedValueAsStringAtTime(inTime));
		result.add(']');
		*/
		
		//result.add(this.table.valueToString());
		result.add(this.table.getEvaluatedValueAsStringAtTime(inTime));
		result.add('[');
		//result.add(this.indexer.valueToString());
		result.add(this.indexer.getEvaluatedValueAsStringAtTime(inTime));
		result.add(']');
		
		return result.toString();
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		final Variable maybeTable = this.table.getEvaluatedValueAtTime(inTime);
		
		if(maybeTable.getType() == VariableTypes.TableOfReferences)
		{
			final Var_Table cast = (Var_Table) maybeTable;
			
			return cast.attemptToGet(this.indexer.getEvaluatedValueAtTime(inTime));
		}
		else
		{
			return null;
		}
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.InstancedReference_Indexed;
	}

	@Override
	public Comparisons compareWith(Variable inIn, StepStage inTime)
	{
		return super.compareWith_base(inIn);
	}

	@Override
	public String valueToString()
	{
		final CharList result = new CharList();
		
		//result.add(this.table.getMangledName());
		result.add(this.table.valueToString());
		result.add(".getAt(");
		//result.add(this.indexer.getMangledName());
		result.add(this.indexer.valueToString());
		result.add(')');
		
		return result.toString();
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}
}
