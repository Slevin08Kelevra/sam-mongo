package com.sam.repo.controllers;

import com.github.javafaker.Faker;
import com.sam.commons.entities.GroupsDTO;
import com.sam.commons.entities.MenuItemDTO;
import com.sam.commons.entities.PriceDTO;
import com.sam.repo.entities.MenuItem;
import com.sam.repo.repositories.MenuItemRepository;
import com.sam.repo.webClients.MongoRepoClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/menuItems")
public class MenuItemController {

  Converter<String, String> itemTypeCodeConverter =
      new Converter<String, String>() {
        public String convert(MappingContext<String, String> context) {
          return context.getSource().toUpperCase();
        }
      };
  PropertyMap<MenuItem, MenuItemDTO> skipModifiedFieldsMap =
      new PropertyMap<>() {
        protected void configure() {
          using(itemTypeCodeConverter).map(source.getTitle()).setTitle(null);
          // map().setDescription(source.getTitle());
          // map().setTitle(source.getDescription());
        }
      };
  private MenuItemRepository menuItemRepository;
  private MongoRepoClient mongoRepoClient;
  private ModelMapper modelMapper;

  @Autowired
  public MenuItemController(MenuItemRepository menuRepository, MongoRepoClient mongoRepoClient) {
    this.menuItemRepository = menuRepository;
    modelMapper = new ModelMapper();
    // modelMapper.addMappings(skipModifiedFieldsMap);
    modelMapper.addMappings(skipModifiedFieldsMap);
    this.mongoRepoClient = mongoRepoClient;
  }

  /*@GetMapping(produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
  Flux<List<MenuItem>> getAll() {
    return this.menuItemRepository
        .findAll()
        .buffer(3)
        .delayElements(Duration.ofSeconds(1))
        ;
  }*/

  @GetMapping()
  Flux<MenuItem> getAll() {
    return this.menuItemRepository.findAll();
  }

  @GetMapping("testFlux")
  String test() {
    mongoRepoClient.getMenuItemList();
    return "done!";
  }

  @GetMapping("/{id}")
  Mono<MenuItemDTO> get(@PathVariable("id") String id) {

    Mono<MenuItem> menuItemMono = this.menuItemRepository.findById(id);
    return menuItemMono.map(
        menuItem -> {
          MenuItemDTO menuItemDTO = modelMapper.map(menuItem, MenuItemDTO.class);
          return menuItemDTO;
        });
  }

  @GetMapping("/fakeTest")
  MenuItemDTO get() {

    Faker faker = new Faker(new Locale("ES"));
    BigDecimal bd = new BigDecimal(faker.number().randomDouble(3, 1, 300));
    bd = bd.setScale(3, RoundingMode.CEILING);
    MenuItemDTO menuItemDTO =
        MenuItemDTO.builder()
            .restaurantId(UUID.randomUUID().toString())
            .title(faker.food().dish())
            .languageId(UUID.randomUUID().toString())
            .groups(
                List.of(
                    GroupsDTO.builder()
                        .id(UUID.randomUUID().toString())
                        .title(faker.name().firstName())
                        .description(faker.hobbit().quote())
                        .parentId(UUID.randomUUID().toString())
                        .build()))
            .description(faker.gameOfThrones().quote())
            .prices(
                List.of(
                    PriceDTO.builder()
                        .title(faker.name().lastName())
                        .description(faker.backToTheFuture().quote())
                        .amount(bd)
                        .build()))
            .build();

    return menuItemDTO;
  }

  @PostMapping()
  public Mono<Void> add(@RequestBody Mono<MenuItem> menuItemMono) {
    return this.menuItemRepository.insert(menuItemMono).then();
  }

  @PutMapping()
  public Mono<Void> update(@RequestBody Mono<MenuItem> menuItemMono) {
    return this.menuItemRepository.saveAll(menuItemMono).then();
  }

  @DeleteMapping("/{id}")
  public Mono<Void> delete(@PathVariable("id") String id) {
    return this.menuItemRepository.deleteById(id);
  }

  @DeleteMapping("/all/delete")
  public Mono<Void> deleteAll() {
    return this.menuItemRepository.deleteAll();
  }

  @GetMapping("/all/count")
  public Mono<Long> count() {
    return this.menuItemRepository.count();
  }
}
