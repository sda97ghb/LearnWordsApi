package storage;

import com.google.gson.annotations.SerializedName;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

public class DTDump {
    private long dt;

    @SerializedName("decks")
    private List<DeckDTDump> deckDTDumpList;

    public DTDump(String email) {
        StorageUser user = StorageUserRepository.getByEmail(email);
        dt = user.getTimestamp();
        deckDTDumpList = user.getPersonalDecks().stream()
            .map(DeckDTDump::new)
            .collect(Collectors.toList());
    }

    static class DeckDTDump {
        String name;
        long dt;

        @SerializedName("cards")
        List<CardDTDump> cardDTDumpList;

        DeckDTDump(ObjectId id) {
            StorageDeck deck = StorageDeckRepository.getById(id);
            name = deck.getName();
            dt = deck.getTimestamp();
            cardDTDumpList = deck.getCards().stream()
                .map(CardDTDump::new)
                .collect(Collectors.toList());
        }
    }

    static class CardDTDump {
        String word;
        String comment;
        long dt;
        CardDTDump(ObjectId id) {
            StorageCard card = StorageCardRepository.getById(id);
            word = card.getWord();
            comment = card.getComment();
            dt = card.getTimestamp();
        }
    }
}
