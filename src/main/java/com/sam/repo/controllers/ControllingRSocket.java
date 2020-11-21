package com.sam.repo.controllers;

import com.sam.commons.entities.BigRequest;
import com.sam.repo.repositories.MenuItemRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.stream.Stream;

@Controller
@Log4j2
public class ControllingRSocket {

  private MenuItemRepository menuItemRepository;

  @Autowired
  public ControllingRSocket(MenuItemRepository menuRepository) {
    this.menuItemRepository = menuRepository;
  }

  @MessageMapping("startPing")
  Flux<String> startPing() {

    System.out.println("iniciamos ping");
    Flux<String> pingSignal =
        Flux.fromStream(Stream.generate(() -> "ping")).delayElements(Duration.ofMillis(1000));

    return pingSignal;
  }

  @MessageMapping("mongoChannel")
  Flux<BigRequest> channel(
      RSocketRequester clientRSocketConnection, Flux<BigRequest> bigRequestFlux) {
    System.out.println("arrived to mongo");
    return Flux.create(
        (FluxSink<BigRequest> sink) -> {
          bigRequestFlux
              .doOnNext(
                  i -> {
                    // System.out.println(i.getId());
                    sink.next(i);
                  })
              .subscribe();
        });
  }

  @MessageMapping("menuItemChannel")
  Flux<BigRequest> menuItemChannel(Flux<BigRequest> bigRequestFlux) {
    return Flux.empty();
  }
}
