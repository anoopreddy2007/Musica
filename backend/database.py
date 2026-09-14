import sqlite3

DATABASE = "musica.db"


def get_connection():
    connection = sqlite3.connect(DATABASE)
    connection.row_factory = sqlite3.Row
    return connection


def init_database():
    connection = get_connection()
    cursor = connection.cursor()

    # =========================================
    # LIKED SONGS
    # =========================================

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS liked_songs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            video_id TEXT UNIQUE NOT NULL,
            title TEXT NOT NULL,
            artists TEXT,
            album TEXT,
            duration TEXT,
            thumbnail TEXT,
            liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    # =========================================
    # PLAY HISTORY
    # =========================================

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS play_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            video_id TEXT NOT NULL,
            title TEXT NOT NULL,
            artists TEXT,
            album TEXT,
            duration TEXT,
            thumbnail TEXT,
            played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    # =========================================
    # PLAYLISTS
    # =========================================

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS playlists (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    # =========================================
    # PLAYLIST SONGS
    # =========================================

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS playlist_songs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            playlist_id INTEGER NOT NULL,
            video_id TEXT NOT NULL,
            title TEXT NOT NULL,
            artists TEXT,
            album TEXT,
            duration TEXT,
            thumbnail TEXT,
            added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

            FOREIGN KEY (playlist_id)
                REFERENCES playlists(id)
                ON DELETE CASCADE,

            UNIQUE(playlist_id, video_id)
        )
    """)

    connection.commit()
    connection.close()