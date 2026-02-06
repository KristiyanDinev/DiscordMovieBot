DO $$
    DECLARE
        u_name text := current_setting('app.user');
        u_pass text := current_setting('app.password');
    BEGIN
        -- 1. Create the User
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = u_name) THEN
            EXECUTE format('CREATE USER %I WITH PASSWORD %L', u_name, u_pass);
        END IF;

        -- 2. Setup Permissions for the Bot
        EXECUTE format('GRANT USAGE, CREATE ON SCHEMA public TO %I', u_name);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO %I', u_name);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO %I', u_name);
    END $$;

-- Create the movies table based on MovieEntity
CREATE TABLE IF NOT EXISTS movies (
                                      id SERIAL PRIMARY KEY,
                                      title VARCHAR(255) NOT NULL UNIQUE,
                                      gif_url VARCHAR(255) NOT NULL,
                                      content_type VARCHAR(50) NOT NULL,
                                      movie_year INTEGER NOT NULL,
                                      rating NUMERIC(4, 2) NOT NULL,
                                      duration INTEGER NOT NULL,
                                      popularity NUMERIC(12, 4) NOT NULL,
                                      created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Ownership for security (as discussed)
DO $$
    DECLARE u_name text := current_setting('app.user');
    BEGIN
        EXECUTE format('ALTER TABLE movies OWNER TO %I', u_name);
END $$;

-- Create the composite index from your @Index annotation
CREATE INDEX IF NOT EXISTS idx_movies_content_type_year_rating
    ON movies (content_type, movie_year, rating);

-- Create the categories table
CREATE TABLE IF NOT EXISTS movie_categories (
                                                movie_id INTEGER NOT NULL,
                                                category VARCHAR(30) NOT NULL,
    -- Constraints
                                                PRIMARY KEY (movie_id, category),
                                                CONSTRAINT fk_movie
                                                    FOREIGN KEY (movie_id)
                                                        REFERENCES movies(id)
                                                        ON DELETE CASCADE
);

-- Ensure the bot user can manage this table
DO $$
    DECLARE u_name text := current_setting('app.user');
    BEGIN
        EXECUTE format('ALTER TABLE movie_categories OWNER TO %I', u_name);
END $$;

BEGIN;

WITH inserted_movies AS (
    INSERT INTO movies (title, gif_url, content_type, movie_year, rating, duration, popularity)
        VALUES
            ('28 Years Later', 'https://i.giphy.com/ZQcFVn1ymQuuDIsOvS.gif', 'movie', 2025, 6.619, 115, 23.3178),
            ('5 Centimeters Per Second', 'https://i.giphy.com/YXfY4UY4dd5sY.gif', 'anime', 2025, 9.5, 121, 4.835),
            ('A Silent Voice', 'https://i.giphy.com/MBmFnxyBRh2JJbB6Ye.gif', 'anime', 2016, 8.382, 129, 8.5158),
            ('Akira', 'https://i.giphy.com/jsoMtBuP1Ahpu.gif', 'anime', 1988, 7.94, 124, 6.237),
            ('Aladdin', 'https://i.giphy.com/nC1toglzIfNFC.gif', 'movie', 1992, 7.7, 91, 11.2132),
            ('Aliens', 'https://i.giphy.com/3ohzdYjwEQuR1J7dte.gif', 'movie', 1986, 7.957, 137, 10.8022),
            ('Amadeus', 'https://i.giphy.com/mGj3SVN7xbPQ4.gif', 'movie', 1984, 8.022, 160, 4.9576),
            ('American Beauty', 'https://i.giphy.com/l0HefWjn5ATRA2F0c.gif', 'movie', 1999, 8.002, 122, 7.2572),
            ('Angel''s Egg', 'https://i.giphy.com/qJkRbWM1MfVjq.gif', 'anime', 1985, 7.733, 71, 2.8417),
            ('Arrival', 'https://i.giphy.com/l1J9wKizQOn7ifyow.gif', 'movie', 2016, 7.624, 116, 11.2559),
            ('Attack on Titan', 'https://i.giphy.com/6my6QGKnRTp9S.gif', 'anime', 2013, 8.671, 24, 31.358),
            ('Avatar', 'https://i.giphy.com/AxhxIcTMEMqR2.gif', 'movie', 2009, 7.6, 162, 40.4645),
            ('Avatar: Fire and Ash', 'https://i.giphy.com/2WHzt2u3S06PgD9SgD.gif', 'movie', 2025, 7.295, 198, 159.2472),
            ('Avatar: The Way of Water', 'https://i.giphy.com/20WiPSNhWV8Qr0UGMn.gif', 'movie', 2022, 7.599, 192, 34.3071),
            ('Avengers: Doomsday', 'https://i.giphy.com/wlOau1cjQyweM6OSII.gif', 'movie', 2026, 0.0, 0, 20.3864),
            ('Avengers: Endgame', 'https://i.giphy.com/d87tLQV2oyarTYZeAk.gif', 'movie', 2019, 8.236, 181, 16.1581),
            ('Avengers: Infinity War', 'https://i.giphy.com/xA7biwSXENswihcvFG.gif', 'movie', 2018, 8.234, 149, 25.5575),
            ('Baccano!', 'https://i.giphy.com/NKKlvFNxzD8SQ.gif', 'anime', 2007, 7.7, 25, 6.025),
            ('Back to the Future', 'https://i.giphy.com/tEsPVINETtZpS.gif', 'movie', 1985, 8.324, 116, 13.7899),
            ('Barbie', 'https://i.giphy.com/3SP9ATEG1KCJ1wUZdC.gif', 'movie', 2023, 6.9, 114, 17.5956),
            ('Beauty and the Beast', 'https://i.giphy.com/xT1R9VVboMNVMZkPKw.gif', 'movie', 2017, 6.968, 129, 9.7536),
            ('Beetlejuice', 'https://i.giphy.com/Rfeku3MGlfuPm.gif', 'movie', 1988, 7.367, 92, 11.1682),
            ('Before Sunrise', 'https://i.giphy.com/ieQMk7UDYyRoc.gif', 'movie', 1995, 8.0, 101, 5.0087),
            ('Berserk', 'https://i.giphy.com/ZBluts0FiVnvtyw7Om.gif', 'anime', 1967, 5.6, 96, 5.1442),
            ('Black Lagoon', 'https://i.giphy.com/VsJbyI1T9ymQM.gif', 'anime', 2006, 8.024, 24, 19.4375),
            ('Black Panther', 'https://i.giphy.com/o6FWop1Gbuyly.gif', 'movie', 2018, 7.4, 135, 13.1717),
            ('Black Swan', 'https://i.giphy.com/3o7btWA2z2fnsP344w.gif', 'movie', 2010, 7.7, 108, 7.978),
            ('Blade Runner', 'https://i.giphy.com/8Dlx8S7TLVvz2.gif', 'movie', 1982, 7.94, 118, 11.4617),
            ('Blade Runner 2049', 'https://i.giphy.com/rRcRhCabBel6xks10Z.gif', 'movie', 2017, 7.589, 164, 14.3844),
            ('Bleach', 'https://i.giphy.com/XDLVGgAsMqBOVWl5Dg.gif', 'anime', 2004, 8.36, 24, 65.6894),
            ('Blue Lock', 'https://i.giphy.com/jdeoZqveFFmmNTIxvf.gif', 'anime', 2022, 8.225, 23, 25.3501),
            ('Blue Velvet', 'https://i.giphy.com/oD3UxjIhisZuE.gif', 'movie', 1986, 7.608, 120, 4.7668),
            ('Bocchi the Rock!', 'https://i.giphy.com/LHLBwKoo5LXnCCihan.gif', 'anime', 2022, 8.3, 24, 11.8212),
            ('Brokeback Mountain', 'https://i.giphy.com/93lyEphbU2Sm4.gif', 'movie', 2005, 7.786, 134, 6.9614),
            ('Bubblegum Crisis', 'https://i.giphy.com/jaOtWJr4cI2Pt1Tyv9.gif', 'anime', 1987, 6.99, 45, 6.6782),
            ('Captain America: Brave New World', 'https://i.giphy.com/LF7iDRta6uK8IcIZFK.gif', 'movie', 2025, 6.0, 119, 14.159),
            ('Captain Tsubasa', 'https://i.giphy.com/tQlAjwnbmk59YYzSuK.gif', 'anime', 1983, 7.8, 24, 14.3568),
            ('Cardcaptor Sakura', 'https://i.giphy.com/tX23iQaQYEcKI.gif', 'anime', 1998, 8.6, 25, 15.324),
            ('Casino', 'https://i.giphy.com/E2UlE5Of9zEjK.gif', 'movie', 1995, 7.99, 179, 7.7914),
            ('Castle in the Sky', 'https://i.giphy.com/4ZeHO6xwFpOq9tNlTO.gif', 'anime', 1986, 7.968, 125, 6.463),
            ('Chainsaw Man', 'https://i.giphy.com/BMdxhHMfD4S7mwL3XK.gif', 'anime', 2025, 8.327, 99, 33.3852),
            ('Challengers', 'https://i.giphy.com/V9i1EWijoNwkJchfDn.gif', 'movie', 2024, 6.9, 132, 6.1423),
            ('City Hunter', 'https://i.giphy.com/yGMKRainOG7zemvomB.gif', 'anime', 1987, 7.9, 25, 19.2795),
            ('City of God', 'https://i.giphy.com/k2aU12mKl3lox2yQGK.gif', 'movie', 2002, 8.427, 129, 8.3002),
            ('Civil War', 'https://i.giphy.com/z4THDw2us3aCY.gif', 'movie', 2024, 6.846, 109, 10.4703),
            ('Clannad: After Story', 'https://i.giphy.com/fARFPMuspJRx6.gif', 'anime', 2007, 8.3, 24, 12.1772),
            ('Claymore', 'https://i.giphy.com/ZxL2arcSrEOiYuVhBf.gif', 'anime', 2007, 8.138, 23, 10.251),
            ('Coco', 'https://i.giphy.com/MCL6prm5SIeuFb91xZ.gif', 'movie', 2017, 8.199, 105, 18.6261),
            ('Code Geass: Lelouch of the Rebellion', 'https://i.giphy.com/aQFiKGhqerg2Y.gif', 'anime', 2006, 8.342, 25, 23.9873),
            ('Coming to America', 'https://i.giphy.com/2WBNMTzB6uB1u.gif', 'movie', 1988, 6.879, 117, 6.2366),
            ('Cowboy Bebop', 'https://i.giphy.com/MBUku5f2PeXFoNN7O8.gif', 'anime', 1998, 8.51, 25, 28.8081),
            ('Cyberpunk: Edgerunners', 'https://i.giphy.com/TMQhGn533KiUW5Q0vD.gif', 'anime', 2022, 8.483, 25, 12.2382),
            ('Dandadan', 'https://i.giphy.com/MD1QDeDxnELtBpIiqd.gif', 'anime', 2024, 8.572, 24, 23.0825),
            ('Darker Than Black', 'https://i.giphy.com/ZvLzYozpSRoCJHv5Ne.gif', 'anime', 2007, 7.572, 25, 7.5859),
            ('Dead Poets Society', 'https://i.giphy.com/HFyBNSSlNwNKE.gif', 'movie', 1989, 8.301, 129, 12.9128),
            ('Deadpool', 'https://i.giphy.com/tJdRK8yEXmhUhzfDXy.gif', 'movie', 2016, 7.622, 108, 13.6328),
            ('Deadpool & Wolverine', 'https://i.giphy.com/J30zgOl5GxzMCKgqfl.gif', 'movie', 2024, 7.6, 128, 22.218),
            ('Death Note', 'https://i.giphy.com/26Ff89l2hGLYmVV5e.gif', 'anime', 2006, 8.6, 22, 22.0076),
            ('Delicious in Dungeon', 'https://i.giphy.com/zRW20g1KJpzQg8rbcE.gif', 'anime', 2024, 8.472, 26, 14.0033),
            ('Demon Slayer', 'https://i.giphy.com/2ya7xLyEeynlZM4FCw.gif', 'anime', 2025, 7.683, 156, 87.6685),
            ('Devilman Crybaby', 'https://i.giphy.com/ygAmzS6qTOkzxG0VsB.gif', 'anime', 2018, 7.741, 25, 9.9416),
            ('Die Hard', 'https://i.giphy.com/CxwvAGYoBTGYU.gif', 'movie', 1988, 7.805, 132, 12.6215),
            ('Digimon Adventure', 'https://i.giphy.com/UOvFfXGINpmN2.gif', 'anime', 2020, 7.539, 24, 10.9646),
            ('District 9', 'https://i.giphy.com/KwNRL1qNvx50A.gif', 'movie', 2009, 7.461, 112, 6.5716),
            ('Django Unchained', 'https://i.giphy.com/9K0k6XktLOhLq.gif', 'movie', 2012, 8.186, 165, 12.5229),
            ('Do the Right Thing', 'https://i.giphy.com/nnZGGqJDtrfCU.gif', 'movie', 1989, 7.766, 120, 2.4488),
            ('Doctor Strange', 'https://i.giphy.com/9Y5oA62nZC19BWFgTs.gif', 'movie', 2016, 7.4, 115, 9.7879),
            ('Donnie Darko', 'https://i.giphy.com/5mUYcIziYkaWI.gif', 'movie', 2001, 7.763, 114, 8.5271),
            ('Dragon Ball', 'https://i.giphy.com/JILOx1KJggOGI.gif', 'anime', 1989, 8.355, 26, 58.0454),
            ('Dragon Ball Daima', 'https://i.giphy.com/mfgWMmIbTx0NHSqs4F.gif', 'anime', 2024, 8.151, 25, 18.6322),
            ('Dragon Ball Z', 'https://i.giphy.com/VUDWelveo5VGo.gif', 'anime', 1989, 8.355, 26, 58.0454),
            ('Drive', 'https://i.giphy.com/grBPQ4hwmsVyZZ51O0.gif', 'movie', 2011, 7.582, 100, 7.7814),
            ('Dune', 'https://i.giphy.com/AkwcbzEPIfZ48i44kx.gif', 'movie', 2021, 7.8, 155, 19.2955),
            ('Dune: Part One', 'https://i.giphy.com/ymfCaSMMv4FNorPHQ6.gif', 'movie', 2021, 7.8, 155, 19.2955),
            ('Dune: Part Two', 'https://i.giphy.com/buBYK5rVXe6lp7UCni.gif', 'movie', 2024, 8.131, 167, 18.0099),
            ('E.T. the Extra-Terrestrial', 'https://i.giphy.com/1049KhpGddOJnq.gif', 'movie', 1982, 7.511, 115, 11.8412),
            ('Elvis', 'https://i.giphy.com/mXBNQ3VzftXji.gif', 'movie', 2022, 7.469, 159, 4.4189),
            ('Erased', 'https://i.giphy.com/iTZXNodtsUSdqOkyCi.gif', 'movie', 2012, 6.07, 100, 2.3357),
            ('Eternal Sunshine of the Spotless Mind', 'https://i.giphy.com/SIIXDrZ8Rh2a4.gif', 'movie', 2004, 8.092, 108, 11.771),
            ('Eureka Seven', 'https://i.giphy.com/2fTMitkrMVaPC.gif', 'anime', 2005, 7.4, 24, 15.3697),
            ('Evangelion: 3.0+1.0 Thrice Upon a Time', 'https://i.giphy.com/13iujv9Z0k3qGP6vjD.gif', 'anime', 2021, 8.194, 155, 3.8626),
            ('Everything Everywhere All at Once', 'https://i.giphy.com/TejmLnMKgnmPInMQjV.gif', 'movie', 2022, 7.722, 140, 9.1332),
            ('Ex Machina', 'https://i.giphy.com/19OIUml06udBC.gif', 'movie', 2015, 7.573, 108, 7.6121),
            ('Eyes Wide Shut', 'https://i.giphy.com/cAHfy7VD1uHdK.gif', 'movie', 1999, 7.483, 159, 18.4475),
            ('FLCL', 'https://i.giphy.com/9dhgKatYHfEuA.gif', 'anime', 2000, 7.945, 25, 10.3239),
            ('Fargo', 'https://i.giphy.com/e5RclbFtYbZmM.gif', 'movie', 2014, 8.291, 0, 87.6315),
            ('Fate/Zero', 'https://i.giphy.com/nSB6hxGgQcIzng0695.gif', 'anime', 2011, 8.0, 25, 12.247),
            ('Fate/strange Fake', 'https://i.giphy.com/Zb7KmSvYgaGJkuK1PV.gif', 'anime', 2024, 8.353, 0, 18.7092),
            ('Ferris Bueller''s Day Off', 'https://i.giphy.com/9IZKPmNdZ7juU.gif', 'movie', 1986, 7.567, 103, 5.8531),
            ('Fight Club', 'https://i.giphy.com/xTiTnnq6SknvxN15Is.gif', 'movie', 1999, 8.438, 139, 21.0039),
            ('Finding Nemo', 'https://i.giphy.com/107gPMgIBBleA8.gif', 'movie', 2003, 7.817, 100, 16.6558),
            ('Fire Force Season 3', 'https://i.giphy.com/yr7n0u3qzO9nG.gif', 'anime', 2019, 8.572, 24, 23.0825),
            ('Fist of the North Star', 'https://i.giphy.com/4SBtIAp4sEDxC.gif', 'anime', 1986, 6.463, 125, 6.463),
            ('Forrest Gump', 'https://i.giphy.com/KEXxJgir4BxFZ7bYsC.gif', 'movie', 1994, 8.467, 142, 41.7119),
            ('Frieren: Beyond Journey''s End', 'https://i.giphy.com/s10xE2pqXO9ntqjpAC.gif', 'anime', 2023, 8.761, 25, 74.9813),
            ('Frozen', 'https://i.giphy.com/OmoKW1gK6ULg4.gif', 'movie', 2013, 7.23, 102, 33.3852),
            ('Full Metal Jacket', 'https://i.giphy.com/AyjFDmMgS7OkU.gif', 'movie', 1987, 8.087, 116, 12.5229),
            ('Fullmetal Alchemist: Brotherhood', 'https://i.giphy.com/12bE9SHGZYR85O.gif', 'anime', 2009, 8.671, 24, 31.358),
            ('Furiosa: A Mad Max Saga', 'https://i.giphy.com/kJ6WA4OS0AUEABqpG8.gif', 'movie', 2024, 7.51, 148, 25.3501),
            ('Gachiakuta', 'https://i.giphy.com/ncg8YBWqAoX6c3oJ1g.gif', 'anime', 2025, 8.619, 24, 26.9386),
            ('Get Out', 'https://i.giphy.com/xUA7bgONYM1FrC7Vra.gif', 'movie', 2017, 7.572, 104, 22.0076),
            ('Ghost in the Shell', 'https://i.giphy.com/imKrSqrDxAuvS.gif', 'anime', 1995, 7.94, 82, 12.2382),
            ('Ghost in the Shell: Stand Alone Complex', 'https://i.giphy.com/rvqCRPWCgctry.gif', 'anime', 2002, 7.945, 25, 10.3239),
            ('Ghostbusters', 'https://i.giphy.com/l3q2HS9FG81YSdkB2.gif', 'movie', 1984, 7.367, 105, 11.1682),
            ('Gintama', 'https://i.giphy.com/1BiWRqQSCNISA.gif', 'anime', 2006, 8.572, 24, 23.0825),
            ('Gladiator', 'https://i.giphy.com/d7mMzaGDYkz4ZBziP6.gif', 'movie', 2000, 8.199, 155, 18.6261),
            ('Godzilla Minus One', 'https://i.giphy.com/IEI6CCSyavr7db90Ko.gif', 'movie', 2023, 7.573, 125, 7.6121),
            ('Gone Girl', 'https://i.giphy.com/ToMjGpNUyxffBJHqrn2.gif', 'movie', 2014, 7.84, 149, 8.46),
            ('Good Will Hunting', 'https://i.giphy.com/lHZO8Bnfd9PSU.gif', 'movie', 1997, 8.092, 126, 11.771),
            ('Goodfellas', 'https://i.giphy.com/3o85xzYIHuDl00se3K.gif', 'movie', 1990, 8.427, 146, 8.3002),
            ('Grave of the Fireflies', 'https://i.giphy.com/AKtV37treIorS.gif', 'anime', 1988, 8.355, 89, 58.0454),
            ('Great Teacher Onizuka', 'https://i.giphy.com/Df1Qh8Ed1FKpi.gif', 'anime', 1999, 8.342, 25, 23.9873),
            ('Guardians of the Galaxy', 'https://i.giphy.com/rne3oSd7DsBDa.gif', 'movie', 2014, 7.923, 121, 9.7004),
            ('Gunbuster', 'https://i.giphy.com/l3q2tzon8OCC7BqmY.gif', 'anime', 1988, 7.945, 30, 10.3239),
            ('Gurren Lagann', 'https://i.giphy.com/yYwzZAV5byR2FBYm5G.gif', 'anime', 2007, 8.199, 24, 18.6261),
            ('Haikyu!!', 'https://i.giphy.com/iQPwaUfgipTJC.gif', 'anime', 2014, 8.671, 24, 31.358),
            ('Harry Potter', 'https://i.giphy.com/6RIEW15CCWRvq.gif', 'movie', 2001, 7.923, 152, 9.7004),
            ('Heat', 'https://i.giphy.com/pQLmrZN8r1p0Q.gif', 'movie', 1995, 7.84, 170, 8.46),
            ('Hell''s Paradise', 'https://i.giphy.com/OIBjJZHpzbcR2.gif', 'anime', 2023, 8.327, 24, 33.3852),
            ('Hellsing Ultimate', 'https://i.giphy.com/vkG6V6ZoLWaPu.gif', 'anime', 2006, 8.355, 50, 58.0454),
            ('Her', 'https://i.giphy.com/Z3fNGG3534aYw.gif', 'movie', 2013, 7.84, 126, 8.46),
            ('Honey Lemon Soda', 'https://i.giphy.com/Tfpsxdv5pNXYgM5mJN.gif', 'anime', 2025, 9.5, 23, 4.835),
            ('Howl''s Moving Castle', 'https://i.giphy.com/DZy8ohXa7cVt6.gif', 'anime', 2004, 8.355, 119, 58.0454),
            ('Hunter x Hunter (2011)', 'https://i.giphy.com/nbB1CV1fazlGo.gif', 'anime', 2011, 8.761, 24, 74.9813),
            ('Inception', 'https://i.giphy.com/9UqRcQHzBou6A.gif', 'movie', 2010, 8.37, 148, 26.6888),
            ('Independence Day', 'https://i.giphy.com/40l0nfT7Wu8bC.gif', 'movie', 1996, 6.898, 145, 8.8267),
            ('Inglourious Basterds', 'https://i.giphy.com/eiwauFJGF0hb2.gif', 'movie', 2009, 8.215, 153, 15.3324),
            ('Initial D', 'https://i.giphy.com/qYUfBeJXRxBu0.gif', 'anime', 1998, 8.342, 25, 23.9873),
            ('Inside Out', 'https://i.giphy.com/8wh8QBUIJzArK.gif', 'movie', 2015, 7.968, 95, 9.7536),
            ('Inside Out 2', 'https://i.giphy.com/pKYb293zuoziUpRwCZ.gif', 'movie', 2024, 7.622, 96, 13.6328),
            ('Interstellar', 'https://i.giphy.com/UCSIkkFXSeg3hugqGf.gif', 'movie', 2014, 8.467, 169, 41.7119),
            ('Iron Man', 'https://i.giphy.com/lySzu7q4P49u8.gif', 'movie', 2008, 7.6, 126, 40.4645),
            ('Jin-Roh: The Wolf Brigade', 'https://i.giphy.com/439vHFJdv468hmEJ6m.gif', 'anime', 1999, 7.572, 102, 7.5859),
            ('JoJo''s Bizarre Adventure', 'https://i.giphy.com/jseFcU9wQaezu.gif', 'anime', 2012, 8.51, 24, 28.8081),
            ('JoJo''s Bizarre Adventure: Steel Ball Run', 'https://i.giphy.com/JA96AEVeYAp7RGmjbM.gif', 'anime', 2025, 9.5, 24, 4.835),
            ('Joker', 'https://i.giphy.com/cEYFeE1QgHWH2YADVHG.gif', 'movie', 2019, 8.156, 122, 13.8901),
            ('Jujutsu Kaisen', 'https://i.giphy.com/PzmwZmvqZA191oqu3L.gif', 'anime', 2020, 8.572, 24, 23.0825),
            ('Jurassic Park', 'https://i.giphy.com/37Fsl1eFxbhtu.gif', 'movie', 1993, 7.923, 127, 9.7004),
            ('Jurassic World Rebirth', 'https://i.giphy.com/kW3AemgUMjN4vg4jrU.gif', 'movie', 2025, 6.0, 119, 14.159),
            ('Kiki''s Delivery Service', 'https://i.giphy.com/KXECBV0GkdCX6.gif', 'anime', 1989, 7.8, 103, 14.3568),
            ('Kill la Kill', 'https://i.giphy.com/37WzogMvUjAaiRXtMA.gif', 'anime', 2013, 8.199, 24, 18.6261),
            ('Killers of the Flower Moon', 'https://i.giphy.com/R9f53DwJW0KJbgCc6o.gif', 'movie', 2023, 7.469, 206, 4.4189),
            ('Knives Out', 'https://i.giphy.com/YAyWp0RxiRrtYVUmR7.gif', 'movie', 2019, 7.84, 130, 8.46),
            ('La La Land', 'https://i.giphy.com/xUySTxVTWeEtJhEzsY.gif', 'movie', 2016, 7.84, 128, 8.46),
            ('Lady Bird', 'https://i.giphy.com/xUySTL4J5ld87vxbSU.gif', 'movie', 2017, 7.469, 94, 4.4189),
            ('Land of the Lustrous', 'https://i.giphy.com/jSPIhce4bOyfHO13bC.gif', 'anime', 2017, 8.761, 24, 74.9813),
            ('Lazarus', 'https://i.giphy.com/4lFfBjcSdZBs4LDmcz.gif', 'anime', 2025, 7.7, 24, 6.025),
            ('Legend of the Galactic Heroes', 'https://i.giphy.com/mVyymZhha6TTakKeLz.gif', 'anime', 1988, 8.761, 26, 74.9813),
            ('Logan', 'https://i.giphy.com/3og0IK2QUzzpkx4EOk.gif', 'movie', 2017, 7.805, 137, 12.6215),
            ('Look Back', 'https://i.giphy.com/F3UEuXNMvRHRuVdgvw.gif', 'anime', 2024, 8.353, 58, 18.7092),
            ('Lord of the Rings', 'https://i.giphy.com/VqleJG2rBop7W.gif', 'movie', 2001, 7.923, 178, 9.7004),
            ('Lost in Translation', 'https://i.giphy.com/YrMrSUfeh5do2FISt8.gif', 'movie', 2003, 7.573, 102, 7.6121),
            ('Macross', 'https://i.giphy.com/r1UKKfxA5riJq.gif', 'anime', 1982, 7.945, 25, 10.3239),
            ('Mad Max: Fury Road', 'https://i.giphy.com/Invo00LrPY49i.gif', 'movie', 2015, 7.51, 120, 25.3501),
            ('Made in Abyss', 'https://i.giphy.com/1AhRIrcUscjJC05LF2.gif', 'anime', 2017, 8.761, 25, 74.9813),
            ('Magnolia', 'https://i.giphy.com/glZz9xgJKbPEs.gif', 'movie', 1999, 7.7, 189, 4.9194),
            ('Memento', 'https://i.giphy.com/3o7TKtg43o0ue8VI7C.gif', 'movie', 2000, 8.199, 113, 18.6261),
            ('Men in Black', 'https://i.giphy.com/R7m04yMaGWVeE.gif', 'movie', 1997, 7.923, 98, 9.7004),
            ('Mob Psycho 100', 'https://i.giphy.com/mlR0QdwsJUDdK.gif', 'anime', 2016, 8.572, 24, 23.0825),
            ('Mobile Suit Gundam Wing', 'https://i.giphy.com/oVkvNRT0UPRfJTgZv5.gif', 'anime', 1995, 7.945, 24, 10.3239),
            ('Monster', 'https://i.giphy.com/X7jjWeC03QDT2.gif', 'anime', 2004, 8.761, 24, 74.9813),
            ('Moonlight', 'https://i.giphy.com/FGp6Y5pXh0GldPTPw2.gif', 'movie', 2016, 7.469, 111, 4.4189),
            ('Mushoku Tensei: Jobless Reincarnation', 'https://i.giphy.com/aIoiSCDS5hoDKjAvdd.gif', 'anime', 2021, 8.572, 24, 23.0825),
            ('Neon Genesis Evangelion', 'https://i.giphy.com/g9W63AVGOHFZaXPC6l.gif', 'anime', 1995, 8.355, 25, 58.0454),
            ('No Country for Old Men', 'https://i.giphy.com/dpk3BuKZX2s8w.gif', 'movie', 2007, 7.923, 122, 9.7004),
            ('No Game No Life', 'https://i.giphy.com/6xTXaszCHgMcE.gif', 'anime', 2014, 8.199, 24, 18.6261),
            ('No Time to Die', 'https://i.giphy.com/cOWEydgKh96VAOrVOO.gif', 'movie', 2021, 7.4, 163, 12.5229),
            ('Once Upon a Time in Hollywood', 'https://i.giphy.com/l1IkSD6mjNVcWQhUwM.gif', 'movie', 2019, 7.469, 161, 4.4189),
            ('Oppenheimer', 'https://i.giphy.com/OstuiXBi4xrMMbTefI.gif', 'movie', 2023, 8.092, 180, 11.771),
            ('Orb: On the Movements of the Earth', 'https://i.giphy.com/nQJLupPZ8NGdk4r4Kd.gif', 'anime', 2024, 8.353, 24, 18.7092),
            ('Ouran High School Host Club', 'https://i.giphy.com/DeJqH1Q76fjtC.gif', 'anime', 2006, 8.342, 23, 23.9873),
            ('Pan''s Labyrinth', 'https://i.giphy.com/Lgb8AJw71yFIk.gif', 'movie', 2006, 7.7, 118, 14.324),
            ('Parasite', 'https://i.giphy.com/RJnPjtME1TryN84Dvq.gif', 'movie', 2019, 8.51, 133, 28.8081),
            ('Perfect Blue', 'https://i.giphy.com/izgCxKfI2PnbO.gif', 'anime', 1997, 8.355, 81, 58.0454),
            ('Poor Things', 'https://i.giphy.com/l3diNGwou8xtS7kAM.gif', 'movie', 2023, 7.7, 141, 14.324),
            ('Portrait of a Lady on Fire', 'https://i.giphy.com/ZbC101txjDJcCcvphp.gif', 'movie', 2019, 8.199, 122, 18.6261),
            ('Princess Mononoke', 'https://i.giphy.com/AoEDhmdgjUws8.gif', 'anime', 1997, 8.355, 134, 58.0454),
            ('Psycho-Pass', 'https://i.giphy.com/12KtPizNOpr476.gif', 'anime', 2012, 7.945, 23, 10.3239),
            ('Pulp Fiction', 'https://i.giphy.com/112zsGNCKs1NpS.gif', 'movie', 1994, 8.467, 154, 41.7119),
            ('Raiders of the Lost Ark', 'https://i.giphy.com/9J8gnvAxmDFbG.gif', 'movie', 1981, 7.923, 115, 9.7004),
            ('Rain Man', 'https://i.giphy.com/FFFGVpPUyQSGY.gif', 'movie', 1988, 7.749, 134, 5.8016),
            ('Ranking of Kings', 'https://i.giphy.com/8N8ulyFMLC4aa6pbqV.gif', 'anime', 2021, 8.059, 23, 13.7909),
            ('Ratatouille', 'https://i.giphy.com/cJeMxgn9kUIG4.gif', 'movie', 2007, 7.836, 111, 14.324),
            ('Saving Private Ryan', 'https://i.giphy.com/JJTkDaJyhs6GPdpRtw.gif', 'movie', 1998, 8.221, 169, 11.1219),
            ('Scarface', 'https://i.giphy.com/sdarezLLwzMkw.gif', 'movie', 1983, 8.156, 170, 13.8901),
            ('Schindler''s List', 'https://i.giphy.com/dBT7mACpkW8Fy.gif', 'movie', 1993, 8.572, 195, 23.0825),
            ('Se7en', 'https://i.giphy.com/9XcD7tH2uKBRC.gif', 'movie', 1995, 8.3, 127, 12.1772),
            ('Shaun of the Dead', 'https://i.giphy.com/LZlwYWkMfj5n2.gif', 'movie', 2004, 7.539, 99, 10.9646),
            ('Shrek', 'https://i.giphy.com/seQilU1T7vDva.gif', 'movie', 2001, 7.7, 90, 11.2132),
            ('Shrek 5', 'https://i.giphy.com/QTrG6mjkHEkpFR3DqX.gif', 'movie', 2026, 0.0, 0, 13.7909),
            ('Sinners', 'https://i.giphy.com/9SRD7KPmISCg8odrFQ.gif', 'movie', 2025, 6.0, 119, 14.159),
            ('Slumdog Millionaire', 'https://i.giphy.com/l1BgR70bd7vCeNUpW.gif', 'movie', 2008, 7.729, 121, 5.8201),
            ('Soul', 'https://i.giphy.com/o2ITDLRkP2oGk.gif', 'movie', 2020, 8.199, 100, 18.6261),
            ('Spider-Man', 'https://i.giphy.com/1qErVv5GVUac8uqBJU.gif', 'movie', 2002, 7.23, 121, 33.3852),
            ('Spider-Man: Across the Spider-Verse', 'https://i.giphy.com/7whm31rMz0t9yB42Tp.gif', 'movie', 2023, 8.353, 140, 18.7092),
            ('Spider-Man: Into the Spider-Verse', 'https://i.giphy.com/1qErVv5GVUac8uqBJU.gif', 'movie', 2018, 8.4, 117, 20.0485),
            ('Spirited Away', 'https://i.giphy.com/6mvRz2t6GPugGTNxLb.gif', 'anime', 2001, 8.5, 125, 41.7119),
            ('Stand by Me', 'https://i.giphy.com/tOCNL4dFV9ndm.gif', 'movie', 1986, 7.923, 89, 9.7004),
            ('Star Wars', 'https://i.giphy.com/BbMnUoWV7mR4k.gif', 'movie', 1977, 8.206, 121, 10.5025),
            ('Superbad', 'https://i.giphy.com/4MVV0NgZGdTt6.gif', 'movie', 2007, 7.23, 113, 33.3852),
            ('Supergirl: Woman of Tomorrow', 'https://i.giphy.com/xBzeaASC01dS0.gif', 'movie', 2026, 0.0, 0, 6.382),
            ('Superman', 'https://i.giphy.com/FiBzv5FRE85PO.gif', 'movie', 1978, 7.1, 143, 14.159),
            ('Tenet', 'https://i.giphy.com/l10bV0Vt2MiyucTMjZ.gif', 'movie', 2020, 7.367, 150, 11.1682),
            ('Terminator 2: Judgment Day', 'https://i.giphy.com/3o72FhJwcj35U5nLVu.gif', 'movie', 1991, 8.087, 137, 12.5229),
            ('The Avengers', 'https://i.giphy.com/PpeGK6edBWAxi.gif', 'movie', 2012, 7.7, 143, 18.6261),
            ('The Batman', 'https://i.giphy.com/4FJ2zEk8WaBHkVyHem.gif', 'movie', 2022, 7.683, 176, 87.6685),
            ('The Batman Part II', 'https://i.giphy.com/l3q2tzon8OCC7BqmY.gif', 'movie', 2026, 0.0, 0, 13.7909),
            ('The Big Lebowski', 'https://i.giphy.com/TzyQtxScgR5p6.gif', 'movie', 1998, 7.84, 117, 8.46),
            ('The Breakfast Club', 'https://i.giphy.com/4spXzGPzlvA76.gif', 'movie', 1985, 7.724, 98, 6.0028),
            ('The Dark Knight', 'https://i.giphy.com/I8SQMuIELiw0w.gif', 'movie', 2008, 8.525, 152, 25.576),
            ('The Departed', 'https://i.giphy.com/11zIWScNDCLjmo.gif', 'movie', 2006, 8.199, 151, 18.6261),
            ('The Empire Strikes Back', 'https://i.giphy.com/10Ovjb6PnK1L2g.gif', 'movie', 1980, 8.39, 124, 15.7165),
            ('The Fantastic Four: First Steps', 'https://i.giphy.com/GZeNDm5ofqPIN8Ifk3.gif', 'movie', 2025, 6.1, 71, 1.0589),
            ('The Godfather', 'https://i.giphy.com/xrvCI5ykhg9QQ.gif', 'movie', 1972, 8.687, 175, 27.0662),
            ('The Grand Budapest Hotel', 'https://i.giphy.com/3oz8xzIL77ybFvYvAs.gif', 'movie', 2014, 8.033, 100, 8.3397),
            ('The Green Mile', 'https://i.giphy.com/cHkA7tJZoWLyU.gif', 'movie', 1999, 8.5, 189, 41.7119),
            ('The Holdovers', 'https://i.giphy.com/ErJynOCv4ymiLPnSH9.gif', 'movie', 2023, 7.622, 133, 13.6328),
            ('The Incredibles', 'https://i.giphy.com/FBgN017wtq1YQ.gif', 'movie', 2004, 7.7, 115, 14.324),
            ('The Lion King', 'https://i.giphy.com/ozKW2C1vkgMJW.gif', 'movie', 1994, 8.255, 89, 15.7165),
            ('The Mandalorian & Grogu', 'https://i.giphy.com/Fq3KoTpCXMfL83q893.gif', 'movie', 2026, 0.0, 0, 6.642),
            ('The Martian', 'https://i.giphy.com/vVMMktHyrZuIo.gif', 'movie', 2015, 7.7, 141, 10.7916),
            ('The Matrix', 'https://i.giphy.com/BdghqxNFV4efm.gif', 'movie', 1999, 8.237, 136, 20.0485),
            ('The Prestige', 'https://i.giphy.com/l0MYsxhGGJGxLqXxm.gif', 'movie', 2006, 8.206, 130, 10.5025),
            ('The Princess Bride', 'https://i.giphy.com/hWjyKDWBopz9K.gif', 'movie', 1987, 7.691, 99, 7.382),
            ('The Shawshank Redemption', 'https://i.giphy.com/MxseFhLGXxWyQ.gif', 'movie', 1994, 8.706, 142, 33.3852),
            ('The Shining', 'https://i.giphy.com/5yeQRdiYrDq2A.gif', 'movie', 1980, 8.212, 144, 25.576),
            ('The Silence of the Lambs', 'https://i.giphy.com/O5Bcu5l71uhMs.gif', 'movie', 1991, 8.346, 118, 2.4488),
            ('The Sixth Sense', 'https://i.giphy.com/DvtsYOKrqPZg4.gif', 'movie', 1999, 7.923, 107, 9.7004),
            ('The Social Network', 'https://i.giphy.com/RkCiLT8fUW9vMPXA5J.gif', 'movie', 2010, 7.763, 120, 8.5271),
            ('The Terminator', 'https://i.giphy.com/RfEbMBTPQ7MOY.gif', 'movie', 1984, 7.624, 108, 11.2559),
            ('The Truman Show', 'https://i.giphy.com/VGHtpsS3UJNRK.gif', 'movie', 1998, 8.118, 103, 10.8022),
            ('The Wolf of Wall Street', 'https://i.giphy.com/CwM9k8RChLqqQ.gif', 'movie', 2013, 8.0, 180, 21.0039),
            ('There Will Be Blood', 'https://i.giphy.com/IwYXRW8IXRftS.gif', 'movie', 2007, 8.092, 158, 11.771),
            ('Thor Ragnarok', 'https://i.giphy.com/l4FGni1RBAR2OWsGk.gif', 'movie', 2017, 7.6, 130, 18.0099),
            ('Thunderbolts*', 'https://i.giphy.com/J61A0tTNf3CTtO8CSY.gif', 'movie', 2025, 6.0, 134, 14.159),
            ('Titanic', 'https://i.giphy.com/mh7DKz8UhQQGk.gif', 'movie', 1997, 7.9, 194, 26.6888),
            ('Top Gun', 'https://i.giphy.com/TL5XQpSpASo4U.gif', 'movie', 1986, 7.07, 110, 12.6215),
            ('Top Gun: Maverick', 'https://i.giphy.com/dcFSeawnz3edbm9n7V.gif', 'movie', 2022, 8.206, 131, 10.5025),
            ('Toy Story', 'https://i.giphy.com/FtBw4gIVV6fTO.gif', 'movie', 1995, 7.968, 81, 6.463),
            ('Toy Story 5', 'https://i.giphy.com/GSE4nkc9I3ZmM.gif', 'movie', 2026, 0.0, 90, 6.382),
            ('Trainspotting', 'https://i.giphy.com/R9E0NRcw1tfJS.gif', 'movie', 1996, 7.961, 94, 4.4157),
            ('Tron: Ares', 'https://i.giphy.com/ZIfywuoNm39lGKOlVq.gif', 'movie', 2025, 6.504, 119, 37.9301),
            ('Up', 'https://i.giphy.com/xxUzMv2i3tze8.gif', 'movie', 2009, 8.3, 96, 26.6888),
            ('V for Vendetta', 'https://i.giphy.com/12rJ98B2Fj8iVq.gif', 'movie', 2005, 7.9, 132, 10.3239),
            ('WALL-E', 'https://i.giphy.com/8FNXce4OdeJxK.gif', 'movie', 2008, 8.087, 98, 12.5229),
            ('When Harry Met Sally', 'https://i.giphy.com/e39tpB7xOf6h2.gif', 'movie', 1989, 7.4, 96, 9.7879),
            ('Whiplash', 'https://i.giphy.com/lUMWQ5FZLM2cDg26Uq.gif', 'movie', 2014, 8.39, 107, 15.7165),
            ('Who Framed Roger Rabbit', 'https://i.giphy.com/iPa2ff1CV9YnS.gif', 'movie', 1988, 7.539, 104, 10.9646),
            ('Wonder Woman', 'https://i.giphy.com/pLnxbpVosgjE4.gif', 'movie', 2017, 7.23, 141, 33.3852),
            ('Zootopia 2', 'https://i.giphy.com/iHJEu41kg6r71Gt4pc.gif', 'movie', 2025, 0.0, 0, 20.3864),
            ('The Lego Batman Movie', 'https://i.giphy.com/l3q2tzon8OCC7BqmY.gif', 'movie', 2017, 7.5, 104, 50.0),
            ('The Croods', 'https://i.giphy.com/Py4loHkx4eo1O', 'movie', 2013, 7.2, 98, 50.0),
            ('The Croods 2', 'https://i.giphy.com/hBLO4wF1x51e8tDKtc', 'movie', 2020, 7.5, 95, 50.0),
            ('Anaconda', 'https://i.giphy.com/jrYhObConD74JEKW8n', 'movie', 2025, 5.87, 100, 216.8087)
        ON CONFLICT (title) DO NOTHING
        RETURNING id, title
)

INSERT INTO movie_categories (movie_id, category)
SELECT id, 'Horror' FROM inserted_movies WHERE title = '28 Years Later'
UNION ALL
SELECT id, 'Sci-Fi' FROM inserted_movies WHERE title = '28 Years Later'
UNION ALL

SELECT id, 'Romance' FROM inserted_movies WHERE title = '5 Centimeters Per Second'


ON CONFLICT DO NOTHING;

COMMIT;

