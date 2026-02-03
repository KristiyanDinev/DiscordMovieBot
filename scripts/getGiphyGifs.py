import requests
import os
import time

# 1. PASTE YOUR GIPHY API KEY HERE
API_KEY = os.getenv('GIPHY_API_KEY')

# The list of movies you want IDs for
movie_titles = [
    "The Shawshank Redemption", "The Godfather", "The Dark Knight", "Pulp Fiction",
    "Forrest Gump", "Inception", "Fight Club", "The Matrix", "Goodfellas",
    "Star Wars", "Interstellar", "The Silence of the Lambs", "Saving Private Ryan",
    "The Green Mile", "Gladiator", "The Lion King", "Back to the Future",
    "Jurassic Park", "Titanic", "The Avengers", "Avatar", "Harry Potter",
    "Lord of the Rings", "The Terminator", "Toy Story", "Frozen", "Spider-Man",
    "Iron Man", "Black Panther", "Joker", "The Incredibles", "Finding Nemo",
    "Shrek", "Coco", "Up", "WALL-E", "Inside Out", "Parasite",
    "Mad Max Fury Road", "Django Unchained", "La La Land", "Whiplash",
    "The Grand Budapest Hotel", "Deadpool", "Wonder Woman",
    "Guardians of the Galaxy", "Doctor Strange", "Thor Ragnarok", "Dune",
    "Everything Everywhere All at Once",

    # --- 1980s Classics ---
    "The Empire Strikes Back", "Raiders of the Lost Ark", "Blade Runner",
    "E.T. the Extra-Terrestrial", "The Shining", "Scarface", "Ghostbusters",
    "The Terminator", "Aliens", "Stand by Me", "Ferris Bueller's Day Off",
    "The Breakfast Club", "Top Gun", "Die Hard", "Beetlejuice",
    "Who Framed Roger Rabbit", "Rain Man", "Dead Poets Society",
    "When Harry Met Sally", "Do the Right Thing", "Akira", "The Princess Bride",
    "Full Metal Jacket", "Blue Velvet", "Amadeus", "Coming to America",

    # --- 1990s Hits ---
    "Terminator 2: Judgment Day", "Beauty and the Beast", "Aladdin",
    "Schindler's List", "Jurassic Park", "Se7en", "Toy Story", "Fargo",
    "Trainspotting", "Independence Day", "Good Will Hunting",
    "Men in Black", "The Big Lebowski", "The Truman Show",
    "Saving Private Ryan", "The Sixth Sense", "The Matrix",
    "American Beauty", "Magnolia", "Eyes Wide Shut", "Heat",
    "Casino", "Before Sunrise", "Princess Mononoke", "Perfect Blue",

    # --- 2000s Blockbusters & Gems ---
    "Gladiator", "Memento", "Spirited Away", "Donnie Darko",
    "City of God", "Lost in Translation", "Kill Bill: Vol. 1",
    "Eternal Sunshine of the Spotless Mind", "Shaun of the Dead",
    "The Incredibles", "Brokeback Mountain", "V for Vendetta",
    "Pan's Labyrinth", "The Departed", "The Prestige",
    "No Country for Old Men", "There Will Be Blood", "Superbad",
    "Ratatouille", "Slumdog Millionaire", "Iron Man", "The Dark Knight",
    "Wall-E", "Avatar", "District 9", "Inglourious Basterds",

    # --- 2010s Modern Classics ---
    "The Social Network", "Black Swan", "Drive", "The Avengers",
    "The Wolf of Wall Street", "Her", "Gone Girl", "The Grand Budapest Hotel",
    "Interstellar", "Whiplash", "Mad Max: Fury Road", "Inside Out",
    "Ex Machina", "The Martian", "Arrival", "Moonlight", "Get Out",
    "Logan", "Blade Runner 2049", "Coco", "Lady Bird",
    "Spider-Man: Into the Spider-Verse", "Avengers: Infinity War",
    "Avengers: Endgame", "Parasite", "Joker", "Knives Out",
    "Once Upon a Time in Hollywood", "Portrait of a Lady on Fire",

    # --- 2020-2024 Recent Hits ---
    "Tenet", "Soul", "Dune: Part One", "No Time to Die",
    "Top Gun: Maverick", "The Batman", "Avatar: The Way of Water",
    "Elvis", "Everything Everywhere All at Once", "Oppenheimer", "Barbie",
    "Spider-Man: Across the Spider-Verse", "Killers of the Flower Moon",
    "Poor Things", "The Holdovers", "Godzilla Minus One",
    "Dune: Part Two", "Inside Out 2", "Deadpool & Wolverine",
    "Furiosa: A Mad Max Saga", "Civil War", "Challengers",

    # --- 2025-2026 Anticipated Releases ---
    "Captain America: Brave New World", "Thunderbolts*",
    "The Fantastic Four: First Steps", "Superman",
    "Jurassic World Rebirth", "Zootopia 2", "Avatar: Fire and Ash",
    "Mickey 17", "Sinners", "Tron: Ares", "28 Years Later",
    "The Mandalorian & Grogu", "Toy Story 5", "Supergirl: Woman of Tomorrow",
    "Shrek 5", "Avengers: Doomsday", "The Batman Part II",

    # --- 1980s Anime Classics ---
    "Akira", "Grave of the Fireflies", "My Neighbor Totoro",
    "Nausicaä of the Valley of the Wind", "Dragon Ball", "Saint Seiya",
    "Fist of the North Star", "Castle in the Sky", "Kiki's Delivery Service",
    "Legend of the Galactic Heroes", "Mobile Suit Zeta Gundam", "Macross",
    "Vampire Hunter D", "Royal Space Force: The Wings of Honneamise",
    "Gunbuster", "Bubblegum Crisis", "Urusei Yatsura 2: Beautiful Dreamer",
    "Captain Tsubasa", "City Hunter", "Ranma ½", "Patlabor: The Movie",
    "Angel's Egg", "Wicked City", "Project A-ko", "Space Cobra",

    # --- 1990s Anime Hits ---
    "Neon Genesis Evangelion", "Cowboy Bebop", "Ghost in the Shell",
    "Princess Mononoke", "Sailor Moon", "Dragon Ball Z", "Yu Yu Hakusho",
    "Rurouni Kenshin", "Berserk", "Trigun", "Perfect Blue",
    "Slam Dunk", "One Piece", "Pokémon", "Digimon Adventure",
    "Serial Experiments Lain", "Cardcaptor Sakura", "Great Teacher Onizuka",
    "Revolutionary Girl Utena", "Mobile Suit Gundam Wing", "Outlaw Star",
    "The Vision of Escaflowne", "Slayers", "Initial D", "Blue Submarine No. 6",
    "Jin-Roh: The Wolf Brigade", "Whisper of the Heart", "Porco Rosso",

    # --- 2000s Anime Blockbusters & Gems ---
    "Spirited Away", "Fullmetal Alchemist: Brotherhood", "Death Note",
    "Code Geass: Lelouch of the Rebellion", "Naruto", "Bleach",
    "Gurren Lagann", "Samurai Champloo", "Monster", "Mushishi",
    "Howl's Moving Castle", "Paprika", "The Girl Who Leapt Through Time",
    "Summer Wars", "FLCL", "Hellsing Ultimate", "Baccano!",
    "Clannad: After Story", "Toradora!", "Soul Eater", "Black Lagoon",
    "Ghost in the Shell: Stand Alone Complex", "Ouran High School Host Club",
    "Wolf's Rain", "Tokyo Godfathers", "5 Centimeters Per Second",
    "Eureka Seven", "Gintama", "Claymore", "Darker Than Black",

    # --- 2010s Modern Anime Classics ---
    "Attack on Titan", "Steins;Gate", "Hunter x Hunter (2011)",
    "Your Name", "One Punch Man", "My Hero Academia", "Demon Slayer",
    "Haikyu!!", "Mob Psycho 100", "Vinland Saga", "Made in Abyss",
    "Violet Evergarden", "A Silent Voice", "Puella Magi Madoka Magica",
    "Kill la Kill", "JoJo's Bizarre Adventure", "Psycho-Pass",
    "Fate/Zero", "Tokyo Ghoul", "Sword Art Online", "No Game No Life",
    "Your Lie in April", "Erased", "Parasyte: The Maxim",
    "The Tale of the Princess Kaguya", "Wolf Children", "Promare",
    "Weathering With You", "Land of the Lustrous", "Devilman Crybaby",

    # --- 2020-2024 Recent Anime Hits ---
    "Jujutsu Kaisen", "Chainsaw Man", "Spy x Family", "Oshi no Ko",
    "Frieren: Beyond Journey's End", "Bocchi the Rock!",
    "Cyberpunk: Edgerunners", "Dandadan", "The Apothecary Diaries",
    "Delicious in Dungeon", "Solo Leveling", "Kaiju No. 8",
    "Suzume", "The First Slam Dunk", "Evangelion: 3.0+1.0 Thrice Upon a Time",
    "Ranking of Kings", "Blue Lock", "Mushoku Tensei: Jobless Reincarnation",
    "Hell's Paradise", "Trigun Stampede", "Mobile Suit Gundam: The Witch from Mercury",
    "Look Back", "Pluto", "Zom 100: Bucket List of the Dead", "Odd Taxi",

    # --- 2025-2026 Anticipated Anime Releases ---
    "Sakamoto Days", "Lazarus", "Witch Hat Atelier", "Fire Force Season 3",
    "Gachiakuta", "Trigun Stargaze", "JoJo's Bizarre Adventure: Steel Ball Run",
    "One Piece: Elbaf Arc", "Re:Zero Season 4",
    "Bleach: Thousand-Year Blood War (The Calamity)", "Ghost in the Shell (2026)",
    "Dragon Ball Daima", "To Be Hero X", "Zenshu", "Honey Lemon Soda",
    "Orb: On the Movements of the Earth", "The Rose of Versailles (Movie)",
    "Puella Magi Madoka Magica: Walpurgisnacht Rising", "Fate/strange Fake"
]

# Ensure uniqueness in case of accidental duplicates
movie_titles = list(set(movie_titles))

# Optional: Sort them alphabetically for easier reading
movie_titles.sort()

def get_giphy_id(movie_name):
    endpoint = "https://api.giphy.com/v1/gifs/search"
    params = {
        'api_key': API_KEY,
        'q': movie_name + " movie OR anime official trailer scene",
        'limit': 1,
        'rating': 'pg-13'
    }

    try:
        response = requests.get(endpoint, params=params)
        data = response.json()

        if data['data']:
            # Return the ID of the first result
            return data['data'][0]['id']
        else:
            return None
    except Exception as e:
        print(f"Error fetching {movie_name}: {e}")
        return None

print("Generating Java Code...\n")
print("private static final Movie[] MOVIES = {")

for title in movie_titles:
    gif_id = get_giphy_id(title)

    if gif_id:
        # Prints valid Java code line
        print(f'    new Movie("{title}", "{gif_id}"),')
    else:
        print(f'    // Could not find GIF for: {title}')

    # Sleep briefly to avoid hitting rate limits
    time.sleep(0.1)

print("};")
print("\nDone! Copy the output above into your Java file.")