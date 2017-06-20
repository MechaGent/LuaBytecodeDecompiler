package CleanStart.Mk04_Working;

import CharList.CharList;
import CleanStart.Mk04_Working.Instructions.ForLoop;
import CleanStart.Mk04_Working.Instructions.Jump_Branched;
import CleanStart.Mk04_Working.Instructions.Jump_Straight;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public interface Instruction
{
	public boolean isExpired();

	public void setExpirationState(boolean newExpirationState);

	public Instruction getNext();

	/**
	 * like calling setNext(in, true);
	 * 
	 * @param in
	 */
	public void setNext(Instruction in);

	public void setNext(Instruction in, boolean linkBoth);

	public Instruction getPrev();

	public int getInstanceNumber();

	/**
	 * like calling setPrev(in, true);
	 * 
	 * @param in
	 */
	public void setPrev(Instruction in);

	public void setPrev(Instruction in, boolean linkBoth);

	public IrCodes getIrCode();

	public CanBeInlinedTypes canBeInlined();

	public Variable getInlinedForm();

	public String getMangledName();

	public CharList getVerboseCommentForm();

	public void revVarTypes();

	public Instruction getFirstSharedInstructionWith(Instruction other);

	public static void link(Instruction node1, Instruction node2)
	{
		node1.setNext(node2);
		node2.setPrev(node1);
	}

	public static Instruction getFirstSharedInstructionBetween(Instruction first, Instruction other)
	{
		if (first != other)
		{
			final SingleLinkedList<Instruction> Tbd = new SingleLinkedList<Instruction>();

			final HalfByteRadixMap<Instruction> processed = new HalfByteRadixMap<Instruction>();
			final int minInstanceNum;
			boolean isOnFirst = true;

			if (first.getInstanceNumber() < other.getInstanceNumber())
			{
				minInstanceNum = other.getInstanceNumber();
				Tbd.add(other);
				Tbd.add(first);
			}
			else
			{
				minInstanceNum = first.getInstanceNumber();
				Tbd.add(first);
				Tbd.add(other);
			}

			while (Tbd.isNotEmpty())
			{
				final Instruction current = Tbd.pop();
				//System.out.println(first.getMangledName() + " vs " + other.getMangledName() + ": trying " + current.getMangledName());

				if (!processed.contains(current.getMangledName()))
				{
						isOnFirst = false;
						processed.put(current.getMangledName(), current);

						switch (current.getIrCode())
						{
							case EvalLoop_For:
							{
								final ForLoop cast = (ForLoop) current;
								Tbd.add(cast.getToAfterLoop());
								break;
							}
							case Jump_Straight:
							{
								final Jump_Straight cast = (Jump_Straight) current;
								Tbd.add(cast.getJumpTo());
								break;
							}
							case Jump_Conditional:
							{
								final Jump_Branched cast = (Jump_Branched) current;
								Tbd.add(cast.getJump_True());
								Tbd.add(cast.getJump_False());

								break;
							}
							case Return_Concretely:
							case Return_Variably:
							{
								return null;
							}
							case EvalLoop_ForEach:
							default:
							{
								Tbd.add(current.getNext());
								break;
							}
						}
				}
				else
				{
					if (current.getInstanceNumber() >= minInstanceNum && !isOnFirst)
					{
						//System.out.println("first shared instruction between " + first.getMangledName() + " and " + other.getMangledName() + " is " + current.getMangledName());
						return current;
					}
				}
			}

			return null;
		}
		else
		{
			return first;
		}
	}
}
