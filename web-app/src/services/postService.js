import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";
import { getToken } from "./localStorageService";

export const getMyPosts = async (page) => {
  return await httpClient.get(API.MY_POST, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
    params: {
      page: page,
      size: 10,
    },
  });
};

export const createPost = async (content, mediaUrls = []) => {
  return await httpClient.post(
    API.CREATE_POST,
    { content: content, mediaUrls: mediaUrls },
    {
      headers: {
        Authorization: `Bearer ${getToken()}`,
        "Content-Type": "application/json",
      },
    }
  );
};

export const getFeed = async (page) => {
  return await httpClient.get(API.FEED, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
    params: {
      page: page,
      size: 10,
    },
  });
};

export const getUserPosts = async (userId, page) => {
  return await httpClient.get(API.USER_POSTS(userId), {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
    params: {
      page: page,
      size: 10,
    },
  });
};

const authHeader = () => ({ Authorization: `Bearer ${getToken()}` });

export const updatePost = async (postId, content) => {
  return await httpClient.put(API.POST(postId), { content }, { headers: authHeader() });
};

export const deletePost = async (postId) => {
  return await httpClient.delete(API.POST(postId), { headers: authHeader() });
};

export const likePost = async (postId) => {
  return await httpClient.post(API.LIKE_POST(postId), null, { headers: authHeader() });
};

export const unlikePost = async (postId) => {
  return await httpClient.delete(API.LIKE_POST(postId), { headers: authHeader() });
};

export const getComments = async (postId, page) => {
  return await httpClient.get(API.POST_COMMENTS(postId), {
    headers: authHeader(),
    params: { page: page, size: 20 },
  });
};

export const createComment = async (postId, content) => {
  return await httpClient.post(API.POST_COMMENTS(postId), { content }, { headers: authHeader() });
};

export const deleteComment = async (commentId) => {
  return await httpClient.delete(API.COMMENT(commentId), { headers: authHeader() });
};

// Uploads one image to file-service and returns its public URL
export const uploadImage = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await httpClient.post(API.UPLOAD_MEDIA, formData, {
    headers: { ...authHeader(), "Content-Type": "multipart/form-data" },
  });

  return response.data.result.url;
};
