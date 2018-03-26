package storage;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class StorageDeckCodec implements Codec {
    @Override
    public Object decode(BsonReader bsonReader, DecoderContext decoderContext) {
        return Database.getGson().fromJson(new DocumentCodec().decode(bsonReader, decoderContext).toJson(), StorageDeck.class);
    }

    @Override
    public void encode(BsonWriter bsonWriter, Object o, EncoderContext encoderContext) {
        new DocumentCodec().encode(bsonWriter, Document.parse(Database.getGson().toJson(o)), encoderContext);
    }

    @Override
    public Class getEncoderClass() {
        return StorageDeck.class;
    }
}
