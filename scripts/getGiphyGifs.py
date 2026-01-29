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
    "Shrek 5", "Avengers: Doomsday", "The Batman Part II"
]

# Ensure uniqueness in case of accidental duplicates
movie_titles = list(set(movie_titles))

# Optional: Sort them alphabetically for easier reading
movie_titles.sort()

def get_giphy_id(movie_name):
    endpoint = "https://api.giphy.com/v1/gifs/search"
    params = {
        'api_key': API_KEY,
        'q': movie_name + " movie", # Adding "movie" helps context
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