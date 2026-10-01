import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  Snackbar,
  Alert,
  Link,
} from "@mui/material";
import { useEffect, useState } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { logIn, isAuthenticated } from "../services/authenticationService";
import { register } from "../services/userService";

const initialForm = {
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  firstName: "",
  lastName: "",
  dob: "",
  city: "",
};

// Mirrors the backend rules in UserCreationRequest so users get feedback before submitting
const validate = (form) => {
  const errors = {};
  if (form.username.trim().length < 3)
    errors.username = "Username must be at least 3 characters";
  if (form.password.length < 8)
    errors.password = "Password must be at least 8 characters";
  if (form.confirmPassword !== form.password)
    errors.confirmPassword = "Passwords do not match";
  if (!form.email.trim()) errors.email = "Email is required";
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim()))
    errors.email = "Email is not valid";
  return errors;
};

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [snackBarOpen, setSnackBarOpen] = useState(false);
  const [snackBarMessage, setSnackBarMessage] = useState("");

  useEffect(() => {
    if (isAuthenticated()) {
      navigate("/");
    }
  }, [navigate]);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleCloseSnackBar = (event, reason) => {
    if (reason === "clickaway") {
      return;
    }
    setSnackBarOpen(false);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const validationErrors = validate(form);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setSubmitting(true);
    try {
      await register({
        username: form.username.trim(),
        password: form.password,
        email: form.email.trim(),
        firstName: form.firstName.trim() || null,
        lastName: form.lastName.trim() || null,
        dob: form.dob || null,
        city: form.city.trim() || null,
      });

      // Sign the new user in straight away
      await logIn(form.username.trim(), form.password);
      navigate("/");
    } catch (error) {
      setSnackBarMessage(
        error.response?.data?.message || "Registration failed. Please try again."
      );
      setSnackBarOpen(true);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Snackbar
        open={snackBarOpen}
        onClose={handleCloseSnackBar}
        autoHideDuration={6000}
        anchorOrigin={{ vertical: "top", horizontal: "right" }}
      >
        <Alert
          onClose={handleCloseSnackBar}
          severity="error"
          variant="filled"
          sx={{ width: "100%" }}
        >
          {snackBarMessage}
        </Alert>
      </Snackbar>
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
        bgcolor={"#f0f2f5"}
        py={4}
      >
        <Card
          sx={{
            minWidth: 300,
            maxWidth: 440,
            width: "100%",
            boxShadow: 3,
            borderRadius: 3,
            padding: 3,
          }}
        >
          <CardContent>
            <Typography variant="h5" component="h1" gutterBottom>
              Create an account
            </Typography>
            <Box
              component="form"
              display="flex"
              flexDirection="column"
              width="100%"
              onSubmit={handleSubmit}
              noValidate
            >
              <TextField
                label="Username"
                required
                fullWidth
                margin="dense"
                value={form.username}
                onChange={handleChange("username")}
                error={Boolean(errors.username)}
                helperText={errors.username}
              />
              <TextField
                label="Email"
                type="email"
                required
                fullWidth
                margin="dense"
                value={form.email}
                onChange={handleChange("email")}
                error={Boolean(errors.email)}
                helperText={errors.email}
              />
              <TextField
                label="Password"
                type="password"
                required
                fullWidth
                margin="dense"
                value={form.password}
                onChange={handleChange("password")}
                error={Boolean(errors.password)}
                helperText={errors.password}
              />
              <TextField
                label="Confirm password"
                type="password"
                required
                fullWidth
                margin="dense"
                value={form.confirmPassword}
                onChange={handleChange("confirmPassword")}
                error={Boolean(errors.confirmPassword)}
                helperText={errors.confirmPassword}
              />
              <Box display="flex" gap={2}>
                <TextField
                  label="First name"
                  fullWidth
                  margin="dense"
                  value={form.firstName}
                  onChange={handleChange("firstName")}
                />
                <TextField
                  label="Last name"
                  fullWidth
                  margin="dense"
                  value={form.lastName}
                  onChange={handleChange("lastName")}
                />
              </Box>
              <TextField
                label="Date of birth"
                type="date"
                fullWidth
                margin="dense"
                value={form.dob}
                onChange={handleChange("dob")}
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label="City"
                fullWidth
                margin="dense"
                value={form.city}
                onChange={handleChange("city")}
              />
              <Button
                type="submit"
                variant="contained"
                color="success"
                size="large"
                fullWidth
                disabled={submitting}
                sx={{ mt: "15px", mb: "15px" }}
              >
                {submitting ? "Creating account..." : "Sign up"}
              </Button>
            </Box>
            <Typography variant="body2" textAlign="center">
              Already have an account?{" "}
              <Link component={RouterLink} to="/login">
                Log in
              </Link>
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </>
  );
}
