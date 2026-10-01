import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Alert,
  Avatar,
  Box,
  Button,
  CircularProgress,
  IconButton,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import SendIcon from "@mui/icons-material/Send";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { createComment, deleteComment, getComments } from "../services/postService";

const MAX_LENGTH = 1000;

// Comments for one post. postOwnedByMe lets the post owner remove any comment on their post.
export default function CommentSection({ postId, postOwnedByMe, onCountChange }) {
  const [comments, setComments] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const loadComments = useCallback(
    (pageToLoad) => {
      setLoading(true);
      getComments(postId, pageToLoad)
        .then((response) => {
          const result = response.data.result;
          setTotalPages(result.totalPages);
          setComments((prev) => (pageToLoad === 1 ? result.data : [...prev, ...result.data]));
          setPage(pageToLoad);
        })
        .catch(() => setError("Failed to load comments."))
        .finally(() => setLoading(false));
    },
    [postId]
  );

  useEffect(() => {
    loadComments(1);
  }, [loadComments]);

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!content.trim()) return;

    setSubmitting(true);
    setError(null);
    createComment(postId, content)
      .then((response) => {
        setComments((prev) => [...prev, response.data.result]);
        setContent("");
        onCountChange?.(1);
      })
      .catch((err) => setError(err.response?.data?.message || "Failed to add comment."))
      .finally(() => setSubmitting(false));
  };

  const handleDelete = (commentId) => {
    deleteComment(commentId)
      .then(() => {
        setComments((prev) => prev.filter((comment) => comment.id !== commentId));
        onCountChange?.(-1);
      })
      .catch((err) => setError(err.response?.data?.message || "Failed to delete comment."));
  };

  return (
    <Box sx={{ mt: 1, pl: 1, borderLeft: 2, borderColor: "divider" }}>
      {error && (
        <Alert severity="error" sx={{ mb: 1 }}>
          {error}
        </Alert>
      )}

      {comments.map((comment) => (
        <Box key={comment.id} sx={{ display: "flex", alignItems: "flex-start", gap: 1, mb: 1 }}>
          <Avatar
            src={comment.avatar}
            component={Link}
            to={`/users/${comment.userId}`}
            sx={{ width: 28, height: 28 }}
          />
          <Box sx={{ flexGrow: 1, bgcolor: "action.hover", borderRadius: 2, px: 1.5, py: 0.75 }}>
            <Box sx={{ display: "flex", gap: 1, alignItems: "baseline" }}>
              <Typography
                component={Link}
                to={`/users/${comment.userId}`}
                sx={{ fontSize: 13, fontWeight: 600, color: "inherit", textDecoration: "none" }}
              >
                {comment.username || "Unknown user"}
              </Typography>
              <Typography sx={{ fontSize: 12, color: "text.secondary" }}>
                {comment.created}
              </Typography>
            </Box>
            <Typography sx={{ fontSize: 14, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
              {comment.content}
            </Typography>
          </Box>
          {(comment.ownedByMe || postOwnedByMe) && (
            <Tooltip title="Delete comment">
              <IconButton size="small" onClick={() => handleDelete(comment.id)}>
                <DeleteOutlineIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      ))}

      {loading && (
        <Box sx={{ display: "flex", justifyContent: "center", py: 1 }}>
          <CircularProgress size="20px" />
        </Box>
      )}
      {!loading && page < totalPages && (
        <Button size="small" onClick={() => loadComments(page + 1)}>
          Load more comments
        </Button>
      )}

      <Box component="form" onSubmit={handleSubmit} sx={{ display: "flex", gap: 1, mt: 1 }}>
        <TextField
          fullWidth
          size="small"
          placeholder="Write a comment..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          inputProps={{ maxLength: MAX_LENGTH }}
        />
        <IconButton type="submit" color="primary" disabled={submitting || !content.trim()}>
          <SendIcon />
        </IconButton>
      </Box>
    </Box>
  );
}
