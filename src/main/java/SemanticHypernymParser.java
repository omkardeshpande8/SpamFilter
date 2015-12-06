
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
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

public class SemanticHypernymParser {

    static HashMap<String, String> wordAndLemma = new HashMap<>();
    static HashMap<String, Integer> spamWords = new HashMap<>();
    static HashMap<String, Integer> hamWords = new HashMap<>();
    static double totalHamWords = 0;
    static double totalSpamWords = 0;
    static ArrayList<String> uniqueWords = new ArrayList<>();
    static HashMap<String, Double> spamProb = new HashMap<>();
    static HashMap<String, Double> hamProb = new HashMap<>();

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
                IIndexWord idxWord = null;

                if (tag.startsWith("N")) {
                    idxWord = dict.getIndexWord(word, POS.NOUN);
                } else if (tag.startsWith("V")) {
                    idxWord = dict.getIndexWord(word, POS.VERB);
                } else if (tag.equals("JJ")) {
                    idxWord = dict.getIndexWord(word, POS.ADJECTIVE);
                } else if (tag.contains("RB")) {
                    idxWord = dict.getIndexWord(word, POS.ADVERB);
                }

                if (idxWord != null) {
                    IIndexWord idxWord2 = dict.getIndexWord(word, idxWord.getPOS());
                    IWordID wordID2 = idxWord2.getWordIDs().get(0); // 1st meaning
                    IWord word2 = dict.getWord(wordID2);
                    ISynset synset = word2.getSynset();

                    List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
                    List<IWord> words;
                    String lemma = "";
                    for (ISynsetID sid : hypernyms) {
                        words = dict.getSynset(sid).getWords();
                        if (!words.isEmpty()) {
                            lemma = words.get(0).getLemma();
                        }
                    }

                    wordAndLemma.put(word, lemma);

                    if (!uniqueWords.contains(lemma)) {
                        uniqueWords.add(lemma);
                    }

                } else {
                    wordAndLemma.put(word, word);

                    if (!uniqueWords.contains(word)) {
                        uniqueWords.add(word);
                    }
                }

                if (path.contains("spam")) {
                    if (spamWords.containsKey(wordAndLemma.get(word))) {
                        spamWords.put(wordAndLemma.get(word), spamWords.get(wordAndLemma.get(word)) + 1);
                    } else {
                        spamWords.put(wordAndLemma.get(word), 1);
                    }

                    totalSpamWords++;
                } else {
                    if (hamWords.containsKey(wordAndLemma.get(word))) {
                        hamWords.put(wordAndLemma.get(word), hamWords.get(wordAndLemma.get(word)) + 1);
                    } else {
                        hamWords.put(wordAndLemma.get(word), 1);
                    }

                    totalHamWords++;
                }
            }

            /*for(Map.Entry<String, String> entry : wordAndLemma.entrySet())
             System.out.println("WORD: " + entry.getKey() + "\t" + "LEMMA: " + entry.getValue());*/
        } catch (IOException ex) {
            Logger.getLogger(SemanticHypernymParser.class.getName()).log(Level.SEVERE, null, ex);
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

            Double denominator = totalSpamWords + uniqueWords.size();
            spamProb.put(s, numerator / denominator);

            System.out.println("SPAM: " + numerator / denominator);

            numerator = 1.0;
            if (hamWords.containsKey(s)) {
                numerator += hamWords.get(s);
            } else {
                numerator = 0 + numerator;
            }

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
            Logger.getLogger(SemanticHypernymParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        URL url = null;
        String path = "C://WordNet-3.0//dict";
        try {
            url = new URL("file", null, path);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SemanticHypernymParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        IDictionary dict = new Dictionary(url);

        constructHashMaps("C://email//spam_training.txt", dict);
        constructHashMaps("C://email//ham_training.txt", dict);
        findProbabilities();
        performTesting();
    }
}
