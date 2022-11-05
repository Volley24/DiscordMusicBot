package max.waouh;

import java.util.Map;

public class UserData {
    public String name, id;
    public Map<String, Integer> words;

    public UserData(String name, String id, Map<String, Integer> words){
        this.name = name;
        this.id = id;

        this.words = words;
    }
}
