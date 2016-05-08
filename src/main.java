import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class main {
	public static void main(String[] args) {
		if (args.length < 3){
			System.out.println("not enough args");
			System.exit(1);
		}
		BufferedWriter writer = null;
		long timeStamp = System.currentTimeMillis();
		for(int k = 4; k < 6; k++) {
			try {
				writer = new BufferedWriter(new FileWriter("D:\\study\\bio\\ass2\\output\\k=" + k + ".txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			SequenceAlignment sequence = new SequenceAlignment(args[0], args[1], args[2], k, 10, 5);
			ArrayList<Msp> bestAns = new ArrayList<>();
			ArrayList<Msp> currBestAns;
			for (Map.Entry<String, String> entry : sequence.sequenceQueries.entrySet()) {//for each query
				String queryLabel = entry.getKey();
				String querySequence = entry.getValue();
				for (String textLabel : sequence.sequenceText.keySet()) {//run on all texts and find the best natch
					currBestAns = sequence.findHits(queryLabel, querySequence, textLabel);
					bestAns.add(currBestAns.get(0));
					bestAns.add(currBestAns.get(1));
				}
				bestAns.sort(new MSPComparator());
				sequence.printHighestHSP(10, bestAns, writer);
			}
		}
		long timeStampEnd = System.currentTimeMillis();
		System.out.println("\ntotal work time was: " +  (timeStampEnd - timeStamp) + " milliseconds");
	}
}
