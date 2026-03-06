import requests
import json
import os

# --- CONFIGURATION ---
BOT_ID = "1466485885074800854"  # Replace with your Bot's User ID
DBL_TOKEN = os.getenv('DISCORD_BOT_LIST')  # Replace with your discordbotlist.com token
API_URL = f"https://discordbotlist.com/api/v1/bots/{BOT_ID}/commands"

commands = [
    {
        "name": "start",
        "description": "Begin a new game with your preferred settings",
        "options": [
            {
                "name": "type",
                "description": "Select the library to play from",
                "type": 3, # STRING
                "required": False,
                "choices": [
                    {"name": "Movies 🎬", "value": "movies"},
                    {"name": "Anime ⛩️", "value": "anime"}
                ]
            },
            {
                "name": "rounds",
                "description": "Number of scenes to play in this session",
                "type": 4, # INTEGER
                "required": False
            },
            {
                "name": "high_stakes",
                "description": "Enable high stakes mode for a harder challenge",
                "type": 5, # BOOLEAN
                "required": False
            },
            {
                "name": "locked",
                "description": "Lock the game so only you can answer (Solo Mode)",
                "type": 5, # BOOLEAN
                "required": False
            }
        ]
    },
    {
        "name": "guess",
        "description": "Submit your movie or anime title",
        "options": [
            {
                "name": "title",
                "description": "The title you want to guess",
                "type": 3,
                "required": True
            }
        ]
    },
    {
        "name": "skip",
        "description": "Give up, reveal the answer, and start fresh"
    },
    {
        "name": "leaderboard",
        "description": "Compare your rank globally and locally"
    },
    {
        "name": "updates",
        "description": "See what's new in the latest version"
    },
    {
        "name": "ping",
        "description": "Displays the latency of the bot"
    },
    {
        "name": "report",
        "description": "Report an inaccurate movie for your current game",
        "options": [
            {
                "name": "reason",
                "description": "Why are you reporting this movie?",
                "type": 3,
                "required": True
            }
        ]
    },
    {
        "name": "help",
        "description": "Displays the helpful menu for the bot"
    }
]

def update_dbl_commands():
    headers = {
        "Authorization": f"Bot {DBL_TOKEN}",
        "Content-Type": "application/json"
    }

    print(f"Updating DBL commands for Bot {BOT_ID}...")
    response = requests.post(API_URL, headers=headers, data=json.dumps(commands))

    if response.status_code in [200, 204]:
        print("✅ Command list successfully synced with DiscordBotList!")
    else:
        print(f"❌ Error {response.status_code}: {response.text}")

if __name__ == "__main__":
    update_dbl_commands()