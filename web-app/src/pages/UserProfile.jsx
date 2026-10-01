import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Alert,
  Avatar,
  Box,
  Button,
  Card,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  List,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  Typography,
} from "@mui/material";
import Scene from "./Scene";
import Post from "../components/Post";
import PostComposer from "../components/PostComposer";
import { isAuthenticated, logOut } from "../services/authenticationService";
import {
  followUser,
  getFollowers,
  getFollowing,
  getMyInfo,
  getUserDetail,
  unfollowUser,
} from "../services/userService";
import { getUserPosts } from "../services/postService";

const displayName = (profile) =>
  profile?.firstName || profile?.lastName
    ? `${profile.firstName ?? ""} ${profile.lastName ?? ""}`.trim()
    : profile?.username;

export default function UserProfile() {
  const { userId } = useParams();
  const navigate = useNavigate();

  const [detail, setDetail] = useState(null);
  const [error, setError] = useState(null);
  const [followLoading, setFollowLoading] = useState(false);

  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [postsLoading, setPostsLoading] = useState(false);

  const [listDialog, setListDialog] = useState(null); // "followers" | "following" | null
  const [listUsers, setListUsers] = useState([]);
  const [listLoading, setListLoading] = useState(false);

  const handleApiError = useCallback(
    (err, fallbackMessage) => {
      if (err.response?.status === 401) {
        logOut();
        navigate("/login");
        return;
      }
      setError(err.response?.data?.message || fallbackMessage);
    },
    [navigate]
  );

  const loadPosts = useCallback(
    (targetUserId, pageToLoad) => {
      setPostsLoading(true);
      getUserPosts(targetUserId, pageToLoad)
        .then((response) => {
          const result = response.data.result;
          setTotalPages(result.totalPages);
          setPosts((prev) =>
            pageToLoad === 1 ? result.data : [...prev, ...result.data]
          );
          setPage(pageToLoad);
        })
        .catch((err) => handleApiError(err, "Failed to load posts."))
        .finally(() => setPostsLoading(false));
    },
    [handleApiError]
  );

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate("/login");
      return;
    }

    // "/users/me" resolves to the signed-in user's id
    if (userId === "me") {
      getMyInfo()
        .then((response) =>
          navigate(`/users/${response.data.result.userId}`, { replace: true })
        )
        .catch((err) => handleApiError(err, "Failed to load your profile."));
      return;
    }

    setDetail(null);
    setError(null);
    setPosts([]);
    setPage(1);
    setTotalPages(0);
    setListDialog(null);

    getUserDetail(userId)
      .then((response) => setDetail(response.data.result))
      .catch((err) => handleApiError(err, "User not found."));

    loadPosts(userId, 1);
  }, [userId, navigate, handleApiError, loadPosts]);

  const handleToggleFollow = () => {
    setFollowLoading(true);
    const action = detail.followedByMe ? unfollowUser : followUser;
    action(userId)
      .then((response) => setDetail(response.data.result))
      .catch((err) => handleApiError(err, "Action failed. Please try again."))
      .finally(() => setFollowLoading(false));
  };

  const openList = (type) => {
    setListDialog(type);
    setListUsers([]);
    setListLoading(true);
    const request = type === "followers" ? getFollowers : getFollowing;
    request(userId)
      .then((response) => setListUsers(response.data.result || []))
      .catch((err) => handleApiError(err, `Failed to load ${type}.`))
      .finally(() => setListLoading(false));
  };

  const handlePostCreated = (post) => setPosts((prev) => [post, ...prev]);

  const handlePostUpdated = (updatedPost) =>
    setPosts((prev) => prev.map((post) => (post.id === updatedPost.id ? updatedPost : post)));

  const handlePostDeleted = (postId) =>
    setPosts((prev) => prev.filter((post) => post.id !== postId));

  const goToUser = (targetUserId) => {
    setListDialog(null);
    navigate(`/users/${targetUserId}`);
  };

  const profile = detail?.profile;

  return (
    <Scene>
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
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {!profile && !error && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {profile && (
          <>
            {/* Header */}
            <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
              <Avatar src={profile.avatar} sx={{ width: 96, height: 96 }}>
                {!profile.avatar && (displayName(profile)?.[0] || "U")}
              </Avatar>
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="h5" fontWeight="bold">
                  {displayName(profile)}
                </Typography>
                <Typography color="text.secondary">
                  @{profile.username}
                  {profile.city && ` · ${profile.city}`}
                </Typography>
                <Box sx={{ display: "flex", gap: 2, mt: 1 }}>
                  <Button
                    size="small"
                    onClick={() => openList("followers")}
                    sx={{ textTransform: "none", px: 0, minWidth: 0 }}
                  >
                    <b>{detail.followersCount}</b>&nbsp;followers
                  </Button>
                  <Button
                    size="small"
                    onClick={() => openList("following")}
                    sx={{ textTransform: "none", px: 0, minWidth: 0 }}
                  >
                    <b>{detail.followingCount}</b>&nbsp;following
                  </Button>
                </Box>
              </Box>
              {detail.me ? (
                <Button variant="outlined" onClick={() => navigate("/profile")}>
                  Edit profile
                </Button>
              ) : (
                <Button
                  variant={detail.followedByMe ? "outlined" : "contained"}
                  onClick={handleToggleFollow}
                  disabled={followLoading}
                >
                  {detail.followedByMe ? "Unfollow" : "Follow"}
                </Button>
              )}
            </Box>

            <Divider sx={{ my: 2 }} />

            {/* Posts */}
            {detail.me && <PostComposer onCreated={handlePostCreated} />}
            <Typography sx={{ fontSize: 18, mb: "10px" }}>Posts</Typography>
            {posts.map((post) => (
              <Post
                key={post.id}
                post={post}
                onUpdated={handlePostUpdated}
                onDeleted={handlePostDeleted}
              />
            ))}
            {!postsLoading && posts.length === 0 && (
              <Typography color="text.secondary" sx={{ textAlign: "center", py: 2 }}>
                No posts yet.
              </Typography>
            )}
            {postsLoading && (
              <Box sx={{ display: "flex", justifyContent: "center", py: 1 }}>
                <CircularProgress size="24px" />
              </Box>
            )}
            {!postsLoading && page < totalPages && (
              <Box sx={{ display: "flex", justifyContent: "center" }}>
                <Button onClick={() => loadPosts(userId, page + 1)}>Load more</Button>
              </Box>
            )}
          </>
        )}
      </Card>

      {/* Followers / following list */}
      <Dialog
        open={Boolean(listDialog)}
        onClose={() => setListDialog(null)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle>
          {listDialog === "followers" ? "Followers" : "Following"}
        </DialogTitle>
        <DialogContent dividers>
          {listLoading && (
            <Box sx={{ display: "flex", justifyContent: "center", py: 2 }}>
              <CircularProgress size="24px" />
            </Box>
          )}
          {!listLoading && listUsers.length === 0 && (
            <Typography color="text.secondary" sx={{ textAlign: "center", py: 2 }}>
              {listDialog === "followers" ? "No followers yet." : "Not following anyone yet."}
            </Typography>
          )}
          <List disablePadding>
            {listUsers.map((user) => (
              <ListItemButton key={user.userId} onClick={() => goToUser(user.userId)}>
                <ListItemAvatar>
                  <Avatar src={user.avatar}>
                    {!user.avatar && (displayName(user)?.[0] || "U")}
                  </Avatar>
                </ListItemAvatar>
                <ListItemText primary={displayName(user)} secondary={`@${user.username}`} />
              </ListItemButton>
            ))}
          </List>
        </DialogContent>
      </Dialog>
    </Scene>
  );
}
