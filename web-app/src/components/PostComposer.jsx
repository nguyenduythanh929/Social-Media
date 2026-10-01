import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, IconButton, TextField, Tooltip } from "@mui/material";
import AddPhotoAlternateIcon from "@mui/icons-material/AddPhotoAlternate";
import CloseIcon from "@mui/icons-material/Close";
import { createPost, uploadImage } from "../services/postService";

const MAX_LENGTH = 5000;
const MAX_IMAGES = 4;
const MAX_IMAGE_SIZE = 5 * 1024 * 1024; // matches file-service's 5MB multipart limit
const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/gif", "image/webp"];

// Inline "What's on your mind?" box shown at the top of the feed and of your own profile
export default function PostComposer({ onCreated }) {
  const [content, setContent] = useState("");
  const [images, setImages] = useState([]); // [{ file, previewUrl }]
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  // Release preview object URLs when the component unmounts
  const imagesRef = useRef(images);
  imagesRef.current = images;
  useEffect(
    () => () => imagesRef.current.forEach((image) => URL.revokeObjectURL(image.previewUrl)),
    []
  );

  const handleSelectImages = (event) => {
    const selected = Array.from(event.target.files || []);
    event.target.value = ""; // allow picking the same file again

    const invalidType = selected.find((file) => !ALLOWED_TYPES.includes(file.type));
    if (invalidType) {
      setError("Only JPEG, PNG, GIF and WebP images are allowed.");
      return;
    }
    const tooLarge = selected.find((file) => file.size > MAX_IMAGE_SIZE);
    if (tooLarge) {
      setError(`"${tooLarge.name}" is larger than 5MB.`);
      return;
    }
    if (images.length + selected.length > MAX_IMAGES) {
      setError(`You can attach up to ${MAX_IMAGES} images.`);
      return;
    }

    setError(null);
    setImages((prev) => [
      ...prev,
      ...selected.map((file) => ({ file, previewUrl: URL.createObjectURL(file) })),
    ]);
  };

  const removeImage = (index) => {
    setImages((prev) => {
      URL.revokeObjectURL(prev[index].previewUrl);
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleSubmit = async () => {
    if (!content.trim() && images.length === 0) return;

    setSubmitting(true);
    setError(null);
    try {
      // Upload images first, then create the post with their URLs
      const mediaUrls = await Promise.all(images.map((image) => uploadImage(image.file)));
      const response = await createPost(content, mediaUrls);

      images.forEach((image) => URL.revokeObjectURL(image.previewUrl));
      setImages([]);
      setContent("");
      onCreated?.(response.data.result);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create post. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };

  const canSubmit = !submitting && (content.trim() || images.length > 0);

  return (
    <Box
      sx={{
        width: "100%",
        border: 1,
        borderColor: "divider",
        borderRadius: 2,
        p: 2,
        mb: 1,
      }}
    >
      <TextField
        fullWidth
        multiline
        minRows={2}
        maxRows={8}
        placeholder="What's on your mind?"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        inputProps={{ maxLength: MAX_LENGTH }}
      />

      {images.length > 0 && (
        <Box sx={{ display: "flex", gap: 1, mt: 1, flexWrap: "wrap" }}>
          {images.map((image, index) => (
            <Box key={image.previewUrl} sx={{ position: "relative", width: 96, height: 96 }}>
              <Box
                component="img"
                src={image.previewUrl}
                alt={`Selected image ${index + 1}`}
                sx={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: 1 }}
              />
              <IconButton
                size="small"
                onClick={() => removeImage(index)}
                disabled={submitting}
                sx={{
                  position: "absolute",
                  top: 2,
                  right: 2,
                  bgcolor: "rgba(0,0,0,0.6)",
                  color: "white",
                  "&:hover": { bgcolor: "rgba(0,0,0,0.8)" },
                }}
              >
                <CloseIcon fontSize="small" />
              </IconButton>
            </Box>
          ))}
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mt: 1 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mt: 1 }}>
        <input
          ref={fileInputRef}
          type="file"
          accept={ALLOWED_TYPES.join(",")}
          multiple
          hidden
          onChange={handleSelectImages}
        />
        <Tooltip title={`Add photos (up to ${MAX_IMAGES})`}>
          <span>
            <IconButton
              color="primary"
              onClick={() => fileInputRef.current?.click()}
              disabled={submitting || images.length >= MAX_IMAGES}
            >
              <AddPhotoAlternateIcon />
            </IconButton>
          </span>
        </Tooltip>
        <Button variant="contained" onClick={handleSubmit} disabled={!canSubmit}>
          {submitting ? "Posting..." : "Post"}
        </Button>
      </Box>
    </Box>
  );
}
