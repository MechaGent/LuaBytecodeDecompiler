package LuaBytecodeDecompiler.Mk03;

import java.util.Map.Entry;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import DataStructures.Maps.HalfByteRadix.Mk02.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk02.Constant;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk02.Function.Local;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructionFactory.InstructionIterator;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Function;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Int;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_String;

public class AnnoFunction
{
	private static final HalfByteRadixMap<AnnoVar> Globals = new HalfByteRadixMap<AnnoVar>();
	private static final AnnoVar NullAnnoVar = new AnnoVar("NullAnnoVar", ScopeLevels.Global);
	private static final boolean shouldAttemptToReplaceSteps = false;

	private final String parentName;
	private final String functionName;
	private final int[] definitionBounds;
	private final int numParameters;
	private final int isVarArgFlag;

	private final AnnoVar[] LocalVars;
	private final AnnoVar[] Constants;
	private final AnnoVar[] Upvalues;
	private final AnnoFunction[] Functions;
	private final AnnoInstruction[] instructions;
	// private final HalfByteRadixMap<Integer> Labels;
	private final String[] Labels;
	private final VarFlowHandler varFlow;

	public AnnoFunction(String inParentName, String inFunctionName, int[] inDefinitionBounds, int inNumParameters, int inIsVarArgFlag, AnnoVar[] inLocalVars, AnnoVar[] inConstants, AnnoVar[] inUpvalues, AnnoFunction[] inFunctions, AnnoInstruction[] inInstructions, String[] inLabels, VarFlowHandler inVarFlow)
	{
		this.parentName = inParentName;
		this.functionName = inFunctionName;
		this.definitionBounds = inDefinitionBounds;
		this.numParameters = inNumParameters;
		this.isVarArgFlag = inIsVarArgFlag;
		this.LocalVars = inLocalVars;
		this.Constants = inConstants;
		this.Upvalues = inUpvalues;
		this.Functions = inFunctions;
		this.instructions = inInstructions;
		this.Labels = inLabels;
		this.varFlow = inVarFlow;
	}

	public static AnnoFunction convert(Function in)
	{
		final String inParentName = in.getParentName();
		final String inFunctionName = in.getFunctionName();
		final int[] inDefinitionBounds = new int[2];
		inDefinitionBounds[0] = in.getLineDefined();
		inDefinitionBounds[1] = in.getLastLineDefined();
		final int inNumParameters = in.getNumParameters();
		final int inIsVarArgFlag = in.getIsVarArgFlag();
		final AnnoVar[] inLocalVars = convertLocals(in.getLocalsDebugData());
		final AnnoVar[] inConstants = convertConstants(in.getConstants());
		final AnnoVar[] inUpvalues = convertUpvalues(in.getUpValueNames());
		final AnnoFunction[] inFunctions = convertFunctions(in.getFunctionPrototypes());

		final VarFlowHandler inVarFlow = new VarFlowHandler();
		final Instruction[] vanillaInstructions = in.getInstructions();
		final AnnoInstructionFactory factory = new AnnoInstructionFactory(Globals, inLocalVars, inConstants, inUpvalues, inFunctions, vanillaInstructions.length);
		final SingleLinkedList<AnnoInstruction> out = new SingleLinkedList<AnnoInstruction>();
		final InstructionIterator itsy = new InstructionIterator(vanillaInstructions);

		while (itsy.hasNext())
		{
			final AnnoInstruction cur = factory.parseInstruction(itsy);
			cur.addAllReadsAndWrites(inVarFlow);
			out.add(cur);
			/*
			final AnnoInstruction[] expand = OpCodeExpander.expand(cur);
			for(AnnoInstruction instruct: expand)
			{
				instruct.addAllReadsAndWrites(inVarFlow);
				out.add(instruct);
			}
			*/
		}

		final AnnoInstruction[] inInstructions = out.toArray(new AnnoInstruction[out.getSize()]);

		final AnnoFunction result = new AnnoFunction(inParentName, inFunctionName, inDefinitionBounds, inNumParameters, inIsVarArgFlag, inLocalVars, inConstants, inUpvalues, inFunctions, inInstructions, factory.getLabelsSparseArr(), inVarFlow);

		// return RegexAutobot.linkLocalVars(result);

		if (shouldAttemptToReplaceSteps)
		{
			return RegexAutobot.linkLocalVars3(result);
		}
		else
		{
			return result;
		}
	}

	private static AnnoVar[] convertLocals(Local[] in)
	{
		final AnnoVar[] result = new AnnoVar[in.length];
		// System.out.println("numLocals: " + in.length);

		for (int i = 0; i < result.length; i++)
		{
			final Local cur = in[i];
			result[i] = new AnnoVar(cur.getName(), ScopeLevels.Local_Variable);
		}

		return result;
	}

	private static AnnoVar[] convertConstants(Constant[] in)
	{
		final AnnoVar[] result = new AnnoVar[in.length];

		for (int i = 0; i < result.length; i++)
		{
			final Constant cur = in[i];
			final String name = "Constant_" + i;

			switch (cur.getType())
			{
				case Null:
				{
					result[i] = NullAnnoVar;
					break;
				}
				case Bool:
				{
					result[i] = new AnnoVar_Int(name, ScopeLevels.Local_Constant, cur.getValAsInt());
					break;
				}
				case Num:
				{
					result[i] = new AnnoVar_Int(name, ScopeLevels.Local_Constant, cur.getValAsInt());
					break;
				}
				case String:
				{
					result[i] = new AnnoVar_String(name, ScopeLevels.Local_Constant, cur.getValAsString());
					break;
				}
				case Unknown:
				{
					result[i] = new AnnoVar(name, ScopeLevels.Local_Constant);
					break;
				}
				default:
				{
					throw new UnhandledEnumException(cur.getType());
				}
			}
		}

		return result;
	}

	private static AnnoVar[] convertUpvalues(String[] upValueNames)
	{
		final AnnoVar[] result = new AnnoVar[upValueNames.length];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = new AnnoVar(upValueNames[i], ScopeLevels.UpValue);
		}

		return result;
	}

	private static AnnoFunction[] convertFunctions(Function[] in)
	{
		final AnnoFunction[] result = new AnnoFunction[in.length];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = convert(in[i]);
		}

		return result;
	}

	public AnnoVar_Function getAsAnnoVar()
	{
		return new AnnoVar_Function(this.functionName, ScopeLevels.Local_Variable, this.LocalVars, this.numParameters);
	}

	public static HalfByteRadixMap<AnnoVar> getGlobals()
	{
		return Globals;
	}

	public static AnnoVar getNullannovar()
	{
		return NullAnnoVar;
	}

	public String getParentName()
	{
		return this.parentName;
	}

	public String getFunctionName()
	{
		return this.functionName;
	}

	public int[] getDefinitionBounds()
	{
		return this.definitionBounds;
	}

	public int getNumParameters()
	{
		return this.numParameters;
	}

	public int getIsVarArgFlag()
	{
		return this.isVarArgFlag;
	}

	public AnnoVar[] getLocalVars()
	{
		return this.LocalVars;
	}

	public AnnoVar[] getConstants()
	{
		return this.Constants;
	}

	public AnnoVar[] getUpvalues()
	{
		return this.Upvalues;
	}

	public AnnoFunction[] getFunctions()
	{
		return this.Functions;
	}

	public AnnoInstruction[] getInstructions()
	{
		return this.instructions;
	}

	public VarFlowHandler getVarFlow()
	{
		return this.varFlow;
	}

	@Override
	public String toString()
	{
		return this.toCharList(0, true).toString();
	}

	public CharList toCharList(int offset, boolean printGlobals)
	{
		final CharList result = new CharList();

		if (printGlobals)
		{
			result.add('\t', offset);
			result.add("Global Variables: [");

			for (Entry<String, AnnoVar> global : Globals.entrySet())
			{
				result.addNewLine();
				result.add('\t', offset + 1);
				result.add('"');
				result.add(global.getKey());
				result.add('"');
			}

			result.addNewLine();
			result.add('\t', offset);
			result.add(']');
			result.addNewLine();
		}

		result.add(this.toCharList(offset), true);

		return result;
	}

	public CharList toCharList(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("***Function:");
		result.add(this.functionName);
		result.add(" START***");

		result.addNewLine();
		result.add('\t', offset);
		result.add("parentName = ");
		result.add(this.parentName);

		result.addNewLine();
		result.add('\t', offset);
		result.add("definitionStartLine = ");
		result.add(Integer.toString(this.definitionBounds[0]));

		result.addNewLine();
		result.add('\t', offset);
		result.add("definitionEndLine = ");
		result.add(Integer.toString(this.definitionBounds[1]));

		result.addNewLine();
		result.add('\t', offset);
		result.add("numParams = ");
		result.add(Integer.toString(this.numParameters));

		result.addNewLine();
		result.add('\t', offset);
		result.add("isVarArgFlag = 0b");
		final String varArgBin = Integer.toBinaryString(this.isVarArgFlag);
		final int varArgLengthDif = 4 - varArgBin.length();

		if (varArgLengthDif > 0)
		{
			result.add('0', varArgLengthDif);
		}

		result.add(varArgBin);

		result.addNewLine();
		result.add('\t', offset);
		result.add("Local Variables = [");

		for (int i = 0; i < this.LocalVars.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.LocalVars[i].getAdjustedName());
		}

		result.add(']');

		result.addNewLine();
		result.add('\t', offset);
		result.add("Constants = [");

		for (int i = 0; i < this.Constants.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Constants[i].getAdjustedName());

			/*
			final String cur = this.Constants[i].getAdjustedName();
			
			if (!TediousTests.isDecNumber(cur))
			{
				result.add('"');
				result.add(cur);
				result.add('"');
			}
			else
			{
				result.add(cur);
			}
			*/

		}

		result.add(']');

		result.addNewLine();
		result.add('\t', offset);
		result.add("Upvalues = [");

		for (int i = 0; i < this.Upvalues.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Upvalues[i].getAdjustedName());
		}

		result.add(']');

		result.addNewLine();
		result.add('\t', offset);
		result.add("Functions = [");

		for (int i = 0; i < this.Functions.length; i++)
		{
			result.addNewLine();
			result.add(this.Functions[i].toCharList(offset + 1), true);
		}

		if (this.Functions.length > 0)
		{
			result.addNewLine();
			result.add('\t', offset);
		}

		result.add(']');

		result.addNewLine();
		result.add('\t', offset);
		result.add("Instructions = [");

		for (int i = 0; i < this.instructions.length; i++)
		{
			result.addNewLine();

			if (this.Labels[i] != null)
			{
				// result.add(this.Labels[i]);
				// result.add(' ');
				MiscAutobot.append(this.Labels[i], result, ' ', 10);
			}
			else
			{
				result.add(' ', 10);
			}

			result.add('\t', offset);

			result.add(this.instructions[i].toCharList(true), true);
		}

		if (this.instructions.length > 0)
		{
			result.addNewLine();
			result.add('\t', offset);
		}

		result.add(']');

		result.addNewLine();
		result.add('\t', offset);
		result.add("***Function:");
		result.add(this.functionName);
		result.add(" END***");

		return result;
	}
}
