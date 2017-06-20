package CleanStart.Mk06.FunctionObjects;

import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Instruction.InstructionList_Locked;
import CleanStart.Mk06.MiscAutobot;
import CleanStart.Mk06.MiscAutobot.ParseTypes;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.OpCodes;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import CleanStart.Mk06.Instructions.MathOp;
import CleanStart.Mk06.Instructions.SetVar;
import CleanStart.Mk06.VarFormula.Operators;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

/**
 * convenience class, to allow better component testing
 * 
 * @author MechaGent
 *
 */
class ProcessingData
{
	private final RawFunctionObject rawObject;
	private final RefinementStagesChecklist checklist;
	private final HalfByteRadixMap<Label> labelsQueue;
	private final int[] lineWeights;
	private final SingleLinkedList<Jump_Straight> Jumps_Straight;
	private final SingleLinkedList<Jump_Branched> Jumps_Branched;

	private InstructionList_Locked instructions;

	private ProcessingData(RawFunctionObject inRawObject, RefinementStagesChecklist inChecklist, InstructionList_Locked inInstructions)
	{
		this.rawObject = inRawObject;
		this.checklist = inChecklist;
		this.labelsQueue = new HalfByteRadixMap<Label>();
		this.lineWeights = new int[inRawObject.rawInstructions.length];
		this.Jumps_Straight = new SingleLinkedList<Jump_Straight>();
		this.Jumps_Branched = new SingleLinkedList<Jump_Branched>();
		this.instructions = inInstructions;
	}
	
	public static ProcessingData getInstance(RawFunctionObject inRawObject, RefinementStagesChecklist inChecklist, InstructionList_Locked inInstructions)
	{
		return new ProcessingData(inRawObject, inChecklist, inInstructions);
	}
	
	public void setInstructions(InstructionList_Locked newInstructions)
	{
		this.instructions = newInstructions;
	}
	
	public InstructionList_Locked getInstructionList()
	{
		return this.instructions;
	}
	
	public Instruction getFirstInstruction()
	{
		return this.instructions.getFirst();
	}
	
	public Instruction getLastInstruction()
	{
		return this.instructions.getLast();
	}
	
	public void addJump_Straight(Jump_Straight in)
	{
		this.Jumps_Straight.add(in);
	}
	
	public void addJump_Branched(Jump_Branched in)
	{
		this.Jumps_Branched.add(in);
	}
	
	public SingleLinkedList<Jump_Straight> getJumps_Straight()
	{
		return this.Jumps_Straight;
	}
	
	public SingleLinkedList<Jump_Branched> getJumps_Branched()
	{
		return this.Jumps_Branched;
	}
	
	public int getNumRawInstructions()
	{
		return this.lineWeights.length;
	}
	
	public void incrementLineWeight(int lineIndex)
	{
		this.lineWeights[lineIndex]++;
	}
	
	public int incrementAndGetLineWeight(int lineIndex)
	{
		return ++this.lineWeights[lineIndex];
	}
	
	public int getLineWeight(int lineIndex)
	{
		return this.lineWeights[lineIndex];
	}
	
	public int getRawLine(int lineIndex)
	{
		return this.getRawObject().rawInstructions[lineIndex];
	}
	
	public OpCodes getOpcodeForRawLine(int lineIndex)
	{
		return OpCodes.parse(this.getRawObject().rawInstructions[lineIndex]);
	}
	
	public int[] parseFieldsOfRawLineAs(int lineIndex, ParseTypes type)
	{
		return MiscAutobot.parseRawInstruction(this.getRawObject().rawInstructions[lineIndex], type);
	}
	
	public MathOp parseRawLineAsMathOp(int lineIndex, Operators opType, StepNum currentStep)
	{
		return this.getRawObject().getMathOp_FromIndex(lineIndex, opType, currentStep);
	}
	
	public SetVar composeGlobalGetterOrSetter(int lineIndex, StepNum currentStep, boolean wantsGetter)
	{
		return this.getRawObject().composeGlobalGetterOrSetter_FromIndex(lineIndex, currentStep, wantsGetter);
	}
	
	public Label getLabelForJumpOffsetOf(int offset, int lineIndex, StepNum inStartTime)
	{
		final int absOffset = offset + lineIndex + 1;
		Label result = this.labelsQueue.get(absOffset);

		if (result == null)
		{
			result = Label.getInstance(inStartTime, "jumpLabel");
			this.labelsQueue.put(absOffset, result);
		}

		return result;
	}
	
	public Label getSkipNextStepLabel(int lineIndex, StepNum inStartTime)
	{
		return this.getLabelForJumpOffsetOf(1, lineIndex, inStartTime);
	}
	
	public Label getLabelFromQueue(int absOffset)
	{
		return this.labelsQueue.get(absOffset);
	}

	public VarRef getVar(VarRepos repo, int index)
	{
		final VarRef result;

		switch (repo)
		{
			case Constants:
			{
				result = this.getRawObject().Constants[index];
				break;
			}
			case Locals:
			{
				result = this.getRawObject().Locals[index];
				break;
			}
			case Upvalues:
			{
				result = this.getRawObject().Upvalues[index];
				break;
			}
			default:
			{
				throw new UnhandledEnumException(repo);
			}
		}
		
		return result;
	}
	
	public VarRef getRkVar(int lineIndex)
	{
		return this.getRawObject().getRkVar(lineIndex);
	}
	
	public FunctionObject getFunctionPrototype(int index)
	{
		return this.getRawObject().rawFunctionPrototypes[index];
	}

	public int getRepoLength(VarRepos repo)
	{
		final int result;

		switch (repo)
		{
			case Constants:
			{
				result = this.getRawObject().Constants.length;
				break;
			}
			case Locals:
			{
				result = this.getRawObject().Locals.length;
				break;
			}
			case Upvalues:
			{
				result = this.getRawObject().Upvalues.length;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(repo);
			}
		}

		return result;
	}

	public RawFunctionObject getRawObject()
	{
		return rawObject;
	}

	static enum VarRepos
	{
		Locals,
		Constants,
		Upvalues;
	}
}
