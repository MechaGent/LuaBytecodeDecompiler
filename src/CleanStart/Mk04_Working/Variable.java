package CleanStart.Mk04_Working;

import CharList.CharList;
import CleanStart.Mk04_Working.VarFormula.FormulaToken;
import CleanStart.Mk04_Working.Variables.VarTypes;

public interface Variable extends FormulaToken
{
	public void revVarType();
	public VarTypes getVarType();
	public int getInstanceNumber();
	
	/**
	 * 
	 * @return <namePrefix>  + '_' + {@code getInstanceNumber()} + '(' + {@code getVarType().toString()} + ')';
	 */
	public String getMangledName();
	public String getMangledName(boolean showInferredTypes);
	public CharList getValueAsCharList();
	
	/**
	 * 
	 * @return either getMangledName() or getValueAsCharList().toString()
	 */
	public String getMostRelevantIdValue();
	public String getMostRelevantIdValue(boolean showInferredTypes);
	
	/**
	 * 
	 * @return the instruction wherein this variable was assigned a value
	 */
	public Instruction getAssignmentInstruction();
	public CanBeInlinedTypes canBeInlined();
	public int getNumInlineRequests();	//ie numReads
	public int getNumChildVars();
	public void addChildVar(Variable inChild);
	public void logInlineRequest(int lineNum, Instruction inRequester);
	
	public void incrementNumReads();
	public int getNumReads();
}
