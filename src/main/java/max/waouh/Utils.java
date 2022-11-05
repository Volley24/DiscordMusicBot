package max.waouh;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {
    private static Random RANDOM = new Random();

    public static boolean chanceHappens(int chance, int total){
        int num = RANDOM.nextInt(total);

        return num < chance;
    }
    public static void writeToFile(String file, String contents){
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(contents);
            myWriter.close();

            return;
        }catch (IOException e){
            e.printStackTrace();
        }

        throw new IllegalArgumentException("Could not find file "+file);
    }

    public static String readFileAsString(String file){
        try {
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("Could not find file "+file);
    }

    public static HashMap<String, UserData> parse(String jsonString){
        HashMap<String, UserData> userProfiles = new HashMap<>();

        JSONObject mainJSONObject = new JSONObject(jsonString);
        JSONArray userJsonArray = mainJSONObject.getJSONArray("users");

        for (int i = 0; i < userJsonArray.length(); i++){
            JSONObject jsonObject = userJsonArray.getJSONObject(i);

            String name = jsonObject.getString("name");
            String id = jsonObject.getString("id");

            Map<String, Object> words = jsonObject.getJSONObject("words").toMap();
            Map<String, Integer> wordsInt = new HashMap<>();

            for (Map.Entry<String, Object> entry : words.entrySet()){
                wordsInt.put(entry.getKey(), (Integer) entry.getValue());
            }

            userProfiles.put(id, new UserData(name, id, wordsInt));
        }
        return userProfiles;
    }

    public static String export(HashMap<String, UserData> userRecord){
        JSONObject mainJSONObject = new JSONObject();
        JSONArray userJsonArray = new JSONArray();

        for(String key : userRecord.keySet()){
            UserData userData = userRecord.get(key);

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("name", userData.name);
            jsonObject.put("id", userData.id);
            jsonObject.put("words", userData.words);

            userJsonArray.put(jsonObject);
        }
        mainJSONObject.put("users", userJsonArray);

        return mainJSONObject.toString();
    }
}
