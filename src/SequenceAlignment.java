import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SequenceAlignment {
    HashMap<String, String> sequenceQueries;
    HashMap<String, String> sequenceText;
    int threshold;
    int k;
    int a;
    int b;
    TransitionMatrix matrix;
    LinkedHashMap<String,LinkedHashMap<String, ArrayList<Integer>>> wordsTextMap;
    LinkedHashMap<String, ArrayList<Word>> similerWordsMap;
    LinkedHashMap<Integer,Word> hits;

    public SequenceAlignment(String queriesFile, String matrixFile, String textFile, int num, int threshold) {
        k = num;
        this.threshold = threshold;
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
        similerWordsMap = new LinkedHashMap<String, ArrayList<Word>>();
        //for each query
        String currentWindow;
        for (Map.Entry<String, String> entry : sequenceQueries.entrySet()) {
            String value = entry.getValue();
            //for each sliding window, find all similar words
            for (int i = 0; i <= value.length() - k; i++) {
                currentWindow = value.substring(i, i + k);
                if (!similerWordsMap.containsKey(currentWindow)) {
                    similerWordsMap.put(currentWindow, findSimilarWords(currentWindow));
                }
            }
        }
    }


    public void findHits(String query, String text) {
        LinkedHashMap<String,ArrayList<Integer>> textWords = wordsTextMap.get(text);
        hits = new LinkedHashMap<>();
        ArrayList<Word> similarWords;
        for(int i = 0; i <= query.length() - k; i++){// for each sliding window in the query
            similarWords = similerWordsMap.get(query.substring(i,i + k));
            for(Word currWord : similarWords){// find the similar word in the text
                if(textWords.containsKey(currWord.getParagraph())){// check if similar word exists in text
                    for(Integer index : textWords.get(currWord.getParagraph())){
                        hits.put(index,currWord);
                    }
                }
            }
        }
    }

    public void extendHits(String query, String text, LinkedHashMap<Integer,Word> hits){

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


    private ArrayList<Word> findSimilarWords(String currentWindow) {
        ArrayList<Word> similarWords = new ArrayList<>();
        //find similar words to "currentWindow"
        similarWords.add(new Word(currentWindow,currentWindow.length()*5));
        checkPerm(similarWords,currentWindow,currentWindow,0,currentWindow.length()*5);
        return similarWords;
    }
    private ArrayList<Word> checkPerm(ArrayList<Word> similarWords, String currentWindow,String originalWindow, int i, int currentScore){
        StringBuilder currPerm = new StringBuilder(currentWindow);
        int newScore;
        if(i == currentWindow.length()){
            return similarWords;
        }
        else{
            //check all permutations of i and send them to checkPerm
            //the idea: change currWindow[i] from X to Y, if it's already Y do nothing
            if(currPerm.charAt(i) != 'a'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'a') >= threshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'a');
                    currPerm.setCharAt(i,'a');
                    similarWords.add(new Word(currPerm.toString(),newScore));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
                }
            }
            else{
                checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
            }
            if(currPerm.charAt(i) != 't'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'t') >= threshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'t');
                    currPerm.setCharAt(i,'t');
                    similarWords.add(new Word(currPerm.toString(),newScore));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
                }
            }
            else{
                checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
            }
            if(currPerm.charAt(i) != 'g'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'g') >= threshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'g');
                    currPerm.setCharAt(i,'g');
                    similarWords.add(new Word(currPerm.toString(),newScore));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
                }
            }
            else{
                checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
            }
            if(currPerm.charAt(i) != 'c'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'c') >= threshold){
                    newScore = currentScore + matrix.score(currPerm.charAt(i),'c');
                    currPerm.setCharAt(i,'c');
                    similarWords.add(new Word(currPerm.toString(),newScore));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,newScore);
                    currPerm.setCharAt(i,currentWindow.charAt(i));
                }
                else{
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
                }
            }
            else{
                checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore);
            }
            return similarWords;
        }
    }
}