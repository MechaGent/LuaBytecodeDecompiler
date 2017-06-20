package CleanStart.Mk04_Working;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.VarTypes;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;

public class VarsDispenser
{
	private static final int RK_Mask = 0x100;

	private final Variable[] ConstantsInstances;

	private final int[] localsInstanceNums;
	private final MystVar[] localsInstances;

	private final int[] upvaluesInstanceNums;
	private final MystVar[] upvaluesInstances;

	private final HalfByteRadixMap<MystVar> Globals;
	
	private final HalfByteRadixMap<VarState> Values;

	public VarsDispenser(Variable[] inConstantsInstances, int numLocals, int numUpvalues)
	{
		this(inConstantsInstances, numLocals, numUpvalues, new HalfByteRadixMap<MystVar>());
	}

	public VarsDispenser(Variable[] inConstantsInstances, int numLocals, int numUpvalues, HalfByteRadixMap<MystVar> inGlobals)
	{
		this.ConstantsInstances = inConstantsInstances;

		this.localsInstanceNums = new int[numLocals];
		this.localsInstances = new MystVar[numLocals];

		this.upvaluesInstanceNums = new int[numUpvalues];
		this.upvaluesInstances = new MystVar[numUpvalues];

		this.Globals = inGlobals;
		this.Values = new HalfByteRadixMap<VarState>();
	}

	/**
	 * CAUTION: the length of Globals may not be accurate
	 * 
	 * @param repo
	 * @return
	 */
	public int getLengthOf(VarRepos repo)
	{
		final int result;

		switch (repo)
		{
			case Constants:
			{
				result = this.ConstantsInstances.length;
				break;
			}
			case Globals:
			{
				result = this.Globals.size();
				break;
			}
			case Locals:
			{
				result = this.localsInstances.length;
				break;
			}
			case Upvalues:
			{
				result = this.upvaluesInstances.length;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(repo);
			}
		}

		return result;
	}

	public MystVar requestVarForRead(String globalVarName)
	{
		MystVar result = this.Globals.get(globalVarName);

		if (result == null)
		{
			result = MystVar.getInstance(globalVarName, SpecialInstanceNums.staticInstanceNum, null);
			this.Globals.put(globalVarName, result);
		}

		result.incrementNumReads();
		
		return result;
	}

	public MystVar requestVarForWrite(String globalVarName, Instruction assigner, MystVar inParentVar)
	{
		MystVar result = this.Globals.get(globalVarName);

		if (result == null)
		{
			result = MystVar.getInstance(globalVarName, SpecialInstanceNums.staticInstanceNum, inParentVar);
			this.Globals.put(globalVarName, result);
		}

		return result;
	}

	public MystVar requestVarForWrite(String globalVarName, Instruction assigner, Variable inParentVar)
	{
		MystVar result = this.Globals.get(globalVarName);

		if (result == null)
		{
			result = MystVar.getInstance(globalVarName, SpecialInstanceNums.staticInstanceNum, inParentVar);
			this.Globals.put(globalVarName, result);
		}

		return result;
	}

	public MystVar requestVarForWrite(String globalVarName, VarTypes newValueType, Instruction assigner, MystVar inParentVar)
	{
		MystVar result = this.Globals.get(globalVarName);

		if (result == null)
		{
			result = MystVar.getInstance(globalVarName, SpecialInstanceNums.staticInstanceNum, inParentVar);
			this.Globals.put(globalVarName, result);
		}

		result.setBestGuess(newValueType);

		return result;
	}

	public MystVar requestVarForWrite(String globalVarName, VarTypes newValueType, Instruction assigner, Variable inParentVar)
	{
		MystVar result = this.Globals.get(globalVarName);

		if (result == null)
		{
			result = MystVar.getInstance(globalVarName, SpecialInstanceNums.staticInstanceNum, inParentVar);
			this.Globals.put(globalVarName, result);
		}

		result.setBestGuess(newValueType);

		return result;
	}

	public MystVar requestVarForRead(int index, VarRepos repo)
	{
		return this.requestVarForRead(index, repo, VarTypes.Mystery);
	}

	public MystVar requestVarForRead(int index, VarRepos repo, VarTypes bestGuess)
	{
		final MystVar result;

		switch (repo)
		{
			case Locals:
			{
				if (this.localsInstanceNums[index] == 0)
				{
					// was initialized elsewhere (or implicitly, but problaby not)
					result = MystVar.getInstance("Locals_" + index, this.localsInstanceNums[index]++, bestGuess, null);
					this.localsInstances[index] = result;
				}
				else
				{
					result = this.localsInstances[index];

					result.setBestGuess(bestGuess);
				}

				break;
			}
			case Upvalues:
			{
				if (this.upvaluesInstanceNums[index] == 0)
				{
					// was initialized elsewhere (or implicitly, but problaby not)
					result = MystVar.getInstance("Upvalues_" + index, this.upvaluesInstanceNums[index]++, bestGuess, null);
					this.upvaluesInstances[index] = result;
				}
				else
				{
					result = this.upvaluesInstances[index];

					result.setBestGuess(bestGuess);
				}
				break;
			}
			case Globals:
			default:
			{
				throw new UnhandledEnumException(repo);
			}
		}
		
		result.incrementNumReads();

		return result;
	}

	public MystVar requestVarForStateChange(int index, VarRepos repo)
	{
		return this.requestVarForWrite(index, repo, null);
	}

	public MystVar requestVarForWrite(int index, VarRepos repo, MystVar inParentVar)
	{
		return this.requestVarForWrite(index, repo, ((inParentVar == null) ? VarTypes.Mystery : inParentVar.getBestGuess()), inParentVar);
	}

	public MystVar requestVarForWrite(int index, VarRepos repo, Variable inParentVar)
	{
		return this.requestVarForWrite(index, repo, ((inParentVar == null) ? VarTypes.Mystery : inParentVar.getVarType()), inParentVar);
	}

	public MystVar requestVarForWrite(int index, VarRepos repo, VarTypes newValueType, MystVar inParentVar)
	{
		final MystVar result;

		switch (repo)
		{
			case Locals:
			{
				result = MystVar.getInstance("Locals_" + index, this.localsInstanceNums[index]++, newValueType, inParentVar);
				this.localsInstances[index] = result;
				break;
			}
			case Upvalues:
			{
				result = MystVar.getInstance("Upvalues_" + index, this.upvaluesInstanceNums[index]++, newValueType, inParentVar);
				this.upvaluesInstances[index] = result;
				break;
			}
			case Globals:
			default:
			{
				throw new UnhandledEnumException(repo);
			}
		}

		result.setBestGuess(newValueType);

		return result;
	}

	public MystVar requestVarForWrite(int index, VarRepos repo, VarTypes newValueType, Variable inParentVar)
	{
		final MystVar result;

		switch (repo)
		{
			case Locals:
			{
				result = MystVar.getInstance("Locals_" + index, this.localsInstanceNums[index]++, newValueType, inParentVar);
				this.localsInstances[index] = result;
				break;
			}
			case Upvalues:
			{
				result = MystVar.getInstance("Upvalues_" + index, this.upvaluesInstanceNums[index]++, newValueType, inParentVar);
				this.upvaluesInstances[index] = result;
				break;
			}
			case Globals:
			default:
			{
				throw new UnhandledEnumException(repo);
			}
		}

		result.setBestGuess(newValueType);

		return result;
	}

	public Variable requestRkVarForRead(int index)
	{
		final int compare = index & RK_Mask;
		final Variable result;

		if (compare == RK_Mask)
		{
			result = this.ConstantsInstances[index & ~RK_Mask];
			result.incrementNumReads();
		}
		else
		{
			result = this.requestVarForRead(index, VarRepos.Locals);
		}

		return result;
	}

	public Variable requestRkVarForRead(int index, VarTypes bestGuess)
	{
		final int compare = index & RK_Mask;
		final Variable result;

		if (compare == RK_Mask)
		{
			result = this.ConstantsInstances[index & ~RK_Mask];
			result.incrementNumReads();
		}
		else
		{
			final MystVar temp = this.requestVarForRead(index, VarRepos.Locals);

			if (bestGuess == VarTypes.Math_Unknown)
			{
				temp.setBestGuess(VarTypes.Math_Variable);
			}
			else
			{
				temp.setBestGuess(bestGuess);
			}

			result = temp;
		}

		return result;
		// return this.requestRkVarForRead(index);
	}

	public Variable requestConstantVarForRead(int index)
	{
		if (index < this.ConstantsInstances.length)
		{
			this.ConstantsInstances[index].incrementNumReads();
			return this.ConstantsInstances[index];
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(index + " of " + this.ConstantsInstances.length);
		}
	}
	
	public CharList toCharList(int offset)
	{
		return this.toCharList(offset, this.ConstantsInstances.length);
	}

	public CharList toCharList(int offset, int numVarsPerLine)
	{
		final CharList result = new CharList();
		int count = 0;

		result.add('\t', offset);
		result.add("Constants: {");
		result.addNewLine();
		result.add('\t', offset + 1);

		for (int i = 0; i < this.ConstantsInstances.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}
			
			if(count == numVarsPerLine)
			{
				count = 0;
				result.addNewLine();
				result.add('\t', offset + 1);
			}

			result.add(this.ConstantsInstances[i].getValueAsCharList(), true);
			count++;
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add('}');

		/*
		result.addNewLine();
		result.add('\t', offset);
		result.add("Locals: {");
		
		for (int i = 0; i < this.localsInstances.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}
		
			result.add(this.localsInstances[i].getValueAsCharList(), true);
		}
		
		result.add('}');
		
		result.addNewLine();
		result.add('\t', offset);
		result.add("Upvalues: {");
		
		for (int i = 0; i < this.upvaluesInstances.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}
		
			result.add(this.upvaluesInstances[i].getValueAsCharList(), true);
		}
		
		result.add('}');
		*/

		return result;
	}

	public CharList globalsToCharList(int offset)
	{
		return this.globalsToCharList(offset, 4, false);
	}

	public CharList globalsToCharList(int offset, int varsPerRow, boolean showInferredTypes)
	{
		final CharList result = new CharList();

		//result.add('\t', offset);
		result.add('{');
		boolean isFirst = true;
		int count = 0;

		for (Entry<HalfByteArray, MystVar> entry : this.Globals)
		{
			if (isFirst)
			{
				isFirst = false;
				result.addNewLine();
				result.add('\t', offset);
			}
			else
			{
				result.add(", ");
			}

			if (count == varsPerRow)
			{
				result.addNewLine();
				result.add('\t', offset);
				count = 0;
			}
			
			result.add(entry.getValue().getMangledName(showInferredTypes));
			count++;
		}

		result.addNewLine();
		result.add('\t', offset-1);
		result.add('}');

		return result;
	}

	public static enum VarRepos
	{
		Constants,
		Locals,
		Upvalues,
		Globals;
	}
	
	private static class VarState
	{
		private final Variable writeValue;
		private int numReads;
		
		public VarState(Variable inWriteValue)
		{
			this.writeValue = inWriteValue;
			this.numReads = 0;
		}
	}
}
