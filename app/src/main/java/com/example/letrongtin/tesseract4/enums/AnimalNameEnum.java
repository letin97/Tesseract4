package com.example.letrongtin.tesseract4.enums;

public enum AnimalNameEnum {

    ARMADILLO("armadillo"),
    BEAR("bear"),
    BEAVER("beaver"),
    BEE("bee"),
    BIRD("bird"),
    BISON("bison"),
    BUTTERFLY("butterfly"),
    CAMEL("camel"),
    CAT("cat"),
    CHICKEN("chicken"),
    COW("cow"),
    CRAB("crab"),
    CROCODILE("crocodile"),
    DEER("deer"),
    DINOSAUR("dinosaur"),
    DOG("dog"),
    DOLPHIN("dolphin"),
    DUCK("duck"),
    ELEPHANT("elephant"),
    FERRET("ferret"),
    FISH("fish"),
    FOX("fox"),
    FROG("frog"),
    GIBBON("gibbon"),
    GIRAFFE("giraffe"),
    GOAT("goat"),
    GOOSE("goose"),
    GULL("gull"),
    HAWK("hawk"),
    HIPPOPOTAMUS("hippopotamus"),
    HORSE("horse"),
    HYENA("hyena"),
    KANGAROO("kangaroo"),
    KINGFISHER("kingfisher"),
    KOALA("koala"),
    LAMB("lamb"),
    LION("lion"),
    LIZARD("lizard"),
    MAMMOTH("mammoth"),
    MANATEE("manatee"),
    MONKEY("monkey"),
    OTTER("otter"),
    PANDA("panda"),
    PARROT("parrot"),
    PEACOCK("peacock"),
    PENGUIN("penguin"),
    PIG("pig"),
    RABBIT("rabbit"),
    RACOON("racoon"),
    REINDEER("reindeer"),
    SEAHORSE("seahorse"),
    SEA_LION("sea lion"),
    SHARK("shark"),
    SHEEP("sheep"),
    SHRIMP("shrimp"),
    SNAIL("snail"),
    SNAKE("snake"),
    SQUIRREL("squirrel"),
    STORK("stork"),
    SWAN("swan"),
    TAPIR("tapir"),
    TIGER("tiger"),
    TURTLE("turtle"),
    MOUSE("mouse"),
    VULTURE("vulture"),
    WALRUS("walrus"),
    WHALE("whale"),
    WOLF("wolf"),
    WOLVERINE("wolverine");

    public String name;

    AnimalNameEnum(String name) {
        this.name = name;
    }

    public static AnimalNameEnum getAnimalName(String name) {
        for (AnimalNameEnum e : AnimalNameEnum.values()) {
            if (e.name.equals(name))
                return e;
        }
        return null;
    }
}
