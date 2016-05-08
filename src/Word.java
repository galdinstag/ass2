public class Word {
	private String seq;
	private int score;
	private int startIndex;
	private int endIndex;
	public Word(String seq, int startIndex, int endIndex) {
		this.seq = seq;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public int getScore(){ return score;}
	public String toString(){
		return(score + " " + startIndex + " " + endIndex);
	}

	public int getIndex() {
		return startIndex;
	}

	public String getSeq() {
		return seq;
	}
}
