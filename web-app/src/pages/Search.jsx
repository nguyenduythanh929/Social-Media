import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import {
  Box,
  Card,
  CircularProgress,
  Typography,
  Tabs,
  Tab,
  List,
  ListItemButton,
  ListItemAvatar,
  Avatar,
  ListItemText,
  Divider,
  Alert,
} from "@mui/material";
import PersonIcon from "@mui/icons-material/Person";
import ArticleIcon from "@mui/icons-material/Article";
import { isAuthenticated } from "../services/authenticationService";
import { searchUsers, searchPosts } from "../services/searchService";
import Scene from "./Scene";

export default function Search() {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get("q") || "";
  const navigate = useNavigate();

  const [tab, setTab] = useState(0);
  const [users, setUsers] = useState([]);
  const [posts, setPosts] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [loadingPosts, setLoadingPosts] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate("/login");
      return;
    }
    if (!keyword.trim()) return;

    setError(null);

    // Search users
    setLoadingUsers(true);
    searchUsers(keyword)
      .then((res) => setUsers(res.data.result || []))
      .catch(() => setError("Failed to search users."))
      .finally(() => setLoadingUsers(false));

    // Search posts
    setLoadingPosts(true);
    searchPosts(keyword)
      .then((res) => setPosts(res.data.result || []))
      .catch(() => setError("Failed to search posts."))
      .finally(() => setLoadingPosts(false));
  }, [keyword, navigate]);

  const handleTabChange = (_, newValue) => setTab(newValue);

  const loading = loadingUsers || loadingPosts;

  return (
    <Scene>
      <Card
        sx={{
          minWidth: 500,
          maxWidth: 700,
          width: "100%",
          boxShadow: 3,
          borderRadius: 2,
          mt: "20px",
          mb: "20px",
          padding: "20px",
        }}
      >
        {/* Header */}
        <Typography variant="h6" sx={{ mb: 1 }}>
          {keyword ? (
            <>
              Results for{" "}
              <Box component="span" sx={{ fontWeight: "bold", color: "primary.main" }}>
                "{keyword}"
              </Box>
            </>
          ) : (
            "Search"
          )}
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {/* Tabs */}
        <Tabs value={tab} onChange={handleTabChange} sx={{ mb: 2 }}>
          <Tab
            icon={<PersonIcon fontSize="small" />}
            iconPosition="start"
            label={`People (${users.length})`}
          />
          <Tab
            icon={<ArticleIcon fontSize="small" />}
            iconPosition="start"
            label={`Posts (${posts.length})`}
          />
        </Tabs>

        {/* Loading */}
        {loading && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {/* No keyword */}
        {!keyword.trim() && !loading && (
          <Typography color="text.secondary" sx={{ textAlign: "center", py: 4 }}>
            Type something in the search bar to get started.
          </Typography>
        )}

        {/* People tab */}
        {tab === 0 && !loading && keyword && (
          <>
            {users.length === 0 ? (
              <Typography color="text.secondary" sx={{ textAlign: "center", py: 4 }}>
                No users found.
              </Typography>
            ) : (
              <List disablePadding>
                {users.map((user, index) => (
                  <Box key={user.userId || index}>
                    <ListItemButton
                      alignItems="flex-start"
                      sx={{ px: 0 }}
                      disabled={!user.userId}
                      onClick={() => navigate(`/users/${user.userId}`)}
                    >
                      <ListItemAvatar>
                        <Avatar
                          src={user.avatar}
                          alt={user.username}
                          sx={{ width: 48, height: 48 }}
                        >
                          {!user.avatar &&
                            (user.firstName?.[0] || user.username?.[0] || "U")}
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={
                          <Typography fontWeight="bold">
                            {user.firstName && user.lastName
                              ? `${user.firstName} ${user.lastName}`
                              : user.username}
                          </Typography>
                        }
                        secondary={
                          <Box component="span">
                            <Typography component="span" variant="body2" color="text.secondary">
                              @{user.username}
                            </Typography>
                            {user.city && (
                              <Typography component="span" variant="body2" color="text.secondary">
                                {" · "}{user.city}
                              </Typography>
                            )}
                          </Box>
                        }
                      />
                    </ListItemButton>
                    {index < users.length - 1 && <Divider component="li" />}
                  </Box>
                ))}
              </List>
            )}
          </>
        )}

        {/* Posts tab */}
        {tab === 1 && !loading && keyword && (
          <>
            {posts.length === 0 ? (
              <Typography color="text.secondary" sx={{ textAlign: "center", py: 4 }}>
                No posts found.
              </Typography>
            ) : (
              <List disablePadding>
                {posts.map((post, index) => (
                  <Box key={post.postId || index}>
                    <ListItemButton
                      alignItems="flex-start"
                      sx={{ px: 0 }}
                      disabled={!post.userId}
                      onClick={() => navigate(`/users/${post.userId}`)}
                    >
                      <ListItemAvatar>
                        <Avatar sx={{ bgcolor: "primary.main" }}>
                          <ArticleIcon />
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={
                          <Typography
                            variant="body1"
                            sx={{
                              display: "-webkit-box",
                              WebkitLineClamp: 3,
                              WebkitBoxOrient: "vertical",
                              overflow: "hidden",
                            }}
                          >
                            {post.content}
                          </Typography>
                        }
                        secondary={
                          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                            {post.createdDate
                              ? new Date(post.createdDate).toLocaleDateString("vi-VN", {
                                  year: "numeric",
                                  month: "short",
                                  day: "numeric",
                                })
                              : ""}
                          </Typography>
                        }
                      />
                    </ListItemButton>
                    {index < posts.length - 1 && <Divider component="li" />}
                  </Box>
                ))}
              </List>
            )}
          </>
        )}
      </Card>
    </Scene>
  );
}
