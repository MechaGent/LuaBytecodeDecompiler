package CleanStart.Mk04_Working;

import java.nio.file.Paths;

import CharList.CharList;
import CleanStart.Mk04_Working.Analysis.CodeBlock;
import CleanStart.Mk04_Working.FunctionObjects.RawFunctionObject;
import CleanStart.Mk04_Working.FunctionObjects.RefinedFunctionObject;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import SingleLinkedList.Mk01.SingleLinkedList;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class SecretVolcanoLair
{
	public static void main(String[] args)
	{
		final OutputOptions option;
		//option = OutputOptions.Disassembly_Lua;
		option = OutputOptions.Disassembly_Ir;
		//option = OutputOptions.printBlockedDisassembly_Ir;
		//option = OutputOptions.PseudoJava;
		final String fileName = getFileName();
		final BytesStreamer stream = getTestStream(fileName);
		final CharList parsed = parseOption(stream, option, fileName);

		System.out.println(parsed.toString());
	}

	private static CharList parseOption(BytesStreamer rawstream, OutputOptions option, String fileName)
	{
		final CharList result;

		switch (option)
		{
			case Disassembly_Ir:
			{
				result = parseOption_Disassembly_Ir(rawstream, fileName);
				break;
			}
			case printBlockedDisassembly_Ir:
			{
				result = parseAndPrintOption_BlockedDisassembly_Ir(rawstream, fileName);
				break;
			}
			case Disassembly_Lua:
			{
				result = parseOption_Disassembly_Lua(rawstream, fileName);
				break;
			}
			case PseudoJava:
			{
				result = parseAndPrintOption_PseudoJava(rawstream, fileName);
				break;
			}
			default:
			{
				throw new UnhandledEnumException(option);
			}
		}

		return result;
	}
	
	private static CharList parseAndPrintOption_PseudoJava(BytesStreamer raw, String functionName)
	{
		final RawFunctionObject root = RawFunctionObject.getNext(raw, functionName);
		final RefinedFunctionObject refined = RefinedFunctionObject.getInstance(root);
		final SingleLinkedList<RefinedFunctionObject> queue = refined.getAll_asQueue();
		final CharList result = new CharList();
		
		for(RefinedFunctionObject curr: queue)
		{
			result.addNewLine();
			result.add(curr.translateInstructionsToCode(), true);
		}
		
		return result;
	}
	
	private static CharList parseAndPrintOption_BlockedDisassembly_Ir(BytesStreamer raw, String functionName)
	{
		final RawFunctionObject root = RawFunctionObject.getNext(raw, functionName);
		final RefinedFunctionObject refined = RefinedFunctionObject.getInstance(root);
		final SingleLinkedList<RefinedFunctionObject> queue = refined.getAll_asQueue();
		final CharList result = new CharList();
		final boolean includeInstructions = false;
		final boolean includeName = false;
		
		for(RefinedFunctionObject curr: queue)
		{
			final SingleLinkedList<CodeBlock> blocks = CodeBlock.parse(curr.getInstructions());
			result.addNewLine();
			result.addNewLine();
			result.add("*****Function ");
			result.add(curr.getName());
			result.add(" Blocks start:");
			//boolean isFirst = true;
			
			for(CodeBlock block: blocks)
			{
				/*
				if(isFirst)
				{
					isFirst = false;
				}
				else
				{
					result.addNewLine();
				}
				*/
				
				result.add(block.toCharList(includeInstructions, includeName), true);
			}
			
			result.add("*****End");
		}
		
		return result;
	}

	private static CharList parseOption_Disassembly_Ir(BytesStreamer raw, String functionName)
	{
		final RawFunctionObject root = RawFunctionObject.getNext(raw, functionName);
		final RefinedFunctionObject refined = RefinedFunctionObject.getInstance(root);
		return refined.disassemble(0, true, true);
	}

	private static CharList parseOption_Disassembly_Lua(BytesStreamer raw, String functionName)
	{
		final RawFunctionObject root = RawFunctionObject.getNext(raw, functionName);

		return root.disassemble(0, true);
	}

	private static BytesStreamer getTestStream(String fileName)
	{
		final String generalBinPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/";
		final String fullFilePath = generalBinPath + fileName + ".lua";
		
		return BytesStreamer.getInstance(Paths.get(fullFilePath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
	}
	
	private static String getFileName()
	{
		final String fileName;
		
		//fileName = "KubrowCharge";
		// fileName = "luac_ManyLocals";
		//fileName = "AbilitySelector";
		//fileName = "AddAscarisNegator";
		// fileName = "ApexFuntions";
		//fileName = "Background";
		//fileName = "LoadOutRedux";	//current beefiest file in there
		//fileName = "BardJam";
		//fileName = "BardAmplify";
		//fileName = "BardCharm";
		//fileName = "BardMusic";
		//fileName = "BardTransfer";
		fileName = "BardTune";
		
		return fileName;
	}
}
