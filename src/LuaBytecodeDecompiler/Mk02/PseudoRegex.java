package LuaBytecodeDecompiler.Mk02;

import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;

public class PseudoRegex
{
	public void foldAssignments(linkedInstructionListWindow in)
	{
		/*
		 * if a LoadK is followed directly by a Set*, and if the left side of the LoadK is the right side of the Set*, fold.
		 */
		
		
	}
	
	private static void foldAssignments_LoadSets(linkedInstructionListWindow in)
	{
		boolean isDone = false;
		
		while(!isDone)
		{
			final Instruction current = in.next();
			
			switch(current.getOpCode())
			{
				case LoadK:
				{
					final Instruction next = in.next();
					
					switch(next.getOpCode())
					{
						case SetGlobal:
						{
							if(current.getVarCode_Left().equals(next.getVarCode_Right()))
							{
								in.replaceInstructionsRelatively(-1, 2, replacements);
							}
							break;
						}
						default:
						{
							in.previous();	//to reverse the window to where it would otherwise be
							break;
						}
					}
					break;
				}
				default:
				{
					break;
				}
			}
		}
	}
	
	public static interface linkedInstructionListWindow
	{
		/**
		 * marks instruction, so that it can be easily jumped back to later
		 * @param markNum
		 */
		public void markLocation(int markNum);
		
		/**
		 * replaces current instruction with replacement instructions
		 * @param replacements
		 */
		public void replaceCurrentInstruction(Instruction... replacements);
		
		public void replaceCurrentInstructions(int oldLength, Instruction... replacements);
		
		public void replaceInstructionsRelatively(int offsetFromCurrent, int length, Instruction... replacements);
		
		public Instruction next();
		
		public Instruction advance(int numSteps);
		
		public Instruction previous();
		
		public Instruction retreat(int numSteps);
		
		public Instruction jumpToMarkedLocation(int markNum);
		
		public boolean hasNext();
		
		public boolean hasPrevious();
		
		public boolean hasStepsRemaining(int numSteps);
	}
}
