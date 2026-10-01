import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";
import { getToken } from "./localStorageService";

export const getMyInfo = async () => {
  return await httpClient.get(API.MY_INFO, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const updateProfile = async (profileData) => {
  return await httpClient.put(API.UPDATE_PROFILE, profileData, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
      "Content-Type": "application/json",
    },
  });
};

export const uploadAvatar = async (formData) => {
  return await httpClient.put(API.UPDATE_AVATAR, formData, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
      "Content-Type": "multipart/form-data",
    },
  });
};

export const search = async (keyword) => {
  return await httpClient.post(
    API.SEARCH_USER,
    { keyword: keyword },
    {
      headers: {
        Authorization: `Bearer ${getToken()}`,
        "Content-Type": "application/json",
      },
    }
  );
};

export const register = async (userData) => {
  return await httpClient.post(API.REGISTER, userData);
};

export const getUserDetail = async (userId) => {
  return await httpClient.get(API.USER_DETAIL(userId), {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const followUser = async (userId) => {
  return await httpClient.post(API.FOLLOW(userId), null, {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const unfollowUser = async (userId) => {
  return await httpClient.delete(API.FOLLOW(userId), {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const getFollowers = async (userId) => {
  return await httpClient.get(API.FOLLOWERS(userId), {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const getFollowing = async (userId) => {
  return await httpClient.get(API.FOLLOWING(userId), {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const getFriends = async (userId) => {
  return await httpClient.get(API.FRIENDS(userId), {
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};
