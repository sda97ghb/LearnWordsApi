import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;

@Controller
@EnableAutoConfiguration
public class Main {
    private static ServiceExecutor serviceExecutor = new ServiceExecutor(LearnWordsService.class);

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "LearnWordsApi server.";
    }

    @RequestMapping(path = "/api/2.1", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    byte[] api(@RequestBody String requestBody) {
        requestBody = URLDecoder.decode(requestBody);
//        System.out.println(requestBody);
        return serviceExecutor.invoke(requestBody);
    }

    public static void main(String... args) {
        SpringApplication.run(Main.class, args);

//        ServiceExecutor serviceExecutor = new ServiceExecutor(LearnWordsService.class);

//        String requestString = "{" +
//        "    \"entity\": \"user\"," +
//        "    \"method\": \"add\"," +
//        "    \"email\": \"sda97g@gmail.com\"," +
//        "    \"password\": \"123456\"" +
//        "}";

//        String requestString = "{" +
//        "    \"entity\": \"user\"," +
//        "    \"method\": \"get\"," +
//        "    \"email\": \"sda97g@gmail.com\"" +
//        "}";

//        String requestString = "{" +
//        "    \"entity\": \"deck\"," +
//        "    \"method\": \"save\"," +
//        "    \"email\": \"sda97g@gmail.com\"," +
//        "    \"deck\": {" +
//        "        \"name\": \"Deck 1\"," +
//        "        \"fromLanguage\": \"English\"," +
//        "        \"toLanguage\": \"Russian\"" +
//        "    }" +
//        "}";

//        String requestString = "{" +
//                "    \"entity\": \"deck\"," +
//                "    \"method\": \"get\"," +
//                "    \"email\": \"sda97g@gmail.com\"," +
//                "    \"name\": \"Deck 1\"" +
//                "}";

//        String requestString = "{" +
//                "    \"entity\": \"deck\"," +
//                "    \"method\": \"getExpanded\"," +
//                "    \"email\": \"sda97g@gmail.com\"," +
//                "    \"name\": \"Deck 1\"" +
//                "}";

//        String requestString = "{" +
//                "    \"entity\": \"deck\"," +
//                "    \"method\": \"delete\"," +
//                "    \"email\": \"sda97g@gmail.com\"," +
//                "    \"name\": \"Deck 1\"" +
//                "}";

//        String requestString = "{" +
//        "    \"entity\": \"deck\"," +
//        "    \"method\": \"update\"," +
//        "    \"email\": \"sda97g@gmail.com\"," +
//        "    \"name\": \"Deck 1\"," +
//        "    \"properties\": {" +
//        "        \"name\": \"Deck 2\"," +
//        "        \"fromLanguage\": \"Norwegian\"," +
//        "        \"toLanguage\": \"Chinese\"" +
//        "    }" +
//        "}";

//        String requestString = "{" +
//        "    \"entity\": \"card\"," +
//        "    \"method\": \"save\"," +
//        "    \"email\": \"sda97g@gmail.com\"," +
//        "    \"card\": {" +
//        "        \"deck\": \"Deck 1\"," +
//        "        \"word\": \"Word 1\"," +
//        "        \"comment\": \"Comment\"," +
//        "        \"translation\": \"Translation\"," +
//        "        \"difficulty\": 17," +
//        "        \"hidden\": true" +
//        "    }" +
//        "}";

//        String requestString = "{" +
//                "    \"entity\": \"card\"" +
//                "    \"method\": \"get\"," +
//                "    \"email\": \"sda97g@gmail.com\"," +
//                "    \"deck\": \"Deck 1\"," +
//                "    \"word\": \"Word 1\"," +
//                "    \"comment\": \"Comment\"" +
//                "}";

//        String requestString = "{" +
//                "    \"entity\": \"card\"," +
//                "    \"method\": \"delete\"," +
//                "    \"email\": \"sda97g@gmail.com\"," +
//                "    \"deck\": \"Deck 1\"," +
//                "    \"word\": \"Word 1\"," +
//                "    \"comment\": \"Comment\"" +
//                "}";

//        String requestString = "{" +
//        "    \"entity\": \"card\"," +
//        "    \"method\": \"update\"," +
//        "    \"email\": \"sda97g@gmail.com\"," +
//        "    \"deck\": \"Deck 1\"," +
//        "    \"word\": \"Word 1\"," +
//        "    \"comment\": \"Comment\"," +
//        "    \"properties\": {" +
//        "        \"word\": \"Word 1\"," +
//        "        \"comment\": \"Comment\"," +
//        "        \"translation\": \"Translation\"," +
//        "        \"difficulty\": 17," +
//        "        \"hidden\": true" +
//        "    }" +
//        "}";

//        byte[] result = serviceExecutor.invoke(requestString);
//        System.out.println(new String(result));
    }
}
