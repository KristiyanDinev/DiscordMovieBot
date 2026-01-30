-- =========================================================
-- Database & User Setup
-- =========================================================

-- Create database (idempotent)
CREATE DATABASE moviebot_db;

-- Create application user (idempotent)
DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_roles WHERE rolname = '...'
        ) THEN
            CREATE USER moviebot_user WITH PASSWORD '..';
        END IF;
    END
$$;

-- Allow user to connect
GRANT CONNECT ON DATABASE moviebot_db TO moviebot_user;

-- =========================================================
-- Switch to application database
-- =========================================================
\c moviebot_db

-- =========================================================
-- Schema permissions
-- =========================================================
GRANT USAGE, CREATE ON SCHEMA public TO moviebot_user;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =========================================================
-- Tables
-- =========================================================

CREATE TABLE IF NOT EXISTS movies (
                                      id        BIGSERIAL PRIMARY KEY,
                                      title     TEXT NOT NULL,
                                      gif_url   TEXT NOT NULL,
                                      created_at TIMESTAMPTZ DEFAULT now(),

                                      CONSTRAINT movies_title_unique UNIQUE (title)
);

-- =========================================================
-- Privileges
-- =========================================================

-- Existing objects
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO moviebot_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO moviebot_user;

-- Future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON TABLES TO moviebot_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON SEQUENCES TO moviebot_user;

-- =========================================================
-- Seed Data
-- =========================================================

BEGIN;
-- INSERT ...

INSERT INTO movies (title, gif_url) VALUES
                                        ('28 Years Later', 'https://i.giphy.com/WyHN26mTRPjhf7bKGS.gif'),
                                        ('5 Centimeters Per Second', 'https://i.giphy.com/YXfY4UY4dd5sY.gif'),
                                        ('A Silent Voice', 'https://i.giphy.com/MBmFnxyBRh2JJbB6Ye.gif'),
                                        ('Akira', 'https://i.giphy.com/jsoMtBuP1Ahpu.gif'),
                                        ('Aladdin', 'https://i.giphy.com/nC1toglzIfNFC.gif'),
                                        ('Aliens', 'https://i.giphy.com/3ohzdYjwEQuR1J7dte.gif'),
                                        ('Amadeus', 'https://i.giphy.com/mGj3SVN7xbPQ4.gif'),
                                        ('American Beauty', 'https://i.giphy.com/l0HefWjn5ATRA2F0c.gif'),
                                        ('Angel''s Egg', 'https://i.giphy.com/qJkRbWM1MfVjq.gif'),
                                        ('Arrival', 'https://i.giphy.com/l1J9wKizQOn7ifyow.gif'),
                                        ('Attack on Titan', 'https://i.giphy.com/6my6QGKnRTp9S.gif'),
                                        ('Avatar', 'https://i.giphy.com/AxhxIcTMEMqR2.gif'),
                                        ('Avatar: Fire and Ash', 'https://i.giphy.com/2WHzt2u3S06PgD9SgD.gif'),
                                        ('Avatar: The Way of Water', 'https://i.giphy.com/20WiPSNhWV8Qr0UGMn.gif'),
                                        ('Avengers: Doomsday', 'https://i.giphy.com/wlOau1cjQyweM6OSII.gif'),
                                        ('Avengers: Endgame', 'https://i.giphy.com/d87tLQV2oyarTYZeAk.gif'),
                                        ('Avengers: Infinity War', 'https://i.giphy.com/xA7biwSXENswihcvFG.gif'),
                                        ('Baccano!', 'https://i.giphy.com/NKKlvFNxzD8SQ.gif'),
                                        ('Back to the Future', 'https://i.giphy.com/tEsPVINETtZpS.gif'),
                                        ('Barbie', 'https://i.giphy.com/3SP9ATEG1KCJ1wUZdC.gif'),
                                        ('Beauty and the Beast', 'https://i.giphy.com/xT1R9VVboMNVMZkPKw.gif'),
                                        ('Beetlejuice', 'https://i.giphy.com/Rfeku3MGlfuPm.gif'),
                                        ('Before Sunrise', 'https://i.giphy.com/ieQMk7UDYyRoc.gif'),
                                        ('Berserk', 'https://i.giphy.com/ZBluts0FiVnvtyw7Om.gif'),
                                        ('Black Lagoon', 'https://i.giphy.com/VsJbyI1T9ymQM.gif'),
                                        ('Black Panther', 'https://i.giphy.com/o6FWop1Gbuyly.gif'),
                                        ('Black Swan', 'https://i.giphy.com/3o7btWA2z2fnsP344w.gif'),
                                        ('Blade Runner', 'https://i.giphy.com/8Dlx8S7TLVvz2.gif'),
                                        ('Blade Runner 2049', 'https://i.giphy.com/rRcRhCabBel6xks10Z.gif'),
                                        ('Bleach', 'https://i.giphy.com/XDLVGgAsMqBOVWl5Dg.gif'),
                                        ('Blue Lock', 'https://i.giphy.com/jdeoZqveFFmmNTIxvf.gif'),
                                        ('Blue Submarine No. 6', 'https://i.giphy.com/TKa7fQzChHylCQ89to.gif'),
                                        ('Blue Velvet', 'https://i.giphy.com/oD3UxjIhisZuE.gif'),
                                        ('Bocchi the Rock!', 'https://i.giphy.com/LHLBwKoo5LXnCCihan.gif'),
                                        ('Brokeback Mountain', 'https://i.giphy.com/93lyEphbU2Sm4.gif'),
                                        ('Bubblegum Crisis', 'https://i.giphy.com/jaOtWJr4cI2Pt1Tyv9.gif'),
                                        ('Captain America: Brave New World', 'https://i.giphy.com/LF7iDRta6uK8IcIZFK.gif'),
                                        ('Captain Tsubasa', 'https://i.giphy.com/tQlAjwnbmk59YYzSuK.gif'),
                                        ('Cardcaptor Sakura', 'https://i.giphy.com/tX23iQaQYEcKI.gif'),
                                        ('Casino', 'https://i.giphy.com/E2UlE5Of9zEjK.gif'),
                                        ('Castle in the Sky', 'https://i.giphy.com/4ZeHO6xwFpOq9tNlTO.gif'),
                                        ('Chainsaw Man', 'https://i.giphy.com/BMdxhHMfD4S7mwL3XK.gif'),
                                        ('Challengers', 'https://i.giphy.com/V9i1EWijoNwkJchfDn.gif'),
                                        ('City Hunter', 'https://i.giphy.com/yGMKRainOG7zemvomB.gif'),
                                        ('City of God', 'https://i.giphy.com/k2aU12mKl3lox2yQGK.gif'),
                                        ('Civil War', 'https://i.giphy.com/z4THDw2us3aCY.gif'),
                                        ('Clannad: After Story', 'https://i.giphy.com/fARFPMuspJRx6.gif'),
                                        ('Claymore', 'https://i.giphy.com/ZxL2arcSrEOiYuVhBf.gif'),
                                        ('Coco', 'https://i.giphy.com/l2YWwAjBwZdjxIXBe.gif'),
                                        ('Code Geass: Lelouch of the Rebellion', 'https://i.giphy.com/aQFiKGhqerg2Y.gif'),
                                        ('Coming to America', 'https://i.giphy.com/2WBNMTzB6uB1u.gif'),
                                        ('Cowboy Bebop', 'https://i.giphy.com/MBUku5f2PeXFoNN7O8.gif'),
                                        ('Cyberpunk: Edgerunners', 'https://i.giphy.com/TMQhGn533KiUW5Q0vD.gif'),
                                        ('Dandadan', 'https://i.giphy.com/MD1QDeDxnELtBpIiqd.gif'),
                                        ('Darker Than Black', 'https://i.giphy.com/ZvLzYozpSRoCJHv5Ne.gif'),
                                        ('Dead Poets Society', 'https://i.giphy.com/HFyBNSSlNwNKE.gif'),
                                        ('Deadpool', 'https://i.giphy.com/tJdRK8yEXmhUhzfDXy.gif'),
                                        ('Deadpool & Wolverine', 'https://i.giphy.com/J30zgOl5GxzMCKgqfl.gif'),
                                        ('Death Note', 'https://i.giphy.com/26Ff89l2hGLYmVV5e.gif'),
                                        ('Delicious in Dungeon', 'https://i.giphy.com/zRW20g1KJpzQg8rbcE.gif'),
                                        ('Demon Slayer', 'https://i.giphy.com/2ya7xLyEeynlZM4FCw.gif'),
                                        ('Devilman Crybaby', 'https://i.giphy.com/ygAmzS6qTOkzxG0VsB.gif'),
                                        ('Die Hard', 'https://i.giphy.com/CxwvAGYoBTGYU.gif'),
                                        ('Digimon Adventure', 'https://i.giphy.com/UOvFfXGINpmN2.gif'),
                                        ('District 9', 'https://i.giphy.com/KwNRL1qNvx50A.gif'),
                                        ('Django Unchained', 'https://i.giphy.com/9K0k6XktLOhLq.gif'),
                                        ('Do the Right Thing', 'https://i.giphy.com/nnZGGqJDtrfCU.gif'),
                                        ('Doctor Strange', 'https://i.giphy.com/9Y5oA62nZC19BWFgTs.gif'),
                                        ('Donnie Darko', 'https://i.giphy.com/5mUYcIziYkaWI.gif'),
                                        ('Dragon Ball', 'https://i.giphy.com/JILOx1KJggOGI.gif'),
                                        ('Dragon Ball Daima', 'https://i.giphy.com/mfgWMmIbTx0NHSqs4F.gif'),
                                        ('Dragon Ball Z', 'https://i.giphy.com/VUDWelveo5VGo.gif'),
                                        ('Drive', 'https://i.giphy.com/grBPQ4hwmsVyZZ51O0.gif'),
                                        ('Dune', 'https://i.giphy.com/AkwcbzEPIfZ48i44kx.gif'),
                                        ('Dune: Part One', 'https://i.giphy.com/ymfCaSMMv4FNorPHQ6.gif'),
                                        ('Dune: Part Two', 'https://i.giphy.com/buBYK5rVXe6lp7UCni.gif'),
                                        ('E.T. the Extra-Terrestrial', 'https://i.giphy.com/1049KhpGddOJnq.gif'),
                                        ('Elvis', 'https://i.giphy.com/mXBNQ3VzftXji.gif'),
                                        ('Erased', 'https://i.giphy.com/iTZXNodtsUSdqOkyCi.gif'),
                                        ('Eternal Sunshine of the Spotless Mind', 'https://i.giphy.com/SIIXDrZ8Rh2a4.gif'),
                                        ('Eureka Seven', 'https://i.giphy.com/2fTMitkrMVaPC.gif'),
                                        ('Evangelion: 3.0+1.0 Thrice Upon a Time', 'https://i.giphy.com/13iujv9Z0k3qGP6vjD.gif'),
                                        ('Everything Everywhere All at Once', 'https://i.giphy.com/TejmLnMKgnmPInMQjV.gif'),
                                        ('Ex Machina', 'https://i.giphy.com/19OIUml06udBC.gif'),
                                        ('Eyes Wide Shut', 'https://i.giphy.com/cAHfy7VD1uHdK.gif'),
                                        ('FLCL', 'https://i.giphy.com/9dhgKatYHfEuA.gif'),
                                        ('Fargo', 'https://i.giphy.com/e5RclbFtYbZmM.gif'),
                                        ('Fate/Zero', 'https://i.giphy.com/nSB6hxGgQcIzng0695.gif'),
                                        ('Fate/strange Fake', 'https://i.giphy.com/Zb7KmSvYgaGJkuK1PV.gif'),
                                        ('Ferris Bueller''s Day Off', 'https://i.giphy.com/9IZKPmNdZ7juU.gif'),
                                        ('Fight Club', 'https://i.giphy.com/xTiTnnq6SknvxN15Is.gif'),
                                        ('Finding Nemo', 'https://i.giphy.com/107gPMgIBBleA8.gif'),
                                        ('Fire Force Season 3', 'https://i.giphy.com/yr7n0u3qzO9nG.gif'),
                                        ('Fist of the North Star', 'https://i.giphy.com/4SBtIAp4sEDxC.gif'),
                                        ('Forrest Gump', 'https://i.giphy.com/KEXxJgir4BxFZ7bYsC.gif'),
                                        ('Frieren: Beyond Journey''s End', 'https://i.giphy.com/s10xE2pqXO9ntqjpAC.gif'),
                                        ('Frozen', 'https://i.giphy.com/OmoKW1gK6ULg4.gif'),
                                        ('Full Metal Jacket', 'https://i.giphy.com/AyjFDmMgS7OkU.gif'),
                                        ('Fullmetal Alchemist: Brotherhood', 'https://i.giphy.com/12bE9SHGZYR85O.gif'),
                                        ('Furiosa: A Mad Max Saga', 'https://i.giphy.com/kJ6WA4OS0AUEABqpG8.gif'),
                                        ('Gachiakuta', 'https://i.giphy.com/ncg8YBWqAoX6c3oJ1g.gif'),
                                        ('Get Out', 'https://i.giphy.com/xUA7bgONYM1FrC7Vra.gif'),
                                        ('Ghost in the Shell', 'https://i.giphy.com/imKrSqrDxAuvS.gif'),
                                        ('Ghost in the Shell (2026)', 'https://i.giphy.com/BO7p37KdlUaXA5nhWO.gif'),
                                        ('Ghost in the Shell: Stand Alone Complex', 'https://i.giphy.com/rvqCRPWCgctry.gif'),
                                        ('Ghostbusters', 'https://i.giphy.com/l3q2HS9FG81YSdkB2.gif'),
                                        ('Gintama', 'https://i.giphy.com/1BiWRqQSCNISA.gif'),
                                        ('Gladiator', 'https://i.giphy.com/d7mMzaGDYkz4ZBziP6.gif'),
                                        ('Godzilla Minus One', 'https://i.giphy.com/IEI6CCSyavr7db90Ko.gif'),
                                        ('Gone Girl', 'https://i.giphy.com/ToMjGpNUyxffBJHqrn2.gif'),
                                        ('Good Will Hunting', 'https://i.giphy.com/lHZO8Bnfd9PSU.gif'),
                                        ('Goodfellas', 'https://i.giphy.com/3o85xzYIHuDl00se3K.gif'),
                                        ('Grave of the Fireflies', 'https://i.giphy.com/AKtV37treIorS.gif'),
                                        ('Great Teacher Onizuka', 'https://i.giphy.com/Df1Qh8Ed1FKpi.gif'),
                                        ('Guardians of the Galaxy', 'https://i.giphy.com/rne3oSd7DsBDa.gif'),
                                        ('Gunbuster', 'https://i.giphy.com/l3q2tzon8OCC7BqmY.gif'),
                                        ('Gurren Lagann', 'https://i.giphy.com/yYwzZAV5byR2FBYm5G.gif'),
                                        ('Haikyu!!', 'https://i.giphy.com/iQPwaUfgipTJC.gif'),
                                        ('Harry Potter', 'https://i.giphy.com/6RIEW15CCWRvq.gif'),
                                        ('Heat', 'https://i.giphy.com/pQLmrZN8r1p0Q.gif'),
                                        ('Hell''s Paradise', 'https://i.giphy.com/OIBjJZHpzbcR2.gif'),
                                        ('Hellsing Ultimate', 'https://i.giphy.com/vkG6V6ZoLWaPu.gif'),
                                        ('Her', 'https://i.giphy.com/iPa2ff1CV9YnS.gif'),
                                        ('Honey Lemon Soda', 'https://i.giphy.com/Tfpsxdv5pNXYgM5mJN.gif'),
                                        ('Howl''s Moving Castle', 'https://i.giphy.com/DZy8ohXa7cVt6.gif'),
                                        ('Hunter x Hunter (2011)', 'https://i.giphy.com/nbB1CV1fazlGo.gif'),
                                        ('Inception', 'https://i.giphy.com/9UqRcQHzBou6A.gif'),
                                        ('Independence Day', 'https://i.giphy.com/40l0nfT7Wu8bC.gif'),
                                        ('Inglourious Basterds', 'https://i.giphy.com/eiwauFJGF0hb2.gif'),
                                        ('Initial D', 'https://i.giphy.com/qYUfBeJXRxBu0.gif'),
                                        ('Inside Out', 'https://i.giphy.com/pKYb293zuoziUpRwCZ.gif'),
                                        ('Inside Out 2', 'https://i.giphy.com/pKYb293zuoziUpRwCZ.gif'),
                                        ('Interstellar', 'https://i.giphy.com/UCSIkkFXSeg3hugqGf.gif'),
                                        ('Iron Man', 'https://i.giphy.com/lySzu7q4P49u8.gif'),
                                        ('Jin-Roh: The Wolf Brigade', 'https://i.giphy.com/439vHFJdv468hmEJ6m.gif'),
                                        ('JoJo''s Bizarre Adventure', 'https://i.giphy.com/jseFcU9wQaezu.gif'),
                                        ('JoJo''s Bizarre Adventure: Steel Ball Run', 'https://i.giphy.com/JA96AEVeYAp7RGmjbM.gif'),
                                        ('Joker', 'https://i.giphy.com/cEYFeE1QgHWH2YADVHG.gif'),
                                        ('Jujutsu Kaisen', 'https://i.giphy.com/PzmwZmvqZA191oqu3L.gif'),
                                        ('Jurassic Park', 'https://i.giphy.com/37Fsl1eFxbhtu.gif'),
                                        ('Jurassic World Rebirth', 'https://i.giphy.com/kW3AemgUMjN4vg4jrU.gif'),
                                        ('Kaiju No. 8', 'https://i.giphy.com/TI0LPr2g3DQzUMGdyg.gif'),
                                        ('Kiki''s Delivery Service', 'https://i.giphy.com/KXECBV0GkdCX6.gif'),
                                        ('Kill Bill: Vol. 1', 'https://i.giphy.com/oL72C4EQIUhbiO6Pe9.gif'),
                                        ('Kill la Kill', 'https://i.giphy.com/37WzogMvUjAaiRXtMA.gif'),
                                        ('Killers of the Flower Moon', 'https://i.giphy.com/R9f53DwJW0KJbgCc6o.gif'),
                                        ('Knives Out', 'https://i.giphy.com/YAyWp0RxiRrtYVUmR7.gif'),
                                        ('La La Land', 'https://i.giphy.com/xUySTxVTWeEtJhEzsY.gif'),
                                        ('Lady Bird', 'https://i.giphy.com/xUySTL4J5ld87vxbSU.gif'),
                                        ('Land of the Lustrous', 'https://i.giphy.com/jSPIhce4bOyfHO13bC.gif'),
                                        ('Lazarus', 'https://i.giphy.com/4lFfBjcSdZBs4LDmcz.gif'),
                                        ('Legend of the Galactic Heroes', 'https://i.giphy.com/mVyymZhha6TTakKeLz.gif'),
                                        ('Logan', 'https://i.giphy.com/3og0IK2QUzzpkx4EOk.gif'),
                                        ('Look Back', 'https://i.giphy.com/F3UEuXNMvRHRuVdgvw.gif'),
                                        ('Lord of the Rings', 'https://i.giphy.com/VqleJG2rBop7W.gif'),
                                        ('Lost in Translation', 'https://i.giphy.com/YrMrSUfeh5do2FISt8.gif'),
                                        ('Macross', 'https://i.giphy.com/r1UKKfxA5riJq.gif'),
                                        ('Mad Max: Fury Road', 'https://i.giphy.com/Invo00LrPY49i.gif'),
                                        ('Made in Abyss', 'https://i.giphy.com/1AhRIrcUscjJC05LF2.gif'),
                                        ('Magnolia', 'https://i.giphy.com/glZz9xgJKbPEs.gif'),
                                        ('Memento', 'https://i.giphy.com/3o7TKtg43o0ue8VI7C.gif'),
                                        ('Men in Black', 'https://i.giphy.com/R7m04yMaGWVeE.gif'),
                                        ('Mickey 17', 'https://i.giphy.com/osju8BpQhWtnRSIu26.gif'),
                                        ('Mob Psycho 100', 'https://i.giphy.com/mlR0QdwsJUDdK.gif'),
                                        ('Mobile Suit Gundam Wing', 'https://i.giphy.com/oVkvNRT0UPRfJTgZv5.gif'),
                                        ('Monster', 'https://i.giphy.com/X7jjWeC03QDT2.gif'),
                                        ('Moonlight', 'https://i.giphy.com/FGp6Y5pXh0GldPTPw2.gif'),
                                        ('Mushoku Tensei: Jobless Reincarnation', 'https://i.giphy.com/aIoiSCDS5hoDKjAvdd.gif'),
                                        ('Neon Genesis Evangelion', 'https://i.giphy.com/g9W63AVGOHFZaXPC6l.gif'),
                                        ('No Country for Old Men', 'https://i.giphy.com/dpk3BuKZX2s8w.gif'),
                                        ('No Game No Life', 'https://i.giphy.com/6xTXaszCHgMcE.gif'),
                                        ('No Time to Die', 'https://i.giphy.com/cOWEydgKh96VAOrVOO.gif'),
                                        ('Once Upon a Time in Hollywood', 'https://i.giphy.com/l1IkSD6mjNVcWQhUwM.gif'),
                                        ('Oppenheimer', 'https://i.giphy.com/OstuiXBi4xrMMbTefI.gif'),
                                        ('Orb: On the Movements of the Earth', 'https://i.giphy.com/nQJLupPZ8NGdk4r4Kd.gif'),
                                        ('Ouran High School Host Club', 'https://i.giphy.com/DeJqH1Q76fjtC.gif'),
                                        ('Pan''s Labyrinth', 'https://i.giphy.com/Lgb8AJw71yFIk.gif'),
                                        ('Parasite', 'https://i.giphy.com/RJnPjtME1TryN84Dvq.gif'),
                                        ('Perfect Blue', 'https://i.giphy.com/izgCxKfI2PnbO.gif'),
                                        ('Poor Things', 'https://i.giphy.com/l3diNGwou8xtS7kAM.gif'),
                                        ('Portrait of a Lady on Fire', 'https://i.giphy.com/ZbC101txjDJcCcvphp.gif'),
                                        ('Princess Mononoke', 'https://i.giphy.com/AoEDhmdgjUws8.gif'),
                                        ('Psycho-Pass', 'https://i.giphy.com/12KtPizNOpr476.gif'),
                                        ('Pulp Fiction', 'https://i.giphy.com/112zsGNCKs1NpS.gif'),
                                        ('Raiders of the Lost Ark', 'https://i.giphy.com/9J8gnvAxmDFbG.gif'),
                                        ('Rain Man', 'https://i.giphy.com/FFFGVpPUyQSGY.gif'),
                                        ('Ranking of Kings', 'https://i.giphy.com/8N8ulyFMLC4aa6pbqV.gif'),
                                        ('Ratatouille', 'https://i.giphy.com/cJeMxgn9kUIG4.gif'),
                                        ('Saving Private Ryan', 'https://i.giphy.com/JJTkDaJyhs6GPdpRtw.gif'),
                                        ('Scarface', 'https://i.giphy.com/sdarezLLwzMkw.gif'),
                                        ('Schindler''s List', 'https://i.giphy.com/dBT7mACpkW8Fy.gif'),
                                        ('Se7en', 'https://i.giphy.com/9XcD7tH2uKBRC.gif'),
                                        ('Shaun of the Dead', 'https://i.giphy.com/LZlwYWkMfj5n2.gif'),
                                        ('Shrek', 'https://i.giphy.com/seQilU1T7vDva.gif'),
                                        ('Shrek 5', 'https://i.giphy.com/QTrG6mjkHEkpFR3DqX.gif'),
                                        ('Sinners', 'https://i.giphy.com/9SRD7KPmISCg8odrFQ.gif'),
                                        ('Slumdog Millionaire', 'https://i.giphy.com/l1BgR70bd7vCeNUpW.gif'),
                                        ('Soul', 'https://i.giphy.com/o2ITDLRkP2oGk.gif'),
                                        ('Spider-Man', 'https://i.giphy.com/1qErVv5GVUac8uqBJU.gif'),
                                        ('Spider-Man: Across the Spider-Verse', 'https://i.giphy.com/7whm31rMz0t9yB42Tp.gif'),
                                        ('Spider-Man: Into the Spider-Verse', 'https://i.giphy.com/1qErVv5GVUac8uqBJU.gif'),
                                        ('Spirited Away', 'https://i.giphy.com/6mvRz2t6GPugGTNxLb.gif'),
                                        ('Stand by Me', 'https://i.giphy.com/tOCNL4dFV9ndm.gif'),
                                        ('Star Wars', 'https://i.giphy.com/BbMnUoWV7mR4k.gif'),
                                        ('Superbad', 'https://i.giphy.com/4MVV0NgZGdTt6.gif'),
                                        ('Supergirl: Woman of Tomorrow', 'https://i.giphy.com/xBzeaASC01dS0.gif'),
                                        ('Superman', 'https://i.giphy.com/FiBzv5FRE85PO.gif'),
                                        ('Tenet', 'https://i.giphy.com/l10bV0Vt2MiyucTMjZ.gif'),
                                        ('Terminator 2: Judgment Day', 'https://i.giphy.com/3o72FhJwcj35U5nLVu.gif'),
                                        ('The Avengers', 'https://i.giphy.com/PpeGK6edBWAxi.gif'),
                                        ('The Batman', 'https://i.giphy.com/l3q2tzon8OCC7BqmY.gif'),
                                        ('The Batman Part II', 'https://i.giphy.com/l3q2tzon8OCC7BqmY.gif'),
                                        ('The Big Lebowski', 'https://i.giphy.com/TzyQtxScgR5p6.gif'),
                                        ('The Breakfast Club', 'https://i.giphy.com/4spXzGPzlvA76.gif'),
                                        ('The Dark Knight', 'https://i.giphy.com/I8SQMuIELiw0w.gif'),
                                        ('The Departed', 'https://i.giphy.com/11zIWScNDCLjmo.gif'),
                                        ('The Empire Strikes Back', 'https://i.giphy.com/10Ovjb6PnK1L2g.gif'),
                                        ('The Fantastic Four: First Steps', 'https://i.giphy.com/GZeNDm5ofqPIN8Ifk3.gif'),
                                        ('The Godfather', 'https://i.giphy.com/xrvCI5ykhg9QQ.gif'),
                                        ('The Grand Budapest Hotel', 'https://i.giphy.com/3oz8xzIL77ybFvYvAs.gif'),
                                        ('The Green Mile', 'https://i.giphy.com/cHkA7tJZoWLyU.gif'),
                                        ('The Holdovers', 'https://i.giphy.com/ErJynOCv4ymiLPnSH9.gif'),
                                        ('The Incredibles', 'https://i.giphy.com/FBgN017wtq1YQ.gif'),
                                        ('The Lion King', 'https://i.giphy.com/ozKW2C1vkgMJW.gif'),
                                        ('The Mandalorian & Grogu', 'https://i.giphy.com/Fq3KoTpCXMfL83q893.gif'),
                                        ('The Martian', 'https://i.giphy.com/vVMMktHyrZuIo.gif'),
                                        ('The Matrix', 'https://i.giphy.com/BdghqxNFV4efm.gif'),
                                        ('The Prestige', 'https://i.giphy.com/l0MYsxhGGJGxLqXxm.gif'),
                                        ('The Princess Bride', 'https://i.giphy.com/hWjyKDWBopz9K.gif'),
                                        ('The Shawshank Redemption', 'https://i.giphy.com/MxseFhLGXxWyQ.gif'),
                                        ('The Shining', 'https://i.giphy.com/5yeQRdiYrDq2A.gif'),
                                        ('The Silence of the Lambs', 'https://i.giphy.com/O5Bcu5l71uhMs.gif'),
                                        ('The Sixth Sense', 'https://i.giphy.com/DvtsYOKrqPZg4.gif'),
                                        ('The Social Network', 'https://i.giphy.com/RkCiLT8fUW9vMPXA5J.gif'),
                                        ('The Terminator', 'https://i.giphy.com/RfEbMBTPQ7MOY.gif'),
                                        ('The Truman Show', 'https://i.giphy.com/VGHtpsS3UJNRK.gif'),
                                        ('The Wolf of Wall Street', 'https://i.giphy.com/CwM9k8RChLqqQ.gif'),
                                        ('There Will Be Blood', 'https://i.giphy.com/IwYXRW8IXRftS.gif'),
                                        ('Thor Ragnarok', 'https://i.giphy.com/l4FGni1RBAR2OWsGk.gif'),
                                        ('Thunderbolts*', 'https://i.giphy.com/J61A0tTNf3CTtO8CSY.gif'),
                                        ('Titanic', 'https://i.giphy.com/mh7DKz8UhQQGk.gif'),
                                        ('Top Gun', 'https://i.giphy.com/TL5XQpSpASo4U.gif'),
                                        ('Top Gun: Maverick', 'https://i.giphy.com/dcFSeawnz3edbm9n7V.gif'),
                                        ('Toy Story', 'https://i.giphy.com/FtBw4gIVV6fTO.gif'),
                                        ('Toy Story 5', 'https://i.giphy.com/GSE4nkc9I3ZmM.gif'),
                                        ('Trainspotting', 'https://i.giphy.com/R9E0NRcw1tfJS.gif'),
                                        ('Tron: Ares', 'https://i.giphy.com/ZIfywuoNm39lGKOlVq.gif'),
                                        ('Up', 'https://i.giphy.com/xxUzMv2i3tze8.gif'),
                                        ('V for Vendetta', 'https://i.giphy.com/12rJ98B2Fj8iVq.gif'),
                                        ('WALL-E', 'https://i.giphy.com/8FNXce4OdeJxK.gif'),
                                        ('When Harry Met Sally', 'https://i.giphy.com/e39tpB7xOf6h2.gif'),
                                        ('Whiplash', 'https://i.giphy.com/lUMWQ5FZLM2cDg26Uq.gif'),
                                        ('Who Framed Roger Rabbit', 'https://i.giphy.com/iPa2ff1CV9YnS.gif'),
                                        ('Wonder Woman', 'https://i.giphy.com/pLnxbpVosgjE4.gif'),
                                        ('Zootopia 2', 'https://i.giphy.com/iHJEu41kg6r71Gt4pc.gif')
    ON CONFLICT (title) DO NOTHING;
COMMIT;

-- =========================================================
-- Verification
-- =========================================================

SELECT COUNT(*) AS total_movies FROM movies;

CREATE INDEX IF NOT EXISTS idx_movies_title_trgm
    ON public.movies
        USING gin (title gin_trgm_ops);



/*
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
    };*/