import { useEffect, useState, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Box,
  Card,
  CircularProgress,
  Typography,
  Snackbar,
  Alert,
} from "@mui/material";
import { isAuthenticated, logOut } from "../services/authenticationService";
import Scene from "./Scene";
import Post from "../components/Post";
import PostComposer from "../components/PostComposer";
import { getFeed } from "../services/postService";

export default function Home() {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [hasMore, setHasMore] = useState(false);
  const observer = useRef();
  const lastPostElementRef = useRef();
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");

  const navigate = useNavigate();

  const showMessage = (message) => {
    setSnackbarMessage(message);
    setSnackbarOpen(true);
  };

  // Handle Snackbar close
  const handleSnackbarClose = (event, reason) => {
    if (reason === "clickaway") {
      return;
    }
    setSnackbarOpen(false);
  };

  const handlePostCreated = (post) => {
    setPosts((prevPosts) => [post, ...prevPosts]);
    showMessage("Post created successfully!");
  };

  const handlePostUpdated = (updatedPost) => {
    setPosts((prevPosts) =>
      prevPosts.map((post) => (post.id === updatedPost.id ? updatedPost : post))
    );
    showMessage("Post updated.");
  };

  const handlePostDeleted = (postId) => {
    setPosts((prevPosts) => prevPosts.filter((post) => post.id !== postId));
    showMessage("Post deleted.");
  };

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate("/login");
    } else {
      loadPosts(page);
    }
  }, [navigate, page]);

  const loadPosts = (page) => {
    console.log(`loading posts for page ${page}`);
    setLoading(true);
    getFeed(page)
      .then((response) => {
        setTotalPages(response.data.result.totalPages);
        setPosts((prevPosts) => [...prevPosts, ...response.data.result.data]);
        setHasMore(response.data.result.data.length > 0);
        console.log("loaded posts:", response.data.result);
      })
      .catch((error) => {
        if (error.response?.status === 401) {
          logOut();
          navigate("/login");
        }
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    if (!hasMore) return;

    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        if (page < totalPages) {
          setPage((prevPage) => prevPage + 1);
        }
      }
    });
    if (lastPostElementRef.current) {
      observer.current.observe(lastPostElementRef.current);
    }

    setHasMore(false);
  }, [hasMore]);

  return (
    <Scene>
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={4000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "top", horizontal: "right" }}
        sx={{ marginTop: "64px" }} // Position below the header
      >
        <Alert onClose={handleSnackbarClose} severity="success" sx={{ width: "100%" }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>
      <Card
        sx={{
          minWidth: 500,
          maxWidth: 600,
          width: "100%",
          boxShadow: 3,
          borderRadius: 2,
          mt: "20px",
          mb: "20px",
          padding: "20px",
        }}
      >
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
            width: "100%",
            gap: "10px",
          }}
        >
          <PostComposer onCreated={handlePostCreated} />
          <Typography sx={{ fontSize: 18, mb: "10px" }}>Your feed</Typography>
          {!loading && posts.length === 0 && (
            <Typography color="text.secondary" sx={{ fontSize: 14 }}>
              Your feed is empty. Write your first post above, or{" "}
              <Link to="/search">find people to follow</Link>.
            </Typography>
          )}
          {posts.map((post, index) => (
            <Post
              ref={posts.length === index + 1 ? lastPostElementRef : undefined}
              key={post.id}
              post={post}
              onUpdated={handlePostUpdated}
              onDeleted={handlePostDeleted}
            />
          ))}
          {loading && (
            <Box sx={{ display: "flex", justifyContent: "center", width: "100%" }}>
              <CircularProgress size="24px" />
            </Box>
          )}
        </Box>
      </Card>
    </Scene>
  );
}
