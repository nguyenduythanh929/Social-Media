const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";

export const CONFIG = {
  API_GATEWAY: process.env.REACT_APP_API_GATEWAY || "/api/v1",
  WS_CHAT:
      process.env.REACT_APP_WS_CHAT || `${wsProtocol}//${window.location.host}/chat/ws`,
};

export const API = {
  LOGIN: "/identity/auth/token",
  REGISTER: "/identity/users/registration",
  MY_INFO: "/profile/users/my-profile",
  MY_POST: "/post/my-posts",
  CREATE_POST: "/post/create",
  FEED: "/post/feed",
  USER_POSTS: (userId) => `/post/users/${userId}`,
  POST: (postId) => `/post/${postId}`,
  LIKE_POST: (postId) => `/post/${postId}/like`,
  POST_COMMENTS: (postId) => `/post/${postId}/comments`,
  COMMENT: (commentId) => `/post/comments/${commentId}`,
  UPDATE_PROFILE: "/profile/users/my-profile",
  UPDATE_AVATAR: "/profile/users/avatar",
  SEARCH_USER: "/profile/users/search",
  USER_DETAIL: (userId) => `/profile/users/${userId}/detail`,
  FOLLOW: (userId) => `/profile/users/${userId}/follow`,
  FOLLOWERS: (userId) => `/profile/users/${userId}/followers`,
  FOLLOWING: (userId) => `/profile/users/${userId}/following`,
  FRIENDS: (userId) => `/profile/users/${userId}/friends`,
  UPLOAD_MEDIA: "/file/media/upload",
  MY_CONVERSATIONS: "/chat/conversations/my-conversations",
  CREATE_CONVERSATION: "/chat/conversations/create",
  CREATE_MESSAGE: "/chat/messages/create",
  GET_CONVERSATION_MESSAGES: "/chat/messages",
  SEARCH_USERS: "/search/users",
  SEARCH_POSTS: "/search/posts",
};
