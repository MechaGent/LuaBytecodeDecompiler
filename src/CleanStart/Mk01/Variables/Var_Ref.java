package CleanStart.Mk01.Variables;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.InstancedVariable;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.VarFormula.FormulaToken;
import SingleLinkedList.Mk01.SingleLinkedList;

/**
 * 
 * this should be used for all the vars directly exposed from FunctionObject, ie locals and upvalues
 *
 */
public class Var_Ref extends BaseVariable implements FormulaToken
{
	// private static final Var_Ref indeterminateValue = new Var_Ref("", StepStage.getNullStepStage(), new Var_Null(StepStage.getNullStepStage()));
	private static final Variable indeterminateValue = new Var_String("indeterminateVarValueVar", StepStage.getNullStepStage(), "IndeterminateVar", true);
	private static final boolean shouldOptimize = false;

	private final TreeMap<StepStage, Life> assigns;

	public Var_Ref(String inMangledName, StepStage initialTime, Variable initialVar)
	{
		super(inMangledName, initialTime);

		if (inMangledName.length() == 0)
		{
			throw new IllegalArgumentException();
		}

		this.assigns = new TreeMap<StepStage, Life>();
		final Life initialLife = new Life(initialVar.getEvaluatedValueAtTime(initialTime));
		this.assigns.put(initialTime, initialLife);
		//final Life innerInitialLife = new Life(BaseVariable.initialVar);
		final Life innerInitialLife = new Life(indeterminateValue);
		this.assigns.put(StepStage.getNullStepStage(), innerInitialLife);
	}

	public boolean logIndeterminateWriteAtTime(StepStage time)
	{
		return this.logWriteAtTime(time, indeterminateValue);
	}

	public boolean logWriteAtTime(StepStage time, Var_Ref setTo)
	{
		final Variable arg = ((Var_Ref) setTo).getEvaluatedValueAtTime(time);

		if (arg.getType() != VariableTypes.Reference)
		{
			return this.logWriteAtTime(time, arg);
		}
		else
		{
			final Entry<StepStage, Life> flooredEntry = this.assigns.floorEntry(time);
			final Life find = flooredEntry.getValue();

			final boolean result;

			if (find.writeValue.equals(arg) && arg != indeterminateValue)
			{
				// write is extraneous

				if (find.hasExtraWrites())
				{
					((ExtraLife) find).extraWrites.add(time);
				}
				else
				{
					this.assigns.put(flooredEntry.getKey(), new ExtraLife(find, time));
					// flooredEntry.setValue(new ExtraLife(find, time));
				}

				result = false;
			}
			else
			{
				this.assigns.put(time, new Life(arg));
				result = true;
			}

			return result;
		}
	}

	/**
	 * 
	 * @param time
	 * @param setTo
	 * @return false if write was unneeded, true otherwise
	 */
	public boolean logWriteAtTime(StepStage time, Variable setTo)
	{
		if (setTo.getType() == VariableTypes.Reference)
		{
			return this.logWriteAtTime(time, (Var_Ref) setTo);
		}
		else
		{
			/*
			if(setTo.getEvaluatedValueAtTime(time) == BaseVariable.initialVar)
			{
				setTo = new Var_String("initialVarValueVar", time, setTo.getMangledName(), true);
			}
			*/

			// System.out.println("Setting Var_Ref " + this.mangledName + " equal to " + setTo.getMangledName() + " at time " + time.toCharList().toString());
			final Entry<StepStage, Life> flooredEntry = this.assigns.floorEntry(time);
			final Life find = flooredEntry.getValue();

			switch (setTo.getType())
			{
				case InstancedReference:
				{
					final Var_InstancedRef cast = (Var_InstancedRef) setTo;
					setTo = cast.getEvaluatedValueAtTime(time);
					break;
				}
				default:
				{
					break;
				}
			}

			final boolean result;

			if (setTo == this)
			{
				throw new IllegalArgumentException();
			}

			if (find.writeValue.equals(setTo) && setTo != indeterminateValue)
			{
				// write is extraneous

				if (find.hasExtraWrites())
				{
					((ExtraLife) find).extraWrites.add(time);
				}
				else
				{
					this.assigns.put(flooredEntry.getKey(), new ExtraLife(find, time));
					// flooredEntry.setValue(new ExtraLife(find, time));
				}

				result = false;
			}
			else
			{
				this.assigns.put(time, new Life(setTo));
				result = true;
			}

			return result;
		}
	}

	public void logReadAtTime(StepStage time)
	{
		final Entry<StepStage, Life> entry = this.assigns.floorEntry(time);

		if (entry != null)
		{
			entry.getValue().reads.add(time);
		}
		else
		{
			System.out.println("read of variable " + this.mangledName + " failed at time " + time.toCharList().toString());
		}
	}

	public void logInternalStateChangeAtTime(StepStage time)
	{
		// TODO this is just a stopgap, need to think about this more
		this.logIndeterminateWriteAtTime(time);
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		if (shouldOptimize)
		{
			final Life targetLife = this.assigns.floorEntry(inTime).getValue();
			final Variable writeValue = targetLife.writeValue;

			// System.out.println("descending from " + this.mangledName + " to " + writeValue.getMangledName());
			if (writeValue.getMangledName().length() == 0)
			{
				throw new IllegalArgumentException(writeValue.getType().toString());
			}

			if (writeValue == BaseVariable.initialVar)
			{
				return this.mangledName;
			}
			else if (writeValue == indeterminateValue)
			{
				return this.mangledName;
			}
			else if (!(writeValue instanceof InstancedVariable))
			{
				// return writeValue.getMangledName();
				return writeValue.valueToString();
			}
			else if(writeValue.getType() == VariableTypes.Reference)
			{
				final Variable value = ((Var_Ref) writeValue).getEvaluatedValueAtTime(inTime);
				
				if(value == indeterminateValue)
				{
					return this.mangledName;
				}
			}

			return writeValue.valueToStringAtTime(inTime);
			// return this.mangledName;
		}
		else
		{
			return this.mangledName;
		}
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		final Variable result = this.assigns.floorEntry(inTime).getValue().writeValue;

		if (result == indeterminateValue)
		{
			return this;
		}
		else
		{
			return result;
		}
	}

	@Override
	public String valueToString()
	{
		// return super.getIndeterminantValue();
		return this.mangledName;
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.Reference;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		final Comparisons result;

		switch (in.getType())
		{
			case Reference:
			{
				result = this.compareWith((Var_Ref) in, inTime);
				break;
			}
			default:
			{
				result = super.compareWith_base(in);
				break;
			}
		}

		return result;
	}

	public Comparisons compareWith(Var_Ref in, StepStage inTime)
	{
		return this.getEvaluatedValueAtTime(inTime).compareWith(in.getEvaluatedValueAtTime(inTime), inTime);
	}

	public static Variable getIndeterminateValue()
	{
		return indeterminateValue;
	}

	private static class Life
	{
		private final Variable writeValue;
		private final SingleLinkedList<StepStage> reads;

		public Life(Variable inWriteValue)
		{
			this(inWriteValue, new SingleLinkedList<StepStage>());
		}

		protected Life(Variable inWriteValue, SingleLinkedList<StepStage> inReads)
		{
			/*
			if (inWriteValue.getType() == VariableTypes.Reference)
			{
				throw new IllegalArgumentException();
			}
			*/

			this.writeValue = inWriteValue;
			this.reads = inReads;
		}

		public boolean hasExtraWrites()
		{
			return false;
		}
	}

	private static class ExtraLife extends Life
	{
		private final TreeSet<StepStage> extraWrites;

		public ExtraLife(Life old, StepStage extraWriteTime)
		{
			this(old.writeValue, old.reads, extraWriteTime);
		}

		private ExtraLife(Variable inWriteValue, SingleLinkedList<StepStage> inReads, StepStage extraWriteTime)
		{
			super(inWriteValue, inReads);
			this.extraWrites = new TreeSet<StepStage>();
			this.extraWrites.add(extraWriteTime);
		}

		@Override
		public boolean hasExtraWrites()
		{
			return true;
		}
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public String valueToStringAtTime(StepStage inTime)
	{
		return this.getEvaluatedValueAsStringAtTime(inTime);
	}
}
