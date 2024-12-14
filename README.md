# What is the purpose of this project?
This project is a way to practice using JWT auth with Spring security. It too should serves as a base tutorial to this process.

Links to tutorials based on this repository. All of them are in this repository, and I recommend following this same order:
- [Encrypt Password Tutorial.md](Encrypt%20Password%20Tutorial.md)

- [Authenticate using JWT.md](Authenticate%20using%20JWT.md)

- [Limiting methods access to specific users.md](Limiting%20methods%20access%20to%20specific%20users.md)

# How it achieves that?
The branch ``initial_setup`` has the API without auth or security measures.
From this branch, the ``securing`` branch implements security measures.

### What are the measure?
The project should have the following security measures:

- [x] Encrypt user password
- [x] Authenticate using JWT
- [x] The resource to create a user should be public
- [x] The resource to show the data of a users should be accessible only to the user and the admin
  - [x] The admin can access the data from all users
  - [x] The user can access only his own data
- [x] The resource to list all user should be accessible only to the admin


# Endpoints

|Method|Url|Action|
|--------|-----|--------|
|Post|/users|Create a user|
|Get|/users|List all user|
|Get|/users/{id}|retrieves data from the user specified|

# How to run the project

1. Clone the repository:
  ```bash
  git clone git@github.com:kennedy-brito/demo-auth-jwt.git
  ```

2. Navigate to the project folder and run the following commands:
  ```bash
  cd demo-auth-jwt
  mvn clean install
  mvn spring-boot:run
  ```