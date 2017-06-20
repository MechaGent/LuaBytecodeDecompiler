package CleanStart.Mk06;

import java.nio.file.Paths;

import CharList.CharList;
import CleanStart.Mk06.CodeBlock2.CodeBlock2;
import CleanStart.Mk06.CodeBlock3.CodeBlock3;
import CleanStart.Mk06.CodeBlock3.CodeBlock3Parser;
import CleanStart.Mk06.FunctionObjects.CodeBlock.CodeBlockChainFactory;
import CleanStart.Mk06.FunctionObjects.FunctionObject_RawInstructionConverter;
import CleanStart.Mk06.FunctionObjects.RawFunctionObject;
import CleanStart.Mk06.FunctionObjects.RefinementStagesChecklist;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import SingleLinkedList.Mk01.SingleLinkedList;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class SecretVolcanoLair
{
	private static final String generalPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/";
	private static final String fileName = getFileName();
	private static final String fullFilePath = generalPath + fileName + ".lua";
	private static final Options option = getOption();

	public static void main(String[] args)
	{
		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(fullFilePath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final CharList output = parseOption(stream, option, fileName);
		System.out.println(output.toString());
	}

	private static enum Options
	{
		Disassemble_Lua,
		Disassemble_Ir_NoLabels,
		Disassemble_Ir_WithLabels,
		Disassemble_Ir_WithLabelsAndSimpleIfs,
		Testing_CodeBlockCascading,
		Disassemble_Ir_WithLabelsAndSimpleIfs_viaCodeblocks,
		Disassemble_Ir_WithLabelsAndSimpleIfs_viaCodeblock2s,
		Testing_CodeBlock3_flowGraph;
	}

	private static String getFileName()
	{
		final String fileName;

		//fileName = "KubrowCharge";
		// fileName = "luac_ManyLocals";
		// fileName = "AbilitySelector";
		// fileName = "AddAscarisNegator";
		// fileName = "ApexFuntions";
		// fileName = "Background";
		//fileName = "LoadOutRedux"; //current beefiest file in there
		// fileName = "BardJam";
		// fileName = "BardAmplify";
		// fileName = "BardCharm";
		// fileName = "BardMusic";
		// fileName = "BardTransfer";
		// fileName = "BardTune";
		//fileName = "Avalanche";
		fileName = "PriestCondemn";

		return fileName;
	}

	private static Options getOption()
	{
		final Options option;

		 option = Options.Disassemble_Lua;
		//option = Options.Disassemble_Ir_NoLabels;
		//option = Options.Disassemble_Ir_WithLabels;
		//option = Options.Disassemble_Ir_WithLabelsAndSimpleIfs;
		//option = Options.Testing_CodeBlockCascading;
		//option = Options.Disassemble_Ir_WithLabelsAndSimpleIfs_viaCodeblocks;
		//option = Options.Disassemble_Ir_WithLabelsAndSimpleIfs_viaCodeblock2s;
		//option = Options.Testing_CodeBlock3_flowGraph;
		
		return option;
	}

	private static CharList parseOption(BytesStreamer stream, Options option, String fileName)
	{
		final CharList result;

		switch (option)
		{
			case Disassemble_Lua:
			{
				final RawFunctionObject root = RawFunctionObject.getNext(stream, fileName);

				result = root.disassemble(0, true, true, false);
				break;
			}
			case Disassemble_Ir_NoLabels:
			case Disassemble_Ir_WithLabels:
			case Disassemble_Ir_WithLabelsAndSimpleIfs:
			{
				result = parseOption_Disassemble_Ir_internal(stream, fileName, compileIrChecklist(option));
				break;
			}
			case Testing_CodeBlockCascading:
			{
				final RefinementStagesChecklist checklist = compileIrChecklist(Options.Disassemble_Ir_WithLabels);
				result = parseOption_testing_CodeBlockCascading(stream, fileName, checklist);
				break;
			}
			case Disassemble_Ir_WithLabelsAndSimpleIfs_viaCodeblocks:
			{
				final RefinementStagesChecklist checklist = compileIrChecklist(Options.Disassemble_Ir_WithLabelsAndSimpleIfs);
				result = parseOption_Disassemble_Ir_internal(stream, fileName, checklist);
				
				break;
			}
			case Disassemble_Ir_WithLabelsAndSimpleIfs_viaCodeblock2s:
			{
				final RefinementStagesChecklist checklist = compileIrChecklist(Options.Disassemble_Ir_WithLabels);
				result = parseOption_testing_CodeBlock2Cascading(stream, fileName, checklist);
				break;
			}
			case Testing_CodeBlock3_flowGraph:
			{
				final RefinementStagesChecklist checklist = compileIrChecklist(Options.Disassemble_Ir_WithLabels);
				result = parseOption_testing_CodeBlock3_flowGraph(stream, fileName, checklist);
				break;
			}
			default:
			{
				throw new UnhandledEnumException(option);
			}
		}

		return result;
	}
	
	private static RefinementStagesChecklist compileIrChecklist(Options option)
	{
		final boolean shouldReinsertLabels;
		final boolean shouldDeduceLoops;
		final boolean shouldProcessSimpleIfElseStatements;
		
		switch(option)
		{
			case Disassemble_Ir_NoLabels:
			{
				shouldReinsertLabels = false;
				shouldDeduceLoops = false;
				shouldProcessSimpleIfElseStatements = false;
				break;
			}
			case Disassemble_Ir_WithLabels:
			{
				shouldReinsertLabels = true;
				shouldDeduceLoops = false;
				shouldProcessSimpleIfElseStatements = false;
				break;
			}
			case Disassemble_Ir_WithLabelsAndSimpleIfs:
			{
				shouldReinsertLabels = true;
				shouldDeduceLoops = false;
				shouldProcessSimpleIfElseStatements = true;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(option);
			}
		}
		
		return RefinementStagesChecklist.getInstance(shouldReinsertLabels, shouldDeduceLoops, shouldProcessSimpleIfElseStatements);
	}

	private static CharList parseOption_Disassemble_Ir_internal(BytesStreamer stream, String fileName, RefinementStagesChecklist checklist)
	{
		final CharList result = new CharList();
		final RawFunctionObject root = RawFunctionObject.getNext(stream, fileName);
		final SingleLinkedList<RawFunctionObject> queue = new SingleLinkedList<RawFunctionObject>();
		queue.add(root);
		boolean includeGlobals = true;
		final boolean includeMetadata = false;

		while (queue.isNotEmpty())
		{
			final RawFunctionObject curr = queue.pop();
			final RawFunctionObject[] children = (RawFunctionObject[]) curr.getFunctionPrototypes();

			queue.add(children);
			result.addNewLine();

			result.add(curr.generateHeader(0, includeGlobals), true);

			if (includeGlobals)
			{
				includeGlobals = false;
			}

			Instruction currInstruct = FunctionObject_RawInstructionConverter.parseRawInstructions(checklist, curr);

			result.addNewLine();
			result.add("Var history: {");
			result.addNewLine();

			for (VarRef var : curr.getLocals())
			{
				result.add('\t');
				result.add(var.generateTypeHistory(), true);
				result.addNewLine();
			}

			result.add('}');
			result.addNewLine();

			while (currInstruct != null)
			{
				result.addNewLine();

				result.add(currInstruct.toVerboseCommentForm(includeMetadata), true);
				//result.add(currInstruct.toCodeForm(false, 0), true);
				
				currInstruct = currInstruct.getLiteralNext();
			}

			result.addNewLine();
		}

		return result;
	}
	
	public static CharList parseOption_testing_CodeBlockCascading(BytesStreamer stream, String fileName, RefinementStagesChecklist checklist)
	{
		final CharList result = new CharList();
		final RawFunctionObject root = RawFunctionObject.getNext(stream, fileName);
		final SingleLinkedList<RawFunctionObject> queue = new SingleLinkedList<RawFunctionObject>();
		queue.add(root);

		while (queue.isNotEmpty())
		{
			final RawFunctionObject curr = queue.pop();
			final RawFunctionObject[] children = (RawFunctionObject[]) curr.getFunctionPrototypes();

			queue.add(children);
			result.addNewLine();
			result.add("*****");
			result.add("Function: ");
			result.add(curr.getName());
			result.addNewLine();

			Instruction currInstruct = FunctionObject_RawInstructionConverter.parseRawInstructions(checklist, curr);
			final CodeBlockChainFactory factory = CodeBlockChainFactory.getInstance(currInstruct);
			factory.buildChain();
			
			result.add(factory.toCharList(), true);

			result.addNewLine();
		}

		return result;
	}
	
	public static CharList parseOption_testing_CodeBlock2Cascading(BytesStreamer stream, String fileName, RefinementStagesChecklist checklist)
	{
		final CharList result = new CharList();
		final RawFunctionObject root = RawFunctionObject.getNext(stream, fileName);
		final SingleLinkedList<RawFunctionObject> queue = new SingleLinkedList<RawFunctionObject>();
		queue.add(root);

		while (queue.isNotEmpty())
		{
			final RawFunctionObject curr = queue.pop();
			final RawFunctionObject[] children = (RawFunctionObject[]) curr.getFunctionPrototypes();

			queue.add(children);
			result.addNewLine();
			result.add("*****");
			result.add("Function: ");
			result.add(curr.getName());
			result.addNewLine();

			final Instruction firstInstruct = FunctionObject_RawInstructionConverter.parseRawInstructions(checklist, curr);
			final CodeBlock2[] blocks = CodeBlock2.getLinkedAndScopedInstances_twoPass(firstInstruct);
			
			result.add(blocks[0].toCharList(0), true);
			result.addNewLine();
		}
		
		return result;
	}
	
	public static CharList parseOption_testing_CodeBlock3_flowGraph(BytesStreamer stream, String fileName, RefinementStagesChecklist checklist)
	{
		final CharList result = new CharList();
		final RawFunctionObject root = RawFunctionObject.getNext(stream, fileName);
		final SingleLinkedList<RawFunctionObject> queue = new SingleLinkedList<RawFunctionObject>();
		queue.add(root);

		while (queue.isNotEmpty())
		{
			final RawFunctionObject curr = queue.pop();
			final RawFunctionObject[] children = (RawFunctionObject[]) curr.getFunctionPrototypes();

			queue.add(children);
			result.addNewLine();
			result.add("*****");
			result.add("Function: ");
			result.add(curr.getName());
			result.addNewLine();

			final Instruction firstInstruct = FunctionObject_RawInstructionConverter.parseRawInstructions(checklist, curr);
			final CodeBlock3[] blocks = CodeBlock3Parser.process(firstInstruct);
			
			for(int i = 0; i < blocks.length; i++)
			{
				result.addNewLine();
				result.add(blocks[i].toCharList(0, true), true);
			}
			result.addNewLine();
		}
		
		return result;
	}
}
