from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from ytmusicapi import YTMusic
from yt_dlp import YoutubeDL

app = FastAPI(title="Musica API")


# =========================================
# CORS
# =========================================

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# =========================================
# YOUTUBE MUSIC
# =========================================

yt = YTMusic()


# =========================================
# HOME
# =========================================

@app.get("/")
def home():
    return {
        "message": "Welcome to Musica API",
        "status": "running"
    }


# =========================================
# HEALTH
# =========================================

@app.get("/health")
def health():
    return {
        "status": "healthy"
    }


# =========================================
# SEARCH
# =========================================

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


# =========================================
# GET AUDIO URL
# =========================================

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


# =========================================
# STREAM
# =========================================

@app.get("/stream/{video_id}")
def stream_song(video_id: str):

    try:

        return get_audio_url(video_id)

    except Exception as error:

        print("STREAM ERROR:", repr(error))

        return {
            "error": str(error)
        }