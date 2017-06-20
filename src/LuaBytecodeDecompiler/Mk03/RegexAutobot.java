package LuaBytecodeDecompiler.Mk03;

import DataStructures.Maps.HalfByteRadix.Mk02.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler.StateSlice;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler.VarTouchy;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Self;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Instruction;

public class RegexAutobot
{
	//private static final int compositeRawOpCode = 0xffff;

	public static AnnoFunction linkLocalVars3(AnnoFunction in)
	{
		final VarFlowHandler flow = in.getVarFlow();

		//System.out.println(flow.toString());

		while (flow.hasMoreReads())
		{
			final VarTouchy nextRead = flow.getNextRead();
			// System.out.println(nextRead.getInstruction().toString());

			final StateSlice writeHistory = flow.getWriteHistory(nextRead.getVar());

			if (writeHistory != null)
			{
				VarTouchy lastWrite = writeHistory.getLastVarWriteBefore(nextRead);
				//System.out.println("search for last write of " + nextRead.getVar().getAdjustedName() + ":");

				if (lastWrite != null)
				{
					//System.out.println("found as: " + lastWrite.getInstruction() + " - " + lastWrite.getSubline());
					final AnnoVar_Instruction replace;
					
					if (lastWrite.getInstruction().getOpCode() == AbstractOpCodes.Self)
					{
						final AnnoInstruction_Self cast = (AnnoInstruction_Self) lastWrite.getInstruction();
						final int subline = lastWrite.getSubline();
						
						final AnnoInstruction nextInstruction = cast.getPseudoInstructionFor(subline);
						
						//System.out.println("SELF's replacement index: " + subline + " gives result of: " + nextInstruction.toCharList_InlineForm().toString());
						
						if(subline == 3)
						{
							replace = new AnnoVar_Instruction("inlined_", ScopeLevels.Temporary, nextInstruction, true);
						}
						else
						{
							replace = new AnnoVar_Instruction("inlined_", ScopeLevels.Temporary, nextInstruction);
						}
					}
					else
					{
						replace = new AnnoVar_Instruction("inlined: ", ScopeLevels.Temporary, lastWrite.getInstruction(), lastWrite);
						lastWrite.getInstruction().setAsNoOp();
					}
					
					nextRead.getInstruction().replaceStep(nextRead.getSubline(), replace);
				}
				else
				{
					// throw new IndexOutOfBoundsException("this read has no pre-existing definition! " + nextRead.getInstruction().toString());
					System.err.println("this read has no pre-existing definition for (" + nextRead.getVar().getAdjustedName() + ")! " + nextRead.getInstruction().toString());
				}
			}
			else
			{
				// throw new IndexOutOfBoundsException("this read has no pre-existing definition! " + nextRead.getInstruction().toString());
				// System.err.println("this read has no pre-existing history for (" + nextRead.getVar().getAdjustedName() + ")! " + nextRead.getInstruction().toString());
			}
		}

		return in;
	}
	
	public static HalfByteRadixMap<AnnoInstruction> generateJumpLabels(AnnoFunction in)
	{
		
	}
}
