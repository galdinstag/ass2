package ass2;

public class main {
	public static void main(String[] args) {
		if (args.length < 4){
			System.out.println("not enough args");
			System.exit(1);
		}        

		SequenceAlignment sequence = new SequenceAlignment(args[0], args[1], args[2], Integer.parseInt(args[3]));
		
    }
}
