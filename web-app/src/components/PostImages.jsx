import { useState } from "react";
import { Box, Dialog } from "@mui/material";

// Image grid for a post: one image full width, several in a two-column grid. Click to enlarge.
export default function PostImages({ urls }) {
  const [openUrl, setOpenUrl] = useState(null);

  if (!urls || urls.length === 0) return null;

  const single = urls.length === 1;

  return (
    <>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: single ? "1fr" : "1fr 1fr",
          gap: 0.5,
          mt: 1,
          borderRadius: 2,
          overflow: "hidden",
        }}
      >
        {urls.map((url, index) => (
          <Box
            key={url}
            component="img"
            src={url}
            alt={`Post image ${index + 1}`}
            loading="lazy"
            onClick={() => setOpenUrl(url)}
            sx={{
              width: "100%",
              height: single ? "auto" : 200,
              maxHeight: single ? 480 : undefined,
              objectFit: "cover",
              cursor: "zoom-in",
              display: "block",
              // A 3-image post: let the first image span the full row
              gridColumn: urls.length === 3 && index === 0 ? "1 / -1" : undefined,
            }}
          />
        ))}
      </Box>

      <Dialog open={Boolean(openUrl)} onClose={() => setOpenUrl(null)} maxWidth="lg">
        {openUrl && (
          <Box
            component="img"
            src={openUrl}
            alt="Enlarged post image"
            onClick={() => setOpenUrl(null)}
            sx={{ display: "block", maxWidth: "90vw", maxHeight: "90vh", cursor: "zoom-out" }}
          />
        )}
      </Dialog>
    </>
  );
}
