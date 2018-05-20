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
    private static ServiceExecutor syncServiceExecutor = new ServiceExecutor(SyncService.class);

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

    @RequestMapping(path = "/syncapi/1.0", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    byte[] syncapi(@RequestBody String requestBody) {
        requestBody = URLDecoder.decode(requestBody);
        return syncServiceExecutor.invoke(requestBody);
    }

    public static void main(String... args) {
//        StorageDeck storageDeck = StorageDeckRepository.getByOwnerIdAndName(null, "TestDeck");
//        System.out.println(storageDeck);

//        StorageDeck storageDeck = new StorageDeck();
//        storageDeck.setTimestamp(null);//TimestampFactory.getTimestamp());
//        storageDeck.setName("TestDeck");
//        StorageDeckRepository.insert(storageDeck);

        SpringApplication.run(Main.class, args);
    }
}
