package CleanStart.Mk01.Interfaces;

import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.VarFormula.FormulaToken;

public interface Variable extends FormulaToken
{
	public String getEvaluatedValueAsStringAtTime(StepStage time);
	
	public Variable getEvaluatedValueAtTime(StepStage time);
	
	public VariableTypes getType();
	
	public String getMangledName();
	
	public Comparisons compareWith(Variable in, StepStage time);
}
