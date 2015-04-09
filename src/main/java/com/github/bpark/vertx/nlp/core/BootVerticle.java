package com.github.bpark.vertx.nlp.core;

import com.github.bpark.vertx.nlp.core.sentence.SentenerVerticle;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class BootVerticle extends Verticle {

    private static final Class<?>[] verticles = {
            SentenerVerticle.class
    };

    private static final Class<?>[] workers = {
    };

    @Override
    public void start() {

        JsonObject appConfig = container.config();

        for (Class<?> verticle : verticles) {
            String verticleName = verticle.getName();
            container.logger().info("deploying " + verticleName);
            container.deployVerticle(
                    verticleName,
                    appConfig.getObject(verticleName));
        }

        for (Class<?> worker : workers) {
            String verticleName = worker.getName();
            container.logger().info("deploying " + verticleName);
            container.deployWorkerVerticle(
                    verticleName,
                    appConfig.getObject(verticleName));
        }

    }
}

