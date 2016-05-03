import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SequenceAlignment {
    int HSPtrash;
	HashMap<String, String> sequenceQueries;
    HashMap<String, String> sequenceText;
    int similarityThreshold;
    int k;
    int a;
    int b;
    TransitionMatrix matrix;
    LinkedHashMap<String,LinkedHashMap<String, ArrayList<Integer>>> wordsTextMap;
    LinkedHashMap<String,LinkedHashMap<String,ArrayList<Word>>> similarWordsMap;
    LinkedHashMap<ArrayList<Word>,ArrayList<Integer>> hits;

    public SequenceAlignment(String queriesFile, String matrixFile, String textFile, int num, int similarityThreshold, int HSPtrash) {
        k = num;
        this.similarityThreshold = similarityThreshold;
        this.HSPtrash = HSPtrash;
        sequenceQueries = new HashMap<>();
        FastaSequence reader = new FastaSequence(queriesFile);
        for (int i = 0; i < reader.size(); i++) {
            sequenceQueries.put(reader.getDescription(i), reader.getSequence(i));
        }
        sequenceText = new HashMap<>();
        reader = new FastaSequence(textFile);
        for (int i = 0; i < reader.size(); i++) {
            sequenceText.put(reader.getDescription(i), reader.getSequence(i));
        }


        matrix = new TransitionMatrix(fixMatrix(matrixFile));
        File toDelete = new File("mid.txt");
        toDelete.deleteOnExit();

        //make hash of words and indexes in text

        wordsTextMap = new LinkedHashMap<>();

        String currentWord;
        for (Map.Entry<String, String> entry : sequenceText.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            wordsTextMap.put(key,new LinkedHashMap<>());
            for (int i = 0; i <= value.length() - k; i++) {
                currentWord = value.substring(i, i + k);
                if (wordsTextMap.get(key).containsKey(currentWord))    //contains word add only new location
                    wordsTextMap.get(key).get(currentWord).add(new Integer(i));
                else {
                    ArrayList<Integer> arraylist = new ArrayList<Integer>();    //create a new arraylist for the word
                    arraylist.add(new Integer(i));
                    wordsTextMap.get(key).put(currentWord, arraylist);
                }
            }
        }
        queryPreProcess();
    }


    private void queryPreProcess() {
        similarWordsMap = new LinkedHashMap<>();
        //for each query
        String currentWindow;
        for (Map.Entry<String, String> entry : sequenceQueries.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();
            similarWordsMap.put(key,new LinkedHashMap<>());
            //for each sliding window, find all similar words
            for (int i = 0; i <= value.length() - k; i++) {
                currentWindow = value.substring(i, i + k);
                findSimilarWords(key,currentWindow,i,i+k-1);
            }
        }
    }


    public void findHits(String queryLabel, String query, String textLabel) {
        LinkedHashMap<String,ArrayList<Integer>> textWords = wordsTextMap.get(textLabel);
        hits = new LinkedHashMap<>();
        for (Map.Entry<String,ArrayList<Word>> entry : similarWordsMap.get(queryLabel).entrySet()  ){
            String key = entry.getKey();
            if (textWords.containsKey(key) ){
                if (!hits.containsKey(entry.getValue()) )
                    hits.put(entry.getValue(), textWords.get(key));
            }
        }
        extendHits(query,sequenceText.get(textLabel),hits);
    }

    public void extendHits(String query, String text, LinkedHashMap<ArrayList<Word>,ArrayList<Integer>> hits){
    	ArrayList<Msp> ans = new ArrayList<>();
    	for(Map.Entry<ArrayList<Word>,ArrayList<Integer>> currHit : hits.entrySet()){
            ArrayList<Word> wordsList = currHit.getKey();
            ArrayList<Integer> indexesList = currHit.getValue();
                for(Word currWord : wordsList){
                    for(int currIndex : indexesList){
                        int score = currWord.getScore();
                        int startTxt = currIndex;
                        int endTxt = currIndex + k - 1;
                        int startQ = currWord.getIndex();	//in query
                        int endQ = startQ + k - 1 ;	//in query
                        boolean endOfHsp = false;
                        while (score > HSPtrash && !endOfHsp){// while it is possible to elongate
                            endOfHsp = true;
                            if (startQ>0 && startTxt>0 && score + matrix.score(query.charAt(startQ - 1), text.charAt(startTxt - 1) ) > HSPtrash){	// try extend left
                                endOfHsp = false;
                                startQ--;
                                startTxt--;
                                score += matrix.score(query.charAt(startQ), text.charAt(startTxt));
                            }
                            if (endQ<query.length()-1 && endTxt<text.length()-1 && score + matrix.score(query.charAt(endQ + 1), text.charAt(endTxt + 1) ) > HSPtrash){	// try extend right
                                endOfHsp = false;
                                endQ++;
                                endTxt++;
                                score += matrix.score(query.charAt(endQ), text.charAt(endTxt));
                            }
                        }
                        ans.add(new Msp(startTxt, endTxt, startQ, endQ));
                    }
                }
            }
        ans.sort(new MSPComparator());	//sort by start index in text and length of hit
        printHigestHSP(100,ans,text,query);
        //printAns(ans);
    	}

    private void printHigestHSP(int i, ArrayList<Msp> ans, String text, String query) {
        for(int j = 0; j < i; j++){
            System.out.println("HSP #"+(j+1));
            System.out.println(ans.get(j));
            System.out.println("text:  "+text.substring(ans.get(j).getStartTxt(),ans.get(j).getEndTxt()+1));
            System.out.println("query: "+query.substring(ans.get(j).getStartQuery(),ans.get(j).getEndQuery()+1));
        }
    }

    private void printAns(ArrayList<Msp> ans) {
		int rightCover = 0;
		int leftCover = -1;
		int coverege = 0;
		for(Msp currHit : ans){
			if(currHit.getStartTxt() > leftCover){
				coverege += currHit.getEndTxt() - currHit.getStartTxt();
				leftCover = currHit.getStartTxt();
				rightCover = currHit.getEndTxt();
			}
			else if(currHit.getEndTxt() > rightCover){
				coverege += currHit.getEndTxt() - rightCover;
				leftCover = currHit.getStartTxt();
				rightCover = currHit.getEndTxt();
			}
		}
		
	}


	public File fixMatrix(String orgMatrix) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(orgMatrix));
            BufferedWriter writer = new BufferedWriter(new FileWriter("mid.txt"));
            String line = bf.readLine();
            while ((line != null) && line.contains("#")) {    //don't read lines with #
                line = bf.readLine();
            }
            while ((line != null) && (!line.contains("#"))) { //read matrix
                writer.write(line);
                writer.write("\n");
                writer.flush();
                line = bf.readLine();
            }
            while ((line != null) && line.contains("#")) { //don't read lines with #
                line = bf.readLine();
            }
            //read A for: W(x) = Ax + B
            String num = line.substring(2);
            a = Integer.parseInt(num.toString());

            //read B for: W(x) = Ax + B
            line = bf.readLine();
            num = line.substring(2);
            b = Integer.parseInt(num.toString());

            writer.close();
            return new File("mid.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void findSimilarWords(String queryLabel, String currentWindow,int startIndex, int endIndex) {
        //ArrayList<Word> similarWords = new ArrayList<>();
        //find similar words to "currentWindow"
        if(!similarWordsMap.get(queryLabel).containsKey(currentWindow)){
            similarWordsMap.get(queryLabel).put(currentWindow,new ArrayList<>());
        }
        similarWordsMap.get(queryLabel).get(currentWindow).add(new Word(currentWindow.length()*5,startIndex,endIndex));
        checkPerm(queryLabel,currentWindow,currentWindow,0,currentWindow.length()*5,startIndex,endIndex);
    }
    private void checkPerm(String queryLabel, String currentWindow,String originalWindow, int i, int currentScore,int startIndex, int endIndex){
        StringBuilder currPerm = new StringBuilder(currentWindow);
        int newScore;
        if(i == currentWindow.length()){
            return;
        }
        else{
            //check all permutations of i and send them to checkPerm
            //the idea: change currWindow[i] from X to Y, if it's already Y do nothing
            if(currPerm.charAt(i) != 'a'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'a') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'a');
                    currPerm.setCharAt(i,'a');
                    if(!similarWordsMap.get(queryLabel).containsKey(currPerm.toString())){
                        similarWordsMap.get(queryLabel).put(currPerm.toString(),new ArrayList<>());
                    }
                    similarWordsMap.get(queryLabel).get(currPerm.toString()).add(new Word(newScore,startIndex,endIndex));
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore,startIndex,endIndex);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
                }
            }
            else{
                checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
            }
            if(currPerm.charAt(i) != 't'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'t') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'t');
                    currPerm.setCharAt(i,'t');
                    if(!similarWordsMap.get(queryLabel).containsKey(currPerm.toString())){
                        similarWordsMap.get(queryLabel).put(currPerm.toString(),new ArrayList<>());
                    }
                    similarWordsMap.get(queryLabel).get(currPerm.toString()).add(new Word(newScore,startIndex,endIndex));
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore,startIndex,endIndex);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
                }
            }
            else{
                checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
            }
            if(currPerm.charAt(i) != 'g'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'g') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'g');
                    currPerm.setCharAt(i,'g');
                    if(!similarWordsMap.get(queryLabel).containsKey(currPerm.toString())){
                        similarWordsMap.get(queryLabel).put(currPerm.toString(),new ArrayList<>());
                    }
                    similarWordsMap.get(queryLabel).get(currPerm.toString()).add(new Word(newScore,startIndex,endIndex));
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore,startIndex,endIndex);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
                }
            }
            else{
                checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
            }
            if(currPerm.charAt(i) != 'c'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'c') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'c');
                    currPerm.setCharAt(i,'c');
                    if(!similarWordsMap.get(queryLabel).containsKey(currPerm.toString())){
                        similarWordsMap.get(queryLabel).put(currPerm.toString(),new ArrayList<>());
                    }
                    similarWordsMap.get(queryLabel).get(currPerm.toString()).add(new Word(newScore,startIndex,endIndex));
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore,startIndex,endIndex);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
                }
            }
            else{
                checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore,startIndex,endIndex);
            }
            return;
        }
    }
}