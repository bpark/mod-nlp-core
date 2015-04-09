package com.github.bpark.vertx.nlp.core.sentence;

import com.github.bpark.vertx.pico.ApplicationContext;
import org.vertx.java.platform.Verticle;

import javax.inject.Inject;

public class SentenerVerticle extends Verticle {

    private Sentencer sentencer;

    @Override
    public void start() {
        super.start();

        ApplicationContext applicationContext = ApplicationContext.create()
                .withContainer(container)
                .withVertx(vertx)
                .withInjectAnnotation(Inject.class)
                .withClass(Sentencer.class)
                .build();

        sentencer = applicationContext.getComponent(Sentencer.class);
        sentencer.start();

        container.logger().info("SentenerVerticle started");
    }

    @Override
    public void stop() {
        super.stop();
        sentencer.stop();

        container.logger().info("SentenerVerticle stopped");
    }
}
