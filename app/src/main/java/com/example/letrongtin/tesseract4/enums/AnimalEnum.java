package com.example.letrongtin.tesseract4.enums;

public enum AnimalEnum {

    BEAR("bear"),
    CAT("cat"),
    COW("cow"),
    DOG("dog"),
    ELEPHANT("elephant"),
    FERRET("ferret"),
    HIPPOPOTAMUS("hippopotamus"),
    HORSE("horse"),
    KOALA("koala"),
    LION("lion"),
    REINDEER("reindeer"),
    WOLVERINE("wolverine");

    public String name;

    AnimalEnum(String name) {
        this.name = name;
    }

    public static AnimalEnum getAnimalEnum(String name) {
        for (AnimalEnum e : AnimalEnum.values()) {
            if (e.name.equals(name))
                return e;
        }
        return null;
    }
}
