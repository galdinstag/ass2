public class main {
	public static void main(String[] args) {
		if (args.length < 6){
			System.out.println("not enough args");
			System.exit(1);
		}        

		SequenceAlignment sequence = new SequenceAlignment(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
		sequence.findHits(">q10",sequence.sequenceQueries.get(">q10"),">t10");
    }
}
