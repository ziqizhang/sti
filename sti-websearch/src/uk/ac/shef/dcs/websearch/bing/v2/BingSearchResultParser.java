package uk.ac.shef.dcs.websearch.bing.v2;

import uk.ac.shef.dcs.websearch.SearchResultParser;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

import javax.json.Json;
import javax.json.stream.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 */

public class BingSearchResultParser extends SearchResultParser {
    public List<WebSearchResultDoc> parse(InputStream is) throws IOException {
        List<WebSearchResultDoc> result = new ArrayList<>();
        if(is==null)
            return result;
        InputStreamReader isr = new InputStreamReader(is);

        int numCharsRead;
        char[] charArray = new char[1024];
        StringBuilder sb = new StringBuilder();
        while ((numCharsRead = isr.read(charArray)) > 0) {
            sb.append(charArray, 0, numCharsRead);
        }
        JsonParser parser = Json.createParser(new StringReader(sb.toString()));

        boolean newDocId=false, newDocTitle=false, newDocDesc=false, newDocURL=false;
        WebSearchResultDoc doc=null;
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            switch(event) {
                /*case START_ARRAY:
                case END_ARRAY:
                case START_OBJECT:
                case END_OBJECT:
                case VALUE_FALSE:
                case VALUE_NULL:
                case VALUE_TRUE:
                    System.out.println(event.toString());
                    break;*/
                case KEY_NAME:
                    /*System.out.print(event.toString() + " " +
                            parser.getString() + " - ");*/
                    String key = parser.getString();
                    if(key.equals("ID")){
                        //finish the previous object
                        if(doc!=null){
                            result.add(doc);
                            newDocId=false;
                            newDocTitle=false;
                            newDocDesc=false;
                            newDocURL=false;
                        }

                        //create new object
                        newDocId=true;
                        doc=new WebSearchResultDoc();
                    }else if(key.equals("Title")){
                        newDocTitle = true;
                    }else if(key.equals("Description")){
                        newDocDesc = true;
                    }else if(key.equals("Url")){
                        newDocURL = true;
                    }
                    break;
                case VALUE_STRING:
                case VALUE_NUMBER:
                    String value = parser.getString();
                    if(newDocId){
                        doc.setId(value);
                        newDocId=false;
                    }else if(newDocTitle){
                        doc.setTitle(value);
                        newDocTitle=false;
                    }else if(newDocDesc){
                        doc.setDescription(value);
                        newDocDesc=false;
                    }else if(newDocURL){
                        doc.setUrl(value);
                        newDocURL=false;
                    }
                    break;
            }
        }
        if(doc!=null)
            result.add(doc);
        return result;
    }
}
