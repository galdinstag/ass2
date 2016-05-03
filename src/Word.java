public class Word {
	private String seq;
	private int score;
	private int startIndex;
	private int endIndex;
	public Word(int score, int startIndex, int endIndex) {
		this.score = score;
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
}
