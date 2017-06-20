package LuaBytecodeDecompiler.Mk02.ConceptualVM;

public class StackFrame
{
	
	private final ResolvedInstruction[] instructions;
	
	private static class ResolvedInstruction
	{
		
	}
	
	private static class TrackedVar
	{
		private final String varName;
		private TrackedVar value;
	}
}
