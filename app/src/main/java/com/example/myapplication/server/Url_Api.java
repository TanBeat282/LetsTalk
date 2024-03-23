package com.example.myapplication.server;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Url_Api {
    private static Url_Api instance;
    private final String BASE_URL = "http://192.168.172.170/duantotnghiep/src/api/";

    private Url_Api() {
    }

    public static Url_Api getInstance() {
        if (instance == null) {
            instance = new Url_Api();
        }
        return instance;
    }

    public String getMessagesList(int userId) {
        String MESSAGES_LIST = "messages/messages_list.php";
        String PARAM_USER_ID = "user_id";
        return BASE_URL + MESSAGES_LIST + "?" + PARAM_USER_ID + "=" + userId;
    }

    public String getMessagesListID(int messages_list_id, int user_id) {
        String MESSAGES_LIST = "messages/get_messages_list.php";
        String PARAM_USER_ID = "messages_list_id";
        String USER = "user_id";
        return BASE_URL + MESSAGES_LIST + "?" + PARAM_USER_ID + "=" + messages_list_id + "&" + USER + "=" + user_id;
    }


    public String getLogin(String email, String password) {
        String LOGIN = "users/login.php";
        String EMAIL = "email";
        String PASSWORD = "password";
        return BASE_URL + LOGIN + "?" + EMAIL + "=" + email + "&" + PASSWORD + "=" + password;
    }

    public String check_Email(String email) {
        String LOGIN = "users/check_email.php";
        String EMAIL = "email";
        return BASE_URL + LOGIN + "?" + EMAIL + "=" + email;
    }

    public String signUp(String email, String password, String full_name) {
        String LOGIN = "users/signup.php";
        String EMAIL = "email";
        String PASSWORD = "password";
        String FULL_NAME = "full_name";
        return BASE_URL + LOGIN + "?" + EMAIL + "=" + email + "&" + PASSWORD + "=" + password + "&" + FULL_NAME + "=" + full_name;
    }

    public String addOtp(int user_id, String email, String otp, String time) {
        String LOGIN = "users/add_otp.php";
        String USER = "user_id";
        String EMAIL = "email";
        String OTP = "otp";
        String TIME = "time";
        return BASE_URL + LOGIN + "?" + USER + "=" + user_id + "&" + EMAIL + "=" + email + "&" + OTP + "=" + otp + "&" + TIME + "=" + time;
    }

    public String checkOtp(int user_id, String email, String otp, String time) {
        String LOGIN = "users/check_otp.php";
        String USER = "user_id";
        String EMAIL = "email";
        String OTP = "otp";
        String TIME = "time";
        return BASE_URL + LOGIN + "?" + USER + "=" + user_id + "&" + EMAIL + "=" + email + "&" + OTP + "=" + otp + "&" + TIME + "=" + time;
    }

    public String changePass(int user_id, String pass) {
        String LOGIN = "users/update_pass.php";
        String USER = "user_id";
        String EMAIL = "password";
        return BASE_URL + LOGIN + "?" + USER + "=" + user_id + "&" + EMAIL + "=" + pass;
    }

    public String getInfo(int user_id) {
        String INFO = "users/get_info.php";
        String USER_ID = "user_id";
        return BASE_URL + INFO + "?" + USER_ID + "=" + user_id;
    }

    public String editImageProfile(int user_id, String profile_image) {
        String INFO = "users/edit_profile.php";
        String USER_ID = "user_id";
        String IMAGE = "profile_image";
        return BASE_URL + INFO + "?" + USER_ID + "=" + user_id + "&" + IMAGE + "=" + profile_image;
    }


    public String getMessages(int messages_list_id, int pageIndex) {
        String messages = "messages/messages.php";
        String messagesListId = "messages_list_id";
        String page = "page";
        return BASE_URL + messages + "?" + messagesListId + "=" + messages_list_id + "&" + page + "=" + pageIndex;
    }

    public String getMediaMessages(int messages_list_id, int type_message) {
        String messages = "messages/get_media_messages.php";
        String messagesListId = "messages_list_id";
        String page = "type_message";
        return BASE_URL + messages + "?" + messagesListId + "=" + messages_list_id + "&" + page + "=" + type_message;
    }


    public String addMessagesList(int user_id, int sender_id, int receiver_id) {
        String messages = "messages/add_messages_list.php";
        String USER = "user_id";
        String SENDER = "sender_id";
        String RECEIVER = "receiver_id";
        return BASE_URL + messages + "?" + USER + "=" + user_id + "&" + SENDER + "=" + sender_id + "&" + RECEIVER + "=" + receiver_id;
    }

    public String checkMessagesList(int user_id, int sender_id, int receiver_id) {
        String messages = "messages/check_messages_list.php";
        String USER = "user_id";
        String SENDER = "sender_id";
        String RECEIVER = "receiver_id";
        return BASE_URL + messages + "?" + USER + "=" + user_id + "&" + SENDER + "=" + sender_id + "&" + RECEIVER + "=" + receiver_id;
    }

    public String searchMessages(int messages_list_id, String content) {
        String messages = "messages/search_messages.php";
        String messagesListId = "messages_list_id";
        String CONTENT = "content";
        return BASE_URL + messages + "?" + messagesListId + "=" + messages_list_id + "&" + CONTENT + "=" + content;
    }

    public String getProfile(int user_id) {
        String PROFILE = "users/get_info.php";
        String USER_ID = "user_id";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + user_id;
    }

    public String getFriend(int user_id, int is_friend) {
        String PROFILE = "friend_ship/get_friend.php";
        String USER_ID = "user_id";
        String IS_FRIEND = "is_friend";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + user_id + "&" + IS_FRIEND + "=" + is_friend;
    }

    public String addFriendShip(int sender_id, int receiver_id, String time) {
        String PROFILE = "friend_ship/add_friend_request.php";
        String Sender_id = "sender_id";
        String Receiver_id = "receiver_id";
        String Time = "time";
        return BASE_URL + PROFILE + "?" + Sender_id + "=" + sender_id + "&" + Receiver_id + "=" + receiver_id + "&" + Time + "=" + time;
    }


    public String getFriendShip(int user_id, int is_friend) {
        String PROFILE = "friend_ship/friend_ship_request.php";
        String USER_ID = "user_id";
        String IS_FRIEND = "is_friend";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + user_id + "&" + IS_FRIEND + "=" + is_friend;
    }

    public String acceptFriendShip(int user_id, int sender_id, int receiver_id, String time) {
        String PROFILE = "friend_ship/accept_friend_request.php";
        String USER_ID = "user_id";
        String SENDER = "sender_id";
        String RECEIVER = "receiver_id";
        String TIME = "time";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + user_id + "&" + SENDER + "=" + sender_id + "&" + RECEIVER + "=" + receiver_id + "&" + TIME + "=" + time;
    }

    public String removeFriendShip(int sender_id, int receiver_id) {
        String PROFILE = "friend_ship/remove_friend_request.php";
        String USER_ID = "sender_id";
        String RECEIVER = "receiver_id";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + sender_id + "&" + RECEIVER + "=" + receiver_id;
    }

    public String deleteFriend(int friend_ship_id) {
        String PROFILE = "friend_ship/delete_friend.php";
        String SENDER_ID = "friend_ship_id";
        return BASE_URL + PROFILE + "?" + SENDER_ID + "=" + friend_ship_id;

    }

    public String checkIsFriend(int sender_id, int receiver_id) {
        String PROFILE = "friend_ship/check_is_friend.php";
        String SENDER_ID = "sender_id";
        String RECEIVER_ID = "receiver_id";
        return BASE_URL + PROFILE + "?" + SENDER_ID + "=" + sender_id + "&" + RECEIVER_ID + "=" + receiver_id;

    }

    public String searchFriend(String full_name, int users_id) {
        String PROFILE = "friend_ship/search_friend.php";
        String USER_ID = "searchTerm";
        String RECEIVER = "loggedInUserId";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + full_name + "&" + RECEIVER + "=" + users_id;
    }


    public String checkUpdateName(int user_id) {
        String PROFILE = "users/check_update_name.php";
        String USER_ID = "user_id";
        String TIME = "time";
        return BASE_URL + PROFILE + "?" + USER_ID + "=" + user_id;

    }

    public String editProfile(String cover_avatar, String profile_image, String description, String full_name, String dob, int sex, String address, int user_id) {
        String EDIT_PROFILE = "users/edit_profile.php";
        String COVER_AVATAR = "cover_avatar";
        String PROFILE_IMAGE = "profile_image";
        String DESCRIPTION = "description";
        String FULL_NAME = "full_name";
        String DOB = "dob";
        String SEX = "sex";
        String ADDRESS = "address";
        String USER_ID = "user_id";

        StringBuilder urlBuilder = new StringBuilder(BASE_URL + EDIT_PROFILE + "?");


        if (cover_avatar != null) {
            urlBuilder.append(COVER_AVATAR).append("=").append(cover_avatar).append("&");
        }
        if (profile_image != null) {
            urlBuilder.append(PROFILE_IMAGE).append("=").append(profile_image).append("&");
        }
        if (description != null) {
            urlBuilder.append(DESCRIPTION).append("=").append(description).append("&");
        }
        if (full_name != null) {
            urlBuilder.append(FULL_NAME).append("=").append(full_name).append("&");
        }
        if (dob != null) {
            urlBuilder.append(DOB).append("=").append(dob).append("&");
        }
        if (sex != -2) {
            urlBuilder.append(SEX).append("=").append(sex).append("&");
        }
        if (address != null) {
            urlBuilder.append(ADDRESS).append("=").append(address).append("&");
        }
        if (user_id != 0) {
            urlBuilder.append(USER_ID).append("=").append(user_id).append("&");
        }

        return urlBuilder.toString();
    }

    public String getPostNewFeed(int user_id) {
        String POST = "/post/get_post_newfeed.php";
        String USER_ID = "user_id";
        return BASE_URL + POST + "?" + USER_ID + "=" + user_id;
    }

    public String getMyPost(int user_id) {
        String POST = "/post/get_my_post.php";
        String USER_ID = "user_id";
        return BASE_URL + POST + "?" + USER_ID + "=" + user_id;
    }

    public String getPostDetail(int post_id, int user_id) {
        String POST = "/post/get_post_detail.php";
        String POST_ID = "post_id";
        String USER_ID = "user_id";
        return BASE_URL + POST + "?" + POST_ID + "=" + post_id
                + "&" + USER_ID + "=" + user_id;
    }


    public String getCmt(int post_id, int is_latest) {
        String POST = "/post/get_comment.php";
        String USER_ID = "post_id";
        String IS_LAST = "is_latest";
        return BASE_URL + POST + "?" + USER_ID + "=" + post_id + "&" + IS_LAST + "=" + is_latest;
    }

    public String addCmt(int post_id, int user_id, String content, String time) {
        String POST = "/post/add_comment.php";
        String POST_ID = "post_id";
        String USER_ID = "user_id";
        String CONTENT = "content";
        String TIME = "time";
        return BASE_URL + POST
                + "?" + POST_ID + "=" + post_id
                + "&" + USER_ID + "=" + user_id
                + "&" + CONTENT + "=" + content
                + "&" + TIME + "=" + time;
    }

    public String deleteCmt(int comment_id) {
        String POST = "/post/delete_comment.php";
        String COMMENT_ID = "comment_id";
        return BASE_URL + POST + "?" + COMMENT_ID + "=" + comment_id;
    }

    public String deletePost(int post_id) {
        String POST = "/post/delete_post.php";
        String COMMENT_ID = "post_id";
        return BASE_URL + POST + "?" + COMMENT_ID + "=" + post_id;
    }

    public String savePost(int user_id, int post_id, String time) {
        String POST = "/post/save_post.php";
        String USER = "user_id";
        String POST_ID = "post_id";
        String TIME = "time";
        return BASE_URL + POST
                + "?" + USER + "=" + user_id
                + "&" + POST_ID + "=" + post_id
                + "&" + TIME + "=" + time;
    }

    public String addHeart(int post_id, int user_id, String time) {
        String POST = "/post/add_heart.php";
        String POST_ID = "post_id";
        String USER_ID = "user_id";
        String TIME = "time";
        return BASE_URL + POST
                + "?" + POST_ID + "=" + post_id
                + "&" + USER_ID + "=" + user_id
                + "&" + TIME + "=" + time;
    }

    public String addPost(int user_id, String content, String time) {
        String POST = "post/add_post.php";
        String USER_ID = "user_id";
        String CONTENT = "content";
        String TIME = "time";
        return BASE_URL + POST + "?" + USER_ID + "=" + user_id + "&" + CONTENT + "=" + content + "&" + TIME + "=" + time;
    }

    public String addImagesPost(int user_id, int post_id, String arrayListUrl) {
        String POST = "/post/add_image_post.php";
        String USER_ID = "user_id";
        String POST_ID = "post_id";
        String ARRAYLISTURL = "array_list_url";
        return BASE_URL + POST + "?" + USER_ID + "=" + user_id + "&" + POST_ID + "=" + post_id + "&" + ARRAYLISTURL + "=" + arrayListUrl;
    }
}
