package ru.isu.model.enums;

public enum Type {
    XML, XSD, ZIP, SCH, OTHER, TXT;

    public String getName() {
        switch (this){
            case XML -> {
                return "xml";
            }
            case XSD -> {return "xsd";
            }
            case ZIP -> {return "zip";
            }
            case SCH -> {return "sch";
            }
            case TXT -> {return "txt";
            }
        }
        return "other";
    }
}
