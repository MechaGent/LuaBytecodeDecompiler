package CleanStart.Mk06.CodeBlock5;

import java.nio.file.Paths;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.FunctionObjects.FunctionObject_RawInstructionConverter;
import CleanStart.Mk06.FunctionObjects.RawFunctionObject;
import CleanStart.Mk06.FunctionObjects.RefinementStagesChecklist;
import SingleLinkedList.Mk01.SingleLinkedList;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class CodeBlock5Tester
{
	private static final String generalPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/";
	private static final String fileName = getFileName();
	private static final String fullFilePath = generalPath + fileName + ".lua";

	private CodeBlock5Tester()
	{
		// to prevent instantiation
	}

	public static void main(String[] args)
	{
		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(fullFilePath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final CharList output;

		//output = test_firstPassProcessing(stream, fileName);
		 output = test_fullProcessing(stream, fileName);

		System.out.println(output.toString());
	}

	private static String getFileName()
	{
		final String fileName;

		//fileName = "KubrowCharge";
		// fileName = "luac_ManyLocals";
		//fileName = "AbilitySelector";
		// fileName = "AddAscarisNegator";
		// fileName = "ApexFuntions";
		// fileName = "Background";
		//fileName = "LoadOutRedux"; //current beefiest file in there
		// fileName = "BardJam";
		//fileName = "BardAmplify";
		// fileName = "BardCharm";
		// fileName = "BardMusic";
		// fileName = "BardTransfer";
		// fileName = "BardTune";
		//fileName = "Avalanche";
		fileName = "PriestCondemn";

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
			result.add(CodeBlock5Parser.process_toCharList(firstInstruct), true);
			result.addNewLine();
		}
		
		return result;
	}
	
	public static CharList test_fullProcessing(BytesStreamer stream, String fileName)
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
					result.add(CodeBlock5Parser.process_toCodeCharList(firstInstruct), true);
					result.addNewLine();
				}
				
				return result;
	}
}
