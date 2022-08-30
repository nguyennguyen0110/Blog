# Blog Restful API

- /api
- /api/signup
- /api/signin
- /api/comment
- /api/post
- /api/user

## Anonymous user can:

- Access: "/api"
- Access: "/api/signup"
- Access: "/api/signin"
- Access: "/api/comment" with GET method. Only return **not** soft-deleted comment.
- Access: "/api/post" with GET method.

## User with ROLE_USER can:

- All the rights as anonymous user.
- Access: "/api/comment" with other methods:
  - Create comment with POST.
  - Edit comment with PUT (only edit comment of his own).
  - Delete comment with DELETE (only soft-delete comment of his own, field "deleted" will be set to true).

## User with ROLE_ADMIN can:

- All the rights as ROLE_USER.
- Can soft-delete comment of other users (field "content" will be changed to "Deleted by admin", field "deleted" will stay false).
- Can delete comment for real if comment is soft-deleted.
- Comment return in GET method include soft-deleted.
- Access: "/api/post" with other methods.
- Access: "/api/user"

## Use of API:

- **"/api/"** with method **GET**: just a welcome message. Response data "Hello Spring Boot Restful API".


- **"/api/signup"** with method **POST**: request with user information to create a user. User info in request body as follows:

        {
            "username": username_to_create (length min 3 character, max 50),
            "password": password_of_user (length min 8 character, max 254),
            "email": user_email,
            "firstName": first_name_of_user,
            "lastName": last_name_of_user,
            "role": role_of_user (ROLE_USER or ROLE_ADMIN)
        }
    Response data is created user or null if any error (check code and message).

        {
            "code": "200",
            "message": "Success",
            "data": {
                "username": username_created,
                "password": encoded_password,
                "email": user_email,
                "firstName": first_name_of_user,
                "lastName": last_name_of_user,
                "role": role_of_user (ROLE_USER or ROLE_ADMIN)
            }
        }

- **"/api/signin"** with method **POST**: request authentication token for user. Request body just need username and password:

        {
            "username": username_to_authen,
            "password": password_of_user 
        }

    Response data is token or null if any error (check code and message).

        {
            "code": "200",
            "message": "Success",
            "data": created_token
        }


- **"/api/comment":**
  - **GET:** can send with request parameters. Request param available:
    - id: ID of comment (to find by)
    - owner: username of owner (to find by)
    - post: id of post (to find by)
    - createDate: date comment created format YYYY-MM-DD (to find by)
    - page: page want to get, default 0 (ex: 1 to return page 1, 5 to return page 5)
    - size: number of users in a page, default 10

    Response data can be comment found or null, list of comments of empty list. Notice that list of comments return is paged and sorted (by post ascending and by create date descending).

        {
            "code": "200",
            "message": "Success",
            "data": {
                "id": id_of_comment,
                "content": content_of_comment,
                "createDate": post_created_date,
                "modifyDate": post_modify_date,
                "deleted": false,
                "owner": {
                    "username": username_of_owner,
                    "firstName": first_name_of_owner,
                    "lastName": last_name_of_owner
                }
                "post": {
                    "id": id_of_post,
                    "title": title_of_post
                }
            }
        }
  - **POST:** request to create a comment. Response data is created comment or null if any error (check code and message). Comment info in request body:

        {
            "content": content_of_comment
        }
  - **PUT:** request to edit comment. Response data is edited comment or null if any error (check code and message). Comment update in request body:

        {
            "id": id_of_comment,
            "content": content_to_update
        }
  - **DELETE:** request delete a comment by giving id in path variable "/{id}". Response data is deleted comment or null if not found.


- **"/api/post":**
  - **GET:** can send with request parameters. Request param available:
    - id: ID of post (to find by)
    - owner: username of owner (to find by)
    - createDate: date post created format YYYY-MM-DD (to find by)
    - title: title of post (to find by)
    - page: page want to get, default 0 (ex: 1 to return page 1, 5 to return page 5)
    - size: number of users in a page, default 10

    Response data can be post found or null, list of posts of empty list. Notice that list of posts return is paged and sorted by create date descending.

        {
            "code": "200",
            "message": "Success",
            "data": {
                "id": id_of_post,
                "title": title_of_post,
                "content": content_of_post,
                "createDate": post_created_date,
                "modifyDate": post_modify_date,
                "view": number_of_post_view,
                "owner": {
                    "username": username_of_owner,
                    "firstName": first_name_of_owner,
                    "lastName": last_name_of_owner
                }
            }
        }
  - **POST:** request with post information to create a post. Response data is created post or null if any error (check code and message). Post info in request body as follows:

        {
            "title": title_of_post,
            "content": content_of_post
        }
  - **PUT:** request edit a post. Response data is edited post or null if any error (check code and message). Post update in request body as follows:

        {
            "id": id_of_post,
            "title": title_to_update (use old title if don't want to update title),
            "content": content_to_update (use old content if don't want to update)
        }
  - **DELETE:** request delete a post by giving id in path variable "/{id}". Response data is deleted post or null if not found.


- **"/api/user":**
  - **GET:** can send with request parameters. Request param available:
    - username: provide username (to find by)
    - firstName: first name of user (to find by)
    - lastName: last name of user (to find by)
    - page: page want to get, default 0 (ex: 1 to return page 1, 5 to return page 5)
    - size: number of users in a page, default 10
    - *If both firstName and lastName provided, will find user by first name and last name*
    - *If no param provided, will find all user and return first page (page 0) with size 10.*
    
    Response data can be user found or null, list of users of empty list. Notice that list of users return is paged and sorted (by role and by username ascending).
  - **PUT:** request edit a user. Response data is edited user or null if any error (check code and message). User in request body like in "/api/signup".
  - **DELETE:** request delete a user by giving username in path variable "/{username}". Response data is deleted user or null if not found.

## Code:

- "200": success
- "400": data not found (comment, post, username)
- "401": authentication error
- "402": date in wrong format
- "403": authorization error
- "404": token error
- "405": duplicate email or username
- "406": invalid entity - field error
- "407": other error

