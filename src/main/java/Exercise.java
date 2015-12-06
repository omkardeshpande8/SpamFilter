
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

class Exercise {

    public static void main(String[] args) throws Exception {
//    if (args.length != 2) {
//      System.err.println("usage: java TaggerDemo modelFile fileToTag");
//      return;
//    }
        MaxentTagger tagger = new MaxentTagger("C:\\Users\\Omkar\\Downloads\\stanford-postagger-2015-04-20\\stanford-postagger-2015-04-20\\models\\english-bidirectional-distsim.tagger");
        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader("C:\\Users\\Omkar\\Documents\\Fall15\\NLP\\Project\\train\\ham\\0004.1999-12-14.farmer.ham.txt")));
        for (List<HasWord> sentence : sentences) {
            List<TaggedWord> tSentence = tagger.tagSentence(sentence);
            for(TaggedWord t:tSentence){
                String tag = t.tag();
                String word =  t.word();
            }
            //System.out.println(Sentence.listToString(tSentence, false));
        }
        DocumentPreprocessor docPreprocessor = new DocumentPreprocessor(new BufferedReader(new FileReader("C:\\Users\\Omkar\\Documents\\Fall15\\NLP\\Project\\train\\ham\\0004.1999-12-14.farmer.ham.txt")));
        int numSents = 0;
          String encoding ="utf-8";
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, encoding), true);

        for (List<HasWord> sentence : docPreprocessor) {
            numSents++;
             //  System.err.printf("Length: %d%n", sentence.size());
            boolean printSpace = false;
            for (HasWord word : sentence) {
                    if (printSpace) {
                        pw.print(" ");
                    }
                    printSpace = true;
                    pw.print(word.word());
                
            }
            pw.println();
        }
    }

}

