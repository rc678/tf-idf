import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static ArrayList<String> reviewList = new ArrayList<String>();
    public static ArrayList<String> R = new ArrayList<String>();
    public static String r_star = "";
    public static ArrayList<String> topFiveReviews = new ArrayList<String>();
    public static HashMap<String, Float> idfList = new HashMap<String, Float>();
    public static HashMap<String, Integer> wordAndDoc = new HashMap<>();
    public static HashMap<Integer, HashMap<String, Float>> tfValues = new HashMap<>();
    public static String query1;
    public static String query2;

    public static void main(String[] args) throws IOException {
            /**************PARSES UNLABEL_REVIEW**********************/
            // To connect to mongodb server
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

            // Now connect to your databases
            DB db = mongoClient.getDB("cs336");

            System.out.println("Connect to database successfully");
            DBCollection col = db.getCollection("unlabel_review");

            DBCursor cur = col.find();

            Random rand = new Random();
            int randomIndex = rand.nextInt(5-0) + 0;

            /*traverses through entire mongo database and gets reviews*/
            for(int j=0; j < cur.toArray().size(); j++)
            {
                 /*review 355 is null, sp skip to avoid error*/
                if (j != 354) {
                    String rev = (String) cur.toArray().get(j).get("review");
                    reviewList.add(rev);
                    /*adds the first 6 reviews to the R*/
                    if(j <= 5)
                    {
                        R.add(rev);
                    }
                }/*end of outer if statement*/
            }/*end of for loop */

            /*gets one random review from R*/
            r_star = R.get(randomIndex);

            selectQuery();
            setTfValues();
            setIDFValues();
    }//end of main

    /*gets 2 random words from r_star*/
    public static void selectQuery()
    {
        String rev = r_star;
        String[] review = rev.split(" ");
        //rev = rev.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

        Random rand = new Random();
        int randIndex = rand.nextInt(review.length - 0) + 0;
        query1 = review[randIndex];
        randIndex = rand.nextInt(review.length - 0) + 0;
        query2 = review[randIndex];
    }

    /*parses review into individual words and sets the TF value*/
    public static void setTfValues()
    {
        float wordCountInReview;
        float totalNumWordsReview;
        float tfForCurrWord;
        HashMap<String, Integer> wordCounter = new HashMap<>();

        /*traverses through list of reviews for parsing */
        for(int i = 0; i < reviewList.size() ; i++)
        {
            String[] currReview = reviewList.get(i).split(" "); //splits review into words which are stored in each index
            totalNumWordsReview = (float) currReview.length;

            /*traverses through the words in the curr review*/
            for(int j = 0; j < currReview.length; j++)
            {
                String currWordInReview = currReview[j].toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                /*if word in review was not found HashMap*/
                if(wordCounter.get(currWordInReview) == null)
                {
                    wordCounter.put(currWordInReview, 1);
                    if(wordAndDoc.get(currWordInReview) == null)
                    {
                        wordAndDoc.put(currWordInReview, 1);
                    }else
                    {
                        int docNum = wordAndDoc.get(currWordInReview);
                        docNum++;
                        wordAndDoc.remove(currWordInReview);
                        wordAndDoc.put(currWordInReview, docNum);
                    }
                }else /*if word is in HashMap, increment count*/
                {
                    int numCount = wordCounter.get(currWordInReview);
                    numCount++;
                    wordCounter.remove(currWordInReview);
                    wordCounter.put(currWordInReview, numCount);
                }
            }/*end of inner for loop*/

            HashMap<String, Float> tmp = new HashMap<>();
            /*traverses through the words in the curr review to get tf value*/
            for(int j=0; j < currReview.length; j++)
            {
                String currWord = currReview[j].toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                wordCountInReview =  wordCounter.get(currWord);
                tfForCurrWord = wordCountInReview / totalNumWordsReview;

                if(tmp.get(currWord) == null)
                {
                    tmp.put(currWord, tfForCurrWord);
                }
            }/*end of for inner loop*/

            tfValues.put(i, tmp);
            tmp.clear();
            wordCounter.clear();
        }/*end of outer for loop*/

    }/*end of setTfValues*/

    /*creates HashMap of word and it's idf value. Needed to calculate cosine similarity*/
    public static void setIDFValues()
    {
        float totalNumDocuments = reviewList.size();
        float numDocsWithWord;
        float idf;

        /*traverse hashmap of unique words*/
        for (String uniqueWord: wordAndDoc.keySet()){
            String key  = uniqueWord.toString();
            numDocsWithWord = wordAndDoc.get(key);
            float df = ((totalNumDocuments) / (numDocsWithWord));
            idf = (float) (1 + Math.log(df));

            idfList.put(key, df);
        }
    } /*end of setIDFValues*/









}//end of class
