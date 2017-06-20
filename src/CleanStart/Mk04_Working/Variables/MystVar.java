package CleanStart.Mk04_Working.Variables;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.SpecialInstanceNums;
import CleanStart.Mk04_Working.Variable;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;

public class MystVar extends BaseVar
{
	private static boolean allowTrickle = true;

	private final Variable parentVar;
	
	private boolean bestGuessIsGood;

	protected VarTypes bestGuess;

	protected Variable inlinedVar;
	protected boolean useSelfAsInline;

	protected MystVar(String inNamePrefix, int inInstanceNumber, Variable inParentVar)
	{
		this(inNamePrefix, inInstanceNumber, ((inParentVar != null) ? inParentVar.getVarType() : VarTypes.Mystery), inParentVar);
	}

	protected MystVar(String inNamePrefix, int inInstanceNumber, VarTypes inBestGuess, Variable inParentVar)
	{
		super(inNamePrefix, VarTypes.Mystery, inInstanceNumber, null); // no assignment instruction, because if it had one, it wouldn't be a mystery

		if (inParentVar != null && inBestGuess == VarTypes.Mystery)
		{
			this.bestGuess = inParentVar.getVarType();
		}
		else
		{
			this.bestGuess = inBestGuess;
		}

		this.parentVar = inParentVar;
		this.bestGuessIsGood = true;
		this.inlinedVar = null;
		this.useSelfAsInline = false;
	}

	/*
	public static MystVar getInstance(String inNamePrefix)
	{
		return new MystVar(inNamePrefix, BaseVar.notAnInstanceNum);
	}
	*/

	public static MystVar getInstance(String inNamePrefix, SpecialInstanceNums specialCase, Variable inParentVar)
	{
		final MystVar result;

		if (inParentVar instanceof MystVar)
		{
			result = getInstance(inNamePrefix, specialCase, (MystVar) inParentVar);
		}
		else
		{
			result = new MystVar(inNamePrefix, specialCase.getValue(), inParentVar);
			
			if(inParentVar != null)
			{
				inParentVar.addChildVar(result);
			}
		}

		return result;
	}

	public static MystVar getInstance(String inNamePrefix, int inInstanceNumber, Variable inParentVar)
	{
		final MystVar result;

		if (inParentVar instanceof MystVar)
		{
			result = getInstance(inNamePrefix, inInstanceNumber, (MystVar) inParentVar);
		}
		else
		{
			result = new MystVar(inNamePrefix, inInstanceNumber, inParentVar);
			
			if(inParentVar != null)
			{
				inParentVar.addChildVar(result);
			}
		}

		return result;
	}

	public static MystVar getInstance(String inNamePrefix, int inInstanceNumber, VarTypes inBestGuess, Variable inParentVar)
	{
		final MystVar result;

		if (inParentVar instanceof MystVar)
		{
			result = getInstance(inNamePrefix, inInstanceNumber, inBestGuess, (MystVar) inParentVar);
		}
		else
		{
			result = new MystVar(inNamePrefix, inInstanceNumber, inBestGuess, inParentVar);
			
			if(inParentVar != null)
			{
				inParentVar.addChildVar(result);
			}
		}

		return result;
	}

	public static MystVar getInstance(String inNamePrefix, SpecialInstanceNums specialCase, MystVar inParentVar)
	{
		final MystVar result = new MystVar(inNamePrefix, specialCase.getValue(), inParentVar);

		if (inParentVar != null)
		{
			inParentVar.childVars.put(result.getMangledName(false), result);
			inParentVar.bestGuessIsGood = false;
		}

		return result;
	}

	public static MystVar getInstance(String inNamePrefix, int inInstanceNumber, MystVar inParentVar)
	{
		final MystVar result = new MystVar(inNamePrefix, inInstanceNumber, inParentVar);

		if (inParentVar != null)
		{
			inParentVar.childVars.put(result.getMangledName(), result);
			inParentVar.bestGuessIsGood = false;
		}

		return result;
	}

	public static MystVar getInstance(String inNamePrefix, int inInstanceNumber, VarTypes inBestGuess, MystVar inParentVar)
	{
		final MystVar result = new MystVar(inNamePrefix, inInstanceNumber, inBestGuess, inParentVar);

		if (inParentVar != null)
		{
			inParentVar.childVars.put(result.getMangledName(false), result);
			inParentVar.bestGuessIsGood = false;
		}

		return result;
	}

	public void setBestGuess(VarTypes change)
	{
		final CastTypes castType = this.bestGuess.attemptCastTo(change);

		switch (castType)
		{
			case Illegal:
			{
				throw new IllegalArgumentException("Be advised: variable '" + this.getMangledName(false) + " has attempted to change type from " + this.bestGuess + " to " + change);
			}
			case isString:
			{
				this.setBestGuess_internal(VarTypes.String);
				break;
			}
			case wasCoercedTo:
			case Legal:
			{
				this.setBestGuess_internal(change);
				break;
			}
			case ShakilyLegal:
			{
				System.err.println("Be advised: variable '" + this.getMangledName(false) + "' has been blocked from changing type from " + this.bestGuess + " to " + change);
			}
			case Coerce:
			case Unnecessary:
			{
				// System.out.println(this.getMangledName(false) + " is not swapping " + this.bestGuess.toString() + " with " + change.toString());
				break;
			}
			default:
			{
				throw new UnhandledEnumException(castType);
			}
		}
	}

	private void setBestGuess_internal(VarTypes change)
	{
		// System.out.println(this.getMangledName(false) + " is swapping " + this.bestGuess.toString() + " with " + change.toString());
		this.bestGuess = change;
		this.bestGuessIsGood = true;

		if (this.parentVar != null && this.parentVar instanceof MystVar)
		{
			((MystVar) this.parentVar).setBestGuess(change);
		}

		for (Entry<HalfByteArray, Variable> kid : this.childVars)
		{
			final Variable temp1 = kid.getValue();

			if (temp1 instanceof MystVar)
			{
				((MystVar) temp1).setBestGuess(change);
			}
		}
	}

	public void setBestGuess2(VarTypes change)
	{
		switch (change)
		{
			case Mystery:
			{
				break;
			}
			case ComparableVar:
			{
				switch (this.getBestGuess())
				{
					case Mystery:
					{
						this.bestGuess = VarTypes.ComparableVar;
						break;
					}
					default:
					{
						break;
					}
				}

				break;
			}
			default:
			{
				final CastCases castCase;

				if (this.getBestGuess() != change)
				{
					switch (this.bestGuess)
					{
						case ComparableVar:
						{
							castCase = CastCases.isLegalCast;
							break;
						}
						case Int:
						case Float:
						{
							switch (change)
							{
								case Math_Variable:
								case String:
								{
									castCase = CastCases.isUnnecessaryCast;
									break;
								}
								case TableObject:
								{
									castCase = CastCases.isLegalCast;
									change = VarTypes.String;
									break;
								}
								default:
								{
									castCase = CastCases.isIllegalCast;
									break;
								}
							}

							break;
						}
						case Math_Variable:
						{
							switch (change)
							{
								case Float:
								case Int:
								{
									castCase = CastCases.isLegalCast;
									break;
								}
								case String:
								{
									castCase = CastCases.isUnnecessaryCast;
									break;
								}
								case TableObject: // it must be a string, logically
								{
									change = VarTypes.String;
									castCase = CastCases.isLegalCast;
									break;
								}
								default:
								{
									castCase = CastCases.isIllegalCast;
									break;
								}
							}
							break;
						}
						case Null:
						{
							castCase = CastCases.isUnnecessaryCast;
							// castCase = CastCases.isLegalCast;
							System.err.println("Be advised: variable '" + this.getMangledName(false) + "' has changed type from " + this.bestGuess + " to " + change);
							// throw new IllegalArgumentException("Be advised: variable '" + this.getMangledName(false) + " has changed type from " + this.bestGuess + " to " + change);
							break;
						}
						case Mystery:
						case TableElement:
						{
							castCase = CastCases.isLegalCast;
							break;
						}
						case String:
						{
							switch (change)
							{
								case Int:
								case Float:
								case TableObject:
								case Math_Variable:
								{
									castCase = CastCases.isUnnecessaryCast;
									break;
								}
								default:
								{
									castCase = CastCases.isIllegalCast;
									break;
								}
							}

							break;
						}
						case TableObject:
						{
							switch (change)
							{
								case FunctionObject:
								case String:
								{
									castCase = CastCases.isUnnecessaryCast;
									break;
								}
								default:
								{
									castCase = CastCases.isIllegalCast;
									break;
								}
							}

							break;
						}
						case FunctionObject:
						{
							switch (change)
							{
								case TableObject:
								{
									castCase = CastCases.isLegalCast;
									break;
								}
								case String:
								{
									castCase = CastCases.isUnnecessaryCast;
									break;
								}
								default:
								{
									castCase = CastCases.isIllegalCast;
									break;
								}
							}

							break;
						}
						default:
						{
							if (bestGuess != change)
							{
								castCase = CastCases.isIllegalCast;
							}
							else
							{
								castCase = CastCases.isLegalCast;
							}

							break;
						}
					}
				}
				else
				{
					castCase = CastCases.isUnnecessaryCast;
				}

				switch (castCase)
				{
					case isIllegalCast:
					{
						// System.out.println("Be advised: variable '" + this.getMangledName() + " has changed type from " + bestGuess + " to " + change);
						throw new IllegalArgumentException("Be advised: variable '" + this.getMangledName(false) + " has changed type from " + this.bestGuess + " to " + change);
					}
					case isLegalCast:
					{
						// System.out.println(this.getMangledName(false) + " is swapping " + this.bestGuess.toString() + " with " + change.toString());
						this.bestGuess = change;
						this.bestGuessIsGood = true;

						if (this.parentVar != null && this.parentVar instanceof MystVar)
						{
							((MystVar) this.parentVar).setBestGuess(change);
						}

						for (Entry<HalfByteArray, Variable> kid : this.childVars)
						{
							final Variable temp1 = kid.getValue();

							if (temp1 instanceof MystVar)
							{
								((MystVar) temp1).setBestGuess(change);
							}
						}

						break;
					}
					case isUnnecessaryCast:
					{
						// System.out.println(this.getMangledName(false) + " is not swapping " + this.bestGuess.toString() + " with " + change.toString());
						break;
					}
					default:
					{
						throw new UnhandledEnumException(castCase);
					}
				}
			}
		}
	}

	private static enum CastCases
	{
		isLegalCast,
		isUnnecessaryCast,
		isIllegalCast;
	}

	@Override
	public VarTypes getVarType()
	{
		final VarTypes result = this.getBestGuess();
		// System.out.println("called from " + this.getMangledName(false) + ", with result " + result);
		return result;
	}

	@Override
	public void revVarType()
	{
		if (!this.bestGuessIsGood)
		{
			// System.out.println("revving " + this.getMangledName(false));
			boolean parentIsNull = this.parentVar == null;

			if (this.bestGuess == VarTypes.Mystery)
			{
				if (!parentIsNull)
				{
					if (this.parentVar instanceof MystVar)
					{
						this.bestGuess = ((MystVar) this.parentVar).getBestGuess(this);
					}
					else
					{
						this.bestGuess = this.parentVar.getVarType();
					}

					// System.out.println("checking parent of " + this.getMangledName(false) + " for type");
				}

				if (this.bestGuess == VarTypes.Mystery)
				{
					final int numKids = this.childVars.size();
					int count = 0;
					// System.out.println("checking children of " + this.getMangledName(false) + " for type. Expected number of children: " + this.childVars.size());

					for (Entry<HalfByteArray, Variable> entry : this.childVars)
					{
						if (count >= numKids)
						{
							throw new IndexOutOfBoundsException();
						}

						final VarTypes temp = entry.getValue().getVarType();

						// System.out.println("checking at key: " + entry.getKey().interpretAsChars());

						if (temp != VarTypes.Mystery)
						{
							this.bestGuess = temp;
							break;
						}
						count++;
					}
				}
			}

			this.bestGuessIsGood = true;
		}
	}

	public VarTypes getBestGuess()
	{
		this.revVarType();
		return this.bestGuess;
	}

	private VarTypes getBestGuess(MystVar callerChild)
	{
		// System.out.println("getting best guess for type of: " + this.getMangledName(false));

		if (!this.bestGuessIsGood)
		{
			if (this.bestGuess == VarTypes.Mystery)
			{
				if (this.parentVar != null)
				{
					if (this.parentVar instanceof MystVar)
					{
						this.bestGuess = ((MystVar) this.parentVar).getBestGuess(this);
					}
					else
					{
						this.bestGuess = this.parentVar.getVarType();
					}

					// System.out.println("checking parent of " + this.getMangledName(false) + " for type");
				}

				if (this.bestGuess == VarTypes.Mystery)
				{
					// System.out.println("checking children of " + this.getMangledName(false) + " for type");

					for (Entry<HalfByteArray, Variable> entry : this.childVars)
					{
						final Variable current = entry.getValue();

						if (current != callerChild)
						{
							final VarTypes temp = current.getVarType();

							if (temp != VarTypes.Mystery)
							{
								this.bestGuess = temp;
								break;
							}
						}
					}
				}
			}

			this.bestGuessIsGood = true;
		}

		return this.bestGuess;
	}

	@Override
	public CharList getValueAsCharList()
	{
		final CharList result = new CharList();

		result.add("<best guess: ");
		result.add(this.getBestGuess().toString());
		result.add('>');

		return result;
	}

	@Override
	public String getMangledName()
	{
		return this.getMangledName(true);
	}

	public String getMangledName(boolean includeBestGuess)
	{
		final CharList result = new CharList();

		result.add(this.namePrefix);

		switch (SpecialInstanceNums.parse(this.instanceNumber))
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

		if (includeBestGuess)
		{
			// result.add("<best guess: ");
			result.add('<');
			result.add(this.getBestGuess().toString());
			result.add('>');
		}

		return result.toString();
	}

	@Override
	public String getMostRelevantIdValue(boolean showInferredTypes)
	{
		return this.getMangledName(showInferredTypes);
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	public Variable getParentVar()
	{
		return this.parentVar;
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
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public String valueToString()
	{
		// returns mangledName
		return this.getMangledName();
	}

	@Override
	public String valueToString(boolean showInferredTypes)
	{
		return this.getMostRelevantIdValue(showInferredTypes);
	}
}
