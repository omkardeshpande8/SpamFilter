
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SyntacticPosParser {

    public static class Data {

        public Data(String POS, int count) {
            super();
            this.POS = POS;
            this.count = count;
        }

        public String POS;
        public int count;
        public Double probability;
    }

    static HashMap<String, String> wordAndLemma = new HashMap<>();
    static HashMap<String, Integer> spamWords = new HashMap<>();
    static HashMap<String, Integer> hamWords = new HashMap<>();
    static double totalHamWords = 0;
    static double totalSpamWords = 0;
    static ArrayList<String> uniqueWords = new ArrayList<>();
    static HashMap<String, Double> spamProb = new HashMap<>();
    static HashMap<String, Double> hamProb = new HashMap<>();
    static HashMap<String, Data> posCounts = new HashMap<>();
    static HashMap<String, Integer> tagMap = new HashMap<>();

    public static void constructHashMaps(String path, IDictionary dict) {
        try {
            dict.open();
            String fileString = "";
            List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(path)));
            MaxentTagger tagger = new MaxentTagger("C://email//english-bidirectional-distsim.tagger");

            for (List<HasWord> sentence : sentences) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                fileString = fileString.concat(Sentence.listToString(tSentence, false));
            }

            fileString.replace("(", "").replace(".", "").replace(":", "").replace(")", "").replace("\"", "");
            String[] lineArray = fileString.split(" ");

            for (String s : lineArray) {
                String word = s.split("/")[0];
                String tag = s.split("/")[1];

                if (tagMap.containsKey(tag)) {
                    tagMap.put(tag, tagMap.get(tag));
                } else {
                    tagMap.put(tag, 1);
                }

                if (posCounts.containsKey(word)) {
                    Data tempData = posCounts.get(word);
                    tempData.count++;
                    posCounts.put(word, tempData);
                } else {
                    Data newData = new Data(tag, 1);
                    posCounts.put(word, newData);
                }

                if (path.contains("spam")) {
                    if (spamWords.containsKey(word)) {
                        spamWords.put(word, spamWords.get(word) + 1);
                    } else {
                        spamWords.put(word, 1);
                    }
                    totalSpamWords++;
                } else {
                    if (hamWords.containsKey(word)) {
                        hamWords.put(word, hamWords.get(word) + 1);
                    } else {
                        hamWords.put(word, 1);
                    }
                    totalHamWords++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SyntacticPosParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void findProbabilities() {
        for (String s : uniqueWords) {
            Double numerator = 1.0;
            if (spamWords.containsKey(s)) {
                numerator += spamWords.get(s);
            } else {
                numerator = 0 + numerator;
            }
            numerator = numerator * (posCounts.get(s).count / tagMap.get(posCounts.get(s).POS));

            Double denominator = totalSpamWords + uniqueWords.size();
            spamProb.put(s, numerator / denominator);
            System.out.println("SPAM: " + numerator / denominator);
            numerator = 1.0;
            if (hamWords.containsKey(s)) {
                numerator += hamWords.get(s);
            } else {
                numerator = 0 + numerator;
            }
            numerator = numerator * (posCounts.get(s).count / tagMap.get(posCounts.get(s).POS));
            denominator = totalHamWords + uniqueWords.size();
            hamProb.put(s, numerator / denominator);
            System.out.println("HAM: " + numerator / denominator);
        }
    }

    public static void performTesting() {
        try {
            String testFileString = new Scanner(new File("C://email//temp.txt")).useDelimiter("\\A").next();
            testFileString.replace("(", "").replace(".", "").replace(":", "").replace(")", "").replace("\"", "");

            String[] wordArrayTest = testFileString.split(" ");
            Double spamProbability = 0.0;
            for (String s : wordArrayTest) {
                if (spamWords.containsKey(s)) {
                    spamProbability += Math.log(spamWords.get(s));
                }
            }
            System.out.println(spamProbability);

            Double hamProbability = 0.0;
            for (String s : wordArrayTest) {
                if (hamWords.containsKey(s)) {
                    hamProbability += Math.log(hamWords.get(s));
                }
            }
            System.out.println(hamProbability);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SyntacticPosParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        URL url = null;
        String path = "C://WordNet-3.0//dict";
        try {
            url = new URL("file", null, path);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SyntacticPosParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        IDictionary dict = new Dictionary(url);

        constructHashMaps("C://email//spam_training.txt", dict);
        constructHashMaps("C://email//ham_training.txt", dict);
        findProbabilities();
        performTesting();
    }
}
