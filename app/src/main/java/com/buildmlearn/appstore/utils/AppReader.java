package com.buildmlearn.appstore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import com.buildmlearn.appstore.models.AppInfo;
import com.buildmlearn.appstore.models.Card;
import com.buildmlearn.appstore.models.FlashModel;
import com.buildmlearn.appstore.models.InfoModel;
import com.buildmlearn.appstore.models.Question;
import com.buildmlearn.appstore.models.QuizModel;
import com.buildmlearn.appstore.models.SpellingsModel;
import com.buildmlearn.appstore.models.WordModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class to read through different types of buildmlearn files-Info, Quiz, FlashCards and Spellings Puzzle.
 */
public class AppReader {
    private static BufferedReader br;
    public static ArrayList<AppInfo>AppList;

    /**
     * Gets the list of Installed Apps.
     * @param context Context object of the current Activity
     * @return ArrayList of apps
     */
    public static ArrayList<AppInfo> listApps(Context context) {
        ArrayList<AppInfo> mFileList = new ArrayList<>();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();
        AssetManager am = res.getAssets();
        String appList[],iconList[];
        try {
            appList = am.list("Apps");
            iconList = am.list("Icons");
            for (int i = 0; i < appList.length; i++) {
                AppInfo app = new AppInfo();
                app.Name = (appList[i].substring(0, appList[i].indexOf(".buildmlearn")));
                if (!SP.contains(app.Name)) {
                    SharedPreferences.Editor editor1 = SP.edit();
                    editor1.putBoolean(app.Name, false);
                    editor1.apply();
                    continue;
                }
                if (!SP.getBoolean(app.Name, false)) continue;

                app.AppIcon = BitmapFactory.decodeStream(am.open("Icons/" + iconList[i]));
                BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("Apps/" + appList[i])));
                String type = br.readLine();
                if (type.contains("InfoTemplate")) app.Type = 0;
                else if (type.contains("QuizTemplate")) app.Type = 2;
                else if (type.contains("FlashCardsTemplate")) app.Type = 1;
                else if (type.contains("SpellingTemplate")) app.Type = 3;
                br.readLine();
                type = br.readLine();
                int x = type.indexOf("<name>") + 6;
                int y = type.indexOf("<", x + 1);
                app.Author = type.substring(x, y);
                mFileList.add(app);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        AppList=mFileList;
        return mFileList;
    }

    /**
     * Reads the Info type of app
     * @param myContext Context object of the Activity
     * @param fileName Name of the file to be read
     */
    public static void readInfoFile(Context myContext, String fileName) {
        InfoModel model = InfoModel.getInstance();
        ArrayList<String> stringList = new ArrayList<>();
        try {
            HashMap<String, String> mInfoMap = new HashMap<>();
            XMLParser parser = new XMLParser();
            br = new BufferedReader(new InputStreamReader(myContext.getAssets().open(fileName))); // throwing a FileNotFoundException
            String xml = "", temp;
            while ((temp = br.readLine()) != null) {xml+=temp;} //getting xml
            Document doc = parser.getDomElement(xml); // getting DOM element
            Element elementAuthor= (Element) doc.getElementsByTagName("author").item(0);
            model.setInfoAuthor(parser.getValue(elementAuthor, "name"));
            model.setInfoName(fileName.substring(5,fileName.length()-12));
            NodeList nodeList = doc.getElementsByTagName("item");   // looping through all item nodes <app>
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element elementInfo = (Element) nodeList.item(i);
                String infoKey = parser.getValue(elementInfo, "item_title");
                String infoValue = parser.getValue(elementInfo, "item_description");
                mInfoMap.put(infoKey, infoValue);
                stringList.add(infoKey);
            }
            model.setInfoMap(mInfoMap);
            model.setListTitleList(stringList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
                try {
                    br.close(); // stop reading
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        }
    }

    /**
     * Reads the Quiz type of app
     * @param myContext Context object of the Activity
     * @param fileName Name of the file to be read
     */
    public static void readQuizFile(Context myContext, String fileName) {
        QuizModel model = QuizModel.getInstance();
        ArrayList<Question> mQuestionList = new ArrayList<>();

        try {
            XMLParser parser = new XMLParser();

            br = new BufferedReader(new InputStreamReader(myContext.getAssets().open(fileName))); // throwing a FileNotFoundException
            String xml = "", temp;
            while ((temp = br.readLine()) != null) {
                xml += temp;
            } //getting xml
            Document doc = parser.getDomElement(xml); // getting DOM element
            Element elementAuthor = (Element) doc.getElementsByTagName("author").item(0);
            model.setQuizAuthor(parser.getValue(elementAuthor, "name"));
            model.setQuizName(fileName.substring(5, fileName.length() - 12));
            NodeList nodeList = doc.getElementsByTagName("item");
            NodeList optionList=doc.getElementsByTagName("option");
            int index=0;
            // looping through all item nodes <app>
            for (int i = 0; i < nodeList.getLength(); i++) {

                Element elementInfo = (Element) nodeList.item(i);
                ArrayList<String> mOptionList = new ArrayList<>();
                Question ques = new Question();
                ques.setQuestion(parser.getValue(elementInfo, "question"));

                for (int j = 0; j < 4; j++) {
                    mOptionList.add(parser.getElementValue(optionList.item(index++)));
                }
                ques.setAnswerOption(mOptionList);
                ques.setOptionNumber(Integer.parseInt(parser.getValue(elementInfo, "answer")));
                mQuestionList.add(ques);
            }
            model.setQueAnsList(mQuestionList);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reads the Spelling Puzzle type of app
     * @param myContext Context object of the Activity
     * @param fileName Name of the file to be read
     */
    public static void readSpellingsContent(Context myContext, String fileName) {

        SpellingsModel model = SpellingsModel.getInstance();
        ArrayList<WordModel> wordList = new ArrayList<>();
        try {
            XMLParser parser = new XMLParser();
            br = new BufferedReader(new InputStreamReader(myContext.getAssets().open(fileName))); // throwing a FileNotFoundException
            String xml = "", temp;
            while ((temp = br.readLine()) != null) {
                xml += temp;
            } //getting xml
            Document doc = parser.getDomElement(xml); // getting DOM element
            Element elementAuthor = (Element) doc.getElementsByTagName("author").item(0);
            model.setPuzzleAuthor(parser.getValue(elementAuthor, "name"));
            model.setPuzzleName(fileName.substring(5, fileName.length() - 12));
            NodeList nodeList = doc.getElementsByTagName("item");
            // looping through all item nodes <app>
            for (int i = 0; i < nodeList.getLength(); i++) {

                Element elementInfo = (Element) nodeList.item(i);
                WordModel word = new WordModel(
                        parser.getValue(elementInfo, "word"),
                        parser.getValue(elementInfo, "meaning"));
                wordList.add(word);
            }
            model.setSpellingsList(wordList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reads the FlashCards type of app
     * @param myContext Context object of the Activity
     * @param fileName Name of the file to be read
     */
    public static void readFlashContent(Context myContext, String fileName) {
        FlashModel model = FlashModel.getInstance();
        ArrayList<Card> cardList = new ArrayList<>();
        try {
            XMLParser parser = new XMLParser();
            br = new BufferedReader(new InputStreamReader(myContext.getAssets().open(fileName))); // throwing a FileNotFoundException
            String xml = "", temp;
            while ((temp = br.readLine()) != null) {
                xml += temp;
            } //getting xml
            Document doc = parser.getDomElement(xml); // getting DOM element
            Element elementAuthor = (Element) doc.getElementsByTagName("author").item(0);
            model.setAuthor(parser.getValue(elementAuthor, "name"));
            model.setName(fileName.substring(5, fileName.length() - 12));
            NodeList nodeList = doc.getElementsByTagName("item");
            // looping through all item nodes <app>
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element elementInfo = (Element) nodeList.item(i);
                Card card = new Card(
                        parser.getValue(elementInfo, "question"),
                        parser.getValue(elementInfo, "answer"),
                        parser.getValue(elementInfo, "hint"),
                        parser.getValue(elementInfo, "image"));
                cardList.add(card);
            }
            model.setList(cardList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}