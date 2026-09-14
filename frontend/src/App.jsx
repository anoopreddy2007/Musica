import { useEffect, useRef, useState } from "react";
import "./App.css";

function App() {
  // =========================================
  // LOAD PLAYLISTS
  // =========================================

  useEffect(() => {
    const loadPlaylists = async () => {
      try {
        const response = await fetch(
          "http://127.0.0.1:8000/playlists"
        );

        if (!response.ok) {
          throw new Error("Failed to load playlists");
        }

        const data = await response.json();
        setPlaylists(data.playlists || []);
      } catch (error) {
        console.error("Failed to load playlists:", error);
      }
    };

    loadPlaylists();
  }, []);

  // =========================================
  // SEARCH
  // =========================================

  const [searchQuery, setSearchQuery] = useState("");
  const [songs, setSongs] = useState([]);
  const [loading, setLoading] = useState(false);

  // =========================================
  // LIKED SONGS
  // =========================================

  const [likedSongs, setLikedSongs] = useState([]);
  const [historySongs, setHistorySongs] = useState([]);
  const [currentPage, setCurrentPage] = useState("home");

  // =========================================
  // PLAYLISTS
  // =========================================

  const [playlists, setPlaylists] = useState([]);
  const [selectedPlaylist, setSelectedPlaylist] = useState(null);
  const [playlistLoading, setPlaylistLoading] = useState(false);
  const [playlistPickerSong, setPlaylistPickerSong] = useState(null);

  // =========================================
  // EXPLORE
  // =========================================

  const [exploreSongs, setExploreSongs] = useState([]);
  const [exploreCategory, setExploreCategory] = useState("Trending");
  const [exploreLoading, setExploreLoading] = useState(false);

  const exploreCategories = [
    { name: "Trending", query: "trending songs" },
    { name: "English Hits", query: "english hits" },
    { name: "Bollywood", query: "bollywood hits" },
    { name: "Chill", query: "chill songs" },
    { name: "Workout", query: "workout songs" },
    { name: "Lo-fi", query: "lofi songs" },
    { name: "Romantic", query: "romantic songs" },
    { name: "Classical", query: "classical music" },
  ];

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
  const [playlistPlayback, setPlaylistPlayback] = useState(false);

  // 0 = OFF
  // 1 = REPEAT SONG
  // 2 = REPEAT QUEUE
  const [repeatMode, setRepeatMode] = useState(0);

  // =========================================
  // AUDIO
  // =========================================

  const audioRef = useRef(null);

  // =========================================
  // LOAD LIKED SONGS
  // =========================================

  useEffect(() => {
    const loadLikedSongs = async () => {
      try {
        const response = await fetch(
          "http://127.0.0.1:8000/likes"
        );

        if (!response.ok) {
          throw new Error("Failed to load liked songs");
        }

        const data = await response.json();

        setLikedSongs(data.songs || []);
      } catch (error) {
        console.error(
          "Failed to load liked songs:",
          error
        );
      }
    };

    loadLikedSongs();
  }, []);

  // =========================================
  // LOAD PLAY HISTORY
  // =========================================

  useEffect(() => {
    const loadHistory = async () => {
      try {
        const response = await fetch(
          "http://127.0.0.1:8000/history"
        );

        if (!response.ok) {
          throw new Error("Failed to load history");
        }

        const data = await response.json();
        setHistorySongs(data.songs || []);
      } catch (error) {
        console.error(
          "Failed to load history:",
          error
        );
      }
    };

    loadHistory();
  }, []);

  // =========================================
  // EXPLORE CATEGORY
  // =========================================

  const loadExploreCategory = async (category) => {
    const selectedCategory = exploreCategories.find(
      (item) => item.name === category
    );

    if (!selectedCategory) return;

    setExploreCategory(selectedCategory.name);
    setExploreLoading(true);

    try {
      const response = await fetch(
        `http://127.0.0.1:8000/search?q=${encodeURIComponent(
          selectedCategory.query
        )}`
      );

      if (!response.ok) {
        throw new Error("Explore search failed");
      }

      const data = await response.json();
      setExploreSongs(data.songs || []);
    } catch (error) {
      console.error("Explore failed:", error);
      setExploreSongs([]);
    } finally {
      setExploreLoading(false);
    }
  };

  // Load the default Explore section once.
  useEffect(() => {
    loadExploreCategory("Trending");
  }, []);

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
  // LIKE / UNLIKE SONG
  // =========================================

  const toggleLike = async (song) => {
    if (!song) return;

    const isLiked = likedSongs.some(
      (item) => item.videoId === song.videoId
    );

    try {
      // =====================================
      // UNLIKE
      // =====================================

      if (isLiked) {
        const response = await fetch(
          `http://127.0.0.1:8000/likes/${song.videoId}`,
          {
            method: "DELETE",
          }
        );

        if (!response.ok) {
          throw new Error("Failed to unlike song");
        }

        setLikedSongs((previousSongs) =>
          previousSongs.filter(
            (item) => item.videoId !== song.videoId
          )
        );

        return;
      }

      // =====================================
      // LIKE
      // =====================================

      const response = await fetch(
        "http://127.0.0.1:8000/likes",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(song),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to like song");
      }

      setLikedSongs((previousSongs) => [
        song,
        ...previousSongs,
      ]);
    } catch (error) {
      console.error(
        "Like/unlike failed:",
        error
      );
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
      setAudioUrl(null);

      return;
    }

    // If a song before the currently playing song
    // was removed, adjust currentIndex.
    if (
      index < currentIndex
    ) {
      setCurrentIndex(
        (previousIndex) => previousIndex - 1
      );
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

      newQueue.splice(
        targetIndex,
        0,
        movedSong
      );

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
  // ADD SONG TO HISTORY
  // =========================================

  const saveToHistory = async (song) => {
    if (!song?.videoId) return;

    try {
      const response = await fetch(
        "http://127.0.0.1:8000/history",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(song),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to save history");
      }

      setHistorySongs((previousSongs) => [
        {
          ...song,
          playedAt: new Date().toISOString(),
        },
        ...previousSongs,
      ]);
    } catch (error) {
      console.error(
        "Failed to save history:",
        error
      );
    }
  };

  // =========================================
  // PLAYLIST HELPERS
  // =========================================

  const createPlaylist = async () => {
    const name = window.prompt("Enter playlist name:");

    if (!name || !name.trim()) {
      return;
    }

    try {
      const response = await fetch(
        "http://127.0.0.1:8000/playlists",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: name.trim(),
          }),
        }
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to create playlist");
      }

      const newPlaylist = {
        id: data.playlist.id,
        name: data.playlist.name,
        songCount: 0,
      };

      setPlaylists((previousPlaylists) => [
        newPlaylist,
        ...previousPlaylists,
      ]);

      setSelectedPlaylist({
        ...newPlaylist,
        songs: [],
      });

      setCurrentPage("playlist");
      setSearchQuery("");
    } catch (error) {
      console.error("Failed to create playlist:", error);
      window.alert("Could not create playlist.");
    }
  };

  const openPlaylist = async (playlistId) => {
    try {
      setPlaylistLoading(true);

      const response = await fetch(
        `http://127.0.0.1:8000/playlists/${playlistId}`
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to load playlist");
      }

      setSelectedPlaylist(data.playlist);
      setCurrentPage("playlist");
      setSearchQuery("");
    } catch (error) {
      console.error("Failed to load playlist:", error);
      window.alert("Could not load playlist.");
    } finally {
      setPlaylistLoading(false);
    }
  };

  const deletePlaylist = async () => {
    if (!selectedPlaylist) {
      return;
    }

    const confirmed = window.confirm(
      `Delete "${selectedPlaylist.name}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      const response = await fetch(
        `http://127.0.0.1:8000/playlists/${selectedPlaylist.id}`,
        {
          method: "DELETE",
        }
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to delete playlist");
      }

      setPlaylists((previousPlaylists) =>
        previousPlaylists.filter(
          (playlist) => playlist.id !== selectedPlaylist.id
        )
      );

      setSelectedPlaylist(null);
      setCurrentPage("home");
    } catch (error) {
      console.error("Failed to delete playlist:", error);
      window.alert("Could not delete playlist.");
    }
  };

  const addSongToPlaylist = async (song, playlistId) => {
    if (!song || !playlistId) {
      return;
    }

    try {
      const response = await fetch(
        `http://127.0.0.1:8000/playlists/${playlistId}/songs`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(song),
        }
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to add song");
      }

      // If the playlist being viewed is the same playlist,
      // refresh it immediately.
      if (
        selectedPlaylist &&
        selectedPlaylist.id === playlistId
      ) {
        await openPlaylist(playlistId);
      }

      // Refresh playlist counts in the sidebar.
      const playlistResponse = await fetch(
        "http://127.0.0.1:8000/playlists"
      );

      if (playlistResponse.ok) {
        const playlistData = await playlistResponse.json();
        setPlaylists(playlistData.playlists || []);
      }

      // The picker closes immediately, so no extra prompt is needed.
    } catch (error) {
      console.error("Failed to add song to playlist:", error);
      window.alert("Could not add song to playlist.");
    }
  };

  const removeSongFromPlaylist = async (song) => {
    if (!selectedPlaylist || !song) {
      return;
    }

    try {
      const response = await fetch(
        `http://127.0.0.1:8000/playlists/${selectedPlaylist.id}/songs/${song.videoId}`,
        {
          method: "DELETE",
        }
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to remove song");
      }

      setSelectedPlaylist((previousPlaylist) => {
        if (!previousPlaylist) {
          return previousPlaylist;
        }

        const newSongs = previousPlaylist.songs.filter(
          (item) => item.videoId !== song.videoId
        );

        return {
          ...previousPlaylist,
          songs: newSongs,
          songCount: newSongs.length,
        };
      });

      setPlaylists((previousPlaylists) =>
        previousPlaylists.map((playlist) =>
          playlist.id === selectedPlaylist.id
            ? {
                ...playlist,
                songCount: Math.max(
                  0,
                  (playlist.songCount || 0) - 1
                ),
              }
            : playlist
        )
      );
    } catch (error) {
      console.error(
        "Failed to remove song from playlist:",
        error
      );
      window.alert("Could not remove song.");
    }
  };

  // Open the visual playlist picker for a song.
  const choosePlaylistForSong = (song) => {
    if (!song) {
      return;
    }

    setPlaylistPickerSong(song);
  };

  // Start playback using every song in a playlist.
  // This deliberately replaces the queue so playlist playback
  // also works when the queue was empty.
  const playPlaylist = async (
    playlistSongs,
    startIndex = 0,
    shouldShuffle = false
  ) => {
    if (!playlistSongs || playlistSongs.length === 0) {
      return;
    }

    let playbackList = [...playlistSongs];

    if (shouldShuffle) {
      for (let i = playbackList.length - 1; i > 0; i -= 1) {
        const randomIndex = Math.floor(
          Math.random() * (i + 1)
        );

        [playbackList[i], playbackList[randomIndex]] = [
          playbackList[randomIndex],
          playbackList[i],
        ];
      }

      startIndex = 0;
    }

    setQueue(playbackList);
    setCurrentIndex(startIndex);
    setShuffle(shouldShuffle);
    setPlaylistPlayback(shouldShuffle);

    await playSong(
      playbackList[startIndex],
      startIndex
    );
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
      await saveToHistory(song);
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

    playSong(
      song,
      newQueue.length - 1
    );
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

    // =====================================
    // SHUFFLE
    // =====================================

    // When a playlist was explicitly shuffled, the queue itself
    // already contains the randomized order. Continue through it
    // in order instead of picking a new random song every time.
    if (shuffle && !playlistPlayback) {
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
      } while (
        randomIndex === currentIndex
      );

      playSong(
        queue[randomIndex],
        randomIndex
      );

      return;
    }

    // =====================================
    // NORMAL ORDER
    // =====================================

    const nextIndex =
      currentIndex + 1;

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

    // =====================================
    // SHUFFLE
    // =====================================

    if (shuffle && !playlistPlayback) {
      if (queue.length === 1) {
        playSong(queue[0], 0);
        return;
      }

      let randomIndex;

      do {
        randomIndex = Math.floor(
          Math.random() * queue.length
        );
      } while (
        randomIndex === currentIndex
      );

      playSong(
        queue[randomIndex],
        randomIndex
      );

      return;
    }

    // =====================================
    // NORMAL ORDER
    // =====================================

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
    setShuffle((previous) => !previous);
    setPlaylistPlayback(false);
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

          <button
            className={`nav-item ${
              currentPage === "home" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage("home");
              setSearchQuery("");
              setSelectedPlaylist(null);
            }}
          >
            <span>⌂</span>
            <span>Home</span>
          </button>

          <button
            className={`nav-item ${
              currentPage === "explore" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage("explore");
              setSearchQuery("");
              setSelectedPlaylist(null);
            }}
          >
            <span>🔎</span>
            <span>Explore</span>
          </button>

          <button
            className={`nav-item ${
              currentPage === "library" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage("library");
              setSearchQuery("");
              setSelectedPlaylist(null);
            }}
          >
            <span>♫</span>
            <span>Library</span>
          </button>

        </nav>


        <div className="sidebar-section">

          <div className="sidebar-title">
            Your Library
          </div>

          <button
            className={`nav-item ${
              currentPage === "liked" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage("liked");
              setSearchQuery("");
              setSelectedPlaylist(null);
            }}
          >
            <span>♡</span>
            <span>Liked Songs</span>
          </button>

          <button
            className={`nav-item ${
              currentPage === "history" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage("history");
              setSearchQuery("");
              setSelectedPlaylist(null);
            }}
          >
            <span>◷</span>
            <span>History</span>
          </button>

        </div>


        <div className="sidebar-section">

          <div className="sidebar-title">
            Playlists
          </div>

          <button
            className="nav-item"
            onClick={createPlaylist}
          >
            <span>＋</span>
            <span>Create Playlist</span>
          </button>

          {playlists.map((playlist) => (
            <button
              key={playlist.id}
              className={`nav-item ${
                currentPage === "playlist" &&
                selectedPlaylist?.id === playlist.id
                  ? "active"
                  : ""
              }`}
              onClick={() =>
                openPlaylist(playlist.id)
              }
              title={`${playlist.songCount || 0} songs`}
            >
              <span>♫</span>
              <span>{playlist.name}</span>
            </button>
          ))}

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

          {/* =====================================
              LIKED SONGS PAGE
          ===================================== */}

          {currentPage === "liked" && !searchQuery && (

            <section className="search-results">

              <div className="section-header">

                <div>
                  <h2>Liked Songs</h2>
                  <p>{likedSongs.length} songs</p>
                </div>

                {likedSongs.length > 0 && (
                  <div
                    style={{
                      display: "flex",
                      gap: "8px",
                      flexWrap: "wrap",
                    }}
                  >
                    <button
                      onClick={() =>
                        playPlaylist(likedSongs, 0, false)
                      }
                    >
                      ▶ Play All
                    </button>

                    <button
                      onClick={() =>
                        playPlaylist(likedSongs, 0, true)
                      }
                    >
                      🔀 Shuffle
                    </button>
                  </div>
                )}

              </div>

              {likedSongs.length === 0 ? (

                <div className="empty-state">
                  <div className="empty-icon">♡</div>
                  <h3>No liked songs yet</h3>
                  <p>Like songs and they will appear here.</p>
                </div>

              ) : (

                <div className="song-list">

                  {likedSongs.map((song, index) => (

                    <div
                      className={`song-row ${
                        selectedSong?.videoId === song.videoId
                          ? "playing"
                          : ""
                      }`}
                      key={`${song.videoId}-${index}`}
                      onClick={() =>
                        playPlaylist(likedSongs, index, false)
                      }
                    >

                      <div className="song-number">
                        {selectedSong?.videoId === song.videoId && isPlaying
                          ? "▶"
                          : index + 1}
                      </div>

                      <img
                        className="song-thumbnail"
                        src={song.thumbnail}
                        alt={song.title}
                      />

                      <div className="song-info">
                        <div className="song-title">{song.title}</div>
                        <div className="song-artist">
                          {song.artists?.join(", ") || "Unknown Artist"}
                        </div>
                      </div>

                      <div className="song-album">
                        {song.album || "Unknown Album"}
                      </div>

                      <div className="song-duration">
                        {song.duration || "--:--"}
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

                      <button
                        className="queue-add-button"
                        onClick={(event) => {
                          event.stopPropagation();
                          choosePlaylistForSong(song);
                        }}
                        title="Add to playlist"
                      >
                        ♫
                      </button>

                      <button
                        className="queue-add-button"
                        onClick={(event) => {
                          event.stopPropagation();
                          toggleLike(song);
                        }}
                        title="Unlike"
                      >
                        ♥
                      </button>

                    </div>

                  ))}

                </div>

              )}

            </section>

          )}


          {/* =====================================
              EXPLORE PAGE
          ===================================== */}

          {currentPage === "explore" && !searchQuery && (

            <section className="explore-page">

              <div className="explore-heading">
                <div>
                  <h1>Explore</h1>
                  <p>
                    Discover music by mood, style and category.
                  </p>
                </div>
              </div>

              <div className="explore-categories">
                {exploreCategories.map((category) => (
                  <button
                    key={category.name}
                    className={`explore-chip ${
                      exploreCategory === category.name
                        ? "active"
                        : ""
                    }`}
                    onClick={() =>
                      loadExploreCategory(category.name)
                    }
                  >
                    {category.name}
                  </button>
                ))}
              </div>

              <section className="explore-section">

                <div className="section-header">
                  <div>
                    <h2>{exploreCategory}</h2>
                    <p>Music picked from your search-based catalog</p>
                  </div>

                  {exploreSongs.length > 0 && (
                    <button
                      onClick={() =>
                        playPlaylist(exploreSongs, 0, false)
                      }
                    >
                      ▶ Play All
                    </button>
                  )}
                </div>

                {exploreLoading ? (

                  <div className="explore-loading">
                    <div className="empty-icon">♪</div>
                    <h3>Finding music...</h3>
                    <p>Building your Explore list.</p>
                  </div>

                ) : exploreSongs.length === 0 ? (

                  <div className="library-empty">
                    <div className="empty-icon">♫</div>
                    <h3>No songs found</h3>
                    <p>Try another Explore category.</p>
                  </div>

                ) : (

                  <div className="song-list">
                    {exploreSongs.slice(0, 12).map((song, index) => (
                      <div
                        className={`song-row ${
                          selectedSong?.videoId === song.videoId
                            ? "playing"
                            : ""
                        }`}
                        key={`${song.videoId}-${index}`}
                        onClick={() =>
                          playPlaylist(exploreSongs, index, false)
                        }
                      >
                        <div className="song-number">
                          {selectedSong?.videoId === song.videoId &&
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
                            {song.artists?.join(", ") ||
                              "Unknown Artist"}
                          </div>
                        </div>

                        <div className="song-album">
                          {song.album || "Unknown Album"}
                        </div>

                        <div className="song-duration">
                          {song.duration || "--:--"}
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

                        <button
                          className="queue-add-button"
                          onClick={(event) => {
                            event.stopPropagation();
                            choosePlaylistForSong(song);
                          }}
                          title="Add to playlist"
                        >
                          ♫
                        </button>

                        <button
                          className="queue-add-button"
                          onClick={(event) => {
                            event.stopPropagation();
                            toggleLike(song);
                          }}
                          title={
                            likedSongs.some(
                              (item) =>
                                item.videoId === song.videoId
                            )
                              ? "Unlike"
                              : "Like"
                          }
                        >
                          {likedSongs.some(
                            (item) =>
                              item.videoId === song.videoId
                          )
                            ? "♥"
                            : "♡"}
                        </button>
                      </div>
                    ))}
                  </div>

                )}

              </section>

            </section>

          )}


                    {/* =====================================
              LIBRARY PAGE
          ===================================== */}

          {currentPage === "library" && !searchQuery && (

            <section className="library-page">

              <div className="library-heading">
                <div>
                  <h1>Your Library</h1>
                  <p>
                    Everything you have saved and listened to in Musica.
                  </p>
                </div>

                <button
                  className="library-create-button"
                  onClick={createPlaylist}
                >
                  ＋ Create Playlist
                </button>
              </div>


              <div className="library-cards">

                <button
                  className="library-card"
                  onClick={() => {
                    setCurrentPage("liked");
                    setSearchQuery("");
                    setSelectedPlaylist(null);
                  }}
                >
                  <div className="library-card-icon">♡</div>

                  <div className="library-card-info">
                    <h3>Liked Songs</h3>
                    <p>{likedSongs.length} songs</p>
                  </div>

                  <span className="library-card-arrow">→</span>
                </button>


                <button
                  className="library-card"
                  onClick={() => {
                    setCurrentPage("history");
                    setSearchQuery("");
                    setSelectedPlaylist(null);
                  }}
                >
                  <div className="library-card-icon">◷</div>

                  <div className="library-card-info">
                    <h3>History</h3>
                    <p>{historySongs.length} plays</p>
                  </div>

                  <span className="library-card-arrow">→</span>
                </button>

              </div>


              <section className="library-section">

                <div className="section-header">
                  <h2>Your Playlists</h2>
                </div>

                {playlists.length === 0 ? (

                  <div className="library-empty">

                    <div className="empty-icon">♫</div>

                    <h3>No playlists yet</h3>

                    <p>
                      Create a playlist and start building
                      your personal music collection.
                    </p>

                    <button onClick={createPlaylist}>
                      ＋ Create Playlist
                    </button>

                  </div>

                ) : (

                  <div className="playlist-grid">

                    {playlists.map((playlist) => (

                      <button
                        key={playlist.id}
                        className="playlist-card"
                        onClick={() => openPlaylist(playlist.id)}
                      >

                        <div className="playlist-card-art">
                          <span>♫</span>
                        </div>

                        <div className="playlist-card-name">
                          {playlist.name}
                        </div>

                        <div className="playlist-card-count">
                          {playlist.songCount || 0} songs
                        </div>

                      </button>

                    ))}

                  </div>

                )}

              </section>


              <section className="library-section">

                <div className="section-header">
                  <h2>Recently Played</h2>
                </div>

                {historySongs.length === 0 ? (

                  <div className="library-empty compact">
                    <div className="empty-icon">◷</div>

                    <p>
                      Songs you play will appear here.
                    </p>
                  </div>

                ) : (

                  <div className="song-list">

                    {historySongs.slice(0, 6).map((song, index) => (

                      <div
                        className={`song-row ${
                          selectedSong?.videoId === song.videoId
                            ? "playing"
                            : ""
                        }`}
                        key={`${song.videoId}-${song.playedAt || index}-${index}`}
                        onClick={() => {
                          const historyIndex =
                            historySongs.findIndex(
                              (item, itemIndex) =>
                                item.videoId === song.videoId &&
                                item.playedAt === song.playedAt &&
                                itemIndex >= 0
                            );

                          playPlaylist(
                            historySongs,
                            historyIndex === -1 ? 0 : historyIndex,
                            false
                          );
                        }}
                      >

                        <div className="song-number">
                          {selectedSong?.videoId === song.videoId &&
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
                            {song.artists?.join(", ") ||
                              "Unknown Artist"}
                          </div>

                        </div>

                        <div className="song-album">
                          {song.album || "Unknown Album"}
                        </div>

                        <div className="song-duration">
                          {song.duration || "--:--"}
                        </div>

                      </div>

                    ))}

                  </div>

                )}

              </section>

            </section>

          )}


          {/* =====================================
              HISTORY PAGE
          ===================================== */}

          {currentPage === "history" && !searchQuery && (

            <section className="search-results">

              <div className="section-header">
                <div>
                  <h2>History</h2>
                  <p>{historySongs.length} plays</p>
                </div>

                {historySongs.length > 0 && (
                  <div
                    style={{
                      display: "flex",
                      gap: "8px",
                      flexWrap: "wrap",
                    }}
                  >
                    <button
                      onClick={() =>
                        playPlaylist(historySongs, 0, false)
                      }
                    >
                      ▶ Play All
                    </button>

                    <button
                      onClick={() =>
                        playPlaylist(historySongs, 0, true)
                      }
                    >
                      🔀 Shuffle
                    </button>

                    <button
                    onClick={async () => {
                      try {
                        const response = await fetch(
                          "http://127.0.0.1:8000/history",
                          {
                            method: "DELETE",
                          }
                        );

                        if (!response.ok) {
                          throw new Error(
                            "Failed to clear history"
                          );
                        }

                        setHistorySongs([]);
                      } catch (error) {
                        console.error(
                          "Failed to clear history:",
                          error
                        );
                      }
                    }}
                  >
                    Clear History
                  </button>
                  </div>
                )}
              </div>

              {historySongs.length === 0 ? (

                <div className="empty-state">
                  <div className="empty-icon">
                    ◷
                  </div>

                  <h3>No history yet</h3>

                  <p>
                    Songs you play will appear here.
                  </p>
                </div>

              ) : (

                <div className="song-list">

                  {historySongs.map((song, index) => (

                    <div
                      className={`song-row ${
                        selectedSong?.videoId === song.videoId
                          ? "playing"
                          : ""
                      }`}
                      key={`${song.videoId}-${song.id || index}-${index}`}
                      onClick={() =>
                        playPlaylist(historySongs, index, false)
                      }
                    >

                      <div className="song-number">
                        {selectedSong?.videoId === song.videoId &&
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
                          {song.artists?.join(", ") ||
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

                      <button
                        className="queue-add-button"
                        onClick={(event) => {
                          event.stopPropagation();
                          choosePlaylistForSong(song);
                        }}
                        title="Add to playlist"
                      >
                        ♫
                      </button>

                    </div>

                  ))}

                </div>

              )}

            </section>

          )}

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

                          <button
                            className="queue-add-button"
                            onClick={(event) => {
                              event.stopPropagation();
                              choosePlaylistForSong(song);
                            }}
                            title="Add to playlist"
                          >
                            ♫
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


          {/* =====================================
              PLAYLIST PAGE
          ===================================== */}

          {currentPage === "playlist" && !searchQuery && (

            <section className="search-results">

              {playlistLoading ? (

                <div className="loading">
                  Loading playlist...
                </div>

              ) : selectedPlaylist ? (

                <>

                  <div className="section-header">

                    <div>
                      <h2>{selectedPlaylist.name}</h2>
                      <p>
                        {selectedPlaylist.songs?.length || 0} songs
                      </p>
                    </div>

                    <div
                      style={{
                        display: "flex",
                        gap: "8px",
                        flexWrap: "wrap",
                      }}
                    >

                      {selectedPlaylist.songs?.length > 0 && (

                        <>
                          <button
                            onClick={() =>
                              playPlaylist(
                                selectedPlaylist.songs,
                                0,
                                false
                              )
                            }
                          >
                            ▶ Play All
                          </button>

                          <button
                            onClick={() =>
                              playPlaylist(
                                selectedPlaylist.songs,
                                0,
                                true
                              )
                            }
                          >
                            🔀 Shuffle
                          </button>
                        </>

                      )}

                      <button
                        onClick={deletePlaylist}
                      >
                        Delete Playlist
                      </button>

                    </div>

                  </div>

                  {!selectedPlaylist.songs ||
                  selectedPlaylist.songs.length === 0 ? (

                    <div className="empty-state">

                      <div className="empty-icon">
                        ♫
                      </div>

                      <h3>
                        This playlist is empty
                      </h3>

                      <p>
                        Search for songs and use the ♫ button
                        to add them here.
                      </p>

                    </div>

                  ) : (

                    <div className="song-list">

                      {selectedPlaylist.songs.map(
                        (song, index) => (

                          <div
                            className={`song-row ${
                              selectedSong?.videoId === song.videoId
                                ? "playing"
                                : ""
                            }`}
                            key={`${song.videoId}-${song.id || index}`}
                            onClick={() =>
                              playPlaylist(
                                selectedPlaylist.songs,
                                index,
                                false
                              )
                            }
                          >

                            <div className="song-number">
                              {selectedSong?.videoId === song.videoId &&
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
                                {song.artists?.join(", ") ||
                                  "Unknown Artist"}
                              </div>

                            </div>

                            <div className="song-album">
                              {song.album || "Unknown Album"}
                            </div>

                            <div className="song-duration">
                              {song.duration || "--:--"}
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

                            <button
                              className="queue-add-button"
                              onClick={(event) => {
                                event.stopPropagation();
                                removeSongFromPlaylist(song);
                              }}
                              title="Remove from playlist"
                            >
                              ×
                            </button>

                          </div>

                        )
                      )}

                    </div>

                  )}

                </>

              ) : (

                <div className="empty-state">
                  <h3>No playlist selected</h3>
                </div>

              )}

            </section>

          )}

          {currentPage === "home" && !searchQuery && (

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
          PLAYLIST PICKER MODAL
      ===================================== */}

      {playlistPickerSong && (

        <div
          onClick={() => setPlaylistPickerSong(null)}
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0, 0, 0, 0.68)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 2000,
            padding: "20px",
          }}
        >

          <div
            onClick={(event) => event.stopPropagation()}
            style={{
              width: "min(460px, 100%)",
              maxHeight: "80vh",
              overflowY: "auto",
              background: "#181818",
              border: "1px solid #333",
              borderRadius: "16px",
              padding: "22px",
              boxShadow: "0 20px 60px rgba(0,0,0,0.5)",
            }}
          >

            <div
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                gap: "16px",
                marginBottom: "8px",
              }}
            >
              <div>
                <h2 style={{ margin: 0 }}>
                  Add to playlist
                </h2>
                <p
                  style={{
                    margin: "6px 0 0",
                    opacity: 0.65,
                    fontSize: "14px",
                  }}
                >
                  {playlistPickerSong.title}
                </p>
              </div>

              <button
                onClick={() => setPlaylistPickerSong(null)}
                style={{
                  width: "36px",
                  height: "36px",
                  borderRadius: "50%",
                  border: "1px solid #444",
                  background: "transparent",
                  color: "inherit",
                  cursor: "pointer",
                  fontSize: "22px",
                }}
                title="Close"
              >
                ×
              </button>
            </div>

            {playlists.length === 0 ? (

              <div style={{ paddingTop: "20px" }}>
                <div
                  style={{
                    padding: "24px 12px",
                    textAlign: "center",
                    opacity: 0.75,
                  }}
                >
                  You don't have any playlists yet.
                </div>

                <button
                  onClick={async () => {
                    setPlaylistPickerSong(null);
                    await createPlaylist();
                  }}
                  style={{
                    width: "100%",
                    padding: "13px 16px",
                    borderRadius: "10px",
                    border: "none",
                    cursor: "pointer",
                    fontWeight: 600,
                  }}
                >
                  ＋ Create Playlist
                </button>
              </div>

            ) : (

              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "8px",
                  marginTop: "18px",
                }}
              >

                {playlists.map((playlist) => (

                  <button
                    key={playlist.id}
                    onClick={async () => {
                      const songToAdd = playlistPickerSong;
                      setPlaylistPickerSong(null);
                      await addSongToPlaylist(songToAdd, playlist.id);
                    }}
                    style={{
                      width: "100%",
                      display: "flex",
                      alignItems: "center",
                      gap: "14px",
                      padding: "12px 14px",
                      borderRadius: "10px",
                      border: "1px solid #333",
                      background: "#222",
                      color: "inherit",
                      textAlign: "left",
                      cursor: "pointer",
                    }}
                  >
                    <span
                      style={{
                        width: "42px",
                        height: "42px",
                        borderRadius: "8px",
                        display: "grid",
                        placeItems: "center",
                        background: "#303030",
                        fontSize: "20px",
                        flexShrink: 0,
                      }}
                    >
                      ♫
                    </span>

                    <span style={{ flex: 1, minWidth: 0 }}>
                      <span
                        style={{
                          display: "block",
                          fontWeight: 600,
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                          whiteSpace: "nowrap",
                        }}
                      >
                        {playlist.name}
                      </span>
                      <span
                        style={{
                          display: "block",
                          marginTop: "3px",
                          fontSize: "13px",
                          opacity: 0.6,
                        }}
                      >
                        {playlist.songCount || 0} songs
                      </span>
                    </span>

                    <span style={{ opacity: 0.6, fontSize: "18px" }}>
                      ›
                    </span>
                  </button>

                ))}

                <button
                  onClick={async () => {
                    setPlaylistPickerSong(null);
                    await createPlaylist();
                  }}
                  style={{
                    width: "100%",
                    marginTop: "6px",
                    padding: "12px 16px",
                    borderRadius: "10px",
                    border: "1px dashed #555",
                    background: "transparent",
                    color: "inherit",
                    cursor: "pointer",
                    textAlign: "left",
                  }}
                >
                  ＋ Create New Playlist
                </button>

              </div>

            )}

          </div>

        </div>

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


              {/* LIKE BUTTON */}

              <button
                className={`like-button ${
                  likedSongs.some(
                    (song) =>
                      song.videoId ===
                      selectedSong.videoId
                  )
                    ? "liked"
                    : ""
                }`}
                onClick={() =>
                  toggleLike(selectedSong)
                }
                title={
                  likedSongs.some(
                    (song) =>
                      song.videoId ===
                      selectedSong.videoId
                  )
                    ? "Unlike"
                    : "Like"
                }
              >
                {likedSongs.some(
                  (song) =>
                    song.videoId ===
                    selectedSong.videoId
                )
                  ? "♥"
                  : "♡"}
              </button>

              <button
                className="like-button"
                onClick={() =>
                  choosePlaylistForSong(selectedSong)
                }
                title="Add to playlist"
              >
                ♫
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