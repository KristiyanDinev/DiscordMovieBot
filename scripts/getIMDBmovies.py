import requests
from bs4 import BeautifulSoup
import re

HEADERS = {
    "User-Agent": "Mozilla/5.0"
}

def get_imdb_id(title):
    search_url = f"https://www.imdb.com/find?q={title.replace(' ', '+')}&s=tt"

    r = requests.get(search_url, headers=HEADERS)
    soup = BeautifulSoup(r.text, "html.parser")

    # first search result
    result = soup.select_one("a.ipc-metadata-list-summary-item__t")

    if not result:
        return None

    href = result["href"]

    match = re.search(r"/title/(tt\d+)/", href)
    if match:
        return match.group(1)

    return None


def get_quotes(imdb_id, min_quotes=2):
    url = f"https://www.imdb.com/title/{imdb_id}/quotes"

    r = requests.get(url, headers=HEADERS)
    soup = BeautifulSoup(r.text, "html.parser")

    quotes = []

    blocks = soup.select(".ipc-html-content-inner-div")

    for block in blocks:
        text = block.get_text(separator=" ").strip()

        if len(text) > 40:  # filter tiny lines
            quotes.append(text)

        if len(quotes) >= min_quotes:
            break

    return quotes


def get_movie_quotes(title):
    imdb_id = get_imdb_id(title)

    if not imdb_id:
        print("Movie not found")
        return

    quotes = get_quotes(imdb_id)

    print("IMDb ID:", imdb_id)
    print("\nQuotes:\n")

    for q in quotes:
        print("-", q)
        print()


if __name__ == "__main__":
    movie = input("Movie title: ")
    get_movie_quotes(movie)