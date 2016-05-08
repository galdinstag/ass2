import java.io.*;
import java.util.*;
import java.util.logging.Logger;

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
    LinkedHashMap<String,LinkedHashMap<Word,ArrayList<String>>> similarWordsMap;
    LinkedHashMap<Word,ArrayList<Integer>> hits;
    Logger logger;

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
                    wordsTextMap.get(key).get(currentWord).add(i);
                else {
                    ArrayList<Integer> arraylist = new ArrayList<>();    //create a new arraylist for the word
                    arraylist.add(i);
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
        ArrayList<String> currList;
        Word currWindowWord;
        for (Map.Entry<String, String> entry : sequenceQueries.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            similarWordsMap.put(key, new LinkedHashMap<>());
            //for each sliding window, find all similar words in texts
            for (int i = 0; i <= value.length() - k; i++) {
                currentWindow = value.substring(i, i + k);
                currList = containsWindow(key, currentWindow);
                if (currList != null) {// we have already chacked for this window, no point on checking again
                    similarWordsMap.get(key).put(new Word(currentWindow,i,i+k-1),currList);
                } else {// check window against all words in all texts
                    currWindowWord = new Word(currentWindow, i, i + k - 1);
                    similarWordsMap.get(key).put(currWindowWord, new ArrayList<>());
                    for (LinkedHashMap<String, ArrayList<Integer>> currTextMap : wordsTextMap.values()) {
                        for (String currTextWindow : currTextMap.keySet())
                            if (getScore(currentWindow, currTextWindow) > similarityThreshold) {
                                similarWordsMap.get(key).get(currWindowWord).add(currTextWindow);
                            }
                    }
                }
            }
        }
    }

    private ArrayList<String> containsWindow(String key, String currentWindow) {
        for(Word currWord : similarWordsMap.get(key).keySet()){
            if(currWord.getSeq().equals(currentWindow)){
                return similarWordsMap.get(key).get(currWord);
            }
        }
        return null;
    }

    public ArrayList<Msp> findHits(String queryLabel, String query, String textLabel) {
        LinkedHashMap<String,ArrayList<Integer>> textWords = wordsTextMap.get(textLabel);
        hits = new LinkedHashMap<>();
        for (Map.Entry<Word,ArrayList<String>> entry : similarWordsMap.get(queryLabel).entrySet()  ){
            Word key = entry.getKey();
            for(String currWord : entry.getValue()){
                if (textWords.containsKey(currWord)){
                    hits.put(key,textWords.get(currWord));

                }
            }
            }
        return extendHits(query,sequenceText.get(textLabel),hits, queryLabel, textLabel);
    }



    public ArrayList<Msp> extendHits(String query, String text, LinkedHashMap<Word,ArrayList<Integer>> hits, String queryLabel, String textLabel){
        ArrayList<Msp> ans = new ArrayList<>();
    	for(Map.Entry<Word,ArrayList<Integer>> currHit : hits.entrySet()){
            Word word = currHit.getKey();
            ArrayList<Integer> indexesList = currHit.getValue();
                    for(int currIndex : indexesList){
                        int score = getScore(word.getSeq(), text.substring(currIndex, currIndex + k));
                        int startTxt = currIndex;
                        int endTxt = currIndex + k - 1;
                        int startQ = word.getIndex();	//in query
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
                        ans.add(new Msp(startTxt, endTxt, startQ, endQ,queryLabel,textLabel,text.length()));
                    }

            }
        ans.sort(new MSPComparator());	//sort by start index in text and length of hit
        return ans;
    	}

    private int getScore(String seq, String substring) {
        int score = 0;
        for (int i = 0; i < seq.length(); i++)
            score += matrix.score(seq.charAt(i), substring.charAt(i));
        return score;
    }

    public void printHighestHSP(int i, ArrayList<Msp> ans, BufferedWriter writer) {
        float score;
        for(int j = 0; j < i; j++){
            String query = sequenceQueries.get(ans.get(j).getQueryLabel());
            String text = sequenceText.get(ans.get(j).getTextLabel());
            try {
                writer.write("HSP #"+(j+1));
                writer.write("\n");
                writer.flush();
                score = ans.get(j).getScore();
                writer.write("score: " + score);
                writer.write("\n");
                writer.flush();
                writer.write(ans.get(j).toString());
                writer.write("\n");
                writer.flush();
                writer.write("text:  "+text.substring(ans.get(j).getStartTxt(),ans.get(j).getEndTxt()+1));
                writer.write("\n");
                writer.flush();
                writer.write("query: "+query.substring(ans.get(j).getStartQuery(),ans.get(j).getEndQuery()+1));
                writer.write("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
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
        Word originalWindow = new Word(currentWindow,startIndex,endIndex);
        similarWordsMap.get(queryLabel).put(originalWindow,new ArrayList<>());
        similarWordsMap.get(queryLabel).get(originalWindow).add(currentWindow);
        checkPerm(queryLabel,currentWindow,originalWindow,0,currentWindow.length()*5);
    }
    private void checkPerm(String queryLabel, String currentWindow,Word originalWindow, int i, int currentScore){
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
                    if(!similarWordsMap.get(queryLabel).get(originalWindow).contains(currPerm.toString())){
                        similarWordsMap.get(queryLabel).get(originalWindow).add(currPerm.toString());
                    }
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    currPerm.setCharAt(i,'a');
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
            }
            if(currPerm.charAt(i) != 't'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'t') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'t');
                    currPerm.setCharAt(i,'t');
                    if(!similarWordsMap.get(queryLabel).get(originalWindow).contains(currPerm.toString())){
                        similarWordsMap.get(queryLabel).get(originalWindow).add(currPerm.toString());
                    }
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    currPerm.setCharAt(i,'t');
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
            }
            if(currPerm.charAt(i) != 'g'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'g') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'g');
                    currPerm.setCharAt(i,'g');
                    if(!similarWordsMap.get(queryLabel).get(originalWindow).contains(currPerm.toString())){
                        similarWordsMap.get(queryLabel).get(originalWindow).add(currPerm.toString());
                    }
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    currPerm.setCharAt(i,'g');
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
            }
            if(currPerm.charAt(i) != 'c'){
                //if score after change >= similarityThreshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'c') >= similarityThreshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'c');
                    currPerm.setCharAt(i,'c');
                    if(!similarWordsMap.get(queryLabel).get(originalWindow).contains(currPerm.toString())){
                        similarWordsMap.get(queryLabel).get(originalWindow).add(currPerm.toString());
                    }
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    currPerm.setCharAt(i,'c');
                    checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
            }
            checkPerm(queryLabel,currPerm.toString(),originalWindow,i+1,currentScore);
        }
    }
}