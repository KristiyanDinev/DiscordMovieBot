import os
import json



def main():
    input_file = "movies_and_anime.json"
    if os.path.exists(input_file):
        with open(input_file, "r", encoding="utf-8") as f:
            try:
                results = json.load(f)
            except json.JSONDecodeError:
                print("Can't read that file")
                exit()
    else:
        print("File "+input_file+" doesn't exists.")

    sql = """
    WITH inserted_movies AS (
    INSERT INTO movies (title, gif_url, content_type, movie_year, rating, duration, popularity)
        VALUES
        """
    size = len(results)
    for i in range(size):
        movie = results[i]
        # Assuming 'movie' is your current dictionary item
        sql += (f"\n(\"{movie['original_title']}\", "
            + f"\"{str(MOVIES[i][1])}\", " 
            + f"\"{str(MOVIES[i][2])}\", "
            + f"{movie['release_date'].split('-')[0]}, "
            + f"{movie['vote_average']}, "
            +f"{movie['runtime']}, "
            +f"{movie['popularity']})")
        if i < size -1:
            sql += ","
    sql += """  ON CONFLICT (title) DO NOTHING
        RETURNING id, title
        )
    """
    with open("results.sql", "a", encoding="utf-8") as f:
        f.write(sql)
    


if __name__ == "__main__":
    main()


