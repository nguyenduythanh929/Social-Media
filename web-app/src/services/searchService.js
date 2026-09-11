import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";
import { getToken } from "./localStorageService";

export const searchUsers = async (keyword) => {
  return await httpClient.get(API.SEARCH_USERS, {
    params: { q: keyword },
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};

export const searchPosts = async (keyword) => {
  return await httpClient.get(API.SEARCH_POSTS, {
    params: { q: keyword },
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });
};
