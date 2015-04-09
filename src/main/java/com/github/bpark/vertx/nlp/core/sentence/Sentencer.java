package com.github.bpark.vertx.nlp.core.sentence;

import com.github.bpark.vertx.nlp.core.ComConstants;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class Sentencer {

    @Inject
    private Logger logger;

    @Inject
    private EventBus eventBus;


    public void start() {
        try {
            final SentenceDetectorME detector = createDetector();
            logger.info("sentence detector created");
            final Tokenizer tokenizer = createTokenizer();
            logger.info("tokenizer created");
            final POSTaggerME posTagger = createPosTagger();
            logger.info("postagger created");

            eventBus.registerHandler(ComConstants.SENTENCE_CHANNEL, (Message<JsonObject> busmessage) -> {
                String text = busmessage.body().getString("text");
                List<String> sentences = Arrays.asList(detector.sentDetect(text));
                JsonArray sentenceArray = new JsonArray();
                sentences.forEach(sentence -> {
                    String[] tokenArray = tokenizer.tokenize(sentence);
                    List<String> tokens = Arrays.asList(tokenArray);
                    tokens.forEach(System.out::println);
                    List<String> tags = Arrays.asList(posTagger.tag(tokenArray));
                    tags.forEach(System.out::println);
                    sentenceArray.add(createSentence(tokens, tags));
                });

                JsonObject sendMessage = new JsonObject();
                sendMessage.putArray("sentences", sentenceArray);
                eventBus.send(ComConstants.TOKEN_CHANNEL, sendMessage);
            });
        } catch (IOException e) {
            logger.error("NLP Verticle inactive");
        }
    }

    private JsonObject createSentence(List<String> tokens, List<String> tags) {
        JsonObject sentence = new JsonObject();
        sentence.putArray("tokens", createStringArray(tokens));
        sentence.putArray("tags", createStringArray(tags));
        return sentence;
    }

    private JsonArray createStringArray(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    public void stop() {

    }

    private SentenceDetectorME createDetector() throws IOException {
        try (InputStream modelIn = Sentencer.class.getResourceAsStream("/opennlp/de-sent.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        }  catch (IOException e) {
            logger.error("sentence detector not created, reason: " + e.getMessage());
            throw e;
        }
    }

    public Tokenizer createTokenizer() throws IOException {

        try (InputStream modelIn = Sentencer.class.getResourceAsStream("/opennlp/de-token.bin")){
            TokenizerModel model = new TokenizerModel(modelIn);
            return new TokenizerME(model);
        } catch (IOException e) {
            logger.error("tokenizer  not created, reason: " + e.getMessage());
            throw e;
        }
    }

    public POSTaggerME createPosTagger() throws IOException {
        try(InputStream modelIn = Sentencer.class.getResourceAsStream("/opennlp/de-pos-maxent.bin")) {
            POSModel model = new POSModel(modelIn);
            return new POSTaggerME(model);
        }
        catch (IOException e) {
            logger.error("postagger not created, reason: " + e.getMessage());
            throw e;
        }
    }
}
