package me.kristiyan.moviebot.utils;

import java.util.Random;

public class MovieProvider {
    private static final Random random = new Random();

    private static final Movie[] MOVIES = {
            // 0-9
            new Movie("28 Years Later", "WyHN26mTRPjhf7bKGS"),
            new Movie("5 Centimeters Per Second", "YXfY4UY4dd5sY"),

            // A
            new Movie("A Silent Voice", "MBmFnxyBRh2JJbB6Ye"),
            new Movie("Akira", "jsoMtBuP1Ahpu"),
            new Movie("Aladdin", "nC1toglzIfNFC"),
            new Movie("Aliens", "3ohzdYjwEQuR1J7dte"),
            new Movie("Amadeus", "mGj3SVN7xbPQ4"),
            new Movie("American Beauty", "l0HefWjn5ATRA2F0c"),
            new Movie("Angel's Egg", "qJkRbWM1MfVjq"),
            new Movie("Arrival", "l1J9wKizQOn7ifyow"),
            new Movie("Attack on Titan", "6my6QGKnRTp9S"),
            new Movie("Avatar", "AxhxIcTMEMqR2"),
            new Movie("Avatar: Fire and Ash", "2WHzt2u3S06PgD9SgD"),
            new Movie("Avatar: The Way of Water", "20WiPSNhWV8Qr0UGMn"),
            new Movie("Avengers: Doomsday", "wlOau1cjQyweM6OSII"),
            new Movie("Avengers: Endgame", "d87tLQV2oyarTYZeAk"),
            new Movie("Avengers: Infinity War", "xA7biwSXENswihcvFG"),

            // B
            new Movie("Baccano!", "NKKlvFNxzD8SQ"),
            new Movie("Back to the Future", "tEsPVINETtZpS"),
            new Movie("Barbie", "3SP9ATEG1KCJ1wUZdC"),
            new Movie("Beauty and the Beast", "xT1R9VVboMNVMZkPKw"),
            new Movie("Beetlejuice", "Rfeku3MGlfuPm"),
            new Movie("Before Sunrise", "ieQMk7UDYyRoc"),
            new Movie("Berserk", "ZBluts0FiVnvtyw7Om"),
            new Movie("Black Lagoon", "VsJbyI1T9ymQM"),
            new Movie("Black Panther", "o6FWop1Gbuyly"),
            new Movie("Black Swan", "3o7btWA2z2fnsP344w"),
            new Movie("Blade Runner", "8Dlx8S7TLVvz2"),
            new Movie("Blade Runner 2049", "rRcRhCabBel6xks10Z"),
            new Movie("Bleach", "XDLVGgAsMqBOVWl5Dg"),
            new Movie("Blue Lock", "jdeoZqveFFmmNTIxvf"),
            new Movie("Blue Submarine No. 6", "TKa7fQzChHylCQ89to"),
            new Movie("Blue Velvet", "oD3UxjIhisZuE"),
            new Movie("Bocchi the Rock!", "LHLBwKoo5LXnCCihan"),
            new Movie("Brokeback Mountain", "93lyEphbU2Sm4"),
            new Movie("Bubblegum Crisis", "jaOtWJr4cI2Pt1Tyv9"),

            // C
            new Movie("Captain America: Brave New World", "LF7iDRta6uK8IcIZFK"),
            new Movie("Captain Tsubasa", "tQlAjwnbmk59YYzSuK"),
            new Movie("Cardcaptor Sakura", "tX23iQaQYEcKI"),
            new Movie("Casino", "E2UlE5Of9zEjK"),
            new Movie("Castle in the Sky", "4ZeHO6xwFpOq9tNlTO"),
            new Movie("Chainsaw Man", "BMdxhHMfD4S7mwL3XK"),
            new Movie("Challengers", "V9i1EWijoNwkJchfDn"),
            new Movie("City Hunter", "yGMKRainOG7zemvomB"),
            new Movie("City of God", "k2aU12mKl3lox2yQGK"),
            new Movie("Civil War", "z4THDw2us3aCY"),
            new Movie("Clannad: After Story", "fARFPMuspJRx6"),
            new Movie("Claymore", "ZxL2arcSrEOiYuVhBf"),
            new Movie("Coco", "l2YWwAjBwZdjxIXBe"),
            new Movie("Code Geass: Lelouch of the Rebellion", "aQFiKGhqerg2Y"),
            new Movie("Coming to America", "2WBNMTzB6uB1u"),
            new Movie("Cowboy Bebop", "MBUku5f2PeXFoNN7O8"),
            new Movie("Cyberpunk: Edgerunners", "TMQhGn533KiUW5Q0vD"),

            // D
            new Movie("Dandadan", "MD1QDeDxnELtBpIiqd"),
            new Movie("Darker Than Black", "ZvLzYozpSRoCJHv5Ne"),
            new Movie("Dead Poets Society", "HFyBNSSlNwNKE"),
            new Movie("Deadpool", "tJdRK8yEXmhUhzfDXy"),
            new Movie("Deadpool & Wolverine", "J30zgOl5GxzMCKgqfl"),
            new Movie("Death Note", "26Ff89l2hGLYmVV5e"),
            new Movie("Delicious in Dungeon", "zRW20g1KJpzQg8rbcE"),
            new Movie("Demon Slayer", "2ya7xLyEeynlZM4FCw"),
            new Movie("Devilman Crybaby", "ygAmzS6qTOkzxG0VsB"),
            new Movie("Die Hard", "CxwvAGYoBTGYU"),
            new Movie("Digimon Adventure", "UOvFfXGINpmN2"),
            new Movie("District 9", "KwNRL1qNvx50A"),
            new Movie("Django Unchained", "9K0k6XktLOhLq"),
            new Movie("Do the Right Thing", "nnZGGqJDtrfCU"),
            new Movie("Doctor Strange", "9Y5oA62nZC19BWFgTs"),
            new Movie("Donnie Darko", "5mUYcIziYkaWI"),
            new Movie("Dragon Ball", "JILOx1KJggOGI"),
            new Movie("Dragon Ball Daima", "mfgWMmIbTx0NHSqs4F"),
            new Movie("Dragon Ball Z", "VUDWelveo5VGo"),
            new Movie("Drive", "grBPQ4hwmsVyZZ51O0"),
            new Movie("Dune", "AkwcbzEPIfZ48i44kx"),
            new Movie("Dune: Part One", "ymfCaSMMv4FNorPHQ6"),
            new Movie("Dune: Part Two", "buBYK5rVXe6lp7UCni"),

            // E
            new Movie("E.T. the Extra-Terrestrial", "1049KhpGddOJnq"),
            new Movie("Elvis", "mXBNQ3VzftXji"),
            new Movie("Erased", "iTZXNodtsUSdqOkyCi"),
            new Movie("Eternal Sunshine of the Spotless Mind", "SIIXDrZ8Rh2a4"),
            new Movie("Eureka Seven", "2fTMitkrMVaPC"),
            new Movie("Evangelion: 3.0+1.0 Thrice Upon a Time", "13iujv9Z0k3qGP6vjD"),
            new Movie("Everything Everywhere All at Once", "TejmLnMKgnmPInMQjV"),
            new Movie("Ex Machina", "19OIUml06udBC"),
            new Movie("Eyes Wide Shut", "cAHfy7VD1uHdK"),

            // F
            new Movie("FLCL", "9dhgKatYHfEuA"),
            new Movie("Fargo", "e5RclbFtYbZmM"),
            new Movie("Fate/Zero", "nSB6hxGgQcIzng0695"),
            new Movie("Fate/strange Fake", "Zb7KmSvYgaGJkuK1PV"),
            new Movie("Ferris Bueller's Day Off", "9IZKPmNdZ7juU"),
            new Movie("Fight Club", "xTiTnnq6SknvxN15Is"),
            new Movie("Finding Nemo", "107gPMgIBBleA8"),
            new Movie("Fire Force Season 3", "yr7n0u3qzO9nG"),
            new Movie("Fist of the North Star", "4SBtIAp4sEDxC"),
            new Movie("Forrest Gump", "KEXxJgir4BxFZ7bYsC"),
            new Movie("Frieren: Beyond Journey's End", "s10xE2pqXO9ntqjpAC"),
            new Movie("Frozen", "OmoKW1gK6ULg4"),
            new Movie("Full Metal Jacket", "AyjFDmMgS7OkU"),
            new Movie("Fullmetal Alchemist: Brotherhood", "12bE9SHGZYR85O"),
            new Movie("Furiosa: A Mad Max Saga", "kJ6WA4OS0AUEABqpG8"),

            // G
            new Movie("Gachiakuta", "ncg8YBWqAoX6c3oJ1g"),
            new Movie("Get Out", "xUA7bgONYM1FrC7Vra"),
            new Movie("Ghost in the Shell", "imKrSqrDxAuvS"),
            new Movie("Ghost in the Shell (2026)", "BO7p37KdlUaXA5nhWO"),
            new Movie("Ghost in the Shell: Stand Alone Complex", "rvqCRPWCgctry"),
            new Movie("Ghostbusters", "l3q2HS9FG81YSdkB2"),
            new Movie("Gintama", "1BiWRqQSCNISA"),
            new Movie("Gladiator", "d7mMzaGDYkz4ZBziP6"),
            new Movie("Godzilla Minus One", "IEI6CCSyavr7db90Ko"),
            new Movie("Gone Girl", "ToMjGpNUyxffBJHqrn2"),
            new Movie("Good Will Hunting", "lHZO8Bnfd9PSU"),
            new Movie("Goodfellas", "3o85xzYIHuDl00se3K"),
            new Movie("Grave of the Fireflies", "AKtV37treIorS"),
            new Movie("Great Teacher Onizuka", "Df1Qh8Ed1FKpi"),
            new Movie("Guardians of the Galaxy", "rne3oSd7DsBDa"),
            new Movie("Gunbuster", "l3q2tzon8OCC7BqmY"),
            new Movie("Gurren Lagann", "yYwzZAV5byR2FBYm5G"),

            // H
            new Movie("Haikyu!!", "iQPwaUfgipTJC"),
            new Movie("Harry Potter", "6RIEW15CCWRvq"),
            new Movie("Heat", "pQLmrZN8r1p0Q"),
            new Movie("Hell's Paradise", "OIBjJZHpzbcR2"),
            new Movie("Hellsing Ultimate", "vkG6V6ZoLWaPu"),
            new Movie("Her", "iPa2ff1CV9YnS"),
            new Movie("Honey Lemon Soda", "Tfpsxdv5pNXYgM5mJN"),
            new Movie("Howl's Moving Castle", "DZy8ohXa7cVt6"),
            new Movie("Hunter x Hunter (2011)", "nbB1CV1fazlGo"),

            // I
            new Movie("Inception", "9UqRcQHzBou6A"),
            new Movie("Independence Day", "40l0nfT7Wu8bC"),
            new Movie("Inglourious Basterds", "eiwauFJGF0hb2"),
            new Movie("Initial D", "qYUfBeJXRxBu0"),
            new Movie("Inside Out", "pKYb293zuoziUpRwCZ"),
            new Movie("Inside Out 2", "pKYb293zuoziUpRwCZ"),
            new Movie("Interstellar", "UCSIkkFXSeg3hugqGf"),
            new Movie("Iron Man", "lySzu7q4P49u8"),

            // J
            new Movie("Jin-Roh: The Wolf Brigade", "439vHFJdv468hmEJ6m"),
            new Movie("JoJo's Bizarre Adventure", "jseFcU9wQaezu"),
            new Movie("JoJo's Bizarre Adventure: Steel Ball Run", "JA96AEVeYAp7RGmjbM"),
            new Movie("Joker", "cEYFeE1QgHWH2YADVHG"),
            new Movie("Jujutsu Kaisen", "PzmwZmvqZA191oqu3L"),
            new Movie("Jurassic Park", "37Fsl1eFxbhtu"),
            new Movie("Jurassic World Rebirth", "kW3AemgUMjN4vg4jrU"),

            // K
            new Movie("Kaiju No. 8", "TI0LPr2g3DQzUMGdyg"),
            new Movie("Kiki's Delivery Service", "KXECBV0GkdCX6"),
            new Movie("Kill Bill: Vol. 1", "oL72C4EQIUhbiO6Pe9"),
            new Movie("Kill la Kill", "37WzogMvUjAaiRXtMA"),
            new Movie("Killers of the Flower Moon", "R9f53DwJW0KJbgCc6o"),
            new Movie("Knives Out", "YAyWp0RxiRrtYVUmR7"),

            // L
            new Movie("La La Land", "xUySTxVTWeEtJhEzsY"),
            new Movie("Lady Bird", "xUySTL4J5ld87vxbSU"),
            new Movie("Land of the Lustrous", "jSPIhce4bOyfHO13bC"),
            new Movie("Lazarus", "4lFfBjcSdZBs4LDmcz"),
            new Movie("Legend of the Galactic Heroes", "mVyymZhha6TTakKeLz"),
            new Movie("Logan", "3og0IK2QUzzpkx4EOk"),
            new Movie("Look Back", "F3UEuXNMvRHRuVdgvw"),
            new Movie("Lord of the Rings", "VqleJG2rBop7W"),
            new Movie("Lost in Translation", "YrMrSUfeh5do2FISt8"),

            // M
            new Movie("Macross", "r1UKKfxA5riJq"),
            new Movie("Mad Max: Fury Road", "Invo00LrPY49i"),
            new Movie("Made in Abyss", "1AhRIrcUscjJC05LF2"),
            new Movie("Magnolia", "glZz9xgJKbPEs"),
            new Movie("Memento", "3o7TKtg43o0ue8VI7C"),
            new Movie("Men in Black", "R7m04yMaGWVeE"),
            new Movie("Mickey 17", "osju8BpQhWtnRSIu26"),
            new Movie("Mob Psycho 100", "mlR0QdwsJUDdK"),
            new Movie("Mobile Suit Gundam Wing", "oVkvNRT0UPRfJTgZv5"),
            new Movie("Monster", "X7jjWeC03QDT2"),
            new Movie("Moonlight", "FGp6Y5pXh0GldPTPw2"),
            new Movie("Mushoku Tensei: Jobless Reincarnation", "aIoiSCDS5hoDKjAvdd"),

            // N
            new Movie("Neon Genesis Evangelion", "g9W63AVGOHFZaXPC6l"),
            new Movie("No Country for Old Men", "dpk3BuKZX2s8w"),
            new Movie("No Game No Life", "6xTXaszCHgMcE"),
            new Movie("No Time to Die", "cOWEydgKh96VAOrVOO"),

            // O
            new Movie("Once Upon a Time in Hollywood", "l1IkSD6mjNVcWQhUwM"),
            new Movie("Oppenheimer", "OstuiXBi4xrMMbTefI"),
            new Movie("Orb: On the Movements of the Earth", "nQJLupPZ8NGdk4r4Kd"),
            new Movie("Ouran High School Host Club", "DeJqH1Q76fjtC"),

            // P
            new Movie("Pan's Labyrinth", "Lgb8AJw71yFIk"),
            new Movie("Parasite", "RJnPjtME1TryN84Dvq"),
            new Movie("Perfect Blue", "izgCxKfI2PnbO"),
            new Movie("Poor Things", "l3diNGwou8xtS7kAM"),
            new Movie("Portrait of a Lady on Fire", "ZbC101txjDJcCcvphp"),
            new Movie("Princess Mononoke", "AoEDhmdgjUws8"),
            new Movie("Psycho-Pass", "12KtPizNOpr476"),
            new Movie("Pulp Fiction", "112zsGNCKs1NpS"),

            // R
            new Movie("Raiders of the Lost Ark", "9J8gnvAxmDFbG"),
            new Movie("Rain Man", "FFFGVpPUyQSGY"),
            new Movie("Ranking of Kings", "8N8ulyFMLC4aa6pbqV"),
            new Movie("Ratatouille", "cJeMxgn9kUIG4"),

            // S
            new Movie("Saving Private Ryan", "JJTkDaJyhs6GPdpRtw"),
            new Movie("Scarface", "sdarezLLwzMkw"),
            new Movie("Schindler's List", "dBT7mACpkW8Fy"),
            new Movie("Se7en", "9XcD7tH2uKBRC"),
            new Movie("Shaun of the Dead", "LZlwYWkMfj5n2"),
            new Movie("Shrek", "seQilU1T7vDva"),
            new Movie("Shrek 5", "QTrG6mjkHEkpFR3DqX"),
            new Movie("Sinners", "9SRD7KPmISCg8odrFQ"),
            new Movie("Slumdog Millionaire", "l1BgR70bd7vCeNUpW"),
            new Movie("Soul", "o2ITDLRkP2oGk"),
            new Movie("Spider-Man", "1qErVv5GVUac8uqBJU"),
            new Movie("Spider-Man: Across the Spider-Verse", "7whm31rMz0t9yB42Tp"),
            new Movie("Spider-Man: Into the Spider-Verse", "1qErVv5GVUac8uqBJU"),
            new Movie("Spirited Away", "6mvRz2t6GPugGTNxLb"),
            new Movie("Stand by Me", "tOCNL4dFV9ndm"),
            new Movie("Star Wars", "BbMnUoWV7mR4k"),
            new Movie("Superbad", "4MVV0NgZGdTt6"),
            new Movie("Supergirl: Woman of Tomorrow", "xBzeaASC01dS0"),
            new Movie("Superman", "FiBzv5FRE85PO"),

            // T
            new Movie("Tenet", "l10bV0Vt2MiyucTMjZ"),
            new Movie("Terminator 2: Judgment Day", "3o72FhJwcj35U5nLVu"),
            new Movie("The Avengers", "PpeGK6edBWAxi"),
            new Movie("The Batman", "l3q2tzon8OCC7BqmY"),
            new Movie("The Batman Part II", "l3q2tzon8OCC7BqmY"),
            new Movie("The Big Lebowski", "TzyQtxScgR5p6"),
            new Movie("The Breakfast Club", "4spXzGPzlvA76"),
            new Movie("The Dark Knight", "I8SQMuIELiw0w"),
            new Movie("The Departed", "11zIWScNDCLjmo"),
            new Movie("The Empire Strikes Back", "10Ovjb6PnK1L2g"),
            new Movie("The Fantastic Four: First Steps", "GZeNDm5ofqPIN8Ifk3"),
            new Movie("The Godfather", "xrvCI5ykhg9QQ"),
            new Movie("The Grand Budapest Hotel", "3oz8xzIL77ybFvYvAs"),
            new Movie("The Green Mile", "cHkA7tJZoWLyU"),
            new Movie("The Holdovers", "ErJynOCv4ymiLPnSH9"),
            new Movie("The Incredibles", "FBgN017wtq1YQ"),
            new Movie("The Lion King", "ozKW2C1vkgMJW"),
            new Movie("The Mandalorian & Grogu", "Fq3KoTpCXMfL83q893"),
            new Movie("The Martian", "vVMMktHyrZuIo"),
            new Movie("The Matrix", "BdghqxNFV4efm"),
            new Movie("The Prestige", "l0MYsxhGGJGxLqXxm"),
            new Movie("The Princess Bride", "hWjyKDWBopz9K"),
            new Movie("The Shawshank Redemption", "MxseFhLGXxWyQ"),
            new Movie("The Shining", "5yeQRdiYrDq2A"),
            new Movie("The Silence of the Lambs", "O5Bcu5l71uhMs"),
            new Movie("The Sixth Sense", "DvtsYOKrqPZg4"),
            new Movie("The Social Network", "RkCiLT8fUW9vMPXA5J"),
            new Movie("The Terminator", "RfEbMBTPQ7MOY"),
            new Movie("The Truman Show", "VGHtpsS3UJNRK"),
            new Movie("The Wolf of Wall Street", "CwM9k8RChLqqQ"),
            new Movie("There Will Be Blood", "IwYXRW8IXRftS"),
            new Movie("Thor Ragnarok", "l4FGni1RBAR2OWsGk"),
            new Movie("Thunderbolts*", "J61A0tTNf3CTtO8CSY"),
            new Movie("Titanic", "mh7DKz8UhQQGk"),
            new Movie("Top Gun", "TL5XQpSpASo4U"),
            new Movie("Top Gun: Maverick", "dcFSeawnz3edbm9n7V"),
            new Movie("Toy Story", "FtBw4gIVV6fTO"),
            new Movie("Toy Story 5", "GSE4nkc9I3ZmM"),
            new Movie("Trainspotting", "R9E0NRcw1tfJS"),
            new Movie("Tron: Ares", "ZIfywuoNm39lGKOlVq"),

            // U
            new Movie("Up", "xxUzMv2i3tze8"),

            // V
            new Movie("V for Vendetta", "12rJ98B2Fj8iVq"),

            // W
            new Movie("WALL-E", "8FNXce4OdeJxK"),
            new Movie("When Harry Met Sally", "e39tpB7xOf6h2"),
            new Movie("Whiplash", "lUMWQ5FZLM2cDg26Uq"),
            new Movie("Who Framed Roger Rabbit", "iPa2ff1CV9YnS"),
            new Movie("Wonder Woman", "pLnxbpVosgjE4"),

            // Z
            new Movie("Zootopia 2", "iHJEu41kg6r71Gt4pc"),
    };

    public static Movie getRandomMovie() {
        return MOVIES[random.nextInt(MOVIES.length)];
    }

    public static class Movie {
        public final String title;
        public final String gifUrl;

        // Constructor now takes the ID and builds the robust URL automatically
        public Movie(String title, String giphyId) {
            this.title = title;
            // The "i.giphy.com" format is the most reliable for Discord embeds
            this.gifUrl = "https://i.giphy.com/" + giphyId + ".gif";
        }
    }
}