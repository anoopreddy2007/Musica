from fastapi import FastAPI, Query, Body
from fastapi.middleware.cors import CORSMiddleware
from ytmusicapi import YTMusic
from yt_dlp import YoutubeDL

from database import init_database, get_connection


# ============================================================
# APP SETUP
# ============================================================

app = FastAPI(title="Musica API")

# Create all database tables when the server starts
init_database()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

yt = YTMusic()


# ============================================================
# BASIC ROUTES
# ============================================================

@app.get("/")
def home():
    return {
        "message": "Welcome to Musica API",
        "status": "running"
    }


@app.get("/health")
def health():
    return {
        "status": "healthy"
    }


# ============================================================
# SEARCH
# ============================================================

@app.get("/search")
def search_songs(
    q: str = Query(..., min_length=1),
    limit: int = Query(20, ge=1, le=50)
):
    print(f"Searching YouTube Music for: {q}")

    try:
        results = yt.search(
            q,
            filter="songs",
            limit=limit
        )

        songs = []

        for song in results:
            songs.append({
                "videoId": song.get("videoId"),
                "title": song.get("title"),

                "artists": [
                    artist.get("name")
                    for artist in song.get("artists", [])
                ],

                "album": (
                    song.get("album", {}).get("name")
                    if song.get("album")
                    else None
                ),

                "duration": song.get("duration"),

                "thumbnail": (
                    song.get("thumbnails", [{}])[-1].get("url")
                    if song.get("thumbnails")
                    else None
                )
            })

        print(f"Found {len(songs)} songs")

        return {
            "query": q,
            "count": len(songs),
            "songs": songs
        }

    except Exception as error:
        print("SEARCH ERROR:", repr(error))

        return {
            "query": q,
            "count": 0,
            "songs": [],
            "error": str(error)
        }


# ============================================================
# AUDIO STREAMING
# ============================================================

def get_audio_url(video_id: str):

    ydl_opts = {
        "format": "bestaudio/best",
        "quiet": True,
        "no_warnings": True,
    }

    with YoutubeDL(ydl_opts) as ydl:

        info = ydl.extract_info(
            f"https://www.youtube.com/watch?v={video_id}",
            download=False
        )

        return {
            "url": info.get("url"),
            "title": info.get("title"),
            "duration": info.get("duration"),
            "ext": info.get("ext"),
        }


@app.get("/stream/{video_id}")
def stream_song(video_id: str):

    try:
        return get_audio_url(video_id)

    except Exception as error:

        print("STREAM ERROR:", repr(error))

        return {
            "error": str(error)
        }


# ============================================================
# LIKED SONGS
# ============================================================

@app.post("/likes")
def like_song(song: dict = Body(...)):

    connection = get_connection()
    cursor = connection.cursor()

    try:

        cursor.execute("""
            INSERT OR IGNORE INTO liked_songs
            (
                video_id,
                title,
                artists,
                album,
                duration,
                thumbnail
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """, (

            song.get("videoId"),
            song.get("title"),
            ", ".join(song.get("artists", [])),
            song.get("album"),
            song.get("duration"),
            song.get("thumbnail")

        ))

        connection.commit()

        return {
            "success": True,
            "message": "Song liked"
        }

    finally:

        connection.close()


@app.delete("/likes/{video_id}")
def unlike_song(video_id: str):

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute(
        "DELETE FROM liked_songs WHERE video_id = ?",
        (video_id,)
    )

    connection.commit()
    connection.close()

    return {
        "success": True,
        "message": "Song unliked"
    }


@app.get("/likes")
def get_liked_songs():

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT *
        FROM liked_songs
        ORDER BY liked_at DESC
    """)

    rows = cursor.fetchall()

    connection.close()

    songs = []

    for row in rows:

        songs.append({
            "videoId": row["video_id"],
            "title": row["title"],

            "artists": (
                row["artists"].split(", ")
                if row["artists"]
                else []
            ),

            "album": row["album"],
            "duration": row["duration"],
            "thumbnail": row["thumbnail"]
        })

    return {
        "songs": songs,
        "count": len(songs)
    }


@app.get("/likes/{video_id}")
def check_liked(video_id: str):

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute(
        """
        SELECT video_id
        FROM liked_songs
        WHERE video_id = ?
        """,
        (video_id,)
    )

    row = cursor.fetchone()

    connection.close()

    return {
        "liked": row is not None
    }


# ============================================================
# PLAY HISTORY
# ============================================================

@app.post("/history")
def add_to_history(song: dict = Body(...)):

    connection = get_connection()
    cursor = connection.cursor()

    try:

        cursor.execute("""
            INSERT INTO play_history
            (
                video_id,
                title,
                artists,
                album,
                duration,
                thumbnail
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """, (

            song.get("videoId"),
            song.get("title"),
            ", ".join(song.get("artists", [])),
            song.get("album"),
            song.get("duration"),
            song.get("thumbnail")

        ))

        connection.commit()

        return {
            "success": True,
            "message": "Song added to history"
        }

    finally:

        connection.close()


@app.get("/history")
def get_history():

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT *
        FROM play_history
        ORDER BY played_at DESC
    """)

    rows = cursor.fetchall()

    connection.close()

    songs = []

    for row in rows:

        songs.append({
            "id": row["id"],
            "videoId": row["video_id"],
            "title": row["title"],

            "artists": (
                row["artists"].split(", ")
                if row["artists"]
                else []
            ),

            "album": row["album"],
            "duration": row["duration"],
            "thumbnail": row["thumbnail"],
            "playedAt": row["played_at"]
        })

    return {
        "songs": songs,
        "count": len(songs)
    }


@app.delete("/history")
def clear_history():

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("DELETE FROM play_history")

    connection.commit()
    connection.close()

    return {
        "success": True,
        "message": "History cleared"
    }


@app.delete("/history/{history_id}")
def delete_history_item(history_id: int):

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute(
        "DELETE FROM play_history WHERE id = ?",
        (history_id,)
    )

    connection.commit()
    connection.close()

    return {
        "success": True,
        "message": "History item deleted"
    }


# ============================================================
# PLAYLISTS
# ============================================================

# ------------------------------------------------------------
# CREATE PLAYLIST
# ------------------------------------------------------------

@app.post("/playlists")
def create_playlist(playlist: dict = Body(...)):

    name = playlist.get("name")

    if not name or not name.strip():

        return {
            "success": False,
            "message": "Playlist name is required"
        }

    name = name.strip()

    connection = get_connection()
    cursor = connection.cursor()

    try:

        cursor.execute(
            """
            INSERT INTO playlists (name)
            VALUES (?)
            """,
            (name,)
        )

        connection.commit()

        playlist_id = cursor.lastrowid

        return {
            "success": True,
            "message": "Playlist created",
            "playlist": {
                "id": playlist_id,
                "name": name
            }
        }

    finally:

        connection.close()


# ------------------------------------------------------------
# GET ALL PLAYLISTS
# ------------------------------------------------------------

@app.get("/playlists")
def get_playlists():

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT
            p.id,
            p.name,
            p.created_at,
            COUNT(ps.id) AS song_count
        FROM playlists p
        LEFT JOIN playlist_songs ps
            ON p.id = ps.playlist_id
        GROUP BY p.id
        ORDER BY p.created_at DESC
    """)

    rows = cursor.fetchall()

    connection.close()

    playlists = []

    for row in rows:

        playlists.append({
            "id": row["id"],
            "name": row["name"],
            "songCount": row["song_count"],
            "createdAt": row["created_at"]
        })

    return {
        "playlists": playlists,
        "count": len(playlists)
    }


# ------------------------------------------------------------
# GET ONE PLAYLIST + ITS SONGS
# ------------------------------------------------------------

@app.get("/playlists/{playlist_id}")
def get_playlist(playlist_id: int):

    connection = get_connection()
    cursor = connection.cursor()

    # Get playlist information
    cursor.execute(
        """
        SELECT *
        FROM playlists
        WHERE id = ?
        """,
        (playlist_id,)
    )

    playlist = cursor.fetchone()

    if playlist is None:

        connection.close()

        return {
            "success": False,
            "message": "Playlist not found"
        }

    # Get songs belonging to this playlist
    cursor.execute(
        """
        SELECT *
        FROM playlist_songs
        WHERE playlist_id = ?
        ORDER BY added_at ASC
        """,
        (playlist_id,)
    )

    rows = cursor.fetchall()

    connection.close()

    songs = []

    for row in rows:

        songs.append({
            "id": row["id"],
            "videoId": row["video_id"],
            "title": row["title"],

            "artists": (
                row["artists"].split(", ")
                if row["artists"]
                else []
            ),

            "album": row["album"],
            "duration": row["duration"],
            "thumbnail": row["thumbnail"],
            "addedAt": row["added_at"]
        })

    return {
        "success": True,

        "playlist": {
            "id": playlist["id"],
            "name": playlist["name"],
            "createdAt": playlist["created_at"],
            "songs": songs,
            "songCount": len(songs)
        }
    }


# ------------------------------------------------------------
# DELETE PLAYLIST
# ------------------------------------------------------------

@app.delete("/playlists/{playlist_id}")
def delete_playlist(playlist_id: int):

    connection = get_connection()
    cursor = connection.cursor()

    # Delete songs first.
    # This makes deletion work even if SQLite foreign-key
    # cascading is not enabled.
    cursor.execute(
        """
        DELETE FROM playlist_songs
        WHERE playlist_id = ?
        """,
        (playlist_id,)
    )

    # Then delete the playlist itself.
    cursor.execute(
        """
        DELETE FROM playlists
        WHERE id = ?
        """,
        (playlist_id,)
    )

    connection.commit()

    deleted = cursor.rowcount > 0

    connection.close()

    if not deleted:

        return {
            "success": False,
            "message": "Playlist not found"
        }

    return {
        "success": True,
        "message": "Playlist deleted"
    }


# ------------------------------------------------------------
# ADD SONG TO PLAYLIST
# ------------------------------------------------------------

@app.post("/playlists/{playlist_id}/songs")
def add_song_to_playlist(
    playlist_id: int,
    song: dict = Body(...)
):

    connection = get_connection()
    cursor = connection.cursor()

    # Check whether playlist exists
    cursor.execute(
        """
        SELECT id
        FROM playlists
        WHERE id = ?
        """,
        (playlist_id,)
    )

    playlist = cursor.fetchone()

    if playlist is None:

        connection.close()

        return {
            "success": False,
            "message": "Playlist not found"
        }

    video_id = song.get("videoId")

    if not video_id:

        connection.close()

        return {
            "success": False,
            "message": "Song videoId is required"
        }

    try:

        cursor.execute("""
            INSERT OR IGNORE INTO playlist_songs
            (
                playlist_id,
                video_id,
                title,
                artists,
                album,
                duration,
                thumbnail
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, (

            playlist_id,
            video_id,
            song.get("title"),
            ", ".join(song.get("artists", [])),
            song.get("album"),
            song.get("duration"),
            song.get("thumbnail")

        ))

        connection.commit()

        added = cursor.rowcount > 0

        if added:

            message = "Song added to playlist"

        else:

            message = "Song is already in playlist"

        return {
            "success": True,
            "added": added,
            "message": message
        }

    finally:

        connection.close()


# ------------------------------------------------------------
# REMOVE SONG FROM PLAYLIST
# ------------------------------------------------------------

@app.delete("/playlists/{playlist_id}/songs/{video_id}")
def remove_song_from_playlist(
    playlist_id: int,
    video_id: str
):

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute(
        """
        DELETE FROM playlist_songs
        WHERE playlist_id = ?
        AND video_id = ?
        """,
        (playlist_id, video_id)
    )

    connection.commit()

    removed = cursor.rowcount > 0

    connection.close()

    if not removed:

        return {
            "success": False,
            "message": "Song not found in playlist"
        }

    return {
        "success": True,
        "message": "Song removed from playlist"
    }


# ============================================================
# STARTUP MESSAGE
# ============================================================

print("Musica backend loaded successfully.")