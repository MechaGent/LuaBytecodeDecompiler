package CleanStart.Mk06.CodeBlock4;

import java.nio.file.Paths;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.FunctionObjects.FunctionObject_RawInstructionConverter;
import CleanStart.Mk06.FunctionObjects.RawFunctionObject;
import CleanStart.Mk06.FunctionObjects.RefinementStagesChecklist;
import SingleLinkedList.Mk01.SingleLinkedList;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class CodeBlock4Tester
{
	private static final String generalPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/";
	private static final String fileName = getFileName();
	private static final String fullFilePath = generalPath + fileName + ".lua";

	public static void main(String[] args)
	{
		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(fullFilePath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final CharList output;

		output = test_firstPassProcessing(stream, fileName);
		//output = test_totalProcessing(stream, fileName);

		System.out.println(output.toString());
	}

	private static String getFileName()
	{
		final String fileName;

		fileName = "KubrowCharge";
		// fileName = "luac_ManyLocals";
		// fileName = "AbilitySelector";
		// fileName = "AddAscarisNegator";
		// fileName = "ApexFuntions";
		// fileName = "Background";
		// fileName = "LoadOutRedux"; //current beefiest file in there
		// fileName = "BardJam";
		// fileName = "BardAmplify";
		// fileName = "BardCharm";
		// fileName = "BardMusic";
		// fileName = "BardTransfer";
		// fileName = "BardTune";

		return fileName;
	}

	public static CharList test_firstPassProcessing(BytesStreamer stream, String fileName)
	{
		// init
		final boolean shouldReinsertLabels = true;
		final boolean shouldDeduceLoops = false;
		final boolean shouldProcessSimpleIfElseStatements = false;
		final RefinementStagesChecklist checklist = RefinementStagesChecklist.getInstance(shouldReinsertLabels, shouldDeduceLoops, shouldProcessSimpleIfElseStatements);

		// main algo
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
			final CodeBlock4[] blocks = CodeBlock4Parser.process(firstInstruct);

			for (int i = 0; i < blocks.length; i++)
			{
				if (blocks[i] != null)
				{
					result.addNewLine();
					result.add(blocks[i].toCharList(0, true), true);
				}
			}
			
			//result.add(blocks[0].toCharList(0, true), true);
			
			result.addNewLine();
		}

		return result;
	}
	
	public static CharList test_totalProcessing(BytesStreamer stream, String fileName)
	{
		// init
				final boolean shouldReinsertLabels = true;
				final boolean shouldDeduceLoops = false;
				final boolean shouldProcessSimpleIfElseStatements = false;
				final RefinementStagesChecklist checklist = RefinementStagesChecklist.getInstance(shouldReinsertLabels, shouldDeduceLoops, shouldProcessSimpleIfElseStatements);

				// main algo
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
					final CodeBlock4[] blocks = CodeBlock4Parser.process(firstInstruct);

					result.add(blocks[0].toCodeCharList(0), true);
					
					//result.add(blocks[0].toCharList(0, true), true);
					
					result.addNewLine();
				}

				return result;
	}
}
