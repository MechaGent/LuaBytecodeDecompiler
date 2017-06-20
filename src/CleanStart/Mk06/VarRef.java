package CleanStart.Mk06;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.RawEnums;
import CleanStart.Mk06.SubVars.TableElementVar;
import CleanStart.Mk06.VarFormula.FormulaToken;

import java.util.EnumSet;
import java.util.TreeMap;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import SingleLinkedList.Mk01.SingleLinkedList;

public class VarRef extends Var implements Mangleable, FormulaToken
{
	private static final SingleLinkedList<VarRef> allInstances = new SingleLinkedList<VarRef>();
	private static final VarRef[] emptyArr = new VarRef[0];

	private final TreeMap<StepNum, VarValueDefinition> records;
	private Var initialValue;
	private final RawEnums refType;

	private VarRef(String inNamePrefix)
	{
		super(inNamePrefix, RawEnums.WrapperVar.getValue());
		this.records = new TreeMap<StepNum, VarValueDefinition>();

		if (!inNamePrefix.startsWith("global"))
		{
			//inNamePrefix += " /*indeterminate*/";
		}

		this.initialValue = new VarRef_InitialValue(inNamePrefix);

		this.initialValue.incrementNumReads();
		this.refType = RawEnums.WrapperVar;
		
		final VarValueDefinition result = new VarValueDefinition_Init(this.initialValue, this.initialValue);

		this.records.put(StepNum.getInitializerStep(), result);
	}

	private VarRef(String inPrefix, int inInstance, RawEnums inRefType)
	{
		super(inPrefix, inInstance);

		if (inPrefix.length() == 0)
		{
			if (inInstance < 0)
			{
				throw new IllegalArgumentException();
			}
		}
		// this.prefix = inPrefix;
		// this.instance = inInstance;
		this.records = new TreeMap<StepNum, VarValueDefinition>();

		if (inInstance != -1)
		{
			final CharList temp = new CharList();

			temp.add(inPrefix);
			temp.add('_');
			temp.addAsString(inInstance);
			//temp.add(" /*indeterminate*/");

			inPrefix = temp.toString();
		}

		this.initialValue = new VarRef_InitialValue(inPrefix);
		this.refType = inRefType;

		this.initialValue.incrementNumReads();
		final VarValueDefinition result = new VarValueDefinition_Init(this.initialValue, this.initialValue);

		this.records.put(StepNum.getInitializerStep(), result);
	}

	public static VarRef getInstance(String inPrefix)
	{
		return getInstance(inPrefix, -1);
	}

	public static VarRef getInstance(String inPrefix, int inInstance_specific)
	{
		return getInstance(inPrefix, inInstance_specific, RawEnums.NotSpecial);
	}

	public static VarRef getInstance(String inPrefix, int inInstance_specific, RawEnums refType)
	{
		final VarRef result = new VarRef(inPrefix, inInstance_specific, refType);

		//final VarValueDefinition init = new VarValueDefinition_Init(result.initialValue, result.initialValue);

		//result.records.put(StepNum.getInitializerStep(), init);

		allInstances.add(result);
		return result;
	}

	public static VarRef getInstance_forWrapper(Var core, InferenceTypes expectedType)
	{
		// final VarRef result = new VarRef(core.getMangledName(false) + "_wrapper", instanceCounter_wrapper++);
		final VarRef result = new VarRef(core.getMangledName(false) + "_wrapper", RawEnums.WrapperVar.getValue(), RawEnums.WrapperVar);
		
		result.setInitialValue(core, expectedType);
		allInstances.add(result);
		return result;
	}

	public static VarRef getInstance_forWrapper(Var core, InferenceTypes expectedType, InlineSchemes scheme)
	{
		final VarRef result = new VarRef(core.getMangledName(false) + "_wrapper", RawEnums.WrapperVar.getValue(), RawEnums.WrapperVar);
		result.setInitialValue(core, expectedType, scheme);
		allInstances.add(result);
		return result;
	}

	public static SingleLinkedList<VarRef> getAllInstances()
	{
		return allInstances;
	}
	
	public static VarRef[] getEmptyArr()
	{
		return emptyArr;
	}

	@Override
	public String getMangledName()
	{
		return this.getMangledName(false);
	}

	@Override
	public String getMangledName(boolean appendMetadata)
	{
		final CharList result = new CharList();

		switch (this.refType)
		{
			case NotSpecial:
			{
				result.add(this.namePrefix);

				if (this.instanceNumber_specific != -1)
				{
					result.add('_');
					result.addAsString(this.instanceNumber_specific);
				}
				
				break;
			}
			case StaticVar:
			{
				result.add(this.namePrefix);
				result.add("_STATIC");
				break;
			}
			case WrapperVar:
			{
				result.add(this.getLatestValue().getMangledName(appendMetadata));
				break;
			}
			default:
			{
				throw new UnhandledEnumException(RawEnums.parse(this.instanceNumber_specific));
			}
		}

		return result.toString();

		// return this.namePrefix + ((this.instanceNumber_specific == -1) ? "" : '_' + this.instanceNumber_specific) + (appendMetadata ? "<implement VarRef metadata>" : "");
	}

	/**
	 * CAUTION: only call this in the final loop.
	 * 
	 * @param time
	 * @return
	 */
	public Var getValueAtTime(StepNum time)
	{
		final Entry<StepNum, VarValueDefinition> recordEntry = this.records.floorEntry(time);
		final VarValueDefinition record = recordEntry.getValue();
		Var result = record.getValue();
		final Var[] buffer = new Var[2];
		int index = 0;
		buffer[index] = record.getValue();

		/*
		if(buffer[index] instanceof VarRef)
		{
			
		}
		*/

		while (result instanceof VarRef)
		{
			if (result == this)
			{
				return this.initialValue;
			}

			result = ((VarRef) result).records.floorEntry(time).getValue().getValue();
		}

		return result;
	}

	public CharList generateTypeHistory()
	{
		final CharList result = new CharList();
		boolean isFirst = true;

		for (Entry<StepNum, VarValueDefinition> entry : this.records.entrySet())
		{
			if (!isFirst)
			{
				result.add(", ");
			}
			else
			{
				isFirst = false;
			}

			result.add('{');
			result.add(entry.getKey().getInDiagForm(), true);
			result.add(", ");
			final VarValueDefinition def = entry.getValue();

			/*
			if(def instanceof VarValueDefinition_Init)
			{
				result.add("<VVD_Init>");
			}
			else if(def instanceof VarValueDefinition_Normal)
			{
				result.add("<VVD_Norm>");
			}
			*/

			/*
			if (def.isFromChangedState())
			{
				result.add("<state changed>");
			}
			else
			{
				result.add(def.getValue_NoInlining().getValueAsString());
			}
			*/

			// result.add(def.getValue_NoInlining().getValueAsString());
			result.add(def.getValue().getValueAsString());
			// result.add('(');
			// result.addAsString(def.getValue().getNumReads());
			// result.add(')');
			result.add('}');
		}

		return result;
	}

	public Var getNonInlinedValueAtTime(StepNum time)
	{
		final Entry<StepNum, VarValueDefinition> recordEntry = this.records.floorEntry(time);
		final VarValueDefinition record = recordEntry.getValue();
		return record.getValue_NoInlining();
	}

	public Var getNonInlinedValueAtTime(StepNum time, InferenceTypes expectedType)
	{
		final Entry<StepNum, VarValueDefinition> recordEntry = this.records.floorEntry(time);
		final VarValueDefinition record = recordEntry.getValue();
		record.TypeClues.add(expectedType);
		return record.getValue_NoInlining();
	}

	protected Entry<StepNum, VarValueDefinition> getNonInlinedEntryAtTime(StepNum time)
	{
		return this.records.floorEntry(time);
	}

	public void logReadAtTime(StepNum timeOfRead, InferenceTypes expectedType)
	{
		this.records.floorEntry(timeOfRead).getValue().incrementNumReads();

		// final Var sourceValue = this.getNonInlinedValueAtTime(timeOfRead);

		/*
		if(sourceValue == this.initialValue)
		{
			System.err.println("@ time: " + timeOfRead.getInDiagForm().toString() + ", indeterminate value read of " + this.getMangledName());
		}
		*/

		// sourceValue.incrementNumReads();

		// System.out.println("logging read for varref " + this.getMangledName(false) + ": " + timeOfRead.getInDiagForm().toString());
	}

	/**
	 * this one's for single variables, tableElements should use the other one
	 * 
	 * @param source
	 * @param index
	 * @param timeOfRead
	 * @param timeOfWrite
	 * @param expectedType
	 */
	public void logWriteAtTime(Var source, InstructionIndex index, StepNum timeOfRead, StepNum timeOfWrite, InferenceTypes expectedType)
	{
		final VarValueDefinition result;
		
		if (source == this)
		{
			result = new VarValueDefinition_Normal(this.initialValue, this.initialValue, index);

			this.records.put(timeOfWrite, result);
		}
		else
		{
			//source.incrementNumReads();
			// System.out.println("here");
			result = new VarValueDefinition_Normal(source, source, index);

			this.records.put(timeOfWrite, result);
		}
		
		//result.incrementNumReads();
	}
	
	/**
	 * this one's for single variables, tableElements should use the other one
	 * 
	 * @param source
	 * @param index
	 * @param timeOfRead
	 * @param timeOfWrite
	 * @param expectedType
	 */
	public void logWriteAtTime(VarRef source, InstructionIndex index, StepNum timeOfRead, StepNum timeOfWrite, InferenceTypes expectedType)
	{

		if (source == this)
		{
			final VarValueDefinition result = new VarValueDefinition_Normal(this.initialValue, this.initialValue, index);

			this.records.put(timeOfWrite, result);
		}
		/*
		else if(source.records.lastEntry().getValue().getValue() == this)
		{
			throw new IllegalArgumentException("\r\nvar1: " + source.generateTypeHistory().toString() + "\r\nvar2: " + this.generateTypeHistory().toString());
		}
		*/
		else
		{
			final Var temp = source.getValueAtTime(timeOfRead);
			//System.out.println("source " + temp.getMangledName(false) + " has numReads: " + temp.getNumReads());
			//temp.incrementNumReads();
			// System.out.println("here");
			final VarValueDefinition result = new VarValueDefinition_Normal(temp, source, index);

			this.records.put(timeOfWrite, result);
		}
		
		this.resetNumReads();

		// final Var sourceValue = source.getNonInlinedValueAtTime(timeOfRead, expectedType);

		/*
		final Entry<StepNum, VarValueDefinition> entry = source.getNonInlinedEntryAtTime(timeOfRead);
		final VarValueDefinition def = entry.getValue();
		
		final Var sourceValue = def.getValue_NoInlining();
		
		
		if (sourceValue == this.initialValue)
		{
			throw new IllegalArgumentException();
			// System.err.println("@ time: " + timeOfRead.getInDiagForm().toString() + ", indeterminate value read of " + source.getMangledName() + " by " + this.getMangledName());
		}
		
		sourceValue.incrementNumReads();
		*/

		// final VarValueDefinition result = new VarValueDefinition_Normal(sourceValue, this.initialValue, index);
	}

	public void logWriteAtTime(Var sourceTable, Var sourceIndex, InstructionIndex index, StepNum timeOfRead, StepNum timeOfWrite, InferenceTypes expectedType)
	{
		final VarValueDefinition result = new VarValueDefinition_Indexed(this.initialValue, sourceTable, sourceIndex);

		this.records.put(timeOfWrite, result);
		
		this.resetNumReads();
	}

	public void logMiscClue(InferenceTypes expectedType)
	{
		this.records.lastEntry().getValue().TypeClues.add(expectedType);
	}

	public void setInitialValue(Var initialValue, InferenceTypes expectedType)
	{
		initialValue.incrementNumReads();
		final VarValueDefinition result = new VarValueDefinition_Init(initialValue, this.initialValue);

		this.records.put(StepNum.getInitializerStep(), result);
	}

	public void setInitialValue(Var initialValue, InferenceTypes expectedType, InlineSchemes scheme)
	{
		initialValue.incrementNumReads();
		final VarValueDefinition result = new VarValueDefinition_Init(initialValue, this.initialValue, scheme);

		this.records.put(StepNum.getInitializerStep(), result);
		this.initialValue = initialValue;
	}

	public void logStateChangeAtTime(InstructionIndex index, StepNum timeOfChange)
	{
		final VarValueDefinition result = new VarValueDefinition_Normal(new Var_Indeterm(this.namePrefix + '_' + this.instanceNumber_specific), this.initialValue, index);

		this.records.put(timeOfChange, result);
		
		this.resetNumReads();
	}

	public Var getInitialValue()
	{
		return this.initialValue;
	}

	public Var getLatestValue()
	{
		final Entry<StepNum, VarValueDefinition> recordEntry = this.records.lastEntry();
		final VarValueDefinition record = recordEntry.getValue();
		Var result = record.getValue();

		while (result instanceof VarRef)
		{

			if (result == this)
			{
				// System.out.println("current result (same): " + result);
				return this.initialValue;
			}

			final Entry<StepNum, VarValueDefinition> locEntry = ((VarRef) result).records.lastEntry();
			final Var temp = locEntry.getValue().getValue();

			if (temp == result)
			{
				// System.out.println("current result: " + result);
				return ((VarRef) result).initialValue;
			}
			else
			{
				result = temp;
			}
		}

		return result;
	}

	@Override
	public void incrementNumReads()
	{
		//super.incrementNumReads();
		//this.getLatestValue().incrementNumReads();
		this.records.lastEntry().getValue().incrementNumReads();
	}
	
	@Override
	public void decrementNumReads()
	{
		this.getLatestValue().decrementNumReads();
	}

	@Override
	public int getNumReads()
	{
		//return super.getNumReads();
		//return this.getLatestValue().getNumReads();
		//return this.records.lastEntry().getValue().getNumReads();
		return 69;	//manually shutting this off until I can think about it more
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
	public int getPrecedence()
	{
		return -1;
	}

	@Override
	public String getValueAsString()
	{
		final CharList result = new CharList();

		switch (RawEnums.parse(this.instanceNumber_specific))
		{
			case NotSpecial:
			{
				result.add(this.namePrefix);
				result.add('_');
				result.addAsString(this.instanceNumber_specific);
				break;
			}
			case StaticVar:
			{
				result.add(this.namePrefix);
				result.add("_STATIC");
				break;
			}
			case WrapperVar:
			{
				result.add(this.getLatestValue().getValueAsString());
				break;
			}
			default:
			{
				throw new UnhandledEnumException(RawEnums.parse(this.instanceNumber_specific));
			}
		}

		return result.toString();
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return this.initialValue.getInitialType();
	}

	@Override
	public int getValueAsInt()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public float getValueAsFloat()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getValueAsBoolean()
	{
		throw new UnsupportedOperationException();
	}

	private static class Var_Indeterm extends Var
	{
		protected Var_Indeterm(String inNamePrefix)
		{
			super(inNamePrefix, RawEnums.WrapperVar.getValue());
		}

		@Override
		public InferenceTypes getInitialType()
		{
			return InferenceTypes.Mystery;
		}

		@Override
		public String getValueAsString()
		{
			return this.namePrefix + "_<Indeterminate>";
		}

		@Override
		public int getValueAsInt()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public float getValueAsFloat()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getValueAsBoolean()
		{
			throw new UnsupportedOperationException();
		}
	}

	private static class VarRef_InitialValue extends Var
	{
		public VarRef_InitialValue(String inMangledName)
		{
			super(inMangledName, RawEnums.WrapperVar.getValue());
		}

		@Override
		public InferenceTypes getInitialType()
		{
			return InferenceTypes.Mystery;
		}

		@Override
		public String getMangledName(boolean includeMetadata)
		{
			return this.namePrefix;
		}

		@Override
		public String getValueAsString()
		{
			return this.getMangledName();
		}

		@Override
		public int getValueAsInt()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public float getValueAsFloat()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getValueAsBoolean()
		{
			throw new UnsupportedOperationException();
		}
	}

	private static abstract class VarValueDefinition
	{
		protected final Var initialAssign;
		protected final EnumSet<InferenceTypes> TypeClues;
		protected final Var altAssign;
		protected int numReads;

		public VarValueDefinition(Var inInitialAssign, Var inAltAssign)
		{
			this.initialAssign = inInitialAssign;

			this.TypeClues = EnumSet.noneOf(InferenceTypes.class);
			this.TypeClues.add(inInitialAssign.getInitialType());

			this.altAssign = inAltAssign;
			this.numReads = 0;
		}

		public Var getValue_NoInlining()
		{
			return this.initialAssign;
		}
		
		public void incrementNumReads()
		{
			this.numReads++;
		}
		
		public int getNumReads()
		{
			return this.numReads;
		}

		public abstract Var getValue();

		public abstract InlineSchemes getScheme();
	}

	private static class VarValueDefinition_Init extends VarValueDefinition
	{
		private final InlineSchemes scheme;

		public VarValueDefinition_Init(Var inInitialAssign, Var inAltAssign)
		{
			super(inInitialAssign, inAltAssign);
			this.scheme = InlineSchemes.Inline_EveryTime;
		}

		public VarValueDefinition_Init(Var inInitialAssign, Var inAltAssign, InlineSchemes inScheme)
		{
			super(inInitialAssign, inAltAssign);
			this.scheme = inScheme;
		}

		@Override
		public Var getValue()
		{
			//System.out.println("returning " + this.initialAssign.getMangledName(false));
			
			return this.initialAssign;
		}

		@Override
		public InlineSchemes getScheme()
		{
			return this.scheme;
		}
	}

	private static class VarValueDefinition_Normal extends VarValueDefinition
	{
		protected final InstructionIndex index;
		protected boolean hasUpdatedExiration;

		public VarValueDefinition_Normal(Var inInitialAssign, Var inAltAssign, InstructionIndex inIndex)
		{
			super(inInitialAssign, inAltAssign);
			this.index = inIndex;

			this.hasUpdatedExiration = false;
		}

		@Override
		public Var getValue()
		{
			final Var result;

			switch (this.index.getRelevantScheme())
			{
				case Inline_EveryTime:
				{
					result = this.initialAssign;

					if (!this.hasUpdatedExiration)
					{
						this.index.setPartialExpirationState(true);
						this.hasUpdatedExiration = true;
					}

					break;
				}
				case Inline_IfOnlyReadOnce:
				{
					//System.out.println("testing: " + this.initialAssign.getValueAsString() + ", with " + this.initialAssign.getNumReads() + " reads");

					switch (this.initialAssign.getNumReads())
					{
						case 1:
						{
							result = this.initialAssign;

							if (!this.hasUpdatedExiration)
							{
								this.index.setPartialExpirationState(true);
								this.hasUpdatedExiration = true;
							}
							break;
						}
						case 0:
						{
							// System.out.println("read 0 times: " + this.initialAssign.getMangledName());
						}
						default:
						{
							result = this.altAssign;
							break;
						}
					}

					break;
				}
				case Inline_Never:
				case NonApplicable:
				{
					result = this.altAssign;
					break;
				}
				default:
				{
					throw new UnhandledEnumException(this.index.getRelevantScheme());
				}
			}

			return result;
		}

		@Override
		public InlineSchemes getScheme()
		{
			return this.index.getRelevantScheme();
		}
	}

	private static class VarValueDefinition_Indexed extends VarValueDefinition
	{
		@SuppressWarnings("unused")
		private final Var table;
		@SuppressWarnings("unused")
		private final Var index;

		public VarValueDefinition_Indexed(Var inAltAssign, Var inTable, Var inIndex)
		{
			super(TableElementVar.getInstance("", inTable, inIndex, InferenceTypes.TableElement), inAltAssign);
			this.table = inTable;
			this.index = inIndex;
			this.initialAssign.incrementNumReads();
		}

		@Override
		public Var getValue()
		{
			return this.initialAssign;
		}

		@Override
		public InlineSchemes getScheme()
		{
			return InlineSchemes.Inline_EveryTime;
		}
	}
}
