
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 *
 */
public class SyntacticSentencePOSTagParser {

    static HashMap<String, String> wordAndLemma = new HashMap<>();
    static HashMap<String, Integer> spamWords = new HashMap<>();
    static HashMap<String, Integer> hamWords = new HashMap<>();
    static double totalHamWords = 0;
    static double totalSpamWords = 0;
    static ArrayList<String> uniqueWords = new ArrayList<>();
    static HashMap<String, Double> spamProb = new HashMap<>();
    static HashMap<String, Double> hamProb = new HashMap<>();
    static HashMap<String, SyntacticPosParser.Data> posCounts = new HashMap<>();
    static HashMap<String, Integer> tagMap = new HashMap<>();
    static HashMap<List<String>, Integer> spamSentenceCounts = new HashMap<>();
    static HashMap<List<String>, Integer> hamSentenceCounts = new HashMap<>();
    static HashMap<List<String>, Integer> testSentenceCounts = new HashMap<>();
    static MaxentTagger tagger;

    public static void constructHashMaps(String path, IDictionary dict) {
        try {
            dict.open();
            List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(new File(SyntacticSentencePOSTagParser.class.getResource(path).toURI()))));

            for (List<HasWord> sentence : sentences) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                List<String> tagList = new ArrayList<>();
                for (TaggedWord t : tSentence) {
                    tagList.add(t.tag());
                }
                if (path.contains("spam")) {
                    if (spamSentenceCounts.containsKey(tagList)) {
                        spamSentenceCounts.put(tagList, spamSentenceCounts.get(tagList) + 1);
                    } else {
                        spamSentenceCounts.put(tagList, 1);
                    }
                } else {
                    if (hamSentenceCounts.containsKey(tagList)) {
                        hamSentenceCounts.put(tagList, hamSentenceCounts.get(tagList) + 1);
                    } else {
                        hamSentenceCounts.put(tagList, 1);
                    }
                }
            }
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(SyntacticSentencePOSTagParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void performTesting() {
        try {
            List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(new File(SyntacticSentencePOSTagParser.class.getResource("temp.txt").toURI()))));

            for (List<HasWord> sentence : sentences) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                List<String> tagList = new ArrayList<>();
                for (TaggedWord t : tSentence) {
                    tagList.add(t.tag());
                }
                if (testSentenceCounts.containsKey(tagList)) {
                    testSentenceCounts.put(tagList, testSentenceCounts.get(tagList) + 1);
                } else {
                    testSentenceCounts.put(tagList, 1);
                }
            }

            int spamLength = 0;
            for (int count : spamSentenceCounts.values()) {
                spamLength += count;
            }
            int hamLength = 0;
            for (int count : hamSentenceCounts.values()) {
                hamLength += count;
            }

            int uniqueCount = spamSentenceCounts.keySet().size() + hamSentenceCounts.keySet().size();

            double spamProb2 = 0;
            double hamProb2 = 0;

            for (List<String> test : testSentenceCounts.keySet()) {
                int spamCount = 0;
                if (spamSentenceCounts.get(test) != null) {
                    spamCount = spamSentenceCounts.get(test);
                }

                int hamCount = 0;
                if (hamSentenceCounts.get(test) != null) {
                    hamCount = hamSentenceCounts.get(test);
                }
                spamProb2 += Math.log((double) (spamCount + 1) / (spamLength + uniqueCount));
                hamProb2 += Math.log((double) (hamCount + 1) / (hamLength + uniqueCount));
            }
            spamProb2 *= -1;
            hamProb2 *= -1;

            Helper.displayClassification(spamProb2, hamProb2);
        } catch (FileNotFoundException | URISyntaxException ex) {
            Logger.getLogger(SyntacticSentencePOSTagParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        FileInputStream fis;
        try {
            fis = new FileInputStream(SemanticHypernymParser.class.getResource("resources.properties").toURI().getPath());
            PropertyResourceBundle resourceBundle = new PropertyResourceBundle(fis);

            String path = resourceBundle.getString("wordnet");
            URL url = new URL("file", null, path);
            IDictionary dict = new Dictionary(url);
            tagger = new MaxentTagger(SyntacticSentencePOSTagParser.class.getResource("english-bidirectional-distsim.tagger").toURI().getPath());
            constructHashMaps("spam_training.txt", dict);
            constructHashMaps("ham_training.txt", dict);
            performTesting();

        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(SyntacticSentencePOSTagParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
