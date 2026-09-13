import { useEffect, useRef, useState } from "react";
import "./App.css";

function App() {
  // =========================================
  // SEARCH
  // =========================================

  const [searchQuery, setSearchQuery] = useState("");
  const [songs, setSongs] = useState([]);
  const [loading, setLoading] = useState(false);

  // =========================================
  // PLAYER
  // =========================================

  const [selectedSong, setSelectedSong] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(-1);

  const [isPlaying, setIsPlaying] = useState(false);
  const [audioLoading, setAudioLoading] = useState(false);

  const [audioUrl, setAudioUrl] = useState(null);

  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);

  // =========================================
  // QUEUE
  // =========================================

  const [queue, setQueue] = useState([]);
  const [showQueue, setShowQueue] = useState(false);

  // Used while dragging a queue item
  const [draggedIndex, setDraggedIndex] = useState(null);

  // =========================================
  // SHUFFLE / REPEAT
  // =========================================

  const [shuffle, setShuffle] = useState(false);

  // 0 = OFF
  // 1 = REPEAT SONG
  // 2 = REPEAT QUEUE
  const [repeatMode, setRepeatMode] = useState(0);

  // =========================================
  // AUDIO
  // =========================================

  const audioRef = useRef(null);

  // =========================================
  // SEARCH
  // =========================================

  const searchSongs = async () => {
    if (!searchQuery.trim()) return;

    setLoading(true);

    try {
      const response = await fetch(
        `http://127.0.0.1:8000/search?q=${encodeURIComponent(
          searchQuery
        )}`
      );

      if (!response.ok) {
        throw new Error("Search failed");
      }

      const data = await response.json();

      setSongs(data.songs || []);
    } catch (error) {
      console.error("Search failed:", error);
    } finally {
      setLoading(false);
    }
  };

  // =========================================
  // SEARCH ENTER
  // =========================================

  const handleSearchKeyDown = (event) => {
    if (event.key === "Enter") {
      searchSongs();
    }
  };

  // =========================================
  // ADD TO QUEUE
  // =========================================

  const addToQueue = (song) => {
    setQueue((previousQueue) => {
      const exists = previousQueue.some(
        (item) => item.videoId === song.videoId
      );

      if (exists) {
        return previousQueue;
      }

      return [...previousQueue, song];
    });
  };

  // =========================================
  // REMOVE FROM QUEUE
  // =========================================

  const removeFromQueue = (index) => {
    const removedSong = queue[index];

    setQueue((previousQueue) =>
      previousQueue.filter(
        (_, songIndex) => songIndex !== index
      )
    );

    // If the removed song is currently playing,
    // stop playback.
    if (
      removedSong &&
      selectedSong?.videoId === removedSong.videoId
    ) {
      if (audioRef.current) {
        audioRef.current.pause();
      }

      setSelectedSong(null);
      setCurrentIndex(-1);
      setIsPlaying(false);
    }
  };

  // =========================================
  // CLEAR QUEUE
  // =========================================

  const clearQueue = () => {
    setQueue([]);

    if (audioRef.current) {
      audioRef.current.pause();
    }

    setSelectedSong(null);
    setCurrentIndex(-1);
    setIsPlaying(false);
    setAudioUrl(null);
  };

  // =========================================
  // DRAG START
  // =========================================

  const handleDragStart = (event, index) => {
    setDraggedIndex(index);

    event.dataTransfer.effectAllowed = "move";

    event.dataTransfer.setData(
      "text/plain",
      index.toString()
    );
  };

  // =========================================
  // DRAG OVER
  // =========================================

  const handleDragOver = (event) => {
    event.preventDefault();

    event.dataTransfer.dropEffect = "move";
  };

  // =========================================
  // DROP
  // =========================================

  const handleDrop = (event, targetIndex) => {
    event.preventDefault();

    const sourceIndex = Number(
      event.dataTransfer.getData("text/plain")
    );

    if (
      isNaN(sourceIndex) ||
      sourceIndex === targetIndex
    ) {
      setDraggedIndex(null);
      return;
    }

    setQueue((previousQueue) => {
      const newQueue = [...previousQueue];

      const [movedSong] =
        newQueue.splice(sourceIndex, 1);

      newQueue.splice(targetIndex, 0, movedSong);

      return newQueue;
    });

    /*
      Important:

      If we move a song around, we also need
      to update currentIndex so the player still
      knows where the currently playing song is.
    */

    setCurrentIndex((previousIndex) => {
      if (previousIndex === -1) {
        return previousIndex;
      }

      if (sourceIndex === previousIndex) {
        return targetIndex;
      }

      if (
        sourceIndex < previousIndex &&
        targetIndex >= previousIndex
      ) {
        return previousIndex - 1;
      }

      if (
        sourceIndex > previousIndex &&
        targetIndex <= previousIndex
      ) {
        return previousIndex + 1;
      }

      return previousIndex;
    });

    setDraggedIndex(null);
  };

  // =========================================
  // DRAG END
  // =========================================

  const handleDragEnd = () => {
    setDraggedIndex(null);
  };

  // =========================================
  // PLAY SONG
  // =========================================

  const playSong = async (song, index) => {
    try {
      setSelectedSong(song);
      setCurrentIndex(index);

      setIsPlaying(false);
      setAudioLoading(true);

      setCurrentTime(0);
      setDuration(0);
      setAudioUrl(null);

      const response = await fetch(
        `http://127.0.0.1:8000/stream/${song.videoId}`
      );

      if (!response.ok) {
        throw new Error("Could not get stream");
      }

      const data = await response.json();

      if (!data.url) {
        throw new Error("No audio URL returned");
      }

      setAudioUrl(data.url);
    } catch (error) {
      console.error("Playback failed:", error);
      setAudioLoading(false);
    }
  };

  // =========================================
  // SELECT SEARCH RESULT
  // =========================================

  const selectSong = (song) => {
    if (queue.length === 0) {
      const newQueue = [...songs];

      setQueue(newQueue);

      const index = newQueue.findIndex(
        (item) => item.videoId === song.videoId
      );

      playSong(song, index);

      return;
    }

    const queueIndex = queue.findIndex(
      (item) => item.videoId === song.videoId
    );

    if (queueIndex !== -1) {
      playSong(song, queueIndex);
      return;
    }

    const newQueue = [...queue, song];

    setQueue(newQueue);

    playSong(song, newQueue.length - 1);
  };

  // =========================================
  // LOAD AUDIO
  // =========================================

  useEffect(() => {
    if (!audioUrl || !audioRef.current) {
      return;
    }

    const audio = audioRef.current;

    audio.src = audioUrl;

    audio.load();

    audio
      .play()
      .then(() => {
        setIsPlaying(true);
        setAudioLoading(false);
      })
      .catch((error) => {
        console.error(
          "Audio playback error:",
          error
        );

        setIsPlaying(false);
        setAudioLoading(false);
      });
  }, [audioUrl]);

  // =========================================
  // NEXT SONG
  // =========================================

  const playNext = () => {
    if (queue.length === 0) {
      return;
    }

    // SHUFFLE

    if (shuffle) {
      if (queue.length === 1) {
        if (repeatMode !== 0) {
          playSong(queue[0], 0);
        }

        return;
      }

      let randomIndex;

      do {
        randomIndex = Math.floor(
          Math.random() * queue.length
        );
      } while (randomIndex === currentIndex);

      playSong(
        queue[randomIndex],
        randomIndex
      );

      return;
    }

    // NORMAL ORDER

    const nextIndex = currentIndex + 1;

    if (nextIndex >= queue.length) {
      if (repeatMode === 2) {
        playSong(queue[0], 0);
        return;
      }

      setIsPlaying(false);

      return;
    }

    playSong(
      queue[nextIndex],
      nextIndex
    );
  };

  // =========================================
  // PREVIOUS SONG
  // =========================================

  const playPrevious = () => {
    if (queue.length === 0) {
      return;
    }

    if (
      audioRef.current &&
      audioRef.current.currentTime > 3
    ) {
      audioRef.current.currentTime = 0;
      return;
    }

    if (shuffle) {
      if (queue.length === 1) {
        playSong(queue[0], 0);
        return;
      }

      let randomIndex;

      do {
        randomIndex = Math.floor(
          Math.random() * queue.length
        );
      } while (randomIndex === currentIndex);

      playSong(
        queue[randomIndex],
        randomIndex
      );

      return;
    }

    const previousIndex =
      currentIndex - 1;

    if (previousIndex < 0) {
      if (repeatMode === 2) {
        const lastIndex =
          queue.length - 1;

        playSong(
          queue[lastIndex],
          lastIndex
        );
      }

      return;
    }

    playSong(
      queue[previousIndex],
      previousIndex
    );
  };

  // =========================================
  // AUDIO EVENTS
  // =========================================

  useEffect(() => {
    const audio = audioRef.current;

    if (!audio) return;

    const handlePlay = () => {
      setIsPlaying(true);
    };

    const handlePause = () => {
      setIsPlaying(false);
    };

    const handleLoadedMetadata = () => {
      setDuration(audio.duration);
    };

    const handleTimeUpdate = () => {
      setCurrentTime(audio.currentTime);
    };

    const handleEnded = () => {
      if (repeatMode === 1) {
        audio.currentTime = 0;

        audio.play();

        return;
      }

      playNext();
    };

    audio.addEventListener(
      "play",
      handlePlay
    );

    audio.addEventListener(
      "pause",
      handlePause
    );

    audio.addEventListener(
      "loadedmetadata",
      handleLoadedMetadata
    );

    audio.addEventListener(
      "timeupdate",
      handleTimeUpdate
    );

    audio.addEventListener(
      "ended",
      handleEnded
    );

    return () => {
      audio.removeEventListener(
        "play",
        handlePlay
      );

      audio.removeEventListener(
        "pause",
        handlePause
      );

      audio.removeEventListener(
        "loadedmetadata",
        handleLoadedMetadata
      );

      audio.removeEventListener(
        "timeupdate",
        handleTimeUpdate
      );

      audio.removeEventListener(
        "ended",
        handleEnded
      );
    };
  }, [
    repeatMode,
    shuffle,
    currentIndex,
    queue
  ]);

  // =========================================
  // PLAY / PAUSE
  // =========================================

  const togglePlayPause = async () => {
    const audio = audioRef.current;

    if (!audio || !selectedSong) {
      return;
    }

    try {
      if (audio.paused) {
        await audio.play();

        setIsPlaying(true);
      } else {
        audio.pause();

        setIsPlaying(false);
      }
    } catch (error) {
      console.error(
        "Play/pause error:",
        error
      );
    }
  };

  // =========================================
  // SHUFFLE
  // =========================================

  const toggleShuffle = () => {
    setShuffle(
      (previous) => !previous
    );
  };

  // =========================================
  // REPEAT
  // =========================================

  const toggleRepeat = () => {
    setRepeatMode((previous) => {
      if (previous === 0) {
        return 1;
      }

      if (previous === 1) {
        return 2;
      }

      return 0;
    });
  };

  // =========================================
  // SEEK
  // =========================================

  const handleSeek = (event) => {
    const newTime =
      Number(event.target.value);

    if (!audioRef.current) {
      return;
    }

    audioRef.current.currentTime =
      newTime;

    setCurrentTime(newTime);
  };

  // =========================================
  // VOLUME
  // =========================================

  const handleVolumeChange = (event) => {
    const volume =
      Number(event.target.value) / 100;

    if (audioRef.current) {
      audioRef.current.volume =
        volume;
    }
  };

  // =========================================
  // FORMAT TIME
  // =========================================

  const formatTime = (time) => {
    if (!time || isNaN(time)) {
      return "0:00";
    }

    const minutes =
      Math.floor(time / 60);

    const seconds =
      Math.floor(time % 60);

    return `${minutes}:${seconds
      .toString()
      .padStart(2, "0")}`;
  };

  // =========================================
  // UI
  // =========================================

  return (
    <div className="musica-app">

      {/* =====================================
          SIDEBAR
      ===================================== */}

      <aside className="sidebar">

        <div className="logo">

          <span className="logo-icon">
            ♪
          </span>

          <span>
            Musica
          </span>

        </div>


        <nav className="sidebar-nav">

          <button className="nav-item active">
            <span>⌂</span>
            <span>Home</span>
          </button>

          <button className="nav-item">
            <span>🔎</span>
            <span>Explore</span>
          </button>

          <button className="nav-item">
            <span>♫</span>
            <span>Library</span>
          </button>

        </nav>


        <div className="sidebar-section">

          <div className="sidebar-title">
            Your Library
          </div>

          <button className="nav-item">
            <span>♡</span>
            <span>Liked Songs</span>
          </button>

          <button className="nav-item">
            <span>◷</span>
            <span>History</span>
          </button>

        </div>


        <div className="sidebar-section">

          <div className="sidebar-title">
            Playlists
          </div>

          <button className="nav-item">
            <span>＋</span>
            <span>Create Playlist</span>
          </button>

          <button className="nav-item">
            <span>♫</span>
            <span>My Playlist</span>
          </button>

        </div>

      </aside>


      {/* =====================================
          MAIN
      ===================================== */}

      <main className="main-content">

        <header className="topbar">

          <div className="search-box">

            <span className="search-icon">
              🔎
            </span>

            <input
              type="text"
              value={searchQuery}
              onChange={(event) =>
                setSearchQuery(
                  event.target.value
                )
              }
              onKeyDown={
                handleSearchKeyDown
              }
              placeholder="Search songs, artists or albums"
            />

            <button
              className="search-button"
              onClick={searchSongs}
            >
              Search
            </button>

          </div>


          <button className="settings-button">
            ⚙
          </button>

        </header>


        {/* =====================================
            CONTENT
        ===================================== */}

        <section className="content">

          {searchQuery && (

            <section className="search-results">

              <div className="section-header">

                <h2>
                  Search Results
                </h2>

              </div>


              {loading && (
                <div className="loading">
                  Searching...
                </div>
              )}


              {!loading &&
                songs.length > 0 && (

                  <div className="song-list">

                    {songs.map(
                      (song, index) => (

                        <div
                          className={`song-row ${
                            selectedSong?.videoId ===
                            song.videoId
                              ? "playing"
                              : ""
                          }`}
                          key={
                            song.videoId ||
                            index
                          }
                          onClick={() =>
                            selectSong(song)
                          }
                        >

                          <div className="song-number">
                            {selectedSong?.videoId ===
                              song.videoId &&
                            isPlaying
                              ? "▶"
                              : index + 1}
                          </div>


                          <img
                            className="song-thumbnail"
                            src={song.thumbnail}
                            alt={song.title}
                          />


                          <div className="song-info">

                            <div className="song-title">
                              {song.title}
                            </div>

                            <div className="song-artist">
                              {song.artists?.join(
                                ", "
                              ) ||
                                "Unknown Artist"}
                            </div>

                          </div>


                          <div className="song-album">
                            {song.album ||
                              "Unknown Album"}
                          </div>


                          <div className="song-duration">
                            {song.duration ||
                              "--:--"}
                          </div>


                          <button
                            className="queue-add-button"
                            onClick={(event) => {

                              event.stopPropagation();

                              addToQueue(song);

                            }}
                            title="Add to queue"
                          >
                            ＋
                          </button>

                        </div>

                      )
                    )}

                  </div>

                )}


              {!loading &&
                songs.length === 0 && (

                  <div className="empty-state">
                    No songs found.
                  </div>

                )}

            </section>

          )}


          {!searchQuery && (

            <>

              <section className="hero">

                <div className="hero-content">

                  <h1>
                    Welcome to Musica
                  </h1>

                  <p>
                    Your music. Your library.
                    Your way.
                  </p>

                  <p className="hero-subtitle">
                    Search for any song to
                    start listening.
                  </p>

                </div>

              </section>


              <section className="home-section">

                <div className="section-header">

                  <h2>
                    Quick Picks
                  </h2>

                </div>


                <div className="empty-home">

                  <div className="empty-icon">
                    ♪
                  </div>

                  <h3>
                    Find something you love
                  </h3>

                  <p>
                    Use the search bar above
                    to discover music.
                  </p>

                </div>

              </section>

            </>

          )}

        </section>

      </main>


      {/* =====================================
          QUEUE PANEL
      ===================================== */}

      {showQueue && (

        <aside className="queue-panel">

          <div className="queue-header">

            <div>

              <h2>
                Queue
              </h2>

              <span>
                {queue.length} songs
              </span>

            </div>


            <button
              className="queue-close"
              onClick={() =>
                setShowQueue(false)
              }
            >
              ×
            </button>

          </div>


          {queue.length === 0 ? (

            <div className="queue-empty">

              <div className="empty-icon">
                ♫
              </div>

              <h3>
                Your queue is empty
              </h3>

              <p>
                Add songs from search
                results.
              </p>

            </div>

          ) : (

            <>

              <div className="queue-actions">

                <button
                  onClick={clearQueue}
                >
                  Clear Queue
                </button>

              </div>


              <div className="queue-list">

                {queue.map(
                  (song, index) => (

                    <div
                      className={`queue-item ${
                        selectedSong?.videoId ===
                        song.videoId
                          ? "queue-current"
                          : ""
                      } ${
                        draggedIndex === index
                          ? "queue-dragging"
                          : ""
                      }`}
                      key={`${song.videoId}-${index}`}
                      draggable
                      onDragStart={(event) =>
                        handleDragStart(
                          event,
                          index
                        )
                      }
                      onDragOver={
                        handleDragOver
                      }
                      onDrop={(event) =>
                        handleDrop(
                          event,
                          index
                        )
                      }
                      onDragEnd={
                        handleDragEnd
                      }
                    >

                      {/* DRAG HANDLE */}

                      <div
                        className="queue-drag-handle"
                        title="Drag to reorder"
                      >
                        ⋮⋮
                      </div>


                      {/* NUMBER */}

                      <div
                        className="queue-item-number"
                        onClick={() =>
                          playSong(
                            song,
                            index
                          )
                        }
                      >

                        {selectedSong?.videoId ===
                          song.videoId &&
                        isPlaying
                          ? "▶"
                          : index + 1}

                      </div>


                      {/* IMAGE */}

                      <img
                        src={song.thumbnail}
                        alt={song.title}
                        onClick={() =>
                          playSong(
                            song,
                            index
                          )
                        }
                      />


                      {/* SONG INFO */}

                      <div
                        className="queue-item-info"
                        onClick={() =>
                          playSong(
                            song,
                            index
                          )
                        }
                      >

                        <div>
                          {song.title}
                        </div>

                        <span>
                          {song.artists?.join(
                            ", "
                          ) ||
                            "Unknown Artist"}
                        </span>

                      </div>


                      {/* REMOVE */}

                      <button
                        className="queue-remove"
                        onClick={(event) => {

                          event.stopPropagation();

                          removeFromQueue(
                            index
                          );

                        }}
                        title="Remove from queue"
                      >
                        ×
                      </button>

                    </div>

                  )
                )}

              </div>

            </>

          )}

        </aside>

      )}


      {/* =====================================
          PLAYER
      ===================================== */}

      <footer className="player">

        <audio
          ref={audioRef}
          preload="auto"
        />


        {/* CURRENT SONG */}

        <div className="current-song">

          {selectedSong ? (

            <>

              <img
                className="player-thumbnail"
                src={selectedSong.thumbnail}
                alt={selectedSong.title}
              />


              <div className="player-song-info">

                <div className="player-song-title">
                  {selectedSong.title}
                </div>

                <div className="player-song-artist">
                  {selectedSong.artists?.join(
                    ", "
                  ) ||
                    "Unknown Artist"}
                </div>

              </div>


              <button className="like-button">
                ♡
              </button>

            </>

          ) : (

            <div className="no-song">
              No song selected
            </div>

          )}

        </div>


        {/* CONTROLS */}

        <div className="player-controls">

          <div className="control-buttons">

            <button
              className={`control-button ${
                shuffle
                  ? "active-control"
                  : ""
              }`}
              onClick={
                toggleShuffle
              }
              title="Shuffle"
            >
              🔀
            </button>


            <button
              className="control-button"
              onClick={
                playPrevious
              }
              disabled={
                !selectedSong
              }
              title="Previous"
            >
              ⏮
            </button>


            <button
              className="play-button"
              onClick={
                togglePlayPause
              }
              disabled={
                !selectedSong ||
                audioLoading
              }
              title={
                isPlaying
                  ? "Pause"
                  : "Play"
              }
            >

              {audioLoading
                ? "..."
                : isPlaying
                ? "❚❚"
                : "▶"}

            </button>


            <button
              className="control-button"
              onClick={
                playNext
              }
              disabled={
                !selectedSong
              }
              title="Next"
            >
              ⏭
            </button>


            <button
              className={`control-button ${
                repeatMode !== 0
                  ? "active-control"
                  : ""
              }`}
              onClick={
                toggleRepeat
              }
              title="Repeat"
            >

              {repeatMode === 1
                ? "🔂"
                : "🔁"}

            </button>

          </div>


          {/* PROGRESS */}

          <div className="progress-container">

            <span className="time">
              {formatTime(
                currentTime
              )}
            </span>


            <input
              type="range"
              min="0"
              max={duration || 0}
              value={currentTime}
              onChange={
                handleSeek
              }
              className="progress-bar"
              disabled={
                !selectedSong
              }
            />


            <span className="time">
              {formatTime(
                duration
              )}
            </span>

          </div>

        </div>


        {/* VOLUME + QUEUE */}

        <div className="volume">

          <button className="volume-button">
            🔊
          </button>


          <input
            type="range"
            min="0"
            max="100"
            defaultValue="100"
            className="volume-slider"
            onChange={
              handleVolumeChange
            }
          />


          <button
            className={`queue-button ${
              showQueue
                ? "active-control"
                : ""
            }`}
            onClick={() =>
              setShowQueue(
                (previous) =>
                  !previous
              )
            }
            title="Queue"
          >
            ☷
          </button>

        </div>

      </footer>

    </div>
  );
}

export default App;