import requests as rq
import time


def escape_sql(s: str) -> str:
    return s.replace("'", "''")

def classify_content_type(genres):
    anime_keys = {"Anime"}
    return "anime" if any(g in anime_keys for g in genres) else "movie"

def get_movie_metadata(title):
    """
    Search for a movie by title and return metadata:
    - official title
    - year
    - IMDb rating
    - genres
    """
    try:
        result = 
    except Exception as e:
        print(f"-- ❌ search failed for {title}: {e}")
        return None
    if not result:
        print(f"-- ❌ no IMDb results for {title}")
        return None

    return {
        "title": title,
        "year": year,
        "rating": round(rating, 1),
        "genres": genres,
    }

def sql_for_movie(movie, gif_url):
    title = escape_sql(movie["title"])
    gif = escape_sql(gif_url)
    content_type = classify_content_type(movie["genres"])

    movie_sql = f"""
INSERT INTO movies (title, gif_url, content_type, movie_year, rating)
VALUES ('{title}', '{gif}', '{content_type}', {movie["year"]}, {movie["rating"]})
ON CONFLICT (title) DO NOTHING;
"""

    category_sql = ""
    for g in movie["genres"]:
        cat = escape_sql(g.lower())
        category_sql += f"""
INSERT INTO movie_categories (movie_id, category)
SELECT m.id, '{cat}'
FROM movies m
WHERE m.title = '{title}';
"""

    return movie_sql + "\n" + category_sql

def process(title, gif_url):
    data = get_movie_metadata(title)
    if data:
        print("-- ===============================")
        print(sql_for_movie(data, gif_url))
        print("-- ===============================\n")
    time.sleep(1)




MOVIES = [
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
    ]
if __name__ == "__main__":
    for title, gif in MOVIES:
        process(title, gif)
