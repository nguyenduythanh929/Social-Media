import {
  Alert,
  Avatar,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Menu,
  MenuItem,
  TextField,
  Typography,
} from "@mui/material";
import ThumbUpIcon from "@mui/icons-material/ThumbUp";
import ThumbUpOutlinedIcon from "@mui/icons-material/ThumbUpOutlined";
import ChatBubbleOutlineIcon from "@mui/icons-material/ChatBubbleOutline";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import React, { forwardRef, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import CommentSection from "./CommentSection";
import PostImages from "./PostImages";
import { deletePost, likePost, unlikePost, updatePost } from "../services/postService";

const MAX_LENGTH = 5000;

// onUpdated(post) and onDeleted(postId) let the parent list replace or remove this post
const Post = forwardRef(({ post, onUpdated, onDeleted }, ref) => {
  const { id, avatar, username, userId, created, content, mediaUrls, edited, ownedByMe } = post;
  const hasImages = mediaUrls?.length > 0;
  const profileLink = userId ? `/users/${userId}` : null;

  const [likeCount, setLikeCount] = useState(post.likeCount ?? 0);
  const [likedByMe, setLikedByMe] = useState(Boolean(post.likedByMe));
  const [liking, setLiking] = useState(false);
  const [commentCount, setCommentCount] = useState(post.commentCount ?? 0);
  const [showComments, setShowComments] = useState(false);

  const [menuAnchor, setMenuAnchor] = useState(null);
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(content);
  const [saving, setSaving] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [error, setError] = useState(null);

  // Keep local counters in sync when the parent replaces this post (e.g. after an edit)
  useEffect(() => {
    setLikeCount(post.likeCount ?? 0);
    setLikedByMe(Boolean(post.likedByMe));
    setCommentCount(post.commentCount ?? 0);
  }, [post.likeCount, post.likedByMe, post.commentCount]);

  const handleToggleLike = () => {
    // Optimistic update, rolled back if the request fails
    const wasLiked = likedByMe;
    setLikedByMe(!wasLiked);
    setLikeCount((count) => count + (wasLiked ? -1 : 1));
    setLiking(true);

    (wasLiked ? unlikePost(id) : likePost(id))
      .then((response) => {
        setLikeCount(response.data.result.likeCount);
        setLikedByMe(response.data.result.likedByMe);
      })
      .catch(() => {
        setLikedByMe(wasLiked);
        setLikeCount((count) => count + (wasLiked ? 1 : -1));
        setError("Could not update like. Please try again.");
      })
      .finally(() => setLiking(false));
  };

  const startEditing = () => {
    setMenuAnchor(null);
    setDraft(content);
    setEditing(true);
  };

  const handleSave = () => {
    setSaving(true);
    setError(null);
    updatePost(id, draft)
      .then((response) => {
        setEditing(false);
        onUpdated?.(response.data.result);
      })
      .catch((err) => setError(err.response?.data?.message || "Failed to update post."))
      .finally(() => setSaving(false));
  };

  const handleDelete = () => {
    setConfirmDelete(false);
    deletePost(id)
      .then(() => onDeleted?.(id))
      .catch((err) => setError(err.response?.data?.message || "Failed to delete post."));
  };

  return (
    <Box ref={ref} sx={{ width: "100%", mb: 2 }}>
      <Box sx={{ display: "flex", alignItems: "flex-start" }}>
        <Avatar
          src={avatar}
          component={profileLink ? Link : "div"}
          to={profileLink ?? undefined}
          sx={{ marginRight: 2 }}
        />
        <Box sx={{ flexGrow: 1, minWidth: 0 }}>
          <Box sx={{ display: "flex", flexDirection: "row", gap: "10px", alignItems: "baseline" }}>
            <Typography
              component={profileLink ? Link : "span"}
              to={profileLink ?? undefined}
              sx={{
                fontSize: 14,
                fontWeight: 600,
                color: "inherit",
                textDecoration: "none",
                "&:hover": { textDecoration: profileLink ? "underline" : "none" },
              }}
            >
              {username}
            </Typography>
            <Typography sx={{ fontSize: 14, fontWeight: 400, color: "text.secondary" }}>
              {created}
              {edited && " · edited"}
            </Typography>
          </Box>

          {editing ? (
            <Box sx={{ mt: 1 }}>
              <TextField
                fullWidth
                multiline
                minRows={2}
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                inputProps={{ maxLength: MAX_LENGTH }}
              />
              <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 1, mt: 1 }}>
                <Button onClick={() => setEditing(false)} disabled={saving}>
                  Cancel
                </Button>
                <Button
                  variant="contained"
                  onClick={handleSave}
                  disabled={saving || (!draft.trim() && !hasImages) || draft.trim() === content}
                >
                  Save
                </Button>
              </Box>
            </Box>
          ) : (
            content && (
              <Typography sx={{ fontSize: 14, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                {content}
              </Typography>
            )
          )}

          <PostImages urls={mediaUrls} />

          {error && (
            <Alert severity="error" onClose={() => setError(null)} sx={{ mt: 1 }}>
              {error}
            </Alert>
          )}

          {/* Actions */}
          <Box sx={{ display: "flex", gap: 1, mt: 0.5, ml: -1 }}>
            <Button
              size="small"
              color={likedByMe ? "primary" : "inherit"}
              startIcon={likedByMe ? <ThumbUpIcon /> : <ThumbUpOutlinedIcon />}
              onClick={handleToggleLike}
              disabled={liking}
              sx={{ textTransform: "none" }}
            >
              {likeCount > 0 ? likeCount : ""} Like
            </Button>
            <Button
              size="small"
              color="inherit"
              startIcon={<ChatBubbleOutlineIcon />}
              onClick={() => setShowComments((shown) => !shown)}
              sx={{ textTransform: "none" }}
            >
              {commentCount > 0 ? commentCount : ""} Comment
            </Button>
          </Box>

          {showComments && (
            <CommentSection
              postId={id}
              postOwnedByMe={ownedByMe}
              onCountChange={(delta) => setCommentCount((count) => Math.max(0, count + delta))}
            />
          )}
        </Box>

        {ownedByMe && !editing && (
          <>
            <IconButton size="small" onClick={(e) => setMenuAnchor(e.currentTarget)}>
              <MoreVertIcon fontSize="small" />
            </IconButton>
            <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
              <MenuItem onClick={startEditing}>Edit post</MenuItem>
              <MenuItem
                onClick={() => {
                  setMenuAnchor(null);
                  setConfirmDelete(true);
                }}
                sx={{ color: "error.main" }}
              >
                Delete post
              </MenuItem>
            </Menu>
          </>
        )}
      </Box>

      <Dialog open={confirmDelete} onClose={() => setConfirmDelete(false)}>
        <DialogTitle>Delete post?</DialogTitle>
        <DialogContent>
          This will permanently delete the post and all of its comments.
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDelete(false)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleDelete}>
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
});

export default Post;
