
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class Test {

    static HashMap<String, Integer> overlapMap = new HashMap<>();
    static HashMap<String, List<String>> overlapWords = new HashMap<>();

    public static void main(String[] args) throws IOException {

        String path = args[0];
        String sentence = "The bank can guarantee deposits will eventually cover future tuition costs because it invests in adjustable-rate mortgage securities";

        URL url = null;
        try {
            url = new URL("file", null, path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url == null) {
            return;
        }

        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        for (POS posVal : POS.values()) {
            IIndexWord idxWord = dict.getIndexWord("bank", posVal);

            if (dict.getIndexWord("bank", posVal) != null) {
                List<IWordID> wordSenses = idxWord.getWordIDs();

                for (IWordID sense : wordSenses) {
                    IWord word = dict.getWord(sense);

                    String gloss = word.getSynset().getGloss();
                    calculateOverlap(gloss, sentence, posVal);
                }
            }
        }

        //printing the overlap map
        for (Entry<String, Integer> entry : overlapMap.entrySet()) {
            String key = entry.getKey().toString();
            int value = entry.getValue();
            System.out.println("------");
            System.out.println("SENSE: " + key + "\t\tOVERLAP: " + value);
            List<String> displayOverlap = overlapWords.get(key);
            for (String str : displayOverlap) {
                System.out.println(str);
            }
        }

        //calculating the most likely sense
        int maxVal = -1;
        String sense = "";

        for (Entry<String, Integer> entry : overlapMap.entrySet()) {
            if (entry.getValue() > maxVal) {
                maxVal = entry.getValue();
                sense = entry.getKey();
            }
        }

        System.out.println("------------------------");
        System.out.println("MOST LIKELY SENSE IS: " + sense);
        System.out.println("VALUE OF ITS OVERLAP IS: " + maxVal);
    }

    public static void calculateOverlap(String gloss, String sentence, POS posVal) {
        String glossMeaning = gloss.split(";")[0];
        gloss = cleanGloss(gloss);
        List<String> overlappedWords = new ArrayList<>();

        String[] splitSentence = sentence.split(" ");

        for (int i = 0; i < splitSentence.length; i++) {
            if (gloss.contains(splitSentence[i])) {
                if (overlapMap.containsKey(glossMeaning)) {
                    overlapMap.put(glossMeaning, overlapMap.get(glossMeaning) + 1);
                } else {
                    overlapMap.put(glossMeaning, 1);
                }
                overlappedWords.add(splitSentence[i]);
                overlapWords.put(glossMeaning, overlappedWords);
            }
        }
    }

    public static String cleanGloss(String gloss) {
        gloss = gloss.replace("\"", "").replace(";", "");
        return gloss;
    }

}
