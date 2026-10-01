import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Alert,
  Avatar,
  Box,
  Button,
  Card,
  CircularProgress,
  Divider,
  List,
  ListItem,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  Tab,
  Tabs,
  Typography,
} from "@mui/material";
import Scene from "./Scene";
import { isAuthenticated, logOut } from "../services/authenticationService";
import {
  followUser,
  getFollowers,
  getFollowing,
  getFriends,
  getMyInfo,
  unfollowUser,
} from "../services/userService";

const TABS = [
  { key: "friends", label: "Friends", empty: "No friends yet. Friends are people you follow who follow you back." },
  { key: "following", label: "Following", empty: "You're not following anyone yet." },
  { key: "followers", label: "Followers", empty: "No one is following you yet." },
];

const displayName = (profile) =>
  profile?.firstName || profile?.lastName
    ? `${profile.firstName ?? ""} ${profile.lastName ?? ""}`.trim()
    : profile?.username;

export default function Friends() {
  const navigate = useNavigate();
  const [myUserId, setMyUserId] = useState(null);
  const [tab, setTab] = useState(0);
  const [lists, setLists] = useState({ friends: [], following: [], followers: [] });
  const [loading, setLoading] = useState(true);
  const [pendingUserId, setPendingUserId] = useState(null);
  const [error, setError] = useState(null);

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

  const loadLists = useCallback(
    (userId) =>
      Promise.all([getFriends(userId), getFollowing(userId), getFollowers(userId)])
        .then(([friends, following, followers]) =>
          setLists({
            friends: friends.data.result || [],
            following: following.data.result || [],
            followers: followers.data.result || [],
          })
        )
        .catch((err) => handleApiError(err, "Failed to load your connections.")),
    [handleApiError]
  );

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate("/login");
      return;
    }

    getMyInfo()
      .then((response) => {
        const userId = response.data.result.userId;
        setMyUserId(userId);
        return loadLists(userId);
      })
      .catch((err) => handleApiError(err, "Failed to load your profile."))
      .finally(() => setLoading(false));
  }, [navigate, loadLists, handleApiError]);

  const followingIds = useMemo(
    () => new Set(lists.following.map((user) => user.userId)),
    [lists.following]
  );

  const handleToggleFollow = (user) => {
    setPendingUserId(user.userId);
    const action = followingIds.has(user.userId) ? unfollowUser : followUser;
    action(user.userId)
      .then(() => loadLists(myUserId))
      .catch((err) => handleApiError(err, "Action failed. Please try again."))
      .finally(() => setPendingUserId(null));
  };

  const current = TABS[tab];
  const users = lists[current.key];

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
        <Typography variant="h6" sx={{ mb: 1 }}>
          Friends
        </Typography>

        {error && (
          <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Tabs value={tab} onChange={(_, value) => setTab(value)} sx={{ mb: 1 }}>
          {TABS.map((t) => (
            <Tab key={t.key} label={`${t.label} (${lists[t.key].length})`} />
          ))}
        </Tabs>

        {loading && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {!loading && users.length === 0 && (
          <Typography color="text.secondary" sx={{ textAlign: "center", py: 4 }}>
            {current.empty}
          </Typography>
        )}

        {!loading && users.length > 0 && (
          <List disablePadding>
            {users.map((user, index) => {
              const iFollow = followingIds.has(user.userId);
              return (
                <Box key={user.userId}>
                  <ListItem
                    disablePadding
                    secondaryAction={
                      <Button
                        size="small"
                        variant={iFollow ? "outlined" : "contained"}
                        disabled={pendingUserId === user.userId}
                        onClick={() => handleToggleFollow(user)}
                      >
                        {iFollow ? "Unfollow" : current.key === "followers" ? "Follow back" : "Follow"}
                      </Button>
                    }
                  >
                    <ListItemButton onClick={() => navigate(`/users/${user.userId}`)} sx={{ pr: 14 }}>
                      <ListItemAvatar>
                        <Avatar src={user.avatar}>
                          {!user.avatar && (displayName(user)?.[0] || "U")}
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={<Typography fontWeight="bold">{displayName(user)}</Typography>}
                        secondary={`@${user.username}${user.city ? ` · ${user.city}` : ""}`}
                      />
                    </ListItemButton>
                  </ListItem>
                  {index < users.length - 1 && <Divider component="li" />}
                </Box>
              );
            })}
          </List>
        )}
      </Card>
    </Scene>
  );
}
