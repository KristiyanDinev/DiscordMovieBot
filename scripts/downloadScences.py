#!/usr/bin/env python3
"""
URL Downloader - Downloads content from a URL and saves it to a file
Handles any type of content (images, videos, documents, etc.)
Connects to PostgreSQL to rename files based on database IDs
"""

import requests
import os
import sys
from urllib.parse import urlparse
from pathlib import Path
import psycopg2
from psycopg2 import sql


def get_file_id_from_db(conn, filename_without_ext, url):
    """
    Query PostgreSQL database to get the ID for a given filename.

    Args:
        conn: PostgreSQL database connection
        filename_without_ext (str): Filename without extension
        url (str): Original URL for additional matching

    Returns:
        int: ID from database, or None if not found
    """
    try:
        cursor = conn.cursor()
        query = "SELECT id FROM movies WHERE gif_url LIKE %s"
        pattern = f"%{filename_without_ext}%"

        cursor.execute(query, (pattern,))
        result = cursor.fetchone()
        cursor.close()

        if result:
            return result[0]
        else:
            print(f"  ⚠ Warning: No database entry found for '{filename_without_ext}'")
            return None

    except Exception as e:
        print(f"  ✗ Database query error: {str(e)}")
        return None


def download_file(url, output_path=None, db_conn=None):
    """
    Download content from a URL and save it to a file.

    Args:
        url (str): The URL to download from
        output_path (str, optional): Path to save the file. If None, generates from URL
        db_conn: PostgreSQL database connection for renaming files

    Returns:
        str: Path to the saved file, or None if failed
    """
    if (url is None) or (output_path is None) or len(url) == 0:
        return None
    try:
        # Strip any whitespace/newlines from URL (important for URLs from CSV files)
        url = url.strip()

        print(f"Downloading from: {url}")

        # Set headers to mimic a real browser (avoids 403 errors from sites that block bots)
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': '*/*',
            'Accept-Language': 'en-US,en;q=0.9',
        }

        # Send GET request with a timeout
        response = requests.get(url, headers=headers, timeout=30, stream=True)

        # Check if request was successful
        response.raise_for_status()

        # Determine file extension from content type
        content_type = response.headers.get('Content-Type', '')
        extension = ''
        if 'image' in content_type:
            extension = '.jpg' if 'jpeg' in content_type else ('.png' if 'png' in content_type else '.gif' if 'gif' in content_type else '.img')
        elif 'video' in content_type:
            extension = '.mp4'
        elif 'pdf' in content_type:
            extension = '.pdf'
        else:
            extension = '.bin'

        # If extension can't be determined from content type, get it from URL
        if extension == '.bin':
            parsed_url = urlparse(url)
            url_filename = os.path.basename(parsed_url.path)
            if '.' in url_filename:
                extension = os.path.splitext(url_filename)[1]

        # Create directory if it doesn't exist
        output_dir = os.path.dirname(output_path)
        if output_dir and not os.path.exists(output_dir):
            os.makedirs(output_dir)

        # Get original filename from URL
        original_filename = os.path.basename(urlparse(url).path)
        temp_filepath = os.path.join(output_path, original_filename)

        # Download and save the file
        print(f"Saving to: {temp_filepath}")
        with open(temp_filepath, 'wb') as f:
            # Download in chunks to handle large files
            for chunk in response.iter_content(chunk_size=8192):
                if chunk:
                    f.write(chunk)

        file_size = os.path.getsize(temp_filepath)
        print(f"✓ Successfully downloaded {file_size:,} bytes")

        # Rename file based on database ID if connection provided
        if db_conn:
            # Get filename without extension
            filename_without_ext = os.path.splitext(original_filename)[0]

            # Query database for ID
            file_id = get_file_id_from_db(db_conn, filename_without_ext, url)

            if file_id is not None:
                # Rename file with ID
                new_filepath = os.path.join(output_path, f"{file_id}{extension}")

                # Handle case where target file already exists
                if os.path.exists(new_filepath):
                    print(f"  ⚠ Warning: {new_filepath} already exists, overwriting...")
                    os.remove(new_filepath)

                os.rename(temp_filepath, new_filepath)
                print(f"  ✓ Renamed to: {file_id}{extension}")
                return new_filepath

        return temp_filepath

    except requests.exceptions.Timeout:
        print(f"✗ Error: Request timed out after 30 seconds")
        print(f"  URL: {url}")
        return None

    except requests.exceptions.ConnectionError as e:
        print(f"✗ Error: Connection failed")
        print(f"  URL: {url}")
        print(f"  Details: {str(e)}")
        return None

    except requests.exceptions.HTTPError as e:
        print(f"✗ Error: HTTP error occurred")
        print(f"  URL: {url}")
        print(f"  Status code: {response.status_code}")
        print(f"  Details: {str(e)}")
        return None

    except requests.exceptions.RequestException as e:
        print(f"✗ Error: Request failed")
        print(f"  URL: {url}")
        print(f"  Details: {str(e)}")
        return None

    except IOError as e:
        print(f"✗ Error: Failed to save file")
        print(f"  Path: {output_path}")
        print(f"  Details: {str(e)}")
        return None

    except Exception as e:
        print(f"✗ Error: Unexpected error occurred")
        print(f"  URL: {url}")
        print(f"  Details: {type(e).__name__}: {str(e)}")
        return None


def connect_to_database(host, database, user, password, port=5432):
    """
    Connect to PostgreSQL database.

    Args:
        host (str): Database host
        database (str): Database name
        user (str): Database user
        password (str): Database password
        port (int): Database port (default 5432)

    Returns:
        connection object or None if failed
    """
    try:
        conn = psycopg2.connect(
            host=host,
            database=database,
            user=user,
            password=password,
            port=port
        )
        print(f"✓ Connected to PostgreSQL database '{database}' on {host}")
        return conn
    except Exception as e:
        print(f"✗ Error: Failed to connect to database")
        print(f"  Details: {str(e)}")
        return None


def main():
    """Main function to handle command-line usage"""
    if len(sys.argv) < 2:
        print("Usage: python url_downloader.py <URL> [output_path] [--db-host HOST] [--db-name NAME] [--db-user USER] [--db-password PASS] [--db-port PORT]")
        print("\nAlso the <URL> can be a link to a text file full of links.")
        print("\nExample:")
        print("  python url_downloader.py https://example.com/image.jpg")
        print("  python url_downloader.py https://example.com/image.jpg ./downloads")
        print("  python url_downloader.py urls.txt ./downloads --db-host localhost --db-name mydb --db-user postgres --db-password mypass")
        sys.exit(1)

    url = sys.argv[1]
    output_path = sys.argv[2] if len(sys.argv) > 2 and not sys.argv[2].startswith('--') else './downloads'

    # Parse database connection arguments
    db_host = None
    db_name = None
    db_user = None
    db_password = None
    db_port = 5432

    i = 2
    while i < len(sys.argv):
        if sys.argv[i] == '--db-host' and i + 1 < len(sys.argv):
            db_host = sys.argv[i + 1]
            i += 2
        elif sys.argv[i] == '--db-name' and i + 1 < len(sys.argv):
            db_name = sys.argv[i + 1]
            i += 2
        elif sys.argv[i] == '--db-user' and i + 1 < len(sys.argv):
            db_user = sys.argv[i + 1]
            i += 2
        elif sys.argv[i] == '--db-password' and i + 1 < len(sys.argv):
            db_password = sys.argv[i + 1]
            i += 2
        elif sys.argv[i] == '--db-port' and i + 1 < len(sys.argv):
            db_port = int(sys.argv[i + 1])
            i += 2
        else:
            i += 1

    # Connect to database if credentials provided
    db_conn = None
    if all([db_host, db_name, db_user, db_password]):
        db_conn = connect_to_database(db_host, db_name, db_user, db_password, db_port)
        if db_conn is None:
            print("⚠ Warning: Proceeding without database connection")
            exit()
    elif any([db_host, db_name, db_user, db_password]):
        print("⚠ Warning: Incomplete database credentials provided. Need all of: --db-host, --db-name, --db-user, --db-password")
        print("  Proceeding without database connection (files won't be renamed)")

    # Process URLs
    try:
        if url.endswith(".txt"):
            with open(url) as f:
                for u in f:
                    u = u.strip()
                    if u:  # Skip empty lines
                        download_file(u, output_path, db_conn)
        else:
            download_file(url, output_path, db_conn)
    finally:
        # Close database connection
        if db_conn:
            db_conn.close()
            print("\n✓ Database connection closed")


if __name__ == "__main__":
    main()